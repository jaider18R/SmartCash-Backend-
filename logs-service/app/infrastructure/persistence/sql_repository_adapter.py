from datetime import datetime
from typing import Dict, List, Optional, Tuple

from sqlalchemy import delete, func, select
from sqlalchemy.orm import Session

from app.domain.entities import RegistroLog
from app.domain.ports.repositorio_logs_port import RepositorioLogsPort
from app.infrastructure.persistence.models_sql import LogSistemaModel


def _a_entidad(m: LogSistemaModel) -> RegistroLog:
    return RegistroLog(id=m.id, fecha=m.fecha, servicio=m.servicio, nivel=m.nivel, mensaje=m.mensaje,
                       detalle=m.detalle, ruta=m.ruta, metodo=m.metodo,
                       estado_http=m.estado_http, usuario=m.usuario)


class SqlRepositorioLogsAdapter(RepositorioLogsPort):
    def __init__(self, db: Session):
        self.db = db

    def guardar(self, registro: RegistroLog) -> RegistroLog:
        modelo = LogSistemaModel(fecha=registro.fecha, servicio=registro.servicio, nivel=registro.nivel,
                                 mensaje=registro.mensaje, detalle=registro.detalle, ruta=registro.ruta,
                                 metodo=registro.metodo, estado_http=registro.estado_http,
                                 usuario=registro.usuario)
        self.db.add(modelo)
        self.db.commit()
        self.db.refresh(modelo)
        return _a_entidad(modelo)

    def consultar(self, servicio, nivel, desde, hasta, texto, limite, desplazamiento
                  ) -> Tuple[int, List[RegistroLog]]:
        filtros = []
        if servicio:
            filtros.append(LogSistemaModel.servicio == servicio)
        if nivel:
            filtros.append(LogSistemaModel.nivel == nivel)
        if desde:
            filtros.append(LogSistemaModel.fecha >= desde)
        if hasta:
            filtros.append(LogSistemaModel.fecha <= hasta)
        if texto:
            filtros.append(LogSistemaModel.mensaje.ilike(f"%{texto}%"))
        total = self.db.execute(select(func.count(LogSistemaModel.id)).where(*filtros)).scalar_one()
        filas = self.db.execute(
            select(LogSistemaModel).where(*filtros)
            .order_by(LogSistemaModel.fecha.desc(), LogSistemaModel.id.desc())
            .limit(limite).offset(desplazamiento)
        ).scalars().all()
        return total, [_a_entidad(f) for f in filas]

    def resumen(self) -> List[Dict[str, object]]:
        filas = self.db.execute(
            select(LogSistemaModel.servicio, LogSistemaModel.nivel, func.count(LogSistemaModel.id))
            .group_by(LogSistemaModel.servicio, LogSistemaModel.nivel)
            .order_by(LogSistemaModel.servicio, LogSistemaModel.nivel)
        ).all()
        return [{"servicio": s, "nivel": n, "cantidad": c} for s, n, c in filas]

    def purgar_anteriores_a(self, fecha: datetime) -> int:
        resultado = self.db.execute(delete(LogSistemaModel).where(LogSistemaModel.fecha < fecha))
        self.db.commit()
        return resultado.rowcount or 0
