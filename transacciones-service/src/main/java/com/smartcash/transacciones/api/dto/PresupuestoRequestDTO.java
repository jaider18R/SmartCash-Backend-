package com.smartcash.transacciones.api.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.util.UUID;

public record PresupuestoRequestDTO(
        @NotNull(message = "id_categoria es obligatorio")
        UUID idCategoria,

        @NotBlank(message = "nombre_categoria es obligatorio")
        String nombreCategoria,

        @NotNull(message = "El monto_limite es obligatorio")
        @DecimalMin(value = "0.01", message = "El monto_limite debe ser mayor que cero")
        BigDecimal montoLimite,

        @Min(value = 1, message = "El mes debe estar entre 1 y 12")
        @Max(value = 12, message = "El mes debe estar entre 1 y 12")
        int mes,

        @Min(value = 2000, message = "El anio debe ser valido")
        int anio
) {}
