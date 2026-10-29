from datetime import datetime
from typing import Dict, List, Optional

from pydantic import BaseModel, Field


class LogRequest(BaseModel):
    servicio: str = Field(..., examples=["usuarios-auth"])
    nivel: str = Field(..., examples=["ERROR"], description="DEBUG, INFO, WARN, ERROR o CRITICAL")
    mensaje: str = Field(..., examples=["No se pudo conectar con Brevo"])
    detalle: Optional[str] = Field(None, description="Detalle tecnico (stack trace resumido)")
    ruta: Optional[str] = None
    metodo: Optional[str] = None
    estado_http: Optional[int] = None
    usuario: Optional[str] = None
    fecha: Optional[datetime] = None


class LogResponse(BaseModel):
    id: int
    fecha: datetime
    servicio: str
    nivel: str
    mensaje: str
    detalle: Optional[str] = None
    ruta: Optional[str] = None
    metodo: Optional[str] = None
    estado_http: Optional[int] = None
    usuario: Optional[str] = None


class LogsPaginadosResponse(BaseModel):
    total: int
    limite: int
    desplazamiento: int
    items: List[LogResponse]


class ResumenItem(BaseModel):
    servicio: str
    nivel: str
    cantidad: int


class PurgaResponse(BaseModel):
    eliminados: int
    mensaje: str
