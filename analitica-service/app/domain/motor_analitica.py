import calendar
from datetime import date
from typing import List
from app.domain.entities import TransaccionItem, ResumenMensual, SafeToSpend, SimulacionResultado

class MotorAnalitica:

    def calcular_resumen_mensual(self, transacciones: List[TransaccionItem], mes: int, anio: int) -> ResumenMensual:
        tx_mes = [t for t in transacciones if t.fecha.month == mes and t.fecha.year == anio]

        total_ingresos = sum(t.monto for t in tx_mes if t.tipo_movimiento == "ingreso")
        total_gastos = sum(t.monto for t in tx_mes if t.tipo_movimiento == "gasto")
        balance = total_ingresos - total_gastos

        tasa_ahorro = 0.0
        if total_ingresos > 0:
            tasa_ahorro = round(max(0.0, (balance / total_ingresos) * 100), 2)

        desglose = {}
        for t in tx_mes:
            if t.tipo_movimiento == "gasto":
                cat = t.id_categoria or "Sin Categoria"
                desglose[cat] = round(desglose.get(cat, 0.0) + t.monto, 2)

        return ResumenMensual(
            mes=mes,
            anio=anio,
            total_ingresos=round(total_ingresos, 2),
            total_gastos=round(total_gastos, 2),
            balance_neto=round(balance, 2),
            tasa_ahorro_porcentaje=tasa_ahorro,
            desglose_categorias=desglose
        )

    def calcular_safe_to_spend(self, total_ingresos: float, total_gastos: float,
                               compromisos_fijos: float, ahorro_metas: float,
                               fecha_actual: date) -> SafeToSpend:
        ultimo_dia = calendar.monthrange(fecha_actual.year, fecha_actual.month)[1]
        dias_restantes = max(1, ultimo_dia - fecha_actual.day + 1)

        saldo_disponible = max(0.0, total_ingresos - total_gastos)
        monto_seguro = max(0.0, saldo_disponible - compromisos_fijos - ahorro_metas)
        limite_diario = round(monto_seguro / dias_restantes, 2)

        if limite_diario > 50000:
            nivel = "OPTIMO"
            rec = "Tus finanzas estan equilibradas. Tienes buen margen de gasto diario."
        elif limite_diario > 15000:
            nivel = "MODERADO"
            rec = "Margen moderado. Evita compras impulsivas para asegurar tus metas."
        else:
            nivel = "CRITICO"
            rec = "Alerta de liquidez: Reduce gastos no esenciales para no sobregirarte."

        return SafeToSpend(
            saldo_disponible=round(saldo_disponible, 2),
            compromisos_fijos=round(compromisos_fijos, 2),
            ahorro_metas=round(ahorro_metas, 2),
            monto_seguro_restante=round(monto_seguro, 2),
            dias_restantes_mes=dias_restantes,
            limite_diario_seguro=limite_diario,
            nivel_salud=nivel,
            recomendacion=rec
        )

    def simular_impacto_gasto(self, safe_to_spend: SafeToSpend, monto_simulado: float) -> SimulacionResultado:
        nuevo_monto_seguro = safe_to_spend.monto_seguro_restante - monto_simulado
        es_viable = nuevo_monto_seguro >= 0
        nuevo_limite_diario = round(max(0.0, nuevo_monto_seguro) / safe_to_spend.dias_restantes_mes, 2)
        diferencia = round(safe_to_spend.limite_diario_seguro - nuevo_limite_diario, 2)

        if es_viable:
            impacto = f"El gasto es absorbible. Tu cuota diaria se reducira en ${diferencia:,.0f} COP."
        else:
            deficit = abs(nuevo_monto_seguro)
            impacto = f"El gasto sobrepasa tu margen seguro en ${deficit:,.0f} COP y comprometeria tus metas de ahorro."

        return SimulacionResultado(
            monto_simulado=monto_simulado,
            nuevo_limite_diario=nuevo_limite_diario,
            diferencia_diaria=diferencia,
            impacto_metas=impacto,
            es_viable=es_viable
        )
