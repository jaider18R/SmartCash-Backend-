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
        String nombreDestinatario = (nombre != null && !nombre.isBlank()) ? nombre : "Usuario SmartCash";

        return switch (tipo) {
            case RECUPERACION_PASSWORD -> {
                String enlace = parametros != null ? parametros.getOrDefault("enlace", "#") : "#";
                yield """
                    <!DOCTYPE html>
                    <html lang="es">
                    <head>
                        <meta charset="UTF-8">
                        <meta name="viewport" content="width=device-width, initial-scale=1.0">
                    </head>
                    <body style="margin: 0; padding: 0; background-color: #f1f5f9; font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, Helvetica, Arial, sans-serif;">
                        <table width="100%" cellpadding="0" cellspacing="0" style="background-color: #f1f5f9; padding: 30px 15px;">
                            <tr>
                                <td align="center">
                                    <table width="100%" max-width="560" cellpadding="0" cellspacing="0" style="max-width: 560px; background-color: #ffffff; border-radius: 16px; overflow: hidden; box-shadow: 0 4px 6px -1px rgba(0, 0, 0, 0.05), 0 2px 4px -2px rgba(0, 0, 0, 0.05);">
                                        <tr>
                                            <td style="background: linear-gradient(135deg, #2563eb, #1d4ed8); padding: 32px 30px; text-align: center;">
                                                <h1 style="margin: 0; color: #ffffff; font-size: 24px; font-weight: 700; letter-spacing: -0.5px;">SmartCash</h1>
                                                <p style="margin: 6px 0 0 0; color: #bfdbfe; font-size: 13px;">Gestión Financiera Inteligente</p>
                                            </td>
                                        </tr>
                                        <tr>
                                            <td style="padding: 36px 32px; color: #1e293b;">
                                                <h2 style="margin: 0 0 16px 0; font-size: 20px; font-weight: 600; color: #0f172a;">Hola, %s</h2>
                                                <p style="margin: 0 0 20px 0; font-size: 15px; line-height: 1.6; color: #475569;">
                                                    Recibimos una solicitud para restablecer la contraseña de acceso a tu cuenta en <strong>SmartCash</strong>.
                                                </p>
                                                <div style="text-align: center; margin: 30px 0;">
                                                    <a href="%s" style="background-color: #2563eb; color: #ffffff; padding: 14px 28px; text-decoration: none; border-radius: 8px; font-size: 15px; font-weight: 600; display: inline-block; box-shadow: 0 2px 4px rgba(37, 99, 235, 0.2);">Restablecer Contraseña</a>
                                                </div>
                                                <p style="margin: 0; font-size: 13px; line-height: 1.5; color: #94a3b8; text-align: center;">
                                                    Este enlace tiene una validez de 15 minutos. Si no solicitaste este cambio, puedes ignorar este correo sin ningún riesgo.
                                                </p>
                                            </td>
                                        </tr>
                                        <tr>
                                            <td style="background-color: #f8fafc; padding: 20px 32px; border-top: 1px solid #e2e8f0; text-align: center;">
                                                <p style="margin: 0; font-size: 12px; color: #94a3b8;">© 2026 SmartCash. Todos los derechos reservados.</p>
                                            </td>
                                        </tr>
                                    </table>
                                </td>
                            </tr>
                        </table>
                    </body>
                    </html>
                    """.formatted(nombreDestinatario, enlace);
            }
            case ALERTA_PRESUPUESTO -> {
                String categoria = parametros != null ? parametros.getOrDefault("categoria", "General") : "General";
                String porcentaje = parametros != null ? parametros.getOrDefault("porcentaje", "100%") : "100%";
                yield """
                    <!DOCTYPE html>
                    <html lang="es">
                    <head>
                        <meta charset="UTF-8">
                        <meta name="viewport" content="width=device-width, initial-scale=1.0">
                    </head>
                    <body style="margin: 0; padding: 0; background-color: #f8fafc; font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, Helvetica, Arial, sans-serif;">
                        <table width="100%%" cellpadding="0" cellspacing="0" style="background-color: #f8fafc; padding: 30px 15px;">
                            <tr>
                                <td align="center">
                                    <table width="100%%" max-width="580" cellpadding="0" cellspacing="0" style="max-width: 580px; background-color: #ffffff; border-radius: 16px; overflow: hidden; box-shadow: 0 10px 15px -3px rgba(0, 0, 0, 0.05), 0 4px 6px -4px rgba(0, 0, 0, 0.05); border: 1px solid #e2e8f0;">
                                        <tr>
                                            <td style="background: linear-gradient(135deg, #d97706, #ea580c); padding: 32px 30px; text-align: center;">
                                                <div style="display: inline-block; background-color: rgba(255, 255, 255, 0.2); padding: 8px 16px; border-radius: 20px; color: #ffffff; font-size: 12px; font-weight: 700; letter-spacing: 1px; text-transform: uppercase; margin-bottom: 8px;">
                                                    ⚠️ Alerta Financiera
                                                </div>
                                                <h1 style="margin: 0; color: #ffffff; font-size: 22px; font-weight: 700;">Límite de Presupuesto</h1>
                                                <p style="margin: 6px 0 0 0; color: #fef3c7; font-size: 14px;">SmartCash Control de Gastos</p>
                                            </td>
                                        </tr>
                                        <tr>
                                            <td style="padding: 36px 32px; color: #1e293b;">
                                                <h2 style="margin: 0 0 14px 0; font-size: 20px; font-weight: 600; color: #0f172a;">Hola, %s</h2>
                                                <p style="margin: 0 0 24px 0; font-size: 15px; line-height: 1.6; color: #475569;">
                                                    Te informamos que has consumido o superado el umbral preventivo configurado para tu presupuesto mensual en SmartCash.
                                                </p>
                                                <div style="background-color: #fffbeb; border: 1px solid #fef3c7; border-left: 4px solid #f59e0b; border-radius: 10px; padding: 20px; margin-bottom: 28px;">
                                                    <table width="100%%" cellpadding="0" cellspacing="0">
                                                        <tr>
                                                            <td style="font-size: 14px; color: #92400e; font-weight: 600;">Categoría afectada:</td>
                                                            <td align="right" style="font-size: 15px; color: #78350f; font-weight: 700;">%s</td>
                                                        </tr>
                                                        <tr>
                                                            <td style="padding-top: 10px; font-size: 14px; color: #92400e; font-weight: 600;">Consumo alcanzado:</td>
                                                            <td align="right" style="padding-top: 10px; font-size: 18px; color: #dc2626; font-weight: 800;">%s</td>
                                                        </tr>
                                                    </table>
                                                </div>
                                                <p style="margin: 0 0 24px 0; font-size: 14px; line-height: 1.6; color: #64748b;">
                                                    Te recomendamos ingresar a tu panel de control para revisar los movimientos recientes y ajustar tus proyecciones antes del fin de mes.
                                                </p>
                                                <div style="text-align: center;">
                                                    <a href="http://localhost:5173" style="background-color: #0f172a; color: #ffffff; padding: 13px 26px; text-decoration: none; border-radius: 8px; font-size: 14px; font-weight: 600; display: inline-block;">Ver Mi Presupuesto</a>
                                                </div>
                                            </td>
                                        </tr>
                                        <tr>
                                            <td style="background-color: #f8fafc; padding: 18px 32px; border-top: 1px solid #f1f5f9; text-align: center;">
                                                <p style="margin: 0; font-size: 12px; color: #94a3b8;">Notificación preventiva generada por el Asistente Financiero de SmartCash.</p>
                                            </td>
                                        </tr>
                                    </table>
                                </td>
                            </tr>
                        </table>
                    </body>
                    </html>
                    """.formatted(nombreDestinatario, categoria, porcentaje);
            }
            default -> {
                String mensaje = parametros != null ? parametros.getOrDefault("mensaje", "Notificación informativa de SmartCash.") : "Notificación informativa de SmartCash.";
                yield """
                    <!DOCTYPE html>
                    <html lang="es">
                    <head>
                        <meta charset="UTF-8">
                        <meta name="viewport" content="width=device-width, initial-scale=1.0">
                    </head>
                    <body style="margin: 0; padding: 0; background-color: #f8fafc; font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, Helvetica, Arial, sans-serif;">
                        <table width="100%" cellpadding="0" cellspacing="0" style="background-color: #f8fafc; padding: 30px 15px;">
                            <tr>
                                <td align="center">
                                    <table width="100%" max-width="560" cellpadding="0" cellspacing="0" style="max-width: 560px; background-color: #ffffff; border-radius: 16px; overflow: hidden; box-shadow: 0 4px 6px rgba(0, 0, 0, 0.05); border: 1px solid #e2e8f0;">
                                        <tr>
                                            <td style="background: linear-gradient(135deg, #4f46e5, #4338ca); padding: 28px 30px; text-align: center;">
                                                <h1 style="margin: 0; color: #ffffff; font-size: 22px; font-weight: 700;">SmartCash</h1>
                                                <p style="margin: 4px 0 0 0; color: #c7d2fe; font-size: 13px;">Alerta del Sistema</p>
                                            </td>
                                        </tr>
                                        <tr>
                                            <td style="padding: 32px 30px; color: #1e293b;">
                                                <h2 style="margin: 0 0 14px 0; font-size: 18px; font-weight: 600; color: #0f172a;">Hola, %s</h2>
                                                <div style="background-color: #f1f5f9; border-radius: 8px; padding: 18px; margin-bottom: 20px; font-size: 14px; line-height: 1.6; color: #334155;">
                                                    %s
                                                </div>
                                                <p style="margin: 0; font-size: 13px; color: #64748b;">Ingresa a SmartCash para más información.</p>
                                            </td>
                                        </tr>
                                        <tr>
                                            <td style="background-color: #f8fafc; padding: 16px 30px; border-top: 1px solid #f1f5f9; text-align: center;">
                                                <p style="margin: 0; font-size: 12px; color: #94a3b8;">© 2026 SmartCash</p>
                                            </td>
                                        </tr>
                                    </table>
                                </td>
                            </tr>
                        </table>
                    </body>
                    </html>
                    """.formatted(nombreDestinatario, mensaje);
            }
        };
    }
}
