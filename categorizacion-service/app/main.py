import base64
import os
from fastapi import FastAPI, Depends, HTTPException
from fastapi.middleware.cors import CORSMiddleware
from sqlalchemy import create_engine
from sqlalchemy.orm import sessionmaker, Session

from app.api.schemas import (
    ClasificarRequest, ClasificarResponse,
    CorreccionRequest, MensajeResponse,
    ExplicacionResponse,
    AnalizarPatronRequest, AnalizarPatronResponse,
    EscanearFacturaRequest, EscanearFacturaResponse
)
from app.infrastructure.persistence.models_sql import Base
from app.infrastructure.persistence.sql_repository_adapter import SqlRepositoryAdapter
from app.infrastructure.ml.incremental_classifier import IncrementalClassifierAdapter
from app.infrastructure.ocr.receipt_ocr_adapter import ReceiptOcrAdapter
from app.application.use_cases import (
    ClasificarTransaccionUseCase,
    CorregirCategoriaUseCase,
    ExplicarClasificacionUseCase,
    DetectarPatronInusualUseCase,
    ExtraerDatosFacturaUseCase
)

app = FastAPI(
    title="SmartCash - Microservicio de Categorizacion Inteligente",
    version="1.0.0",
    description="Microservicio con Active & Incremental Learning para clasificacion y analisis de transacciones."
)

app.add_middleware(
    CORSMiddleware,
    allow_origins=["*"],
    allow_credentials=True,
    allow_methods=["*"],
    allow_headers=["*"],
)

DATABASE_URL = os.getenv("DATABASE_URL", "sqlite:///./categorizacion_db.sqlite")
engine = create_engine(DATABASE_URL, connect_args={"check_same_thread": False})
SessionLocal = sessionmaker(bind=engine, autoflush=False, autocommit=False)
Base.metadata.create_all(bind=engine)

ml_adapter = IncrementalClassifierAdapter()
ocr_adapter = ReceiptOcrAdapter()

def get_db():
    db = SessionLocal()
    try:
        yield db
    finally:
        db.close()

@app.post("/clasificar", response_model=ClasificarResponse, tags=["Clasificacion (HU-20)"])
def clasificar(req: ClasificarRequest, db: Session = Depends(get_db)):
    repo = SqlRepositoryAdapter(db)
    use_case = ClasificarTransaccionUseCase(ml_adapter, repo)
    resultado = use_case.ejecutar(req.transaccion_id, req.comercio, req.monto, req.descripcion, req.usuario_id)
    return ClasificarResponse(categoria=resultado.categoria, nivel_confianza=resultado.nivel_confianza)

@app.post("/correcciones", response_model=MensajeResponse, tags=["Aprendizaje Incremental (HU-21)"])
def corregir(req: CorreccionRequest, db: Session = Depends(get_db)):
    repo = SqlRepositoryAdapter(db)
    use_case = CorregirCategoriaUseCase(ml_adapter, repo)
    use_case.ejecutar(req.transaccion_id, req.categoria_anterior, req.categoria_nueva, req.comercio, req.monto)
    return MensajeResponse(mensaje="Modelo actualizado incrementalmente con exito")

@app.get("/clasificar/{transaccion_id}/explicacion", response_model=ExplicacionResponse, tags=["Explicabilidad (HU-22)"])
def explicar(transaccion_id: int, db: Session = Depends(get_db)):
    repo = SqlRepositoryAdapter(db)
    use_case = ExplicarClasificacionUseCase(ml_adapter, repo)
    try:
        res = use_case.ejecutar(transaccion_id)
        return ExplicacionResponse(categoria=res.categoria, explicacion=res.explicacion)
    except ValueError as e:
        raise HTTPException(status_code=404, detail=str(e))

@app.post("/patrones/analizar", response_model=AnalizarPatronResponse, tags=["Deteccion de Anomalias (HU-23)"])
def analizar_patron(req: AnalizarPatronRequest, db: Session = Depends(get_db)):
    repo = SqlRepositoryAdapter(db)
    use_case = DetectarPatronInusualUseCase(repo)
    try:
        res = use_case.ejecutar(req.usuario_id, req.transaccion_id)
        return AnalizarPatronResponse(es_inusual=res.es_inusual, descripcion_alerta=res.descripcion_alerta)
    except ValueError as e:
        raise HTTPException(status_code=404, detail=str(e))

@app.post("/ocr/factura", response_model=EscanearFacturaResponse, tags=["Escaneo Inteligente de Facturas (Zero-Storage)"])
def escanear_factura(req: EscanearFacturaRequest):
    try:
        contenido_b64 = req.imagen_base64
        if "," in contenido_b64:
            contenido_b64 = contenido_b64.split(",", 1)[1]
        imagen_bytes = base64.b64decode(contenido_b64)
    except Exception:
        raise HTTPException(status_code=400, detail="Formato de imagen invalido")

    try:
        use_case = ExtraerDatosFacturaUseCase(ocr_adapter, ml_adapter)
        res = use_case.ejecutar(imagen_bytes, req.nombre_archivo)
        return EscanearFacturaResponse(
            comercio=res.comercio,
            monto=res.monto,
            fecha=res.fecha,
            tipo_movimiento=res.tipo_movimiento,
            categoria_sugerida=res.categoria_sugerida,
            nivel_confianza=res.nivel_confianza,
            nit=res.nit,
            texto_detectado_resumen=res.texto_detectado_resumen
        )
    finally:
        del imagen_bytes

@app.get("/health", tags=["Monitoreo"])
def health():
    return {"status": "UP", "service": "categorizacion-service"}

