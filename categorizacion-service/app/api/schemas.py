from pydantic import BaseModel, Field
from typing import Optional

class ClasificarRequest(BaseModel):
    transaccion_id: int
    comercio: str
    monto: float = Field(..., gt=0)
    descripcion: Optional[str] = ""
    usuario_id: Optional[int] = None

class ClasificarResponse(BaseModel):
    categoria: str
    nivel_confianza: float

class CorreccionRequest(BaseModel):
    transaccion_id: int
    categoria_anterior: str
    categoria_nueva: str
    comercio: str
    monto: float

class MensajeResponse(BaseModel):
    mensaje: str

class ExplicacionResponse(BaseModel):
    categoria: str
    explicacion: str

class AnalizarPatronRequest(BaseModel):
    usuario_id: int
    transaccion_id: int

class AnalizarPatronResponse(BaseModel):
    es_inusual: bool
    descripcion_alerta: Optional[str] = None
