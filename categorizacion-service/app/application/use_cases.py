import numpy as np
from app.domain.ports.ml_model_port import MLModelPort
from app.domain.ports.repository_port import RepositoryPort
from app.domain.ports.ocr_extractor_port import OcrExtractorPort
from app.domain.entities import (
    TransaccionClasificada,
    CorreccionCategoria,
    ExplicacionClasificacion,
    AlertaPatronInusual,
    DatosFacturaExtraidos
)

class ClasificarTransaccionUseCase:
    def __init__(self, ml_model: MLModelPort, repo: RepositoryPort):
        self.ml_model = ml_model
        self.repo = repo

    def ejecutar(self, transaccion_id: int, comercio: str, monto: float, descripcion: str = "", usuario_id: str = None) -> TransaccionClasificada:
        cat, confianza = self.ml_model.predecir(comercio, monto, descripcion)
        t = TransaccionClasificada(
            transaccion_id=transaccion_id,
            comercio=comercio,
            monto=monto,
            descripcion=descripcion,
            categoria=cat,
            nivel_confianza=confianza,
            usuario_id=str(usuario_id) if usuario_id is not None else None
        )
        self.repo.guardar_clasificacion(t)
        return t

class CorregirCategoriaUseCase:
    def __init__(self, ml_model: MLModelPort, repo: RepositoryPort):
        self.ml_model = ml_model
        self.repo = repo

    def ejecutar(self, transaccion_id: int, cat_ant: str, cat_nueva: str, comercio: str, monto: float) -> None:
        c = CorreccionCategoria(transaccion_id, cat_ant, cat_nueva, comercio, monto)
        self.repo.guardar_correccion(c)
        self.ml_model.aprender_incrementalmente(comercio, monto, cat_nueva)

class ExplicarClasificacionUseCase:
    def __init__(self, ml_model: MLModelPort, repo: RepositoryPort):
        self.ml_model = ml_model
        self.repo = repo

    def ejecutar(self, transaccion_id: int) -> ExplicacionClasificacion:
        t = self.repo.obtener_clasificacion(transaccion_id)
        if not t:
            raise ValueError(f"Transaccion {transaccion_id} no encontrada")
        texto_exp = self.ml_model.explicar(t.comercio, t.monto, t.categoria)
        return ExplicacionClasificacion(transaccion_id=t.transaccion_id, categoria=t.categoria, explicacion=texto_exp)

class DetectarPatronInusualUseCase:
    def __init__(self, repo: RepositoryPort):
        self.repo = repo

    def ejecutar(self, usuario_id: str, transaccion_id: int) -> AlertaPatronInusual:
        t = self.repo.obtener_clasificacion(transaccion_id)
        if not t:
            raise ValueError(f"Transaccion {transaccion_id} no encontrada")
        
        historial = self.repo.obtener_historial_usuario(usuario_id, t.categoria)
        
        if len(historial) < 2:
            return AlertaPatronInusual(transaccion_id, str(usuario_id), False, None)

        media = float(np.mean(historial))
        std = float(np.std(historial))

        es_anomalo = False
        if std > 0:
            z_score = (t.monto - media) / std
            if z_score > 2.0:
                es_anomalo = True
        elif t.monto >= (media * 2.0) and t.monto > 50000:
            es_anomalo = True

        if not es_anomalo and len(historial) >= 2 and t.monto >= (media * 2.5) and t.monto > 50000:
            es_anomalo = True

        if es_anomalo:
            alerta = (f"El monto de ${t.monto:,.0f} es inusualmente alto en comparacion con tu promedio "
                      f"historico de ${media:,.0f} para la categoria '{t.categoria}'.")
            return AlertaPatronInusual(transaccion_id, str(usuario_id), True, alerta)

        return AlertaPatronInusual(transaccion_id, str(usuario_id), False, None)

class ExtraerDatosFacturaUseCase:
    def __init__(self, ocr_extractor: OcrExtractorPort, ml_model: MLModelPort):
        self.ocr_extractor = ocr_extractor
        self.ml_model = ml_model

    def ejecutar(self, imagen_bytes: bytes, texto_auxiliar: str = None) -> DatosFacturaExtraidos:
        extraido = self.ocr_extractor.extraer_desde_bytes(imagen_bytes, texto_auxiliar)
        comercio = extraido.get("comercio") or "Comercio Factura"
        monto = float(extraido.get("monto") or 0.0)
        fecha_str = extraido.get("fecha")
        nit = extraido.get("nit")
        resumen = extraido.get("texto_resumen")

        categoria, confianza = self.ml_model.predecir(comercio, max(monto, 1.0), resumen or "")

        return DatosFacturaExtraidos(
            comercio=comercio,
            monto=monto,
            fecha=fecha_str,
            tipo_movimiento="gasto",
            categoria_sugerida=categoria,
            nivel_confianza=confianza,
            nit=nit,
            texto_detectado_resumen=resumen
        )


