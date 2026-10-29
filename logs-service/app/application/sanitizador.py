"""Enmascara datos sensibles antes de guardarlos (contrasenas, tokens, claves)."""
import re

_PATRONES = [
    # clave=valor / clave: valor / "clave":"valor"
    re.compile(r'(?i)("?(?:password|contrasena|contraseña|passwd|secret|token|api[-_ ]?key|authorization)"?\s*[:=]\s*)("[^"]*"|\'[^\']*\'|[^\s,;&}]+)'),
    # Authorization: Bearer xxx
    re.compile(r'(?i)(bearer\s+)[A-Za-z0-9\-_\.=]+'),
    # JWT suelto (tres bloques base64 separados por punto)
    re.compile(r'eyJ[A-Za-z0-9_\-]+\.[A-Za-z0-9_\-]+\.[A-Za-z0-9_\-]+'),
]


def sanitizar(texto):
    if texto is None:
        return None
    resultado = _PATRONES[0].sub(lambda m: m.group(1) + '"***"', texto)
    resultado = _PATRONES[1].sub(lambda m: m.group(1) + '***', resultado)
    resultado = _PATRONES[2].sub('***JWT***', resultado)
    return resultado
