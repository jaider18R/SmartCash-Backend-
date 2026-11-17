import io
from unittest.mock import patch
from PIL import Image
import pytest
from app.infrastructure.ocr.receipt_ocr_adapter import ReceiptOcrAdapter
from app.infrastructure.ml.incremental_classifier import IncrementalClassifierAdapter
from app.application.use_cases import ExtraerDatosFacturaUseCase, DetectarPatronInusualUseCase, ExplicarClasificacionUseCase
from app.domain.entities import TransaccionClasificada


def _crear_imagen_prueba() -> bytes:
    img = Image.new("RGB", (200, 200), color=(255, 255, 255))
    buf = io.BytesIO()
    img.save(buf, format="PNG")
    return buf.getvalue()


def test_ocr_extraer_desde_bytes_con_mock_tesseract():
    adapter = ReceiptOcrAdapter()
    img_bytes = _crear_imagen_prueba()

    texto_simulado = (
        "TIENDAS D1\n"
        "KOBA COLOMBIA S.A.S\n"
        "NIT: 900.276.962-1\n"
        "FECHA: 15/10/2026\n"
        "SUBTOTAL $ 40.000\n"
        "IVA 19% $ 5.000\n"
        "TOTAL A PAGAR $ 45.000\n"
        "EFECTIVO $ 50.000\n"
        "CAMBIO $ 5.000\n"
    )
    with patch("pytesseract.image_to_string", return_value=texto_simulado):
        res = adapter.extraer_desde_bytes(img_bytes, "Compra mercado")
        assert res["comercio"] == "Tiendas D1"
        assert res["monto"] == 45000.0
        assert res["fecha"] == "2026-10-15"
        assert res["nit"] == "900.276.962-1"


def test_ocr_extraer_desde_bytes_cuando_tesseract_falla():
    adapter = ReceiptOcrAdapter()
    img_bytes = _crear_imagen_prueba()

    with patch("pytesseract.image_to_string", side_effect=RuntimeError("No tesseract")):
        res = adapter.extraer_desde_bytes(img_bytes, "EXITO\nTOTAL $ 28.000\n2026-10-20\n890.900.608-2")
        assert res["comercio"] == "Almacenes Éxito"
        assert res["monto"] == 28000.0
        assert res["fecha"] == "2026-10-20"
        assert res["nit"] == "890.900.608-2"


def test_ocr_variantes_comercios_fechas_y_montos():
    adapter = ReceiptOcrAdapter()

    r_sas = adapter._analizar_texto_factura(
        "FACTURA ELECTRONICA\nPANADERIA ARTESANAL DEL SUR S.A.S NIT 900111222\nPAGUESE ANTES DE 18/10/2026\nTOTAL\n$ 32.500"
    )
    assert "Panaderia Artesanal" in r_sas["comercio"]
    assert r_sas["fecha"] == "2026-10-18"
    assert r_sas["monto"] == 32500.0

    r_generico = adapter._analizar_texto_factura(
        "FACTURA ELECTRONICA\nABARROTES DON PEDRO\n14 OCT 2026\nTOTAL A PAGAR $ 45.000"
    )
    assert "Abarrotes Don Pedro" in r_generico["comercio"]
    assert r_generico["fecha"] == "2026-10-14"
    assert r_generico["monto"] == 45000.0

    r_subtotales = adapter._analizar_texto_factura(
        "FACTURA\nACUEDUCTO $ 20.000\nALCANTARILLADO $ 15.000\nASEO $ 10.000"
    )
    assert r_subtotales["comercio"] == "Acueducto y Alcantarillado"
    assert r_subtotales["monto"] == 45000.0

    r_leading_5 = adapter._analizar_texto_factura(
        "FACTURA\n12/09/26\nTOTAL A PAGAR $ 525.000\nVALOR NETO $ 25.000\nACUEDUCTO $ 15.000\nASEO $ 10.000"
    )
    assert r_leading_5["fecha"] == "2026-09-12"
    assert r_leading_5["monto"] == 25000.0

    r_moneda_suelta = adapter._analizar_texto_factura(
        "123\n$ 19.800,00\n15.400,50\n18000"
    )
    assert r_moneda_suelta["comercio"] == "Comercio Factura Escaneada"
    assert r_moneda_suelta["monto"] == 19800.0

    r_vacio = adapter._analizar_texto_factura("")
    assert r_vacio["monto"] == 0.0
    assert r_vacio["nit"] is None


