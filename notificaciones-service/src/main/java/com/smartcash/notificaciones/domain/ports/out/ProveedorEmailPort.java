package com.smartcash.notificaciones.domain.ports.out;

import com.smartcash.notificaciones.domain.model.ResultadoEnvio;

public interface ProveedorEmailPort {
    ResultadoEnvio enviar(String destinatario, String nombreDestinatario, String asunto, String contenidoHtml);
}
