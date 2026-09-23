package com.smartcash.transacciones.api.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

public record TransaccionResponseDTO(
        UUID idTransaccion,
        UUID idUsuario,
        UUID idCategoria,
        BigDecimal monto,
        LocalDate fecha,
        String comercio,
        String tipoMovimiento,
        Double confianzaCategorizacion
) {}
