from pydantic import BaseModel, Field
from typing import Optional

class ClasificarRequest(BaseModel):
    transaccion_id: int
    comercio: str
    monto: float = Field(..., gt=0)
    descripcion: Optional[str] = ""
    usuario_id: Optional[str] = None

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
    usuario_id: str
    transaccion_id: int

class AnalizarPatronResponse(BaseModel):
    es_inusual: bool
    descripcion_alerta: Optional[str] = None

class EscanearFacturaRequest(BaseModel):
    imagen_base64: str
    nombre_archivo: Optional[str] = None

class EscanearFacturaResponse(BaseModel):
    comercio: str
    monto: float
    fecha: str
    tipo_movimiento: str
    categoria_sugerida: str
    nivel_confianza: float
    nit: Optional[str] = None
    texto_detectado_resumen: Optional[str] = None


