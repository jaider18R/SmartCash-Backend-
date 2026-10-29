import os
from datetime import date
import logging
from fastapi import FastAPI, Request, Header, HTTPException, Query
from fastapi.middleware.cors import CORSMiddleware
from fastapi.responses import JSONResponse
from app.infrastructure.observabilidad.logs_remoto import configurar_logs_remoto
from typing import Optional

from app.api.schemas import (
    ResumenMensualResponse,
    SafeToSpendRequest,
    SafeToSpendResponse,
    SimularGastoRequest,
    SimularGastoResponse,
    SimularInversionRequest,
    SimularInversionResponse,
    ProyeccionInversionItemDTO,
    CatalogoInversionesResponse,
    OpcionInversionDTO
)
from app.domain.motor_analitica import MotorAnalitica
from app.infrastructure.clients.transacciones_http_adapter import TransaccionesHttpAdapter
from app.application.use_cases import (
    ObtenerResumenMensualUseCase,
    CalcularSafeToSpendUseCase,
    SimularGastoUseCase,
    SimularInversionesUseCase,
    ObtenerCatalogoInversionesUseCase
)

app = FastAPI(
    title="SmartCash - Microservicio de Analitica Financiera y Safe-to-Spend",
    version="1.0.0",
    description="Microservicio para agregaciones analiticas, calculo de gasto seguro diario y simulaciones What-If."
)

configurar_logs_remoto("analitica-service")
logger = logging.getLogger("analitica-service")


@app.exception_handler(Exception)
async def manejar_error_no_controlado(request: Request, exc: Exception):
    """Registra el error (consola + logs-service) y responde sin exponer detalles tecnicos."""
    logger.error(
        "Error no controlado en %s %s: %s", request.method, request.url.path, exc,
        exc_info=(type(exc), exc, exc.__traceback__),
        extra={"ruta": request.url.path, "metodo": request.method, "estado_http": 500},
    )
    return JSONResponse(status_code=500, content={"detail": "Ocurrio un error interno. Intenta de nuevo mas tarde."})


app.add_middleware(
    CORSMiddleware,
    allow_origins=[o.strip() for o in os.getenv("CORS_ALLOWED_ORIGINS", "*").split(",")],
    allow_credentials=True,
    allow_methods=["*"],
    allow_headers=["*"],
)

transacciones_client = TransaccionesHttpAdapter()
motor = MotorAnalitica()

resumen_use_case = ObtenerResumenMensualUseCase(transacciones_client, motor)
safe_use_case = CalcularSafeToSpendUseCase(transacciones_client, motor)
simular_use_case = SimularGastoUseCase(safe_use_case, motor)
simular_inversiones_use_case = SimularInversionesUseCase(motor)
catalogo_inversiones_use_case = ObtenerCatalogoInversionesUseCase(motor)


@app.get("/api/analitica/resumen-mensual", response_model=ResumenMensualResponse, tags=["Analitica"])
def resumen_mensual(
    mes: Optional[int] = Query(default=None),
    anio: Optional[int] = Query(default=None),
    authorization: str = Header(...)
):
    hoy = date.today()
    mes_final = mes or hoy.month
    anio_final = anio or hoy.year

    res = resumen_use_case.ejecutar(authorization, mes_final, anio_final)
    return ResumenMensualResponse(
        mes=res.mes,
        anio=res.anio,
        total_ingresos=res.total_ingresos,
        total_gastos=res.total_gastos,
        balance_neto=res.balance_neto,
        tasa_ahorro_porcentaje=res.tasa_ahorro_porcentaje,
        desglose_categorias=res.desglose_categorias
    )

@app.post("/api/analitica/safe-to-spend", response_model=SafeToSpendResponse, tags=["Gasto Seguro Diario"])
def safe_to_spend(
    req: SafeToSpendRequest,
    authorization: str = Header(...)
):
    res = safe_use_case.ejecutar(authorization, req.compromisos_fijos, req.ahorro_metas)
    return SafeToSpendResponse(
        saldo_disponible=res.saldo_disponible,
        compromisos_fijos=res.compromisos_fijos,
        ahorro_metas=res.ahorro_metas,
        monto_seguro_restante=res.monto_seguro_restante,
        dias_restantes_mes=res.dias_restantes_mes,
        limite_diario_seguro=res.limite_diario_seguro,
        nivel_salud=res.nivel_salud,
        recomendacion=res.recomendacion
    )

@app.post("/api/analitica/simular-gasto", response_model=SimularGastoResponse, tags=["Simulacion What-If"])
def simular_gasto(
    req: SimularGastoRequest,
    authorization: str = Header(...)
):
    res = simular_use_case.ejecutar(authorization, req.monto_simulado, req.compromisos_fijos, req.ahorro_metas)
    return SimularGastoResponse(
        monto_simulado=res.monto_simulado,
        nuevo_limite_diario=res.nuevo_limite_diario,
        diferencia_diaria=res.diferencia_diaria,
        impacto_metas=res.impacto_metas,
        es_viable=res.es_viable
    )

@app.post("/api/analitica/inversiones/simular", response_model=SimularInversionResponse, tags=["Inversiones"])
def simular_inversiones(req: SimularInversionRequest):
    res = simular_inversiones_use_case.ejecutar(req.monto, req.plazo_meses)
    return SimularInversionResponse(
        monto_invertido=res.monto_invertido,
        plazo_meses=res.plazo_meses,
        inflacion_colombia_estimada_porcentaje=res.inflacion_colombia_estimada_porcentaje,
        perdida_poder_adquisitivo_efectivo=res.perdida_poder_adquisitivo_efectivo,
        proyecciones=[
            ProyeccionInversionItemDTO(
                tipo=p.tipo,
                nombre_producto=p.nombre_producto,
                plataforma=p.plataforma,
                tasa_ea_estimada=p.tasa_ea_estimada,
                rendimiento_bruto=p.rendimiento_bruto,
                retencion_fuente_estimada=p.retencion_fuente_estimada,
                rendimiento_neto=p.rendimiento_neto,
                capital_final=p.capital_final,
                riesgo=p.riesgo,
                descripcion_facil=p.descripcion_facil
            )
            for p in res.proyecciones
        ],
        recomendacion_perfil=res.recomendacion_perfil
    )

@app.get("/api/analitica/inversiones/catalogo", response_model=CatalogoInversionesResponse, tags=["Inversiones"])
def obtener_catalogo_inversiones():
    opciones = catalogo_inversiones_use_case.ejecutar()
    return CatalogoInversionesResponse(
        opciones=[
            OpcionInversionDTO(
                id_opcion=o.id_opcion,
                categoria=o.categoria,
                titulo=o.titulo,
                subtitulo=o.subtitulo,
                que_es_facil=o.que_es_facil,
                rentabilidad_estimada=o.rentabilidad_estimada,
                tasa_ea_promedio=o.tasa_ea_promedio,
                riesgo=o.riesgo,
                plataformas_recomendadas=o.plataformas_recomendadas,
                vigilancia=o.vigilancia,
                monto_minimo=o.monto_minimo,
                disponibilidad_liquidez=o.disponibilidad_liquidez,
                url_promocional=o.url_promocional
            )
            for o in opciones
        ]
    )

@app.get("/health", tags=["Monitoreo"])
def health():
    return {"status": "UP", "service": "analitica-service", "port": 8084}

