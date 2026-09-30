package com.smartcash.notificaciones.application.services;

import com.smartcash.notificaciones.domain.model.ResultadoEnvio;
import com.smartcash.notificaciones.domain.model.TipoNotificacion;
import com.smartcash.notificaciones.domain.ports.in.EnviarNotificacionUseCase;
import com.smartcash.notificaciones.domain.ports.out.ProveedorEmailPort;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
public class EnviarNotificacionService implements EnviarNotificacionUseCase {

    private final ProveedorEmailPort proveedorEmailPort;

    public EnviarNotificacionService(ProveedorEmailPort proveedorEmailPort) {
        this.proveedorEmailPort = proveedorEmailPort;
    }

    @Override
    public ResultadoEnvio enviar(String destinatario, String nombre, TipoNotificacion tipo, String asunto, Map<String, String> parametros) {
        String asuntoFinal = (asunto != null && !asunto.isBlank()) ? asunto : obtenerAsuntoPorDefecto(tipo);
        String html = generarContenidoHtml(tipo, nombre, parametros);

        return proveedorEmailPort.enviar(destinatario, nombre, asuntoFinal, html);
    }

    private String obtenerAsuntoPorDefecto(TipoNotificacion tipo) {
        return switch (tipo) {
            case RECUPERACION_PASSWORD -> "SmartCash - Restablecer tu contraseña";
            case ALERTA_PRESUPUESTO -> "SmartCash - Alerta de Presupuesto Excedido";
            case BIENVENIDA -> "Bienvenido a SmartCash";
            case GENERAL -> "Notificación de SmartCash";
        };
    }

    private String generarContenidoHtml(TipoNotificacion tipo, String nombre, Map<String, String> parametros) {
        String saludo = "<h2>Hola, " + (nombre != null ? nombre : "Usuario") + "</h2>";

        return switch (tipo) {
            case RECUPERACION_PASSWORD -> {
                String enlace = parametros != null ? parametros.getOrDefault("enlace", "#") : "#";
                yield "<div style='font-family: Arial, sans-serif; padding: 20px; color: #333;'>"
                        + saludo
                        + "<p>Recibimos una solicitud para restablecer la contraseña de tu cuenta en <strong>SmartCash</strong>.</p>"
                        + "<p>Haz clic en el siguiente botón para crear una nueva contraseña:</p>"
                        + "<p style='margin: 25px 0;'><a href='" + enlace + "' style='background-color: #2563eb; color: #fff; padding: 12px 24px; text-decoration: none; border-radius: 6px; font-weight: bold;'>Restablecer Contraseña</a></p>"
                        + "<p><small>Este enlace expirará en 15 minutos. Si no realizaste esta solicitud, puedes ignorar este mensaje de forma segura.</small></p>"
                        + "</div>";
            }
            case ALERTA_PRESUPUESTO -> {
                String categoria = parametros != null ? parametros.getOrDefault("categoria", "General") : "General";
                String porcentaje = parametros != null ? parametros.getOrDefault("porcentaje", "100%") : "100%";
                yield "<div style='font-family: Arial, sans-serif; padding: 20px; color: #333;'>"
                        + saludo
                        + "<p>Te informamos que has alcanzado el <strong>" + porcentaje + "</strong> de tu presupuesto en la categoría <strong>" + categoria + "</strong>.</p>"
                        + "<p>Ingresa a SmartCash para revisar tus movimientos financieros.</p>"
                        + "</div>";
            }
            default -> "<div style='font-family: Arial, sans-serif; padding: 20px;'>" + saludo + "<p>Notificación enviada desde SmartCash.</p></div>";
        };
    }
}
