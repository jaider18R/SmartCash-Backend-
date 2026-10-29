from datetime import datetime, timedelta, timezone
from typing import Dict, List, Optional, Tuple

from app.application.sanitizador import sanitizar
from app.domain.entities import NIVELES_VALIDOS, RegistroLog
from app.domain.ports.repositorio_logs_port import RepositorioLogsPort

_MAX_MENSAJE = 2000
_MAX_DETALLE = 8000


def _recortar(texto: Optional[str], maximo: int) -> Optional[str]:
    if texto is None:
        return None
    return texto if len(texto) <= maximo else texto[:maximo] + "...[recortado]"


class RegistrarLogUseCase:
    def __init__(self, repositorio: RepositorioLogsPort):
        self.repositorio = repositorio

    def ejecutar(self, servicio: str, nivel: str, mensaje: str, detalle: Optional[str] = None,
                 ruta: Optional[str] = None, metodo: Optional[str] = None,
                 estado_http: Optional[int] = None, usuario: Optional[str] = None,
                 fecha: Optional[datetime] = None) -> RegistroLog:
        nivel_norm = (nivel or "").upper().replace("WARNING", "WARN")
        if nivel_norm not in NIVELES_VALIDOS:
            raise ValueError(f"Nivel invalido. Use uno de: {', '.join(NIVELES_VALIDOS)}")
        if not servicio or not servicio.strip():
            raise ValueError("El servicio es obligatorio")
        if not mensaje or not mensaje.strip():
            raise ValueError("El mensaje es obligatorio")
        registro = RegistroLog(
            servicio=servicio.strip().lower(),
            nivel=nivel_norm,
            mensaje=_recortar(sanitizar(mensaje), _MAX_MENSAJE),
            detalle=_recortar(sanitizar(detalle), _MAX_DETALLE),
            ruta=ruta, metodo=metodo, estado_http=estado_http, usuario=usuario,
            fecha=fecha or datetime.now(timezone.utc),
        )
        return self.repositorio.guardar(registro)


class ConsultarLogsUseCase:
    def __init__(self, repositorio: RepositorioLogsPort):
        self.repositorio = repositorio

    def ejecutar(self, servicio=None, nivel=None, desde=None, hasta=None, texto=None,
                 limite: int = 100, desplazamiento: int = 0) -> Tuple[int, List[RegistroLog]]:
        nivel_norm = nivel.upper().replace("WARNING", "WARN") if nivel else None
        servicio_norm = servicio.strip().lower() if servicio else None
        return self.repositorio.consultar(servicio_norm, nivel_norm, desde, hasta, texto,
                                          max(1, min(limite, 500)), max(0, desplazamiento))


class ResumenLogsUseCase:
    def __init__(self, repositorio: RepositorioLogsPort):
        self.repositorio = repositorio

    def ejecutar(self) -> List[Dict[str, object]]:
        return self.repositorio.resumen()


class PurgarLogsUseCase:
    def __init__(self, repositorio: RepositorioLogsPort):
        self.repositorio = repositorio

    def ejecutar(self, dias: int) -> int:
        if dias < 1:
            raise ValueError("Los dias de retencion deben ser al menos 1")
        limite = datetime.now(timezone.utc) - timedelta(days=dias)
        return self.repositorio.purgar_anteriores_a(limite)
