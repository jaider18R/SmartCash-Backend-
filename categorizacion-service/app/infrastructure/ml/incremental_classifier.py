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
            "Salud y Farmacia"
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
            ("olímpica compra mercado", "Mercado y Supermercado"),
            ("olimpica compra viveres", "Mercado y Supermercado"),
            ("carulla mercado comida", "Mercado y Supermercado"),
            ("rappi restaurante comida domicilio", "Restaurantes y Comida"),
            ("mcdonalds hamburguesas restaurante", "Restaurantes y Comida"),
            ("crepes and waffles restaurante", "Restaurantes y Comida"),
            ("domicilios restaurante pizza", "Restaurantes y Comida"),
            ("netflix suscripcion mensual entretenimiento", "Servicios y Suscripciones"),
            ("spotify premium streaming musica", "Servicios y Suscripciones"),
            ("claro telefonia internet plan", "Servicios y Suscripciones"),
            ("uber viaje transporte carro", "Transporte y Viajes"),
            ("didi taxi viaje urbano", "Transporte y Viajes"),
            ("cabify viaje transporte", "Transporte y Viajes"),
            ("nequi transferencia enviada dinero", "Transferencias y Finanzas"),
            ("daviplata transferencia retiro", "Transferencias y Finanzas"),
            ("bancolombia transferencia saldo", "Transferencias y Finanzas"),
            ("drogas la rebaja farmacia medicamentos", "Salud y Farmacia"),
            ("cruz verde farmacia medicina salud", "Salud y Farmacia"),
        ]
        textos = [self._extraer_texto(c, "") for c, _ in datos_semilla]
        etiquetas = [cat for _, cat in datos_semilla]
        
        X = self.vectorizer.transform(textos)
        self.clf.partial_fit(X, etiquetas, classes=self.classes)

    def predecir(self, comercio: str, monto: float, descripcion: str = "") -> Tuple[str, float]:
        texto = self._extraer_texto(comercio, descripcion)
        X = self.vectorizer.transform([texto])
        
        probas = self.clf.predict_proba(X)[0]
        max_idx = np.argmax(probas)
        confianza = float(probas[max_idx])
        categoria = str(self.clf.classes_[max_idx])
        
        if "nequi" in texto or "daviplata" in texto:
            return "Transferencias y Finanzas", max(confianza, 0.90)

        return categoria, round(confianza, 2)

    def aprender_incrementalmente(self, comercio: str, monto: float, categoria_correcta: str) -> None:
        texto = self._extraer_texto(comercio)
        X = self.vectorizer.transform([texto])
        self.clf.partial_fit(X, [categoria_correcta])

    def explicar(self, comercio: str, monto: float, categoria: str) -> str:
        comercio_l = comercio.lower()
        if any(c in comercio_l for c in ["rappi", "crepes", "mcdonalds", "restaurante", "pizza"]):
            return f"El comercio '{comercio}' se identifica como establecimiento de alimentos y restaurantes."
        if any(c in comercio_l for c in ["d1", "éxito", "exito", "carulla", "olímpica", "olimpica", "supermercado"]):
            return f"El comercio '{comercio}' corresponde a cadenas de canasta basica y supermercados."
        if any(c in comercio_l for c in ["uber", "didi", "cabify", "taxi"]):
            return f"El comercio '{comercio}' coincide con plataformas de movilidad y transporte urbano."
        if any(c in comercio_l for c in ["netflix", "spotify", "claro"]):
            return f"El comercio '{comercio}' tiene patron recurrente de suscripcion o servicios."
        if any(c in comercio_l for c in ["nequi", "daviplata", "bancolombia"]):
            return f"El comercio '{comercio}' corresponde a transferencias financieras digitales."
        if any(c in comercio_l for c in ["rebaja", "cruz verde", "farmacia", "drogas"]):
            return f"El comercio '{comercio}' corresponde al rubro de salud y medicamentos."
        return f"El comercio '{comercio}' presento una alta correlacion semantica con la categoria '{categoria}' basada en el historico de transacciones."
