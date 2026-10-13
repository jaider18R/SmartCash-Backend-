from abc import ABC, abstractmethod
from typing import List
from app.domain.entities import TransaccionItem

class TransaccionesClientPort(ABC):
    @abstractmethod
    def obtener_transacciones_usuario(self, token: str) -> List[TransaccionItem]:
        pass
