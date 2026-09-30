package com.smartcash.notificaciones.domain.model;

import java.time.LocalDateTime;

public record ResultadoEnvio(
        boolean exitoso,
        String idMensaje,
        String destinatario,
        String proveedor,
        String detalle,
        LocalDateTime fechaEnvio
) {}
