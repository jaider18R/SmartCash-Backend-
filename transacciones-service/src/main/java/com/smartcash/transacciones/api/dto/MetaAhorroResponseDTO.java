package com.smartcash.transacciones.api.dto;

import com.smartcash.transacciones.domain.model.MetaAhorro;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.UUID;

public record MetaAhorroResponseDTO(
        UUID idMeta,
        UUID idUsuario,
        String nombre,
        String descripcion,
        BigDecimal montoObjetivo,
        BigDecimal montoActual,
        BigDecimal porcentajeProgreso,
        LocalDate fechaLimite,
        String estado
) {
    public static MetaAhorroResponseDTO de(MetaAhorro meta) {
        BigDecimal porcentaje = BigDecimal.ZERO;
        if (meta.getMontoObjetivo() != null && meta.getMontoObjetivo().compareTo(BigDecimal.ZERO) > 0) {
            porcentaje = meta.getMontoActual()
                    .divide(meta.getMontoObjetivo(), 4, RoundingMode.HALF_UP)
                    .multiply(BigDecimal.valueOf(100))
                    .setScale(2, RoundingMode.HALF_UP);
        }

        return new MetaAhorroResponseDTO(
                meta.getId(),
                meta.getIdUsuario(),
                meta.getNombre(),
                meta.getDescripcion(),
                meta.getMontoObjetivo(),
                meta.getMontoActual(),
                porcentaje,
                meta.getFechaLimite(),
                meta.getEstado()
        );
    }
}
