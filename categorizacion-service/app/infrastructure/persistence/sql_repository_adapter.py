from typing import List, Optional
from sqlalchemy.orm import Session
from app.domain.ports.repository_port import RepositoryPort
from app.domain.entities import TransaccionClasificada, CorreccionCategoria
from app.infrastructure.persistence.models_sql import TransaccionClasificadaDB, CorreccionDB

class SqlRepositoryAdapter(RepositoryPort):
    def __init__(self, session: Session):
        self.session = session

    def guardar_clasificacion(self, t: TransaccionClasificada) -> None:
        db_obj = TransaccionClasificadaDB(
            transaccion_id=t.transaccion_id,
            comercio=t.comercio,
            monto=t.monto,
            descripcion=t.descripcion,
            categoria=t.categoria,
            nivel_confianza=t.nivel_confianza,
            usuario_id=t.usuario_id
        )
        self.session.merge(db_obj)
        self.session.commit()

    def obtener_clasificacion(self, transaccion_id: int) -> Optional[TransaccionClasificada]:
        obj = self.session.query(TransaccionClasificadaDB).filter_by(transaccion_id=transaccion_id).first()
        if not obj:
            return None
        return TransaccionClasificada(
            transaccion_id=obj.transaccion_id,
            comercio=obj.comercio,
            monto=obj.monto,
            descripcion=obj.descripcion,
            categoria=obj.categoria,
            nivel_confianza=obj.nivel_confianza,
            usuario_id=obj.usuario_id,
            fecha=obj.fecha
        )

    def guardar_correccion(self, c: CorreccionCategoria) -> None:
        db_obj = CorreccionDB(
            transaccion_id=c.transaccion_id,
            categoria_anterior=c.categoria_anterior,
            categoria_nueva=c.categoria_nueva,
            comercio=c.comercio,
            monto=c.monto
        )
        self.session.add(db_obj)
        self.session.commit()

    def obtener_historial_usuario(self, usuario_id: int, categoria: str) -> List[float]:
        filas = (self.session.query(TransaccionClasificadaDB.monto)
                 .filter_by(usuario_id=usuario_id, categoria=categoria)
                 .all())
        return [f[0] for f in filas]
