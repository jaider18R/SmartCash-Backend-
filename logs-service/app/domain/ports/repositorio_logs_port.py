from abc import ABC, abstractmethod
from datetime import datetime
from typing import Dict, List, Optional, Tuple

from app.domain.entities import RegistroLog


class RepositorioLogsPort(ABC):
    """Puerto de salida: como se guardan y consultan los logs (la capa de aplicacion no sabe de SQL)."""

    @abstractmethod
    def guardar(self, registro: RegistroLog) -> RegistroLog: ...

    @abstractmethod
    def consultar(self, servicio: Optional[str], nivel: Optional[str],
                  desde: Optional[datetime], hasta: Optional[datetime],
                  texto: Optional[str], limite: int, desplazamiento: int
                  ) -> Tuple[int, List[RegistroLog]]: ...

    @abstractmethod
    def resumen(self) -> List[Dict[str, object]]: ...

    @abstractmethod
    def purgar_anteriores_a(self, fecha: datetime) -> int: ...
