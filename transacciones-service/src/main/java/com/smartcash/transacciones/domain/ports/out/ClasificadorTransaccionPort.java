package com.smartcash.transacciones.domain.ports.out;

import java.math.BigDecimal;

public interface ClasificadorTransaccionPort {
    ResultadoClasificacion clasificar(String comercio, BigDecimal monto);

    record ResultadoClasificacion(java.util.UUID idCategoria, double confianza) {}
}
