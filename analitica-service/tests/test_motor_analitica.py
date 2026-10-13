from datetime import date
import pytest
from app.domain.entities import TransaccionItem, SafeToSpend
from app.domain.motor_analitica import MotorAnalitica

def test_calcular_resumen_mensual_correcto():
    motor = MotorAnalitica()
    txs = [
        TransaccionItem("1", 3000000.0, date(2026, 10, 1), "Nomina", "ingreso"),
        TransaccionItem("2", 500000.0, date(2026, 10, 5), "Éxito", "gasto", "Mercado"),
        TransaccionItem("3", 200000.0, date(2026, 10, 8), "Restaurante", "gasto", "Restaurantes"),
        TransaccionItem("4", 1000000.0, date(2026, 9, 20), "Ingreso Pasado", "ingreso")
    ]

    resumen = motor.calcular_resumen_mensual(txs, 10, 2026)

    assert resumen.total_ingresos == 3000000.0
    assert resumen.total_gastos == 700000.0
    assert resumen.balance_neto == 2300000.0
    assert resumen.tasa_ahorro_porcentaje == 76.67
    assert resumen.desglose_categorias["Mercado"] == 500000.0
    assert resumen.desglose_categorias["Restaurantes"] == 200000.0

def test_calcular_safe_to_spend_diario():
    motor = MotorAnalitica()
    fecha_ref = date(2026, 10, 22)

    total_ingresos = 3000000.0
    total_gastos = 1000000.0
    compromisos_fijos = 800000.0
    ahorro_metas = 200000.0

    safe = motor.calcular_safe_to_spend(total_ingresos, total_gastos, compromisos_fijos, ahorro_metas, fecha_ref)

    assert safe.saldo_disponible == 2000000.0
    assert safe.monto_seguro_restante == 1000000.0
    assert safe.dias_restantes_mes == 10
    assert safe.limite_diario_seguro == 100000.0
    assert safe.nivel_salud == "OPTIMO"

def test_simular_impacto_gasto_viable():
    motor = MotorAnalitica()
    safe = SafeToSpend(
        saldo_disponible=2000000.0,
        compromisos_fijos=800000.0,
        ahorro_metas=200000.0,
        monto_seguro_restante=1000000.0,
        dias_restantes_mes=10,
        limite_diario_seguro=100000.0,
        nivel_salud="OPTIMO",
        recomendacion="OK"
    )

    sim = motor.simular_impacto_gasto(safe, 300000.0)

    assert sim.es_viable is True
    assert sim.nuevo_limite_diario == 70000.0
    assert sim.diferencia_diaria == 30000.0

def test_simular_impacto_gasto_no_viable():
    motor = MotorAnalitica()
    safe = SafeToSpend(
        saldo_disponible=1200000.0,
        compromisos_fijos=800000.0,
        ahorro_metas=200000.0,
        monto_seguro_restante=200000.0,
        dias_restantes_mes=10,
        limite_diario_seguro=20000.0,
        nivel_salud="MODERADO",
        recomendacion="Cuidado"
    )

    sim = motor.simular_impacto_gasto(safe, 500000.0)

    assert sim.es_viable is False
    assert "sobrepasa tu margen seguro" in sim.impacto_metas
