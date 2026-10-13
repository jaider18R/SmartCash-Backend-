from pydantic import BaseModel, Field
from typing import Dict, Optional

class ResumenMensualResponse(BaseModel):
    mes: int
    anio: int
    total_ingresos: float
    total_gastos: float
    balance_neto: float
    tasa_ahorro_porcentaje: float
    desglose_categorias: Dict[str, float]

class SafeToSpendRequest(BaseModel):
    compromisos_fijos: float = Field(default=0.0, ge=0.0)
    ahorro_metas: float = Field(default=0.0, ge=0.0)

class SafeToSpendResponse(BaseModel):
    saldo_disponible: float
    compromisos_fijos: float
    ahorro_metas: float
    monto_seguro_restante: float
    dias_restantes_mes: int
    limite_diario_seguro: float
    nivel_salud: str
    recomendacion: str

class SimularGastoRequest(BaseModel):
    monto_simulado: float = Field(..., gt=0.0)
    compromisos_fijos: float = Field(default=0.0, ge=0.0)
    ahorro_metas: float = Field(default=0.0, ge=0.0)

class SimularGastoResponse(BaseModel):
    monto_simulado: float
    nuevo_limite_diario: float
    diferencia_diaria: float
    impacto_metas: str
    es_viable: bool
