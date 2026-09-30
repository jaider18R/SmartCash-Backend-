package com.smartcash.notificaciones.infrastructure.adapters;

import com.smartcash.notificaciones.domain.model.ResultadoEnvio;
import com.smartcash.notificaciones.domain.ports.out.ProveedorEmailPort;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Component
public class BrevoEmailAdapter implements ProveedorEmailPort {

    private static final Logger log = LoggerFactory.getLogger(BrevoEmailAdapter.class);

    private final RestClient restClient;
    private final String apiUrl;
    private final String apiKey;
    private final String remitenteNombre;
    private final String remitenteEmail;

    public BrevoEmailAdapter(
            @Value("${smartcash.notificaciones.brevo.api-url}") String apiUrl,
            @Value("${smartcash.notificaciones.brevo.api-key}") String apiKey,
            @Value("${smartcash.notificaciones.remitente.nombre:SmartCash}") String remitenteNombre,
            @Value("${smartcash.notificaciones.remitente.email:soporte@smartcash.com}") String remitenteEmail) {
        this.apiUrl = apiUrl;
        this.apiKey = apiKey;
        this.remitenteNombre = remitenteNombre;
        this.remitenteEmail = remitenteEmail;
        this.restClient = RestClient.builder().build();
    }

    @Override
    public ResultadoEnvio enviar(String destinatario, String nombreDestinatario, String asunto, String contenidoHtml) {
        if (apiKey == null || apiKey.isBlank() || apiKey.equals("mock-key")) {
            log.info("Modo demostracion activado. Simulando envio de correo a: {} - Asunto: {}", destinatario, asunto);
            return new ResultadoEnvio(
                    true,
                    "mock-" + UUID.randomUUID(),
                    destinatario,
                    "Brevo (Modo Demo)",
                    "Correo enviado exitosamente en modo demostracion",
                    LocalDateTime.now()
            );
        }

        try {
            Map<String, Object> payload = Map.of(
                    "sender", Map.of("name", remitenteNombre, "email", remitenteEmail),
                    "to", List.of(Map.of("email", destinatario, "name", nombreDestinatario != null ? nombreDestinatario : "")),
                    "subject", asunto,
                    "htmlContent", contenidoHtml
            );

            Map<?, ?> response = restClient.post()
                    .uri(apiUrl)
                    .header("api-key", apiKey)
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(payload)
                    .retrieve()
                    .body(Map.class);

            String messageId = response != null && response.containsKey("messageId")
                    ? String.valueOf(response.get("messageId"))
                    : UUID.randomUUID().toString();

            return new ResultadoEnvio(
                    true,
                    messageId,
                    destinatario,
                    "Brevo REST API",
                    "Correo entregado a la API externa de Brevo",
                    LocalDateTime.now()
            );
        } catch (Exception e) {
            log.error("Error al consumir la API externa de Brevo: {}", e.getMessage());
            return new ResultadoEnvio(
                    false,
                    null,
                    destinatario,
                    "Brevo REST API",
                    "Fallo al conectar con la API externa: " + e.getMessage(),
                    LocalDateTime.now()
            );
        }
    }
}
