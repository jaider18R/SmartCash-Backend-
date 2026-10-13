import pytest
from app.application.use_cases import DetectarPatronInusualUseCase
from app.domain.entities import TransaccionClasificada
from app.domain.ports.repository_port import RepositoryPort

class FakeRepository(RepositoryPort):
    def __init__(self, clasificacion, historial):
        self._clasificacion = clasificacion
        self._historial = historial

    def guardar_clasificacion(self, t):
        pass

    def obtener_clasificacion(self, transaccion_id):
        return self._clasificacion

    def guardar_correccion(self, c):
        pass

    def obtener_historial_usuario(self, usuario_id, categoria):
        return self._historial

def test_detectar_patron_inusual_cuando_gasto_supera_desviacion():
    transaccion = TransaccionClasificada(
        transaccion_id=101,
        comercio="Restaurante Lujoso",
        monto=500000.0,
        descripcion="Cena evento",
        categoria="Restaurantes y Comida",
        nivel_confianza=0.95,
        usuario_id="user-uuid-1"
    )
    historial = [35000.0, 42000.0, 38000.0, 45000.0, 40000.0]
    repo = FakeRepository(transaccion, historial)

    use_case = DetectarPatronInusualUseCase(repo)
    alerta = use_case.ejecutar("user-uuid-1", 101)

    assert alerta.es_inusual is True
    assert alerta.descripcion_alerta is not None
    assert "significativamente superior" in alerta.descripcion_alerta

def test_no_marca_inusual_cuando_gasto_esta_dentro_del_rango_normal():
    transaccion = TransaccionClasificada(
        transaccion_id=102,
        comercio="Rappi Almuerzo",
        monto=45000.0,
        descripcion="Almuerzo",
        categoria="Restaurantes y Comida",
        nivel_confianza=0.90,
        usuario_id="user-uuid-1"
    )
    historial = [35000.0, 42000.0, 38000.0, 45000.0, 40000.0]
    repo = FakeRepository(transaccion, historial)

    use_case = DetectarPatronInusualUseCase(repo)
    alerta = use_case.ejecutar("user-uuid-1", 102)

    assert alerta.es_inusual is False
    assert alerta.descripcion_alerta is None
