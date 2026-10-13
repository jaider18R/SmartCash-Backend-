import httpx
from datetime import datetime
from typing import List
from app.domain.entities import TransaccionItem
from app.domain.ports.transacciones_client_port import TransaccionesClientPort

class TransaccionesHttpAdapter(TransaccionesClientPort):
    def __init__(self, base_url: str = "http://localhost:8082"):
        self.base_url = base_url

    def obtener_transacciones_usuario(self, token: str) -> List[TransaccionItem]:
        try:
            headers = {"Authorization": f"Bearer {token}" if not token.startswith("Bearer ") else token}
            response = httpx.get(f"{self.base_url}/transacciones", headers=headers, timeout=5.0)
            if response.status_code == 200:
                data = response.json()
                items = []
                for row in data:
                    fecha_dt = datetime.strptime(row["fecha"], "%Y-%m-%d").date()
                    items.append(TransaccionItem(
                        id_transaccion=str(row.get("idTransaccion")),
                        monto=float(row.get("monto", 0.0)),
                        fecha=fecha_dt,
                        comercio=row.get("comercio", ""),
                        tipo_movimiento=row.get("tipoMovimiento", ""),
                        id_categoria=str(row.get("idCategoria")) if row.get("idCategoria") else None
                    ))
                return items
        except Exception:
            pass
        return []
