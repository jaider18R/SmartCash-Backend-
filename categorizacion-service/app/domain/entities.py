from dataclasses import dataclass
from typing import Optional
from datetime import datetime

@dataclass
class TransaccionClasificada:
    transaccion_id: int
    comercio: str
    monto: float
    descripcion: Optional[str]
    categoria: str
    nivel_confianza: float
    usuario_id: Optional[int] = None
    fecha: Optional[datetime] = None

@dataclass
class CorreccionCategoria:
    transaccion_id: int
    categoria_anterior: str
    categoria_nueva: str
    comercio: str
    monto: float
    fecha: Optional[datetime] = None

@dataclass
class ExplicacionClasificacion:
    transaccion_id: int
    categoria: str
    explicacion: str

@dataclass
class AlertaPatronInusual:
    transaccion_id: int
    usuario_id: int
    es_inusual: bool
    descripcion_alerta: Optional[str] = None
