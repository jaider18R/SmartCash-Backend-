package com.smartcash.transacciones.infrastructure.clients;

import com.smartcash.transacciones.domain.ports.out.AlertaNotificacionPort;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.math.BigDecimal;
import java.util.Map;

@Component
public class NotificacionRestClientAdapter implements AlertaNotificacionPort {

    private static final Logger log = LoggerFactory.getLogger(NotificacionRestClientAdapter.class);

    private final RestClient restClient;
    private final String url;

    public NotificacionRestClientAdapter(@Value("${smartcash.servicios.notificaciones.url:http://localhost:8083}") String url) {
        this.url = url;
        this.restClient = RestClient.builder().baseUrl(url).build();
    }

    @Override
    public void enviarAlertaPresupuesto(String correoUsuario, String nombreCategoria, BigDecimal porcentajeConsumido) {
        if (correoUsuario == null || correoUsuario.isBlank()) {
            return;
        }

        try {
            Map<String, Object> body = Map.of(
                    "destinatario", correoUsuario,
                    "nombre", "Usuario SmartCash",
                    "tipo", "ALERTA_PRESUPUESTO",
                    "asunto", "SmartCash - Alerta de Presupuesto Excedido",
                    "parametros", Map.of(
                            "categoria", nombreCategoria,
                            "porcentaje", porcentajeConsumido.stripTrailingZeros().toPlainString() + "%"
                    )
            );

            restClient.post()
                    .uri("/api/notificaciones/email")
                    .body(body)
                    .retrieve()
                    .toBodilessEntity();
        } catch (Exception e) {
            log.warn("No fue posible enviar alerta a notificaciones-service ({}): {}", url, e.getMessage());
        }
    }
}
