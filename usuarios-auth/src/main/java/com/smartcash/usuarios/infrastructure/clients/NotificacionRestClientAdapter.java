package com.smartcash.usuarios.infrastructure.clients;

import com.smartcash.usuarios.domain.ports.out.NotificacionClientPort;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.util.Map;

@Component
public class NotificacionRestClientAdapter implements NotificacionClientPort {

    private static final Logger log = LoggerFactory.getLogger(NotificacionRestClientAdapter.class);

    private final RestClient restClient;
    private final String notificacionesUrl;

    public NotificacionRestClientAdapter(@Value("${smartcash.servicios.notificaciones.url:http://localhost:8083}") String notificacionesUrl) {
        this.notificacionesUrl = notificacionesUrl;
        this.restClient = RestClient.builder().build();
    }

    @Override
    public void enviarRecuperacionPassword(String correo, String nombre, String enlace) {
        try {
            Map<String, Object> payload = Map.of(
                    "destinatario", correo,
                    "nombre", nombre != null ? nombre : "Usuario",
                    "tipo", "RECUPERACION_PASSWORD",
                    "asunto", "Recuperacion de contrasena - SmartCash",
                    "parametros", Map.of("enlace", enlace)
            );

            restClient.post()
                    .uri(notificacionesUrl + "/api/notificaciones/email")
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(payload)
                    .retrieve()
                    .toBodilessEntity();

            log.info("Solicitud de notificacion enviada con exito para: {}", correo);
        } catch (Exception e) {
            log.warn("No se pudo conectar con el microservicio de notificaciones: {}. El enlace generado es: {}", e.getMessage(), enlace);
        }
    }
}
