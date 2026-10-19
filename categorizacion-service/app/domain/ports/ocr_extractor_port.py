from abc import ABC, abstractmethod
from typing import Dict, Any, Optional

class OcrExtractorPort(ABC):
    @abstractmethod
    def extraer_desde_bytes(self, imagen_bytes: bytes, texto_auxiliar: Optional[str] = None) -> Dict[str, Any]:
        pass
