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
