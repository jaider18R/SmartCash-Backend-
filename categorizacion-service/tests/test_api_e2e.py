import base64
from fastapi.testclient import TestClient
from app.main import app

client = TestClient(app)


def test_e2e_health():
    response = client.get("/health")
    assert response.status_code == 200
    assert response.json()["status"] == "UP"


def test_e2e_clasificar_explicar_y_corregir():
    tx_id = 9001
    res_clasificar = client.post(
        "/clasificar",
        json={
            "transaccion_id": tx_id,
            "comercio": "Exito Supermercado",
            "monto": 150000.0,
            "descripcion": "Compra mercado mensual",
            "usuario_id": "user-e2e-1"
        }
    )
    assert res_clasificar.status_code == 200
    datos = res_clasificar.json()
    assert "categoria" in datos
    assert datos["nivel_confianza"] > 0

    res_explicar = client.get(f"/clasificar/{tx_id}/explicacion")
    assert res_explicar.status_code == 200
    assert "explicacion" in res_explicar.json()

    res_corregir = client.post(
        "/correcciones",
        json={
            "transaccion_id": tx_id,
            "categoria_anterior": datos["categoria"],
            "categoria_nueva": "Mercado y Supermercado",
            "comercio": "Exito Supermercado",
            "monto": 150000.0
        }
    )
    assert res_corregir.status_code == 200
    assert "exit" in res_corregir.json()["mensaje"].lower()


def test_e2e_explicar_transaccion_inexistente_retorna_404():
    res = client.get("/clasificar/9999999/explicacion")
    assert res.status_code == 404


def test_e2e_analizar_patron():
    tx_id = 9002
    client.post(
        "/clasificar",
        json={
            "transaccion_id": tx_id,
            "comercio": "Uber Trip",
            "monto": 25000.0,
            "descripcion": "Viaje",
            "usuario_id": "user-e2e-2"
        }
    )
    res = client.post(
        "/patrones/analizar",
        json={"usuario_id": "user-e2e-2", "transaccion_id": tx_id}
    )
    assert res.status_code == 200
    assert "es_inusual" in res.json()


def test_e2e_ocr_factura_invalida():
    res = client.post(
        "/ocr/factura",
        json={"imagen_base64": "a", "nombre_archivo": "recibo.png"}
    )
    assert res.status_code == 400
