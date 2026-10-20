import io
import os
import re
from datetime import date
from typing import Dict, Any, Optional, List, Tuple
from PIL import Image, ImageOps, ImageFilter
import pytesseract
import numpy as np
from app.domain.ports.ocr_extractor_port import OcrExtractorPort

COMERCIOS_CONOCIDOS = {
    "EMSERFUSA": "Emserfusa E.S.P.",
    "FUSAGASUGA": "Emserfusa E.S.P.",
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
    "GAS NATURAL": "Gases Vanti",
    "ACUEDUCTO": "Acueducto y Alcantarillado",
    "ALCANTARILLADO": "Acueducto y Alcantarillado",
    "CELSIA": "Celsia Energía",
    "EMCALI": "Emcali",
    "AFINIA": "Afinia Grupo EPM",
    "AIR-E": "Air-e Energía",
    "ESSA": "Electrificadora de Santander",
    "CLARO": "Claro Colombia",
    "COMCEL": "Claro Colombia",
    "MOVISTAR": "Movistar",
    "TIGO": "Tigo",
    "ETB": "ETB Telecomunicaciones",
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
    "GRACIAS POR SU COMPRA", "DOCUMENTO EQUIVALENTE", "CODIGO INTERNO", "PERIODO"
]

PALABRAS_PROHIBIDAS_TOTAL = [
    "CAMBIO", "DEVUELTA", "SU CAMBIO", "EFECTIVO", "RECIBIDO", "ENTREGADO",
    "VALOR ENTREGADO", "AUTORIZACION", "RESOLUCION", "DIAN", "FACTURA DE VENTA",
    "FACTURA ELECTRONICA", "FACTURA NO", "FACTURA N°", "FACTURA N*", "NUMERO FACTURA",
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
        imagen_scaled = None
        try:
            imagen = Image.open(buffer)
            imagen_proc = ImageOps.exif_transpose(imagen)
            w, h = imagen_proc.size
            scale = max(1600.0 / max(w, 1), 1.0)
            new_w, new_h = int(w * scale), int(h * scale)
            imagen_scaled = imagen_proc.resize((new_w, new_h), Image.Resampling.LANCZOS)

            gray = ImageOps.grayscale(imagen_scaled)
            gray_ac = ImageOps.autocontrast(gray, cutoff=2)
            try:
                txt_pass1 = pytesseract.image_to_string(gray_ac, lang="spa+eng", config="--psm 6")
            except Exception:
                try:
                    txt_pass1 = pytesseract.image_to_string(gray_ac, config="--psm 6")
                except Exception:
                    txt_pass1 = ""

            arr = np.array(gray_ac)
            bina_170 = Image.fromarray((arr > 170).astype(np.uint8) * 255)
            try:
                txt_pass2 = pytesseract.image_to_string(bina_170, lang="spa+eng", config="--psm 6")
            except Exception:
                txt_pass2 = ""

            bot_zone = bina_170.crop((0, int(bina_170.height * 0.85), bina_170.width, bina_170.height))
            try:
                txt_zone_bot = pytesseract.image_to_string(bot_zone, lang="spa+eng", config="--psm 6")
            except Exception:
                txt_zone_bot = ""

            top_zone = bina_170.crop((0, int(bina_170.height * 0.16), bina_170.width, int(bina_170.height * 0.32)))
            try:
                txt_zone_top = pytesseract.image_to_string(top_zone, lang="spa+eng", config="--psm 6")
            except Exception:
                txt_zone_top = ""

            texto_extraido = f"{txt_pass1}\n{txt_pass2}\n{txt_zone_bot}\n{txt_zone_top}"
        finally:
            if imagen_scaled is not None:
                imagen_scaled.close()
            if imagen is not None:
                imagen.close()
            buffer.close()
            del imagen_bytes

        texto_combinado = f"{texto_extraido}\n{texto_auxiliar or ''}".strip()
        return self._analizar_texto_factura(texto_combinado)

    def _analizar_texto_factura(self, texto: str) -> Dict[str, Any]:
        lineas = [l.strip() for l in texto.splitlines() if l.strip()]
        texto_upper = texto.upper()

        comercio = self._extraer_comercio(texto_upper, lineas)
        monto = self._extraer_monto_total(lineas)
        fecha_str = self._extraer_fecha(texto_upper)
        nit = self._extraer_nit(texto_upper)

        resumen_lineas = " | ".join(lineas[:6])[:160] if lineas else "Sin texto legible"

        return {
            "comercio": comercio,
            "monto": monto,
            "fecha": fecha_str,
            "nit": nit,
            "texto_resumen": resumen_lineas
        }

    def _extraer_comercio(self, texto_upper: str, lineas: List[str]) -> str:
        for clave, nombre_limpio in COMERCIOS_CONOCIDOS.items():
            if clave in texto_upper:
                return nombre_limpio

        for linea in lineas[:12]:
            l_up = linea.upper()
            if re.search(r"\b(S\.?A\.?S\.?|S\.?A\.?|E\.?S\.?P\.?|LTDA|COMERCIALIZADORA|SUPERMERCADO|RESTAURANTE|DROGUERIA|FARMACIA|CAFE|TIENDA|SERVICIOS\s*PUBLICOS)\b", l_up):
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
            r"(?:VALOR\s*TOTAL\s*A\s*PAGAR|TOTAL\s*A\s*PAGAR|TOTAL\s*PAGAR|NETO\s*A\s*PAGAR|VALOR\s*A\s*PAGAR|TOTAL\s*FACTURA|VALOR\s*TOTAL|TOTAL\s*VENTA|TOTAL\s*COMPRA|TOTAL\s*CON\s*PROPINA|TOTAL\s*COP)",
            r"(?:^|[\s\*\.\-\:])(?:TOTAL|TOT4L|T0TAL|TOTL)(?:[\s:\$]|$)",
            r"(?:VALOR\s*NETO|IMPORTE\s*TOTAL|PAGO\s*TOTAL)"
        ]

        lineas_limpias = [l.strip() for l in lineas if l.strip()]
        conteo_candidatos: Dict[float, int] = {}

        for patron in patrones_total:
            for idx, linea in enumerate(lineas_limpias):
                l_up = linea.upper()
                if any(p in l_up for p in PALABRAS_PROHIBIDAS_TOTAL):
                    continue
                if re.search(patron, l_up):
                    for c in self._extraer_candidatos_linea(linea):
                        conteo_candidatos[c[0]] = conteo_candidatos.get(c[0], 0) + (c[1] * 3)
                    for offset in [1, 2]:
                        if idx + offset < len(lineas_limpias):
                            sig_linea = lineas_limpias[idx + offset]
                            if not any(p in sig_linea.upper() for p in PALABRAS_PROHIBIDAS_TOTAL):
                                for cs in self._extraer_candidatos_linea(sig_linea):
                                    conteo_candidatos[cs[0]] = conteo_candidatos.get(cs[0], 0) + (cs[1] * 2)

        subtotales = []
        for linea in reversed(lineas_limpias):
            l_up = linea.upper()
            if any(p in l_up for p in PALABRAS_PROHIBIDAS_TOTAL):
                continue
            if any(k in l_up for k in ["ACUEDUCTO", "ALCANTARILLADO", "ASEO", "ENERGIA", "GAS", "SUBTOTAL"]):
                cands = self._extraer_candidatos_linea(linea)
                if cands:
                    val = max(cands, key=lambda x: x[1])[0]
                    if val not in subtotales:
                        subtotales.append(val)

        if conteo_candidatos:
            cands_keys = list(conteo_candidatos.keys())
            for v in cands_keys:
                if v >= 100000 and str(int(v)).startswith("5"):
                    stripped = float(str(int(v))[1:])
                    if stripped >= 1000:
                        evidencia = False
                        target_key = None
                        for other in conteo_candidatos:
                            if other != v:
                                other_clean = float(str(int(other))[1:]) if (other >= 100000 and str(int(other)).startswith("5")) else other
                                if abs(other_clean - stripped) <= 100 or str(int(other_clean))[:2] == str(int(stripped))[:2]:
                                    evidencia = True
                                    target_key = other if other < 100000 else stripped
                                    break
                        if not evidencia and any(abs(sub - stripped) <= 100 for sub in subtotales):
                            evidencia = True
                            target_key = stripped

                        if evidencia and target_key:
                            votes = conteo_candidatos.pop(v)
                            conteo_candidatos[target_key] = conteo_candidatos.get(target_key, 0) + votes

            agrupados: Dict[float, int] = {}
            for v, votes in conteo_candidatos.items():
                matched = False
                for rep in list(agrupados.keys()):
                    if abs(rep - v) <= 50:
                        agrupados[rep] += votes
                        matched = True
                        break
                if not matched:
                    agrupados[v] = votes

            if agrupados:
                mejor_monto = max(agrupados.keys(), key=lambda v: (agrupados[v], v % 10 in (0, 5), -v if v > 200000 else v))
                if len(subtotales) >= 2:
                    suma_subtotales = sum(subtotales)
                    if abs(suma_subtotales - mejor_monto) <= 50:
                        return suma_subtotales
                if mejor_monto > 1000 and mejor_monto % 10 in (1, 2, 3, 4, 6, 7, 8, 9):
                    return float((int(mejor_monto) // 10) * 10)
                return mejor_monto

        if len(subtotales) >= 2:
            suma_subtotales = sum(subtotales)
            if 1000 <= suma_subtotales <= 50000000:
                return suma_subtotales

        candidatos_moneda = []
        for linea in reversed(lineas_limpias):
            if any(p in linea.upper() for p in PALABRAS_PROHIBIDAS_TOTAL):
                continue
            cands = [c for c in self._extraer_candidatos_linea(linea) if c[1] >= 2]
            candidatos_moneda.extend(cands)

        if candidatos_moneda:
            candidatos_moneda.sort(key=lambda x: (x[1], x[0]), reverse=True)
            return candidatos_moneda[0][0]

        return 0.0

    def _extraer_candidatos_linea(self, linea: str) -> List[Tuple[float, int]]:
        s = re.sub(r'[_\-\|~*#><\[\]\(\)\u2014\u2013]', ' ', linea)
        s = re.sub(r'\b\d{1,4}[/\.]\d{1,2}[/\.]\d{1,4}\b', ' ', s)
        s = re.sub(r'\b\d{1,2}:\d{2}(?::\d{2})?\b', ' ', s)
        s = re.sub(r'\b\d{7,10}(?:-\d)?\b', ' ', s)
        s = re.sub(r'(?<=\d)\s+(?=\d)', '', s)

        candidatos = []

        pat_moneda = r'(?:[\$£€S@‘\']|COP|PESOS)\s*([0-9OolEBSTZeas]{1,3}(?:[.,][0-9OolEBSTZeas]{3})*(?:[.,][0-9]{2})?|[0-9OolEBSTZeas]{4,8})'
        for m in re.finditer(pat_moneda, s, re.IGNORECASE):
            raw = m.group(1)
            if sum(1 for c in raw if c.isdigit()) >= 2:
                val = self._normalizar_numero_colombiano(raw)
                if 1000 <= val <= 50000000:
                    candidatos.append((val, 3))

        pat_miles = r'(?:^|\s)([0-9]{1,3}(?:[.,][0-9]{3})+(?:[.,][0-9]{2})?)(?:\s|$)'
        for m in re.finditer(pat_miles, s):
            val = self._normalizar_numero_colombiano(m.group(1))
            if 1000 <= val <= 50000000:
                candidatos.append((val, 2))

        pat_dig = r'(?:^|\s)([0-9]{4,8})(?:\s|$)'
        for m in re.finditer(pat_dig, s):
            val = self._normalizar_numero_colombiano(m.group(1))
            if 1000 <= val <= 50000000 and val not in (2024, 2025, 2026, 2027):
                candidatos.append((val, 1))

        return candidatos

    def _normalizar_numero_colombiano(self, cadena: str) -> float:
        char_map = {
            'O': '0', 'o': '0', 'Q': '0', 'D': '0',
            'l': '1', 'I': '1', 'i': '1', '!': '1',
            'Z': '2', 'z': '2',
            'E': '8', 'e': '8', 'B': '8',
            'S': '5', 's': '5',
            'T': '7', 'r': '7',
            '@': '0', '#': '0', 'H': '0'
        }

        res = []
        for ch in cadena:
            if ch.isdigit() or ch in '.,':
                res.append(ch)
            elif ch in char_map:
                res.append(char_map[ch])

        limpio = "".join(res)
        if re.search(r'[\.,]\d{2}$', limpio) and len(limpio) > 5:
            limpio = limpio[:-3]
        solo_digitos = re.sub(r'[^\d]', '', limpio)
        if not solo_digitos:
            return 0.0
        return float(solo_digitos)

    def _extraer_fecha(self, texto_upper: str) -> str:
        match_vence = re.search(r"(?:PAG[UÚ]ESE\s*ANTES|VENCE|FECHA\s*LIMITE)[^\d]{0,15}(\d{1,2})[/.\-](\d{1,2})[/.\-](202\d)", texto_upper)
        if match_vence:
            d, m, y = int(match_vence.group(1)), int(match_vence.group(2)), int(match_vence.group(3))
            try:
                return date(y, m, d).isoformat()
            except ValueError:
                pass

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
        match = re.search(r"NIT[:\s\.]*(\d{3}[\.\d\s]{5,12}(?:-\d)?)", texto_upper)
        if match:
            nit_limpio = re.sub(r"\s+", "", match.group(1))
            return nit_limpio

        match_generico = re.search(r"\b(8\d{2}[\.\s]\d{3}[\.\s]\d{3}(?:-\d)?)\b", texto_upper)
        if match_generico:
            return re.sub(r"\s+", "", match_generico.group(1))

        return None
