import io
import os
import re
from datetime import date
from typing import Dict, Any, Optional, List
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
    "FACTURA", "ELECTRONICA", "VENTA", "POS", "TIQUETE", "MAQUina",
    "REGIMEN", "RESPONSABLE", "IVA", "AUTORIZACION", "DIAN", "RESOLUCION",
    "DIRECCION", "TELEFONO", "TEL:", "NIT", "CAJERO", "CAJA", "FECHA",
    "CLIENTE", "CONSUMIDOR FINAL", "COPIA", "ORIGINAL", "BIENVENIDO",
    "GRACIAS POR SU COMPRA", "DOCUMENTO EQUIVALENTE"
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
            if ancho < 1000:
                escala = 1200.0 / max(ancho, 1)
                imagen_proc = imagen_proc.resize((int(ancho * escala), int(alto * escala)))
            imagen_proc = ImageOps.autocontrast(imagen_proc)
            imagen_proc = imagen_proc.filter(ImageFilter.SHARPEN)

            try:
                texto_extraido = pytesseract.image_to_string(imagen_proc, lang="spa+eng", config="--psm 6")
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
        patrones_prioritarios = [
            r"(?:TOTAL\s*A\s*PAGAR|VALOR\s*TOTAL|TOTAL\s*VENTA|NETO\s*A\s*PAGAR|TOTAL\s*COP|VENTA\s*TOTAL|TOTAL\s*FACTURA)",
            r"(?:^|\s)TOTAL(?:$|\s|:|\$)",
            r"(?:EFECTIVO|TARJETA|RECIBIDO|PAGADO|SUBTOTAL)"
        ]

        for patron in patrones_prioritarios:
            candidatos = []
            for idx, linea in enumerate(lineas):
                l_up = linea.upper()
                if any(excl in l_up for excl in ["CAMBIO", "DEVUELTA", "IVA ", "DESCUENTO", "AHORRO", "PUNTOS", "CANTIDAD", "ITEMS"]):
                    continue
                if re.search(patron, l_up):
                    valores = self._extraer_valores_monetarios(linea)
                    if not valores and idx + 1 < len(lineas):
                        valores = self._extraer_valores_monetarios(lineas[idx + 1])
                    candidatos.extend(valores)
            if candidatos:
                return float(max(candidatos))

        todos_los_valores = []
        for linea in lineas:
            l_up = linea.upper()
            if any(excl in l_up for excl in ["NIT", "RESOLUCION", "AUTORIZACION", "TEL", "CEL", "CAJA", "FACTURA", "FECHA", "CAMBIO"]):
                continue
            todos_los_valores.extend(self._extraer_valores_monetarios(linea))

        if todos_los_valores:
            return float(max(todos_los_valores))

        return 0.0

    def _extraer_valores_monetarios(self, texto_linea: str) -> List[float]:
        resultados = []
        linea_sin_fechas = re.sub(r"\b\d{1,4}[/\-]\d{1,2}[/\-]\d{1,4}\b", " ", texto_linea)
        linea_sin_horas = re.sub(r"\b\d{1,2}:\d{2}(?::\d{2})?\b", " ", linea_sin_fechas)
        linea_sin_nit = re.sub(r"\b\d{8,10}-\d\b", " ", linea_sin_horas)

        coincidencias = re.findall(r"(?:\$\s*)?(\d{1,3}(?:[\.,]\d{3})+(?:[\.,]\d{2})?|\d{4,8}(?:[\.,]\d{2})?)", linea_sin_nit)
        for bruto in coincidencias:
            valor = self._normalizar_numero_colombiano(bruto)
            if 500 <= valor <= 50000000 and valor not in (2024, 2025, 2026, 2027):
                resultados.append(valor)
        return resultados

    def _normalizar_numero_colombiano(self, cadena: str) -> float:
        limpio = cadena.strip()
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
