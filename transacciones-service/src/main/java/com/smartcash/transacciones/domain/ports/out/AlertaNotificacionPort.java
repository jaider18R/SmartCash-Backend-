package com.smartcash.transacciones.domain.ports.out;

import java.math.BigDecimal;

public interface AlertaNotificacionPort {
    void enviarAlertaPresupuesto(String correoUsuario, String nombreCategoria, BigDecimal porcentajeConsumido);
}
