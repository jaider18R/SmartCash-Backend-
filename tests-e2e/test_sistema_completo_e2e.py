import uuid
from datetime import date
import httpx
import pytest

URL_AUTH = "http://localhost:8081"
URL_TX = "http://localhost:8082"
URL_NOTIF = "http://localhost:8083"
URL_ANALITICA = "http://localhost:8084"
URL_CATEGORIZACION = "http://localhost:8000"


def test_e2e_flujo_completo_ecosistema_smartcash():
    sufijo = uuid.uuid4().hex[:8]
    correo = f"e2e_{sufijo}@smartcash.com"
    password = "PasswordSegura2026*"
    hoy = date.today().isoformat()

    with httpx.Client(timeout=15.0) as client:
        res_reg = client.post(
            f"{URL_AUTH}/auth/registro",
            json={"nombre": "Usuario E2E", "correo": correo, "password": password}
        )
        assert res_reg.status_code == 201
        id_usuario = res_reg.json()["idUsuario"]

        res_login = client.post(
            f"{URL_AUTH}/auth/login",
            json={"correo": correo, "password": password}
        )
        assert res_login.status_code == 200
        token = res_login.json()["token"]
        headers = {"Authorization": f"Bearer {token}"}

        res_cat_ia = client.post(
            f"{URL_CATEGORIZACION}/clasificar",
            json={
                "transaccion_id": 77701,
                "comercio": "Exito Supermercado",
                "monto": 250000.0,
                "descripcion": "Compra mercado",
                "usuario_id": id_usuario
            }
        )
        assert res_cat_ia.status_code == 200
        assert "categoria" in res_cat_ia.json()

        res_ingreso = client.post(
            f"{URL_TX}/transacciones",
            headers=headers,
            json={
                "idUsuario": id_usuario,
                "monto": 4000000.0,
                "fecha": hoy,
                "comercio": "Empresa Nomina",
                "tipoMovimiento": "ingreso"
            }
        )
        assert res_ingreso.status_code == 201

        res_gasto = client.post(
            f"{URL_TX}/transacciones",
            headers=headers,
            json={
                "idUsuario": id_usuario,
                "monto": 350000.0,
                "fecha": hoy,
                "comercio": "Exito Supermercado",
                "tipoMovimiento": "gasto"
            }
        )
        assert res_gasto.status_code == 201

        res_lista_tx = client.get(f"{URL_TX}/transacciones", headers=headers)
        assert res_lista_tx.status_code == 200
        assert len(res_lista_tx.json()) == 2

        res_presupuesto = client.post(
            f"{URL_TX}/api/presupuestos",
            headers=headers,
            json={
                "idCategoria": "00000000-0000-0000-0000-000000000002",
                "nombreCategoria": "Mercado y Supermercado",
                "montoLimite": 800000.0,
                "mes": date.today().month,
                "anio": date.today().year
            }
        )
        assert res_presupuesto.status_code == 201

        res_meta = client.post(
            f"{URL_TX}/api/metas-ahorro",
            headers=headers,
            json={
                "nombre": "Fondo Emergencia E2E",
                "descripcion": "Meta E2E",
                "montoObjetivo": 5000000.0,
                "fechaLimite": "2027-12-31"
            }
        )
        assert res_meta.status_code == 201

        res_resumen = client.get(f"{URL_ANALITICA}/api/analitica/resumen-mensual", headers=headers)
        assert res_resumen.status_code == 200
        datos_resumen = res_resumen.json()
        assert datos_resumen["total_ingresos"] == 4000000.0
        assert datos_resumen["total_gastos"] == 350000.0
        assert datos_resumen["balance_neto"] == 3650000.0

        res_safe = client.post(
            f"{URL_ANALITICA}/api/analitica/safe-to-spend",
            headers=headers,
            json={"compromisos_fijos": 1000000.0, "ahorro_metas": 500000.0}
        )
        assert res_safe.status_code == 200
        assert res_safe.json()["saldo_disponible"] == 3650000.0

        res_notif_health = client.get(f"{URL_NOTIF}/api/notificaciones/health")
        assert res_notif_health.status_code == 200
        assert res_notif_health.json()["status"] == "UP"