def test_ocr_normalizacion_y_fechas_invalidas():
    adapter = ReceiptOcrAdapter()
    assert adapter._normalizar_numero_colombiano("S0.O0O,00") == 50000.0
    assert adapter._normalizar_numero_colombiano("mn") == 0.0

    f_invalida = adapter._extraer_fecha("VENCE 31/02/2026 2026-02-31 31/02/26 31 FEB 2026")
    assert len(f_invalida) == 10


def test_incremental_classifier_todas_las_ramas():
    clf = IncrementalClassifierAdapter()

    casos_predecir = [
        ("Pago Nomina Empresa", "Salario y Nómina"),
        ("Intereses CDT Bancolombia", "Inversiones y Rendimientos"),
        ("Reembolso Gastos", "Otros Ingresos"),
        ("Tiendas D1", "Mercado y Supermercado"),
        ("Rappi Restaurante", "Restaurantes y Comida"),
        ("Uber Viaje", "Transporte y Viajes"),
        ("Cruz Verde Farmacia", "Salud y Farmacia"),
        ("Netflix Suscripcion", "Servicios y Suscripciones"),
        ("Transferencia Nequi", "Transferencias y Finanzas"),
    ]
    for comercio, esperado in casos_predecir:
        cat, conf = clf.predecir(comercio, 50000.0)
        assert cat == esperado
        assert conf >= 0.85

    cat_ml, conf_ml = clf.predecir("Comercio Desconocido XYZ", 25000.0, "compra general")
    assert cat_ml in clf.classes
    assert 0.0 <= conf_ml <= 1.0

    clf.aprender_incrementalmente("Gimnasio Local", 80000.0, "Servicios y Suscripciones")

    comercios_explicar = [
        "Pago Nomina",
        "Rendimiento CDT",
        "Venta de usado",
        "Rappi Burger",
        "Supermercado Exito",
        "Uber Trip",
        "Netflix Mensual",
        "Nequi Envio",
        "Drogueria Farmatodo",
        "Almacen Ropa XYZ",
    ]
    for c in comercios_explicar:
        exp = clf.explicar(c, 30000.0, "Otros")
        assert c in exp


def test_use_cases_casos_borde():
    class RepoDummy:
        def obtener_clasificacion(self, tx_id):
            if tx_id == 999:
                return None
            return TransaccionClasificada(tx_id, "Tienda", 120000.0, "", "Mercado y Supermercado", 0.9, "u1")

        def obtener_historial_usuario(self, uid, cat):
            if uid == "pocos":
                return [10000.0]
            if uid == "std_cero":
                return [20000.0, 20000.0, 20000.0]
            return [10000.0, 12000.0, 11000.0]

    repo = RepoDummy()
    detector = DetectarPatronInusualUseCase(repo)

    with pytest.raises(ValueError):
        detector.ejecutar("u1", 999)

    r_pocos = detector.ejecutar("pocos", 1)
    assert not r_pocos.es_inusual

    r_std_cero = detector.ejecutar("std_cero", 1)
    assert r_std_cero.es_inusual

    explicador = ExplicarClasificacionUseCase(IncrementalClassifierAdapter(), repo)
    with pytest.raises(ValueError):
        explicador.ejecutar(999)

    extractor = ExtraerDatosFacturaUseCase(ReceiptOcrAdapter(), IncrementalClassifierAdapter())
    with patch.object(ReceiptOcrAdapter, "extraer_desde_bytes", return_value={
        "comercio": "Tiendas D1",
        "monto": 35000.0,
        "fecha": "2026-10-28",
        "nit": "900276962",
        "texto_resumen": "TIENDAS D1 TOTAL 35000"
    }):
        datos = extractor.ejecutar(b"fake", "aux")
        assert datos.comercio == "Tiendas D1"
        assert datos.monto == 35000.0
        assert datos.categoria_sugerida == "Mercado y Supermercado"
