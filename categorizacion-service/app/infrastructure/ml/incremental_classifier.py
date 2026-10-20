import numpy as np
from typing import Tuple
from sklearn.feature_extraction.text import HashingVectorizer
from sklearn.linear_model import SGDClassifier
from app.domain.ports.ml_model_port import MLModelPort

class IncrementalClassifierAdapter(MLModelPort):
    def __init__(self):
        self.vectorizer = HashingVectorizer(n_features=2**12, alternate_sign=False)
        self.clf = SGDClassifier(loss="log_loss", penalty="l2", alpha=1e-4, random_state=42)
        
        self.classes = np.array([
            "Mercado y Supermercado",
            "Restaurantes y Comida",
            "Servicios y Suscripciones",
            "Transporte y Viajes",
            "Transferencias y Finanzas",
            "Salud y Farmacia",
            "Salario y Nómina",
            "Inversiones y Rendimientos",
            "Otros Ingresos"
        ])
        
        self._entrenamiento_semilla()

    def _extraer_texto(self, comercio: str, descripcion: str = "") -> str:
        comercio_clean = comercio.lower().strip()
        desc_clean = (descripcion or "").lower().strip()
        return f"{comercio_clean} {desc_clean}".strip()

    def _entrenamiento_semilla(self):
        datos_semilla = [
            ("éxito supermercado mercado", "Mercado y Supermercado"),
            ("exito mercado viveres", "Mercado y Supermercado"),
            ("d1 tiendas viveres", "Mercado y Supermercado"),
            ("tiendas ara mercado viveres", "Mercado y Supermercado"),
            ("jumbo cencosud mercado", "Mercado y Supermercado"),
            ("olímpica compra mercado", "Mercado y Supermercado"),
            ("olimpica compra viveres", "Mercado y Supermercado"),
            ("carulla mercado comida", "Mercado y Supermercado"),
            ("surtimax viveres abarrotes", "Mercado y Supermercado"),
            ("alkosto mercado viveres tecnologia", "Mercado y Supermercado"),
            ("rappi restaurante comida domicilio", "Restaurantes y Comida"),
            ("mcdonalds hamburguesas restaurante", "Restaurantes y Comida"),
            ("crepes and waffles restaurante", "Restaurantes y Comida"),
            ("domicilios restaurante pizza", "Restaurantes y Comida"),
            ("frisby pollo restaurante", "Restaurantes y Comida"),
            ("el corral hamburguesas comida", "Restaurantes y Comida"),
            ("starbucks cafe panaderia", "Restaurantes y Comida"),
            ("juan valdez cafe bebidas", "Restaurantes y Comida"),
            ("panaderia cafeteria desayuno almuerzo", "Restaurantes y Comida"),
            ("netflix suscripcion mensual entretenimiento", "Servicios y Suscripciones"),
            ("spotify premium streaming musica", "Servicios y Suscripciones"),
            ("claro telefonia internet plan", "Servicios y Suscripciones"),
            ("tigo hogar internet movil", "Servicios y Suscripciones"),
            ("movistar fibra optica telefonia", "Servicios y Suscripciones"),
            ("enel codensa energia luz servicio", "Servicios y Suscripciones"),
            ("epm servicios publicos agua energia", "Servicios y Suscripciones"),
            ("acueducto agua factura servicio", "Servicios y Suscripciones"),
            ("vanti gas natural factura servicio", "Servicios y Suscripciones"),
            ("emserfusa servicios publicos agua alcantarillado aseo fusagasuga", "Servicios y Suscripciones"),
            ("uber viaje transporte carro", "Transporte y Viajes"),
            ("didi taxi viaje urbano", "Transporte y Viajes"),
            ("cabify viaje transporte", "Transporte y Viajes"),
            ("indrive viaje taxi conductor", "Transporte y Viajes"),
            ("transmilenio pasaje transporte sitp", "Transporte y Viajes"),
            ("avianca tiquetes vuelo viaje", "Transporte y Viajes"),
            ("latam aerolinea vuelos", "Transporte y Viajes"),
            ("terpel gasolina combustible estacion", "Transporte y Viajes"),
            ("primax estacion gasolina", "Transporte y Viajes"),
            ("peaje autopista transporte", "Transporte y Viajes"),
            ("nequi transferencia enviada dinero", "Transferencias y Finanzas"),
            ("daviplata transferencia retiro", "Transferencias y Finanzas"),
            ("bancolombia transferencia saldo", "Transferencias y Finanzas"),
            ("cajero bancolombia retiro efectivo", "Transferencias y Finanzas"),
            ("bbva cuenta transferencia", "Transferencias y Finanzas"),
            ("drogas la rebaja farmacia medicamentos", "Salud y Farmacia"),
            ("cruz verde farmacia medicina salud", "Salud y Farmacia"),
            ("farmatodo drogueria medicamentos salud", "Salud y Farmacia"),
            ("drogueria pasteur farmacia", "Salud y Farmacia"),
            ("eps sanitas cita medica salud", "Salud y Farmacia"),
            ("compensar eps consulta medica", "Salud y Farmacia"),
            ("nomina mensual pago sueldo salario", "Salario y Nómina"),
            ("pago nomina sueldo empresa quincena", "Salario y Nómina"),
            ("salario quincenal empresa abono", "Salario y Nómina"),
            ("honorarios servicios profesionales pago sueldo", "Salario y Nómina"),
            ("bonificacion laboral prima de servicios sueldo", "Salario y Nómina"),
            ("pago liquidacion cesantias empresa", "Salario y Nómina"),
            ("dividendos acciones rendimientos utilidades", "Inversiones y Rendimientos"),
            ("intereses cdt rendimiento financiero banco", "Inversiones y Rendimientos"),
            ("fondos de inversion rentabilidad rendimiento", "Inversiones y Rendimientos"),
            ("trii rentabilidad acciones bolsa", "Inversiones y Rendimientos"),
            ("tyba fondo inversion rendimientos ganancia", "Inversiones y Rendimientos"),
            ("fiducoomeva fiduciaria rendimiento utilidades", "Inversiones y Rendimientos"),
            ("venta productos ingreso extra negocio", "Otros Ingresos"),
            ("reembolso devolucion dinero consignacion recibida", "Otros Ingresos"),
            ("ingreso extra regalo transferencia recibida", "Otros Ingresos"),
            ("venta garage usado ingreso efectivo", "Otros Ingresos"),
            ("premio sorteo loteria ganancias varias", "Otros Ingresos")
        ]
        textos = [self._extraer_texto(c, "") for c, _ in datos_semilla]
        etiquetas = [cat for _, cat in datos_semilla]
        
        X = self.vectorizer.transform(textos)
        self.clf.partial_fit(X, etiquetas, classes=self.classes)

    def predecir(self, comercio: str, monto: float, descripcion: str = "") -> Tuple[str, float]:
        texto = self._extraer_texto(comercio, descripcion)

        if any(w in texto for w in ["nomina", "nómina", "sueldo", "salario", "quincena", "honorarios", "prima de"]):
            return "Salario y Nómina", 0.95

        if any(w in texto for w in ["cdt", "rendimiento", "dividendo", "intereses cdt", "fiducuenta", "tyba", "trii"]):
            return "Inversiones y Rendimientos", 0.92

        if any(w in texto for w in ["reembolso", "ingreso extra", "venta", "consignacion recibida"]):
            return "Otros Ingresos", 0.88

        if any(w in texto for w in ["d1", "ara", "éxito", "exito", "carulla", "olímpica", "olimpica", "jumbo", "surtimax", "alkosto"]):
            return "Mercado y Supermercado", 0.92

        if any(w in texto for w in ["rappi", "crepes", "mcdonalds", "frisby", "corral", "restaurante", "pizza", "starbucks", "juan valdez"]):
            return "Restaurantes y Comida", 0.92

        if any(w in texto for w in ["uber", "didi", "cabify", "indrive", "transmilenio", "gasolina", "terpel", "primax"]):
            return "Transporte y Viajes", 0.90

        if any(w in texto for w in ["rebaja", "cruz verde", "farmatodo", "pasteur", "farmacia", "drogueria", "sanitas", "eps"]):
            return "Salud y Farmacia", 0.90

        if any(w in texto for w in ["netflix", "spotify", "claro", "tigo", "movistar", "etb", "enel", "codensa", "epm", "acueducto", "alcantarillado", "aseo", "emserfusa", "servicios publicos", "vanti", "gas natural", "celsia", "emcali"]):
            return "Servicios y Suscripciones", 0.95

        if any(w in texto for w in ["nequi", "daviplata", "bancolombia", "cajero"]):
            return "Transferencias y Finanzas", 0.90

        X = self.vectorizer.transform([texto])
        probas = self.clf.predict_proba(X)[0]
        max_idx = np.argmax(probas)
        confianza = float(probas[max_idx])
        categoria = str(self.clf.classes_[max_idx])

        return categoria, round(confianza, 2)

    def aprender_incrementalmente(self, comercio: str, monto: float, categoria_correcta: str) -> None:
        texto = self._extraer_texto(comercio)
        X = self.vectorizer.transform([texto])
        self.clf.partial_fit(X, [categoria_correcta])

    def explicar(self, comercio: str, monto: float, categoria: str) -> str:
        comercio_l = comercio.lower()
        if any(c in comercio_l for c in ["nomina", "nómina", "sueldo", "salario", "quincena", "honorarios"]):
            return f"El concepto '{comercio}' corresponde a percepcion de salarios, nómina o compensaciones laborales."
        if any(c in comercio_l for c in ["cdt", "rendimiento", "dividendo", "tyba", "trii"]):
            return f"El concepto '{comercio}' corresponde a rendimientos o ingresos por inversiones."
        if any(c in comercio_l for c in ["venta", "reembolso", "ingreso extra"]):
            return f"El concepto '{comercio}' corresponde a otros ingresos o entradas extraordinarias."
        if any(c in comercio_l for c in ["rappi", "crepes", "mcdonalds", "frisby", "corral", "restaurante", "pizza"]):
            return f"El comercio '{comercio}' se identifica como establecimiento de alimentos y restaurantes."
        if any(c in comercio_l for c in ["d1", "ara", "éxito", "exito", "carulla", "olímpica", "olimpica", "jumbo", "supermercado"]):
            return f"El comercio '{comercio}' corresponde a cadenas de canasta basica y supermercados."
        if any(c in comercio_l for c in ["uber", "didi", "cabify", "indrive", "taxi", "terpel"]):
            return f"El comercio '{comercio}' coincide con plataformas de movilidad y transporte urbano o combustible."
        if any(c in comercio_l for c in ["netflix", "spotify", "claro", "tigo", "movistar", "enel"]):
            return f"El comercio '{comercio}' tiene patron recurrente de suscripcion o servicios publicos."
        if any(c in comercio_l for c in ["nequi", "daviplata", "bancolombia"]):
            return f"El comercio '{comercio}' corresponde a transferencias financieras digitales."
        if any(c in comercio_l for c in ["rebaja", "cruz verde", "farmatodo", "farmacia", "drogas"]):
            return f"El comercio '{comercio}' corresponde al rubro de salud y medicamentos."
        return f"El comercio '{comercio}' presento una alta correlacion semantica con la categoria '{categoria}' basada en el historico de transacciones."
