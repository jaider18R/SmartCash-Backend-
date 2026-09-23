package com.smartcash.transacciones.domain.ports.in;

import com.smartcash.transacciones.domain.model.Transaccion;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

public interface RegistrarTransaccionUseCase {
    Transaccion registrar(UUID idUsuario, BigDecimal monto, LocalDate fecha,
                           String comercio, String tipoMovimiento);
}
