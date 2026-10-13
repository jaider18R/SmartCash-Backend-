package com.smartcash.transacciones.domain.ports.out;

import java.math.BigDecimal;
import java.util.UUID;


public interface ClasificadorTransaccionPort {
    ResultadoClasificacion clasificar(String comercio, BigDecimal monto);
    ResultadoClasificacionDetallada clasificarConDetalle(UUID idUsuario, String comercio, BigDecimal monto);
    String obtenerExplicacion(long transaccionId);

    record ResultadoClasificacion(UUID idCategoria, double confianza) {}
    record ResultadoClasificacionDetallada(UUID idCategoria, double confianza, long transaccionId, boolean esInusual, String alertaInusual) {}
}

