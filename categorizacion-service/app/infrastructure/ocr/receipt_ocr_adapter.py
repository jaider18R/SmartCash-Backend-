import io
import os
import re
from datetime import date
from typing import Dict, Any, Optional, List, Tuple
from PIL import Image, ImageOps, ImageFilter
import pytesseract
from app.domain.ports.ocr_extractor_port import OcrExtractorPort

COMERCIOS_CONOCIDOS = {
    "KOBA": "Tiendas D1",
    "TIENDAS D1": "Tiendas D1",
    "D1 ": "Tiendas D1",
    "JERONIMO MARTINS": "Tiendas Ara",
    "TIENDAS ARA": "Tiendas Ara",
    "ALMACENES EXITO": "Almacenes Éxito",
    "EXITO": "Almacenes Éxito",
    "CARULLA": "Carulla",
    "SUPERTIENDAS OLIMPICA": "Olímpica",
    "OLIMPICA": "Olímpica",
    "JUMBO": "Tiendas Jumbo",
    "CENCOSUD": "Tiendas Jumbo",
    "ALKOSTO": "Alkosto",
    "KTRONIX": "Ktronix",
    "SURTIMAX": "Surtimax",
    "ISIMO": "Tiendas Ísimo",
    "DOLLARCITY": "Dollarcity",
    "SODIMAC": "Homecenter",
    "HOMECENTER": "Homecenter",
    "CREPES & WAFFLES": "Crepes & Waffles",
    "CREPES Y WAFFLES": "Crepes & Waffles",
    "EL CORRAL": "Hamburguesas El Corral",
    "FRISBY": "Frisby",
    "MCDONALD": "McDonald's",
    "JUAN VALDEZ": "Juan Valdez Café",
    "TOSTAO": "Tostao Café & Pan",
    "BBI COLOMBIA": "Tostao Café & Pan",
    "STARBUCKS": "Starbucks",
    "RAPPI": "Rappi",
    "UBER": "Uber",
    "DIDI": "DiDi",
    "TERPEL": "Estación Terpel",
    "PRIMAX": "Estación Primax",
    "BIOMAX": "Estación Biomax",
    "TEXACO": "Estación Texaco",
    "CRUZ VERDE": "Droguerías Cruz Verde",
    "FARMATODO": "Farmatodo",
    "LA REBAJA": "Droguerías La Rebaja",
    "COLSUBSIDIO": "Colsubsidio",
    "CAFAM": "Cafam",
    "ENEL": "Enel Colombia",
    "CODENSA": "Enel Colombia",
    "EPM": "EPM Servicios Públicos",
    "VANTI": "Gases Vanti",
    "ACUEDUCTO": "Acueducto y Alcantarillado",
    "CLARO": "Claro Colombia",
    "COMCEL": "Claro Colombia",
    "MOVISTAR": "Movistar",
    "TIGO": "Tigo",
    "WOM": "WOM Colombia",
    "CINE COLOMBIA": "Cine Colombia",
    "SMART FIT": "Smart Fit",
    "BODYTECH": "Bodytech",
    "ARTURO CALLE": "Arturo Calle",
    "FALABELLA": "Falabella",
    "PANAMERICANA": "Panamericana",
    "DECATHLON": "Decathlon"
}

PALABRAS_IGNORAR_ENCABEZADO = [
    "FACTURA", "ELECTRONICA", "VENTA", "POS", "TIQUETE", "MAQUINA",
    "REGIMEN", "RESPONSABLE", "IVA", "AUTORIZACION", "DIAN", "RESOLUCION",
    "DIRECCION", "TELEFONO", "TEL:", "NIT", "CAJERO", "CAJA", "FECHA",
    "CLIENTE", "CONSUMIDOR FINAL", "COPIA", "ORIGINAL", "BIENVENIDO",
    "GRACIAS POR SU COMPRA", "DOCUMENTO EQUIVALENTE"
]

PALABRAS_PROHIBIDAS_TOTAL = [
    "CAMBIO", "DEVUELTA", "SU CAMBIO", "EFECTIVO", "RECIBIDO", "ENTREGADO",
    "VALOR ENTREGADO", "AUTORIZACION", "RESOLUCION", "DIAN", "FACTURA",
    "CAJERO", "CAJA", "PUNTOS", "MILLAS", "TELEFONO", "TEL:", "TEL.",
    "CEL:", "CEL.", "NIT", "CUFE", "APROBACION", "TRANSACCION", "TRANS:",
    "TERMINAL", "PROPINA SUGERIDA", "AHORRO", "DESCUENTO"
]

MESES_ES = {
    "ENE": 1, "FEB": 2, "MAR": 3, "ABR": 4, "MAY": 5, "JUN": 6,
    "JUL": 7, "AGO": 8, "SEP": 9, "OCT": 10, "NOV": 11, "DIC": 12
}

