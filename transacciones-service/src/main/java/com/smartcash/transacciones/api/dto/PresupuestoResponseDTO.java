package com.smartcash.transacciones.api.dto;

import com.smartcash.transacciones.domain.model.Presupuesto;

import java.math.BigDecimal;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.UUID;

public record PresupuestoResponseDTO(
        UUID idPresupuesto,
        UUID idUsuario,
        UUID idCategoria,
        String nombreCategoria,
        BigDecimal montoLimite,
        int mes,
        int anio,
        String fechaInicio,
        String fechaFin
) {
    private static final DateTimeFormatter FORMATO_FECHA = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    public static PresupuestoResponseDTO de(Presupuesto p) {
        YearMonth ym = YearMonth.of(p.getAnio(), p.getMes());
        String inicio = ym.atDay(1).format(FORMATO_FECHA);
        String fin = ym.atEndOfMonth().format(FORMATO_FECHA);

        return new PresupuestoResponseDTO(
                p.getId(),
                p.getIdUsuario(),
                p.getIdCategoria(),
                p.getNombreCategoria(),
                p.getMontoLimite(),
                p.getMes(),
                p.getAnio(),
                inicio,
                fin
        );
    }
}
