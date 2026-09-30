package com.smartcash.notificaciones.domain.ports.in;

import com.smartcash.notificaciones.domain.model.ResultadoEnvio;
import com.smartcash.notificaciones.domain.model.TipoNotificacion;

import java.util.Map;

public interface EnviarNotificacionUseCase {
    ResultadoEnvio enviar(String destinatario, String nombre, TipoNotificacion tipo, String asunto, Map<String, String> parametros);
}
