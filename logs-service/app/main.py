import os
from datetime import datetime
from typing import Optional

from fastapi import Depends, FastAPI, Header, HTTPException, Query
from fastapi.middleware.cors import CORSMiddleware
from sqlalchemy.orm import Session

from app.api.schemas import (LogRequest, LogResponse, LogsPaginadosResponse, PurgaResponse, ResumenItem)
from app.application.use_cases import (ConsultarLogsUseCase, PurgarLogsUseCase, RegistrarLogUseCase,
                                       ResumenLogsUseCase)
from app.infrastructure.persistence.database import crear_motor, crear_sesion_factory
from app.infrastructure.persistence.sql_repository_adapter import SqlRepositorioLogsAdapter

app = FastAPI(
    title="SmartCash - Microservicio de Logs",
    version="1.0.0",
    description="Recibe los errores y eventos de todos los microservicios y los guarda en su propia base de datos (logs_db).",
)

app.add_middleware(
    CORSMiddleware,
    allow_origins=[o.strip() for o in os.getenv("CORS_ALLOWED_ORIGINS", "*").split(",")],
    allow_credentials=True,
    allow_methods=["*"],
    allow_headers=["*"],
)

# La sesion se crea al arrancar; los tests pueden reemplazar get_db con dependency_overrides.
_session_factory = None


def _factory():
    global _session_factory
    if _session_factory is None:
        _session_factory = crear_sesion_factory(crear_motor())
    return _session_factory


def get_db():
    db = _factory()()
    try:
        yield db
    finally:
        db.close()


def exigir_api_key(x_api_key: Optional[str] = Header(default=None)):
    """Si LOGS_API_KEY esta definida, consultar y purgar exigen el encabezado X-API-Key."""
    esperada = os.getenv("LOGS_API_KEY", "")
    if esperada and x_api_key != esperada:
        raise HTTPException(status_code=401, detail="API key invalida o ausente")


def _a_respuesta(r) -> LogResponse:
    return LogResponse(id=r.id, fecha=r.fecha, servicio=r.servicio, nivel=r.nivel, mensaje=r.mensaje,
                       detalle=r.detalle, ruta=r.ruta, metodo=r.metodo, estado_http=r.estado_http,
                       usuario=r.usuario)


@app.post("/api/logs", response_model=LogResponse, status_code=201, tags=["Logs"])
def registrar_log(req: LogRequest, db: Session = Depends(get_db)):
    try:
        registro = RegistrarLogUseCase(SqlRepositorioLogsAdapter(db)).ejecutar(
            req.servicio, req.nivel, req.mensaje, req.detalle, req.ruta, req.metodo,
            req.estado_http, req.usuario, req.fecha)
    except ValueError as e:
        raise HTTPException(status_code=400, detail=str(e))
    return _a_respuesta(registro)


@app.get("/api/logs", response_model=LogsPaginadosResponse, tags=["Logs"],
         dependencies=[Depends(exigir_api_key)])
def consultar_logs(
    servicio: Optional[str] = Query(None, description="Ej: usuarios-auth"),
    nivel: Optional[str] = Query(None, description="DEBUG, INFO, WARN, ERROR, CRITICAL"),
    desde: Optional[datetime] = Query(None, description="Fecha ISO, ej. 2026-10-28T00:00:00"),
    hasta: Optional[datetime] = Query(None),
    texto: Optional[str] = Query(None, description="Busca dentro del mensaje"),
    limite: int = Query(100, ge=1, le=500),
    desplazamiento: int = Query(0, ge=0),
    db: Session = Depends(get_db),
):
    total, items = ConsultarLogsUseCase(SqlRepositorioLogsAdapter(db)).ejecutar(
        servicio, nivel, desde, hasta, texto, limite, desplazamiento)
    return LogsPaginadosResponse(total=total, limite=limite, desplazamiento=desplazamiento,
                                 items=[_a_respuesta(i) for i in items])


@app.get("/api/logs/resumen", response_model=list[ResumenItem], tags=["Logs"],
         dependencies=[Depends(exigir_api_key)])
def resumen_logs(db: Session = Depends(get_db)):
    return ResumenLogsUseCase(SqlRepositorioLogsAdapter(db)).ejecutar()


@app.delete("/api/logs/purgar", response_model=PurgaResponse, tags=["Logs"],
            dependencies=[Depends(exigir_api_key)])
def purgar_logs(dias: int = Query(30, ge=1, description="Se borran los logs con mas de N dias"),
                db: Session = Depends(get_db)):
    try:
        n = PurgarLogsUseCase(SqlRepositorioLogsAdapter(db)).ejecutar(dias)
    except ValueError as e:
        raise HTTPException(status_code=400, detail=str(e))
    return PurgaResponse(eliminados=n, mensaje=f"Se eliminaron {n} logs con mas de {dias} dias")


@app.get("/health", tags=["Salud"])
def health():
    return {"status": "UP", "service": "logs-service"}
