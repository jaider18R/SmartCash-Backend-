from datetime import date
from typing import Optional
from app.domain.entities import ResumenMensual, SafeToSpend, SimulacionResultado
from app.domain.motor_analitica import MotorAnalitica
from app.domain.ports.transacciones_client_port import TransaccionesClientPort

class ObtenerResumenMensualUseCase:
    def __init__(self, transacciones_client: TransaccionesClientPort, motor: MotorAnalitica):
        self.transacciones_client = transacciones_client
        self.motor = motor

    def ejecutar(self, token: str, mes: int, anio: int) -> ResumenMensual:
        transacciones = self.transacciones_client.obtener_transacciones_usuario(token)
        return self.motor.calcular_resumen_mensual(transacciones, mes, anio)

class CalcularSafeToSpendUseCase:
    def __init__(self, transacciones_client: TransaccionesClientPort, motor: MotorAnalitica):
        self.transacciones_client = transacciones_client
        self.motor = motor

    def ejecutar(self, token: str, compromisos_fijos: float = 0.0, ahorro_metas: float = 0.0,
                 fecha_referencia: Optional[date] = None) -> SafeToSpend:
        fecha_ref = fecha_referencia or date.today()
        transacciones = self.transacciones_client.obtener_transacciones_usuario(token)

        tx_mes = [t for t in transacciones if t.fecha.month == fecha_ref.month and t.fecha.year == fecha_ref.year]
        total_ingresos = sum(t.monto for t in tx_mes if t.tipo_movimiento == "ingreso")
        total_gastos = sum(t.monto for t in tx_mes if t.tipo_movimiento == "gasto")

        return self.motor.calcular_safe_to_spend(total_ingresos, total_gastos, compromisos_fijos, ahorro_metas, fecha_ref)

class SimularGastoUseCase:
    def __init__(self, use_case_safe: CalcularSafeToSpendUseCase, motor: MotorAnalitica):
        self.use_case_safe = use_case_safe
        self.motor = motor

    def ejecutar(self, token: str, monto_simulado: float, compromisos_fijos: float = 0.0,
                 ahorro_metas: float = 0.0) -> SimulacionResultado:
        safe_actual = self.use_case_safe.ejecutar(token, compromisos_fijos, ahorro_metas)
        return self.motor.simular_impacto_gasto(safe_actual, monto_simulado)

class SimularInversionesUseCase:
    def __init__(self, motor: MotorAnalitica):
        self.motor = motor

    def ejecutar(self, monto: float, plazo_meses: int):
        return self.motor.simular_inversiones(monto, plazo_meses)

class ObtenerCatalogoInversionesUseCase:
    def __init__(self, motor: MotorAnalitica):
        self.motor = motor

    def ejecutar(self):
        return self.motor.obtener_catalogo_inversiones()

