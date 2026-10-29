from datetime import datetime, timedelta, timezone

import pytest
from fastapi.testclient import TestClient
from sqlalchemy import create_engine
from sqlalchemy.pool import StaticPool

from app.application.sanitizador import sanitizar
from app.infrastructure.persistence.database import crear_sesion_factory
from app.main import app, get_db


@pytest.fixture()
def client():
    engine = create_engine("sqlite://", connect_args={"check_same_thread": False}, poolclass=StaticPool)
    factory = crear_sesion_factory(engine)

    def override():
        db = factory()
        try:
            yield db
        finally:
            db.close()

    app.dependency_overrides[get_db] = override
    yield TestClient(app)
    app.dependency_overrides.clear()


def _log(**kw):
    base = {"servicio": "usuarios-auth", "nivel": "ERROR", "mensaje": "Fallo de prueba"}
    base.update(kw)
    return base


def test_registrar_y_consultar(client):
    r = client.post("/api/logs", json=_log(detalle="stack", ruta="/auth/login", metodo="POST", estado_http=500))
    assert r.status_code == 201
    assert r.json()["servicio"] == "usuarios-auth" and r.json()["nivel"] == "ERROR"
    lista = client.get("/api/logs").json()
    assert lista["total"] == 1 and lista["items"][0]["ruta"] == "/auth/login"


def test_nivel_warning_se_normaliza_a_warn(client):
    assert client.post("/api/logs", json=_log(nivel="warning")).json()["nivel"] == "WARN"


def test_nivel_invalido_devuelve_400(client):
    assert client.post("/api/logs", json=_log(nivel="XYZ")).status_code == 400


def test_mensaje_vacio_devuelve_400(client):
    assert client.post("/api/logs", json=_log(mensaje="   ")).status_code == 400


def test_filtros_por_servicio_nivel_y_texto(client):
    client.post("/api/logs", json=_log(servicio="transacciones-service", nivel="WARN", mensaje="Categorizacion lenta"))
    client.post("/api/logs", json=_log())
    assert client.get("/api/logs?servicio=transacciones-service").json()["total"] == 1
    assert client.get("/api/logs?nivel=ERROR").json()["total"] == 1
    assert client.get("/api/logs?texto=lenta").json()["total"] == 1
    assert client.get("/api/logs?texto=inexistente").json()["total"] == 0


def test_filtro_por_fechas_y_paginacion(client):
    ayer = (datetime.now(timezone.utc) - timedelta(days=1)).isoformat()
    for i in range(3):
        client.post("/api/logs", json=_log(mensaje=f"m{i}"))
    client.post("/api/logs", json=_log(mensaje="viejo", fecha=(datetime.now(timezone.utc) - timedelta(days=5)).isoformat()))
    assert client.get("/api/logs", params={"desde": ayer}).json()["total"] == 3
    pagina = client.get("/api/logs?limite=2&desplazamiento=0").json()
    assert pagina["total"] == 4 and len(pagina["items"]) == 2


def test_resumen(client):
    client.post("/api/logs", json=_log())
    client.post("/api/logs", json=_log())
    client.post("/api/logs", json=_log(nivel="INFO"))
    datos = {(d["servicio"], d["nivel"]): d["cantidad"] for d in client.get("/api/logs/resumen").json()}
    assert datos[("usuarios-auth", "ERROR")] == 2 and datos[("usuarios-auth", "INFO")] == 1


def test_purgar_logs_viejos(client):
    client.post("/api/logs", json=_log(mensaje="viejo", fecha=(datetime.now(timezone.utc) - timedelta(days=40)).isoformat()))
    client.post("/api/logs", json=_log(mensaje="nuevo"))
    assert client.delete("/api/logs/purgar?dias=30").json()["eliminados"] == 1
    assert client.get("/api/logs").json()["total"] == 1


def test_api_key_protege_consulta_pero_no_el_registro(client, monkeypatch):
    monkeypatch.setenv("LOGS_API_KEY", "clave-test")
    assert client.post("/api/logs", json=_log()).status_code == 201
    assert client.get("/api/logs").status_code == 401
    assert client.get("/api/logs", headers={"X-API-Key": "mala"}).status_code == 401
    assert client.get("/api/logs", headers={"X-API-Key": "clave-test"}).status_code == 200
    assert client.delete("/api/logs/purgar").status_code == 401


def test_datos_sensibles_se_enmascaran(client):
    r = client.post("/api/logs", json=_log(
        mensaje='Fallo login password=MiClave123 token: abc.def',
        detalle='Authorization: Bearer eyJhbGciOi.eyJzdWIiOi.firma123 y {"password":"secreto"}'))
    cuerpo = r.json()
    assert "MiClave123" not in cuerpo["mensaje"] and "secreto" not in cuerpo["detalle"]
    assert "eyJhbGciOi" not in cuerpo["detalle"]


def test_sanitizar_none():
    assert sanitizar(None) is None


def test_health(client):
    assert client.get("/health").json()["status"] == "UP"
