"""Envia los logs WARNING/ERROR de este servicio al microservicio central logs-service.

- Nunca bloquea ni rompe una peticion: el envio es en segundo plano y los fallos se ignoran.
- Los mensajes tambien siguen saliendo por consola (factor XI de los 12 factores).
- Configuracion por variables de entorno: LOGS_SERVICE_URL, LOGS_HABILITADO, LOGS_NIVEL_MINIMO.
"""
import logging
import os
import traceback
from concurrent.futures import ThreadPoolExecutor

import httpx

_IGNORAR = ("httpx", "httpcore", "urllib3")


class LogsServiceHandler(logging.Handler):
    def __init__(self, servicio: str, url: str, nivel=logging.WARNING):
        super().__init__(level=nivel)
        self.servicio = servicio
        self.url = url.rstrip("/") + "/api/logs"
        self._pool = ThreadPoolExecutor(max_workers=1, thread_name_prefix="logs-remoto")

    def emit(self, record: logging.LogRecord) -> None:
        if record.name.startswith(_IGNORAR):
            return
        try:
            detalle = None
            if record.exc_info:
                detalle = "".join(traceback.format_exception(*record.exc_info))[-6000:]
            payload = {
                "servicio": self.servicio,
                "nivel": "WARN" if record.levelno == logging.WARNING else record.levelname,
                "mensaje": record.getMessage()[:1900],
                "detalle": detalle,
                "ruta": getattr(record, "ruta", None),
                "metodo": getattr(record, "metodo", None),
                "estado_http": getattr(record, "estado_http", None),
            }
            self._pool.submit(self._enviar, payload)
        except Exception:
            pass  # un log nunca debe romper la aplicacion

    def _enviar(self, payload: dict) -> None:
        try:
            httpx.post(self.url, json=payload, timeout=2.0)
        except Exception:
            pass


def configurar_logs_remoto(servicio: str) -> None:
    if os.getenv("LOGS_HABILITADO", "true").lower() != "true":
        return
    url = os.getenv("LOGS_SERVICE_URL", "http://localhost:8085")
    nivel = getattr(logging, os.getenv("LOGS_NIVEL_MINIMO", "WARNING").upper(), logging.WARNING)
    raiz = logging.getLogger()
    if any(isinstance(h, LogsServiceHandler) for h in raiz.handlers):
        return
    raiz.addHandler(LogsServiceHandler(servicio, url, nivel))
    if raiz.level == logging.NOTSET or raiz.level > nivel:
        raiz.setLevel(nivel)
