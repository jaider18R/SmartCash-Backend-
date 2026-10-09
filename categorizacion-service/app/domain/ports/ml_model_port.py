from abc import ABC, abstractmethod
from typing import Tuple

class MLModelPort(ABC):
    @abstractmethod
    def predecir(self, comercio: str, monto: float, descripcion: str = "") -> Tuple[str, float]:
        pass

    @abstractmethod
    def aprender_incrementalmente(self, comercio: str, monto: float, categoria_correcta: str) -> None:
        pass

    @abstractmethod
    def explicar(self, comercio: str, monto: float, categoria: str) -> str:
        pass
