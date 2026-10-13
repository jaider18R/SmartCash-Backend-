package com.smartcash.transacciones.api.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.time.LocalDate;

public record MetaAhorroRequestDTO(
        @NotBlank(message = "El nombre de la meta es obligatorio")
        String nombre,

        String descripcion,

        @NotNull(message = "El monto objetivo es obligatorio")
        @DecimalMin(value = "0.01", message = "El monto objetivo debe ser mayor a cero")
        BigDecimal montoObjetivo,

        LocalDate fechaLimite
) {}
