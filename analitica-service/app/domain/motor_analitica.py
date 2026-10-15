import calendar
from datetime import date
from typing import List
from app.domain.entities import TransaccionItem, ResumenMensual, SafeToSpend, SimulacionResultado

class MotorAnalitica:

    def calcular_resumen_mensual(self, transacciones: List[TransaccionItem], mes: int, anio: int) -> ResumenMensual:
        tx_mes = [t for t in transacciones if t.fecha.month == mes and t.fecha.year == anio]

        total_ingresos = sum(t.monto for t in tx_mes if t.tipo_movimiento == "ingreso")
        total_gastos = sum(t.monto for t in tx_mes if t.tipo_movimiento == "gasto")
        balance = total_ingresos - total_gastos

        tasa_ahorro = 0.0
        if total_ingresos > 0:
            tasa_ahorro = round(max(0.0, (balance / total_ingresos) * 100), 2)

        desglose = {}
        for t in tx_mes:
            if t.tipo_movimiento == "gasto":
                cat = t.id_categoria or "Sin Categoria"
                desglose[cat] = round(desglose.get(cat, 0.0) + t.monto, 2)

        return ResumenMensual(
            mes=mes,
            anio=anio,
            total_ingresos=round(total_ingresos, 2),
            total_gastos=round(total_gastos, 2),
            balance_neto=round(balance, 2),
            tasa_ahorro_porcentaje=tasa_ahorro,
            desglose_categorias=desglose
        )

    def calcular_safe_to_spend(self, total_ingresos: float, total_gastos: float,
                               compromisos_fijos: float, ahorro_metas: float,
                               fecha_actual: date) -> SafeToSpend:
        ultimo_dia = calendar.monthrange(fecha_actual.year, fecha_actual.month)[1]
        dias_restantes = max(1, ultimo_dia - fecha_actual.day + 1)

        saldo_disponible = max(0.0, total_ingresos - total_gastos)
        monto_seguro = max(0.0, saldo_disponible - compromisos_fijos - ahorro_metas)
        limite_diario = round(monto_seguro / dias_restantes, 2)

        if limite_diario > 50000:
            nivel = "OPTIMO"
            rec = "Tus finanzas estan equilibradas. Tienes buen margen de gasto diario."
        elif limite_diario > 15000:
            nivel = "MODERADO"
            rec = "Margen moderado. Evita compras impulsivas para asegurar tus metas."
        else:
            nivel = "CRITICO"
            rec = "Alerta de liquidez: Reduce gastos no esenciales para no sobregirarte."

        return SafeToSpend(
            saldo_disponible=round(saldo_disponible, 2),
            compromisos_fijos=round(compromisos_fijos, 2),
            ahorro_metas=round(ahorro_metas, 2),
            monto_seguro_restante=round(monto_seguro, 2),
            dias_restantes_mes=dias_restantes,
            limite_diario_seguro=limite_diario,
            nivel_salud=nivel,
            recomendacion=rec
        )

    def simular_impacto_gasto(self, safe_to_spend: SafeToSpend, monto_simulado: float) -> SimulacionResultado:
        nuevo_monto_seguro = safe_to_spend.monto_seguro_restante - monto_simulado
        es_viable = nuevo_monto_seguro >= 0
        nuevo_limite_diario = round(max(0.0, nuevo_monto_seguro) / safe_to_spend.dias_restantes_mes, 2)
        diferencia = round(safe_to_spend.limite_diario_seguro - nuevo_limite_diario, 2)

        if es_viable:
            impacto = f"El gasto es absorbible. Tu cuota diaria se reducira en ${diferencia:,.0f} COP."
        else:
            deficit = abs(nuevo_monto_seguro)
            impacto = f"El gasto sobrepasa tu margen seguro en ${deficit:,.0f} COP y comprometeria tus metas de ahorro."

        return SimulacionResultado(
            monto_simulado=monto_simulado,
            nuevo_limite_diario=nuevo_limite_diario,
            diferencia_diaria=diferencia,
            impacto_metas=impacto,
            es_viable=es_viable
        )

    def simular_inversiones(self, monto: float, plazo_meses: int) -> 'ResultadoSimulacionInversion':
        from app.domain.entities import ProyeccionInversionItem, ResultadoSimulacionInversion
        
        tasa_inflacion_anual = 0.058
        factor_tiempo = plazo_meses / 12.0

        perdida_inflacion = round(monto * (1.0 - (1.0 / ((1.0 + tasa_inflacion_anual) ** factor_tiempo))), 2)

        opciones_base = [
            {
                "tipo": "CUENTA_ALTO_RENDIMIENTO",
                "nombre_producto": "Cuentas de Alto Rendimiento (Ahorro a la vista)",
                "plataforma": "Nu / Lulo Bank / Ualá",
                "tasa_ea": 0.115,
                "retencion": 0.0,
                "riesgo": "BAJO (Fogafín)",
                "descripcion": "Rentabilidad liquida diaria con dinero 100% disponible."
            },
            {
                "tipo": "CDT_DIGITAL",
                "nombre_producto": "CDT Digital Tasa Fija",
                "plataforma": "MejorCDT / MPF Invest",
                "tasa_ea": 0.108,
                "retencion": 0.04,
                "riesgo": "BAJO (Fogafín)",
                "descripcion": "Rendimiento garantizado desde el día 1 respaldado por bancos vigilados."
            },
            {
                "tipo": "FIC_RENTA_FIJA",
                "nombre_producto": "Fondo de Inversión Colectiva (FIC)",
                "plataforma": "Tyba (Credicorp) / MPF Invest",
                "tasa_ea": 0.122,
                "retencion": 0.07,
                "riesgo": "MODERADO",
                "descripcion": "Diversificación en deuda corporativa y bonos administrado por fiduciarias."
            },
            {
                "tipo": "CROWDFUNDING",
                "nombre_producto": "Financiación Colaborativa / Crowdfunding",
                "plataforma": "MPF Invest / Bloom Crowdfunding",
                "tasa_ea": 0.150,
                "retencion": 0.07,
                "riesgo": "MEDIO-ALTO (Superfinanciera)",
                "descripcion": "Inversión directa en proyectos inmobiliarios y empresariales colombianos."
            },
            {
                "tipo": "ACCIONES_BVC",
                "nombre_producto": "Renta Variable y Acciones BVC",
                "plataforma": "Trii (Acciones & Valores)",
                "tasa_ea": 0.165,
                "retencion": 0.10,
                "riesgo": "ALTO (Bolsa de Valores)",
                "descripcion": "Participación en acciones líderes de Colombia y el exterior."
            }
        ]

        proyecciones = []
        for o in opciones_base:
            tasa_periodo = ((1.0 + o["tasa_ea"]) ** factor_tiempo) - 1.0
            rend_bruto = round(monto * tasa_periodo, 2)
            retefuente = round(rend_bruto * o["retencion"], 2)
            rend_neto = round(rend_bruto - retefuente, 2)
            cap_final = round(monto + rend_neto, 2)

            proyecciones.append(ProyeccionInversionItem(
                tipo=o["tipo"],
                nombre_producto=o["nombre_producto"],
                plataforma=o["plataforma"],
                tasa_ea_estimada=round(o["tasa_ea"] * 100, 2),
                rendimiento_bruto=rend_bruto,
                retencion_fuente_estimada=retefuente,
                rendimiento_neto=rend_neto,
                capital_final=cap_final,
                riesgo=o["riesgo"],
                descripcion_facil=o["descripcion"]
            ))

        if plazo_meses <= 6:
            rec = "Para plazos cortos (hasta 6 meses), las Cuentas de Alto Rendimiento y CDTs Digitales garantizan rendimientos sin volatilidad."
        else:
            rec = "Para plazos mayores a 6 meses, combinar CDTs con FICs (Tyba/MPF) o Acciones (Trii) maximiza tu rendimiento superando la inflación."

        return ResultadoSimulacionInversion(
            monto_invertido=round(monto, 2),
            plazo_meses=plazo_meses,
            inflacion_colombia_estimada_porcentaje=round(tasa_inflacion_anual * 100, 2),
            perdida_poder_adquisitivo_efectivo=perdida_inflacion,
            proyecciones=proyecciones,
            recomendacion_perfil=rec
        )

    def obtener_catalogo_inversiones(self) -> List['OpcionInversionInfo']:
        from app.domain.entities import OpcionInversionInfo
        return [
            OpcionInversionInfo(
                id_opcion="mpf-invest",
                categoria="Marketplace de Inversión",
                titulo="MPF Invest",
                subtitulo="Plataforma de Mis Propias Finanzas para CDTs, FICs y Crowdfunding",
                que_es_facil="Una app colombiana que reúne múltiples oportunidades de inversión reguladas en un solo lugar: desde CDTs con bancos aliados hasta fondos de inversión con Acciones & Valores y proyectos inmobiliarios con Bloom.",
                rentabilidad_estimada="10.0% a 16.5% E.A.",
                tasa_ea_promedio=13.0,
                riesgo="Moderado / Diversificado",
                plataformas_recomendadas=["MPF Invest"],
                vigilancia="Superintendencia Financiera de Colombia (a través de comisionistas y entidades aliadas)",
                monto_minimo="$ 200.000 COP",
                disponibilidad_liquidez="Desde 90 días hasta 2 años según el producto",
                url_promocional="https://mpfinvest.com"
            ),
            OpcionInversionInfo(
                id_opcion="cuentas-rendimiento",
                categoria="Ahorro a la Vista con Intereses Diarios",
                titulo="Cuentas de Alto Rendimiento",
                subtitulo="Nu Colombia, Lulo Bank y Ualá",
                que_es_facil="Cuentas de ahorro digitales que te pagan intereses todos los días sobre el dinero que tengas guardado, pero sin congelarlo: tu plata sigue 100% disponible para retirar o gastar cuando quieras.",
                rentabilidad_estimada="10.0% a 13.0% E.A.",
                tasa_ea_promedio=11.5,
                riesgo="Bajo / Seguro Fogafín",
                plataformas_recomendadas=["Nu Colombia", "Lulo Bank", "Ualá"],
                vigilancia="Superintendencia Financiera de Colombia y seguro de depósitos Fogafín (hasta $50 millones COP)",
                monto_minimo="$ 1 COP",
                disponibilidad_liquidez="Inmediata (24/7)",
                url_promocional="https://www.nu.com.co"
            ),
            OpcionInversionInfo(
                id_opcion="mejor-cdt",
                categoria="Renta Fija Garantizada",
                titulo="MejorCDT",
                subtitulo="Comparador y apertura de CDTs con los bancos de mejor tasa",
                que_es_facil="Un portal que compara en tiempo real qué banco o entidad vigilada en Colombia está pagando la tasa más alta para tu plazo deseado y te permite abrir el CDT 100% digital sin intermediarios.",
                rentabilidad_estimada="9.5% a 11.5% E.A.",
                tasa_ea_promedio=10.8,
                riesgo="Muy Bajo / Seguro Fogafín",
                plataformas_recomendadas=["MejorCDT"],
                vigilancia="Bancos vigilados por la Superintendencia Financiera con seguro Fogafín",
                monto_minimo="$ 500.000 COP",
                disponibilidad_liquidez="Al vencimiento pactado (30, 90, 180, 360 días)",
                url_promocional="https://mejorcdt.com"
            ),
            OpcionInversionInfo(
                id_opcion="tyba",
                categoria="Fondos de Inversión y Metas",
                titulo="Tyba (Grupo Credicorp)",
                subtitulo="Inversión digital en FICs y portafolios según tu perfil",
                que_es_facil="Una app que arma un portafolio de inversión automático ajustado a tu perfil de riesgo (conservador, moderado o audaz). Tu dinero se invierte en bonos y acciones administrados por Credicorp Capital.",
                rentabilidad_estimada="10.5% a 13.5% E.A. (estimada)",
                tasa_ea_promedio=12.2,
                riesgo="Moderado",
                plataformas_recomendadas=["Tyba"],
                vigilancia="Credicorp Capital Fiduciaria y Comisionista vigiladas por la Superfinanciera",
                monto_minimo="$ 5.000 COP",
                disponibilidad_liquidez="Retiros en 1 a 3 días hábiles",
                url_promocional="https://tyba.com.co"
            ),
            OpcionInversionInfo(
                id_opcion="trii",
                categoria="Acciones y Bolsa de Valores",
                titulo="Trii",
                subtitulo="Acciones locales colombianas (BVC) y globales",
                que_es_facil="Una app que te permite comprar acciones en la Bolsa de Valores de Colombia (como Bancolombia o Ecopetrol) o acciones internacionales de Estados Unidos desde montos muy bajos, sin trámites bancarios complejos.",
                rentabilidad_estimada="Variable (potencial alto a largo plazo)",
                tasa_ea_promedio=16.5,
                riesgo="Dinámico / Bolsa",
                plataformas_recomendadas=["Trii"],
                vigilancia="Acciones & Valores S.A. Comisionista de Bolsa vigilada por la Superfinanciera",
                monto_minimo="Sin mínimo (fracciones de acciones)",
                disponibilidad_liquidez="Venta de acciones en horario bursátil (liquidación T+2)",
                url_promocional="https://trii.co"
            )
        ]
