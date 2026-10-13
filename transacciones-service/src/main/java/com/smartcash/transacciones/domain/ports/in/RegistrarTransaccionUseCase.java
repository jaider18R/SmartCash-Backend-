package com.smartcash.transacciones.domain.ports.in;

import com.smartcash.transacciones.domain.model.Transaccion;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

public interface RegistrarTransaccionUseCase {
    Transaccion registrar(UUID idUsuario, BigDecimal monto, LocalDate fecha,
                           String comercio, String tipoMovimiento);
    Transaccion actualizar(UUID idUsuario, UUID idTransaccion, BigDecimal monto, LocalDate fecha,
                            String comercio, String tipoMovimiento, UUID idCategoria);
    void eliminar(UUID idUsuario, UUID idTransaccion);
    java.util.List<Transaccion> listarPorUsuario(UUID idUsuario);
}


