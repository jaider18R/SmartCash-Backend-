package com.smartcash.notificaciones.api.dto;

import com.smartcash.notificaciones.domain.model.ResultadoEnvio;

import java.time.LocalDateTime;

public record NotificacionResponseDTO(
        boolean exitoso,
        String idMensaje,
        String destinatario,
        String proveedor,
        String detalle,
        LocalDateTime fechaEnvio
) {
    public static NotificacionResponseDTO de(ResultadoEnvio resultado) {
        return new NotificacionResponseDTO(
                resultado.exitoso(),
                resultado.idMensaje(),
                resultado.destinatario(),
                resultado.proveedor(),
                resultado.detalle(),
                resultado.fechaEnvio()
        );
    }
}
