from dataclasses import dataclass
from datetime import datetime
from typing import Optional

NIVELES_VALIDOS = ("DEBUG", "INFO", "WARN", "ERROR", "CRITICAL")


@dataclass
class RegistroLog:
    """Un registro de log (error o evento) enviado por un microservicio."""
    servicio: str
    nivel: str
    mensaje: str
    detalle: Optional[str] = None
    ruta: Optional[str] = None
    metodo: Optional[str] = None
    estado_http: Optional[int] = None
    usuario: Optional[str] = None
    fecha: Optional[datetime] = None
    id: Optional[int] = None
