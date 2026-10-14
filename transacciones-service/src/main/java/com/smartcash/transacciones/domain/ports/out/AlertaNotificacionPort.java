package com.smartcash.transacciones.domain.ports.out;

import java.math.BigDecimal;

public interface AlertaNotificacionPort {
    void enviarAlertaPresupuesto(String correoUsuario, String nombreUsuario, String nombreCategoria, BigDecimal porcentajeConsumido);
    void enviarAlertaGastoInusual(String correoUsuario, String nombreUsuario, String comercio, BigDecimal monto, String detalle);
}

