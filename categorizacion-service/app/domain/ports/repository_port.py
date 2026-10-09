from abc import ABC, abstractmethod
from typing import List, Optional
from app.domain.entities import TransaccionClasificada, CorreccionCategoria

class RepositoryPort(ABC):
    @abstractmethod
    def guardar_clasificacion(self, t: TransaccionClasificada) -> None:
        pass

    @abstractmethod
    def obtener_clasificacion(self, transaccion_id: int) -> Optional[TransaccionClasificada]:
        pass

    @abstractmethod
    def guardar_correccion(self, c: CorreccionCategoria) -> None:
        pass

    @abstractmethod
    def obtener_historial_usuario(self, usuario_id: int, categoria: str) -> List[float]:
        pass
