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

class SimularInversionRequest(BaseModel):
    monto: float = Field(..., gt=0.0)
    plazo_meses: int = Field(default=12, ge=1, le=120)

class ProyeccionInversionItemDTO(BaseModel):
    tipo: str
    nombre_producto: str
    plataforma: str
    tasa_ea_estimada: float
    rendimiento_bruto: float
    retencion_fuente_estimada: float
    rendimiento_neto: float
    capital_final: float
    riesgo: str
    descripcion_facil: str

class SimularInversionResponse(BaseModel):
    monto_invertido: float
    plazo_meses: int
    inflacion_colombia_estimada_porcentaje: float
    perdida_poder_adquisitivo_efectivo: float
    proyecciones: list[ProyeccionInversionItemDTO]
    recomendacion_perfil: str

class OpcionInversionDTO(BaseModel):
    id_opcion: str
    categoria: str
    titulo: str
    subtitulo: str
    que_es_facil: str
    rentabilidad_estimada: str
    tasa_ea_promedio: float
    riesgo: str
    plataformas_recomendadas: list[str]
    vigilancia: str
    monto_minimo: str
    disponibilidad_liquidez: str
    url_promocional: str

class CatalogoInversionesResponse(BaseModel):
    opciones: list[OpcionInversionDTO]

