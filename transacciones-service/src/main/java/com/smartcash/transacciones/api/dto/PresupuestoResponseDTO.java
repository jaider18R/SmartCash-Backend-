package com.smartcash.transacciones.api.dto;

import com.smartcash.transacciones.domain.model.Presupuesto;

import java.math.BigDecimal;
import java.util.UUID;

public record PresupuestoResponseDTO(
        UUID idPresupuesto,
        UUID idUsuario,
        UUID idCategoria,
        String nombreCategoria,
        BigDecimal montoLimite,
        int mes,
        int anio
) {
    public static PresupuestoResponseDTO de(Presupuesto p) {
        return new PresupuestoResponseDTO(
                p.getId(),
                p.getIdUsuario(),
                p.getIdCategoria(),
                p.getNombreCategoria(),
                p.getMontoLimite(),
                p.getMes(),
                p.getAnio()
        );
    }
}
