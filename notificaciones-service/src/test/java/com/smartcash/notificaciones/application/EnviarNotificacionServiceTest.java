package com.smartcash.notificaciones.application;

import com.smartcash.notificaciones.application.services.EnviarNotificacionService;
import com.smartcash.notificaciones.domain.model.ResultadoEnvio;
import com.smartcash.notificaciones.domain.model.TipoNotificacion;
import com.smartcash.notificaciones.domain.ports.out.ProveedorEmailPort;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class EnviarNotificacionServiceTest {

    private ProveedorEmailPort proveedorEmailMock;
    private EnviarNotificacionService service;

    @BeforeEach
    void setUp() {
        proveedorEmailMock = mock(ProveedorEmailPort.class);
        service = new EnviarNotificacionService(proveedorEmailMock);
    }

    @Test
    void deberiaEnviarNotificacionRecuperacionPasswordConAsuntoPorDefecto() {
        String destinatario = "usuario@smartcash.com";
        String nombre = "Carlos";
        Map<String, String> params = Map.of("enlace", "http://localhost:5173/?token=abc-123");
        ResultadoEnvio resultadoSimulado = new ResultadoEnvio(true, "msg-123", destinatario, "Brevo", "Enviado", LocalDateTime.now());

        when(proveedorEmailMock.enviar(eq(destinatario), eq(nombre), eq("SmartCash - Restablecer tu contraseña"), anyString()))
                .thenReturn(resultadoSimulado);

        ResultadoEnvio resultado = service.enviar(destinatario, nombre, TipoNotificacion.RECUPERACION_PASSWORD, null, params);

        assertTrue(resultado.exitoso());
        assertEquals("msg-123", resultado.idMensaje());
        verify(proveedorEmailMock, times(1)).enviar(
                eq(destinatario),
                eq(nombre),
                eq("SmartCash - Restablecer tu contraseña"),
                contains("http://localhost:5173/?token=abc-123")
        );
    }

    @Test
    void deberiaEnviarAlertaPresupuestoConAsuntoPersonalizado() {
        String destinatario = "usuario@smartcash.com";
        String nombre = "Maria";
        Map<String, String> params = Map.of("categoria", "Restaurantes", "porcentaje", "92%");
        ResultadoEnvio resultadoSimulado = new ResultadoEnvio(true, "msg-456", destinatario, "Brevo", "Enviado", LocalDateTime.now());

        when(proveedorEmailMock.enviar(eq(destinatario), eq(nombre), eq("¡Cuidado con tus gastos!"), anyString()))
                .thenReturn(resultadoSimulado);

        ResultadoEnvio resultado = service.enviar(destinatario, nombre, TipoNotificacion.ALERTA_PRESUPUESTO, "¡Cuidado con tus gastos!", params);

        assertTrue(resultado.exitoso());
        verify(proveedorEmailMock, times(1)).enviar(
                eq(destinatario),
                eq(nombre),
                eq("¡Cuidado con tus gastos!"),
                contains("Restaurantes")
        );
    }

    @Test
    void deberiaManejarEnvioFallido() {
        String destinatario = "error@smartcash.com";
        ResultadoEnvio resultadoFallido = new ResultadoEnvio(false, null, destinatario, "Brevo", "Error SMTP", LocalDateTime.now());

        when(proveedorEmailMock.enviar(anyString(), any(), anyString(), anyString()))
                .thenReturn(resultadoFallido);

        ResultadoEnvio resultado = service.enviar(destinatario, "Juan", TipoNotificacion.BIENVENIDA, null, Map.of());

        assertFalse(resultado.exitoso());
        assertEquals("Error SMTP", resultado.detalle());
    }
}
