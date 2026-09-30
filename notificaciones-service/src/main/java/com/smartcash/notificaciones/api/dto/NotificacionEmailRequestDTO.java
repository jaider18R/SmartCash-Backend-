package com.smartcash.notificaciones.api.dto;

import com.smartcash.notificaciones.domain.model.TipoNotificacion;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.util.Map;

public record NotificacionEmailRequestDTO(
        @NotBlank(message = "El correo destinatario es obligatorio")
        @Email(message = "El formato de correo no es valido")
        String destinatario,

        String nombre,

        @NotNull(message = "El tipo de notificacion es obligatorio")
        TipoNotificacion tipo,

        String asunto,

        Map<String, String> parametros
) {}
