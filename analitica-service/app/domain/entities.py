from dataclasses import dataclass
from typing import List, Dict, Optional
from datetime import date

@dataclass
class TransaccionItem:
    id_transaccion: str
    monto: float
    fecha: date
    comercio: str
    tipo_movimiento: str
    id_categoria: Optional[str] = None

@dataclass
class ResumenMensual:
    mes: int
    anio: int
    total_ingresos: float
    total_gastos: float
    balance_neto: float
    tasa_ahorro_porcentaje: float
    desglose_categorias: Dict[str, float]

@dataclass
class SafeToSpend:
    saldo_disponible: float
    compromisos_fijos: float
    ahorro_metas: float
    monto_seguro_restante: float
    dias_restantes_mes: int
    limite_diario_seguro: float
    nivel_salud: str
    recomendacion: str

@dataclass
class SimulacionResultado:
    monto_simulado: float
    nuevo_limite_diario: float
    diferencia_diaria: float
    impacto_metas: str
    es_viable: bool

@dataclass
class ProyeccionInversionItem:
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

@dataclass
class ResultadoSimulacionInversion:
    monto_invertido: float
    plazo_meses: int
    inflacion_colombia_estimada_porcentaje: float
    perdida_poder_adquisitivo_efectivo: float
    proyecciones: List[ProyeccionInversionItem]
    recomendacion_perfil: str

@dataclass
class OpcionInversionInfo:
    id_opcion: str
    categoria: str
    titulo: str
    subtitulo: str
    que_es_facil: str
    rentabilidad_estimada: str
    tasa_ea_promedio: float
    riesgo: str
    plataformas_recomendadas: List[str]
    vigilancia: str
    monto_minimo: str
    disponibilidad_liquidez: str
    url_promocional: str