class ReceiptOcrAdapter(OcrExtractorPort):
    def __init__(self):
        win_path = r"C:\Program Files\Tesseract-OCR\tesseract.exe"
        if os.name == "nt" and os.path.exists(win_path):
            pytesseract.pytesseract.tesseract_cmd = win_path

    def extraer_desde_bytes(self, imagen_bytes: bytes, texto_auxiliar: Optional[str] = None) -> Dict[str, Any]:
        texto_extraido = ""
        buffer = io.BytesIO(imagen_bytes)
        imagen = None
        imagen_proc = None
        try:
            imagen = Image.open(buffer)
            imagen_proc = ImageOps.exif_transpose(imagen)
            imagen_proc = ImageOps.grayscale(imagen_proc)
            ancho, alto = imagen_proc.size
            if ancho < 1200:
                escala = 1400.0 / max(ancho, 1)
                imagen_proc = imagen_proc.resize((int(ancho * escala), int(alto * escala)), Image.Resampling.LANCZOS)
            imagen_proc = ImageOps.autocontrast(imagen_proc, cutoff=2)
            imagen_proc = imagen_proc.filter(ImageFilter.SHARPEN)

            try:
                texto_extraido = pytesseract.image_to_string(imagen_proc, lang="spa+eng", config="--psm 4")
            except Exception:
                try:
                    texto_extraido = pytesseract.image_to_string(imagen_proc, config="--psm 4")
                except Exception:
                    try:
                        texto_extraido = pytesseract.image_to_string(imagen_proc, config="--psm 6")
                    except Exception:
                        texto_extraido = ""
        finally:
            if imagen_proc is not None:
                imagen_proc.close()
            if imagen is not None:
                imagen.close()
            buffer.close()
            del imagen_bytes

        texto_combinado = f"{texto_extraido}\n{texto_auxiliar or ''}".strip()
        return self._analizar_texto_factura(texto_combinado)

    def _analizar_texto_factura(self, texto: str) -> Dict[str, Any]:
        lineas = [l.strip() for l in texto.splitlines() if l.strip()]
        texto_upper = texto.upper()

        comercio = self._extraer_comercio(lineas, texto_upper)
        monto = self._extraer_monto_total(lineas)
        fecha_str = self._extraer_fecha(texto_upper)
        nit = self._extraer_nit(texto_upper)

        resumen_lineas = " | ".join(lineas[:5])[:160] if lineas else "Sin texto legible"

        return {
            "comercio": comercio,
            "monto": monto,
            "fecha": fecha_str,
            "nit": nit,
            "texto_resumen": resumen_lineas
        }

    def _extraer_comercio(self, lineas: List[str], texto_upper: str) -> str:
        for clave, nombre_limpio in COMERCIOS_CONOCIDOS.items():
            if clave in texto_upper:
                return nombre_limpio

        for linea in lineas[:10]:
            l_up = linea.upper()
            if re.search(r"\b(S\.?A\.?S\.?|S\.?A\.?|LTDA|COMERCIALIZADORA|SUPERMERCADO|RESTAURANTE|DROGUERIA|FARMACIA|CAFE|TIENDA)\b", l_up):
                limpio = re.sub(r"NIT[:\s]*[\d\.\-]+", "", linea, flags=re.IGNORECASE).strip(" -*#:.")
                if len(limpio) >= 3:
                    return limpio[:60].title()

        for linea in lineas[:8]:
            l_up = linea.upper()
            if any(ign in l_up for ign in PALABRAS_IGNORAR_ENCABEZADO):
                continue
            solo_letras = re.sub(r"[^A-Za-zÁÉÍÓÚÑáéíóúñ\s]", "", linea).strip()
            if len(solo_letras) >= 4:
                return solo_letras[:50].title()

        return "Comercio Factura Escaneada"

    def _extraer_monto_total(self, lineas: List[str]) -> float:
        patrones_total = [
            r"(?:TOTAL\s*A\s*PAGAR|TOTAL\s*PAGAR|NETO\s*A\s*PAGAR|VALOR\s*A\s*PAGAR|TOTAL\s*FACTURA|VALOR\s*TOTAL|TOTAL\s*VENTA|TOTAL\s*COMPRA|TOTAL\s*CON\s*PROPINA|TOTAL\s*COP)",
            r"(?:^|[\s\*\.\-\:])(?:TOTAL|TOT4L|T0TAL|TOTL)(?:[\s:\$]|$)",
            r"(?:VALOR\s*NETO|IMPORTE\s*TOTAL|PAGO\s*TOTAL)"
        ]

        lineas_limpias = [(i, l.strip()) for i, l in enumerate(lineas) if l.strip()]

        for patron in patrones_total:
            for idx, linea in reversed(lineas_limpias):
                l_up = linea.upper()
                if any(p in l_up for p in PALABRAS_PROHIBIDAS_TOTAL):
                    continue
                if re.search(patron, l_up):
                    cands = self._extraer_candidatos_linea(linea)
                    if cands:
                        cands.sort(key=lambda x: (x[1], x[0]), reverse=True)
                        return cands[0][0]
                    if idx + 1 < len(lineas):
                        cands_sig = self._extraer_candidatos_linea(lineas[idx + 1])
                        if cands_sig:
                            cands_sig.sort(key=lambda x: (x[1], x[0]), reverse=True)
                            return cands_sig[0][0]

        for idx, linea in reversed(lineas_limpias):
            l_up = linea.upper()
            if any(p in l_up for p in PALABRAS_PROHIBIDAS_TOTAL):
                continue
            if "SUBTOTAL" in l_up or "VALOR" in l_up:
                cands = self._extraer_candidatos_linea(linea)
                if cands:
                    cands.sort(key=lambda x: (x[1], x[0]), reverse=True)
                    return cands[0][0]

        candidatos_moneda = []
        for _, linea in reversed(lineas_limpias):
            l_up = linea.upper()
            if any(p in l_up for p in PALABRAS_PROHIBIDAS_TOTAL):
                continue
            cands = self._extraer_candidatos_linea(linea)
            cands_pesados = [c for c in cands if c[1] >= 2]
            candidatos_moneda.extend(cands_pesados)

        if candidatos_moneda:
            candidatos_moneda.sort(key=lambda x: (x[1], x[0]), reverse=True)
            return candidatos_moneda[0][0]

        return 0.0

    def _extraer_candidatos_linea(self, linea: str) -> List[Tuple[float, int]]:
        limpia = re.sub(r"\b\d{1,4}[/\-\.]\d{1,2}[/\-\.]\d{1,4}\b", " ", linea)
        limpia = re.sub(r"\b\d{1,2}:\d{2}(?::\d{2})?\b", " ", limpia)
        limpia = re.sub(r"\b\d{7,10}(?:-\d)?\b", " ", limpia)

        candidatos = []

        m_moneda = re.findall(r"[\$\£\€]\s*([0-9Ool]{1,3}(?:[\.,][0-9Ool]{3})*(?:[\.,][0-9]{2})?|[0-9]{3,7})", limpia)
        for m in m_moneda:
            val = self._normalizar_numero_colombiano(m)
            if 500 <= val <= 25000000:
                candidatos.append((val, 3))

        m_miles = re.findall(r"\b([0-9]{1,3}(?:[\.,][0-9Ool]{3})+(?:[\.,][0-9]{2})?)\b", limpia)
        for m in m_miles:
            val = self._normalizar_numero_colombiano(m)
            if 500 <= val <= 25000000:
                candidatos.append((val, 2))

        m_digitos = re.findall(r"\b([0-9]{4,7})\b", limpia)
        for m in m_digitos:
            val = float(m)
            if 500 <= val <= 25000000 and val not in (2024, 2025, 2026, 2027):
                candidatos.append((val, 1))

        return candidatos

    def _normalizar_numero_colombiano(self, cadena: str) -> float:
        limpio = cadena.strip()
        limpio = re.sub(r"(?<=[.,\d])[Oo](?=[.,\d]|$)", "0", limpio)
        limpio = re.sub(r"(?<=[.,])[Oo]{3}\b", "000", limpio)
        limpio = re.sub(r"(?<=[.,])[Oo]{2}\b", "00", limpio)
        limpio = re.sub(r"(?<=\d)[lI](?=\d)", "1", limpio)

        if re.search(r"[\.,]\d{2}$", limpio) and len(limpio) > 4:
            limpio = limpio[:-3]
        solo_digitos = re.sub(r"[^\d]", "", limpio)
        if not solo_digitos:
            return 0.0
        return float(solo_digitos)

    def _extraer_fecha(self, texto_upper: str) -> str:
        match_iso = re.search(r"\b(202\d)[/\-\.](0[1-9]|1[0-2])[/\-\.](0[1-9]|[12]\d|3[01])\b", texto_upper)
        if match_iso:
            y, m, d = int(match_iso.group(1)), int(match_iso.group(2)), int(match_iso.group(3))
            try:
                return date(y, m, d).isoformat()
            except ValueError:
                pass

        match_dmy = re.search(r"\b(0?[1-9]|[12]\d|3[01])[/\-\.](0?[1-9]|1[0-2])[/\-\.](202\d|\d{2})\b", texto_upper)
        if match_dmy:
            d, m, y_raw = int(match_dmy.group(1)), int(match_dmy.group(2)), int(match_dmy.group(3))
            y = 2000 + y_raw if y_raw < 100 else y_raw
            if 2020 <= y <= 2030:
                try:
                    return date(y, m, d).isoformat()
                except ValueError:
                    pass

        match_texto = re.search(r"\b(0?[1-9]|[12]\d|3[01])[\s\-/]+(ENE|FEB|MAR|ABR|MAY|JUN|JUL|AGO|SEP|OCT|NOV|DIC)[A-Z]*[\s\-/,]+(202\d)\b", texto_upper)
        if match_texto:
            d = int(match_texto.group(1))
            m = MESES_ES.get(match_texto.group(2), 1)
            y = int(match_texto.group(3))
            try:
                return date(y, m, d).isoformat()
            except ValueError:
                pass

        return date.today().isoformat()

    def _extraer_nit(self, texto_upper: str) -> Optional[str]:
        match = re.search(r"NIT[:\s\.]*(\d{3}[\.\d]{5,10}(?:-\d)?)", texto_upper)
        if match:
            return match.group(1)
        return None
