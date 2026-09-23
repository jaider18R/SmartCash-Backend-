package com.smartcash.transacciones.api.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

public record TransaccionRequestDTO(
        @NotNull(message = "id_usuario es obligatorio")
        UUID idUsuario,

        @NotNull(message = "El monto es obligatorio")
        @DecimalMin(value = "0.01", message = "El monto debe ser mayor que cero")
        BigDecimal monto,

        @NotNull(message = "La fecha es obligatoria")
        LocalDate fecha,

        @NotBlank(message = "El comercio es obligatorio")
        String comercio,

        @NotBlank
        @Pattern(regexp = "ingreso|gasto", message = "tipo_movimiento debe ser 'ingreso' o 'gasto'")
        String tipoMovimiento
) {}
