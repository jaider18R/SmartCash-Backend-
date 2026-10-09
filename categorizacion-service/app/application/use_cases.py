import numpy as np
from app.domain.ports.ml_model_port import MLModelPort
from app.domain.ports.repository_port import RepositoryPort
from app.domain.entities import TransaccionClasificada, CorreccionCategoria, ExplicacionClasificacion, AlertaPatronInusual

class ClasificarTransaccionUseCase:
    def __init__(self, ml_model: MLModelPort, repo: RepositoryPort):
        self.ml_model = ml_model
        self.repo = repo

    def ejecutar(self, transaccion_id: int, comercio: str, monto: float, descripcion: str = "", usuario_id: int = None) -> TransaccionClasificada:
        cat, confianza = self.ml_model.predecir(comercio, monto, descripcion)
        t = TransaccionClasificada(
            transaccion_id=transaccion_id,
            comercio=comercio,
            monto=monto,
            descripcion=descripcion,
            categoria=cat,
            nivel_confianza=confianza,
            usuario_id=usuario_id
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

    def ejecutar(self, usuario_id: int, transaccion_id: int) -> AlertaPatronInusual:
        t = self.repo.obtener_clasificacion(transaccion_id)
        if not t:
            raise ValueError(f"Transaccion {transaccion_id} no encontrada")
        
        historial = self.repo.obtener_historial_usuario(usuario_id, t.categoria)
        
        if len(historial) < 3:
            return AlertaPatronInusual(transaccion_id, usuario_id, False, None)

        media = float(np.mean(historial))
        std = float(np.std(historial)) or 1.0

        z_score = (t.monto - media) / std
        if z_score > 2.0:
            alerta = (f"El monto de ${t.monto:,.0f} es significativamente superior al promedio "
                      f"historico de ${media:,.0f} para la categoria '{t.categoria}'.")
            return AlertaPatronInusual(transaccion_id, usuario_id, True, alerta)

        return AlertaPatronInusual(transaccion_id, usuario_id, False, None)
