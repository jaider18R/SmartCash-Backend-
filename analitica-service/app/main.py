from datetime import date
from fastapi import FastAPI, Header, HTTPException, Query
from fastapi.middleware.cors import CORSMiddleware
from typing import Optional

from app.api.schemas import (
    ResumenMensualResponse,
    SafeToSpendRequest,
    SafeToSpendResponse,
    SimularGastoRequest,
    SimularGastoResponse
)
from app.domain.motor_analitica import MotorAnalitica
from app.infrastructure.clients.transacciones_http_adapter import TransaccionesHttpAdapter
from app.application.use_cases import (
    ObtenerResumenMensualUseCase,
    CalcularSafeToSpendUseCase,
    SimularGastoUseCase
)

app = FastAPI(
    title="SmartCash - Microservicio de Analitica Financiera y Safe-to-Spend",
    version="1.0.0",
    description="Microservicio para agregaciones analiticas, calculo de gasto seguro diario y simulaciones What-If."
)

app.add_middleware(
    CORSMiddleware,
    allow_origins=["*"],
    allow_credentials=True,
    allow_methods=["*"],
    allow_headers=["*"],
)

transacciones_client = TransaccionesHttpAdapter()
motor = MotorAnalitica()

resumen_use_case = ObtenerResumenMensualUseCase(transacciones_client, motor)
safe_use_case = CalcularSafeToSpendUseCase(transacciones_client, motor)
simular_use_case = SimularGastoUseCase(safe_use_case, motor)

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

@app.get("/health", tags=["Monitoreo"])
def health():
    return {"status": "UP", "service": "analitica-service", "port": 8084}
