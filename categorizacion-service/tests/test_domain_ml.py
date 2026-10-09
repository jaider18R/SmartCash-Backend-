import pytest
from app.infrastructure.ml.incremental_classifier import IncrementalClassifierAdapter

def test_clasificacion_inicial_colombia():
    ml = IncrementalClassifierAdapter()
    cat, confianza = ml.predecir("Tiendas D1 Bogota", 45000.0)
    assert cat == "Mercado y Supermercado"
    assert confianza > 0.4

def test_aprendizaje_incremental_corrige_prediccion_futura():
    ml = IncrementalClassifierAdapter()
    comercio = "Comercio Desconocido SAS"
    
    for _ in range(5):
        ml.aprender_incrementalmente(comercio, 120000.0, "Transporte y Viajes")
    
    cat_actualizada, confianza_nueva = ml.predecir(comercio, 120000.0)
    assert cat_actualizada == "Transporte y Viajes"
    assert confianza_nueva > 0.4

def test_explicacion_human_readable():
    ml = IncrementalClassifierAdapter()
    exp = ml.explicar("Rappi Almuerzo", 35000.0, "Restaurantes y Comida")
    assert "alimentos y restaurantes" in exp
