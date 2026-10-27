from datetime import date
from unittest.mock import patch
from fastapi.testclient import TestClient
from app.main import app
from app.domain.entities import TransaccionItem

client = TestClient(app)


def test_e2e_health_endpoint():
    response = client.get("/health")
    assert response.status_code == 200
    data = response.json()
    assert data["status"] == "UP"
    assert data["service"] == "analitica-service"


def test_e2e_resumen_mensual():
    hoy = date.today()
    txs_mock = [
        TransaccionItem("1", 4000000.0, date(hoy.year, hoy.month, 1), "Salario", "ingreso"),
        TransaccionItem("2", 600000.0, date(hoy.year, hoy.month, 10), "Supermercado", "gasto", "Mercado")
    ]
    with patch("app.main.transacciones_client.obtener_transacciones_usuario", return_value=txs_mock):
        response = client.get(
            f"/api/analitica/resumen-mensual?mes={hoy.month}&anio={hoy.year}",
            headers={"Authorization": "Bearer fake-jwt-token"}
        )
        assert response.status_code == 200
        data = response.json()
        assert data["total_ingresos"] == 4000000.0
        assert data["total_gastos"] == 600000.0
        assert data["balance_neto"] == 3400000.0


def test_e2e_safe_to_spend():
    hoy = date.today()
    txs_mock = [
        TransaccionItem("1", 3500000.0, date(hoy.year, hoy.month, 1), "Nomina", "ingreso"),
        TransaccionItem("2", 500000.0, date(hoy.year, hoy.month, 5), "Exito", "gasto", "Mercado")
    ]
    with patch("app.main.transacciones_client.obtener_transacciones_usuario", return_value=txs_mock):
        response = client.post(
            "/api/analitica/safe-to-spend",
            json={"compromisos_fijos": 1000000.0, "ahorro_metas": 500000.0},
            headers={"Authorization": "Bearer fake-jwt-token"}
        )
        assert response.status_code == 200
        data = response.json()
        assert data["saldo_disponible"] == 3000000.0
        assert data["monto_seguro_restante"] == 1500000.0
        assert data["nivel_salud"] in ["OPTIMO", "MODERADO", "CRITICO"]


def test_e2e_simular_gasto():
    hoy = date.today()
    txs_mock = [
        TransaccionItem("1", 3500000.0, date(hoy.year, hoy.month, 1), "Nomina", "ingreso"),
        TransaccionItem("2", 500000.0, date(hoy.year, hoy.month, 5), "Exito", "gasto", "Mercado")
    ]
    with patch("app.main.transacciones_client.obtener_transacciones_usuario", return_value=txs_mock):
        response = client.post(
            "/api/analitica/simular-gasto",
            json={
                "monto_simulado": 200000.0,
                "compromisos_fijos": 1000000.0,
                "ahorro_metas": 500000.0
            },
            headers={"Authorization": "Bearer fake-jwt-token"}
        )
        assert response.status_code == 200
        data = response.json()
        assert data["monto_simulado"] == 200000.0
        assert data["es_viable"] is True


def test_e2e_simular_inversiones():
    response = client.post(
        "/api/analitica/inversiones/simular",
        json={"monto": 1000000.0, "plazo_meses": 12}
    )
    assert response.status_code == 200
    data = response.json()
    assert data["monto_invertido"] == 1000000.0
    assert data["plazo_meses"] == 12
    assert len(data["proyecciones"]) > 0


def test_e2e_catalogo_inversiones():
    response = client.get("/api/analitica/inversiones/catalogo")
    assert response.status_code == 200
    data = response.json()
    assert len(data["opciones"]) > 0
