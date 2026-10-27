package com.smartcash.notificaciones.api;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.smartcash.notificaciones.api.controllers.NotificacionController;
import com.smartcash.notificaciones.api.dto.NotificacionEmailRequestDTO;
import com.smartcash.notificaciones.application.services.EnviarNotificacionService;
import com.smartcash.notificaciones.domain.model.ResultadoEnvio;
import com.smartcash.notificaciones.domain.model.TipoNotificacion;
import com.smartcash.notificaciones.domain.ports.out.ProveedorEmailPort;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.time.LocalDateTime;
import java.util.Map;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

class NotificacionControllerE2ETest {

    private MockMvc mockMvc;
    private ObjectMapper objectMapper;
    private ProveedorEmailPort proveedorEmailMock;

    @BeforeEach
    void setUp() {
        proveedorEmailMock = mock(ProveedorEmailPort.class);
        EnviarNotificacionService service = new EnviarNotificacionService(proveedorEmailMock);
        NotificacionController controller = new NotificacionController(service);

        mockMvc = MockMvcBuilders.standaloneSetup(controller).build();
        objectMapper = new ObjectMapper();
    }

    @Test
    void e2eHealthEndpointRetorna200() throws Exception {
        mockMvc.perform(get("/api/notificaciones/health"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("UP"))
                .andExpect(jsonPath("$.service").value("notificaciones-service"));
    }

    @Test
    void e2eEnviarEmailRecuperacionPasswordRetorna200() throws Exception {
        when(proveedorEmailMock.enviar(anyString(), any(), anyString(), anyString()))
                .thenReturn(new ResultadoEnvio(true, "id-001", "user@smartcash.com", "Brevo", "OK", LocalDateTime.now()));

        NotificacionEmailRequestDTO req = new NotificacionEmailRequestDTO(
                "user@smartcash.com",
                "Carlos",
                TipoNotificacion.RECUPERACION_PASSWORD,
                null,
                Map.of("enlace", "http://localhost:5173/?token=123")
        );

        mockMvc.perform(post("/api/notificaciones/email")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.exitoso").value(true))
                .andExpect(jsonPath("$.idMensaje").value("id-001"));
    }

    @Test
    void e2eEnviarEmailAlertaPresupuestoRetorna200() throws Exception {
        when(proveedorEmailMock.enviar(anyString(), any(), anyString(), anyString()))
                .thenReturn(new ResultadoEnvio(true, "id-002", "user@smartcash.com", "Brevo", "OK", LocalDateTime.now()));

        NotificacionEmailRequestDTO req = new NotificacionEmailRequestDTO(
                "user@smartcash.com",
                "Maria",
                TipoNotificacion.ALERTA_PRESUPUESTO,
                null,
                Map.of("categoria", "Mercado", "porcentaje", "88%")
        );

        mockMvc.perform(post("/api/notificaciones/email")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.exitoso").value(true));
    }

    @Test
    void e2eEnviarEmailBienvenidaYGeneralRetorna200() throws Exception {
        when(proveedorEmailMock.enviar(anyString(), any(), anyString(), anyString()))
                .thenReturn(new ResultadoEnvio(true, "id-003", "user@smartcash.com", "Brevo", "OK", LocalDateTime.now()));

        NotificacionEmailRequestDTO reqBienvenida = new NotificacionEmailRequestDTO(
                "user@smartcash.com",
                "Pedro",
                TipoNotificacion.BIENVENIDA,
                null,
                Map.of()
        );

        mockMvc.perform(post("/api/notificaciones/email")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(reqBienvenida)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.exitoso").value(true));

        NotificacionEmailRequestDTO reqGeneral = new NotificacionEmailRequestDTO(
                "user@smartcash.com",
                null,
                TipoNotificacion.GENERAL,
                null,
                Map.of("mensaje", "Resumen semanal listo")
        );

        mockMvc.perform(post("/api/notificaciones/email")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(reqGeneral)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.exitoso").value(true));
    }

    @Test
    void e2eEnviarEmailConCorreoInvalidoRetorna400() throws Exception {
        NotificacionEmailRequestDTO req = new NotificacionEmailRequestDTO(
                "correo-invalido",
                "Pedro",
                TipoNotificacion.BIENVENIDA,
                null,
                Map.of()
        );

        mockMvc.perform(post("/api/notificaciones/email")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isBadRequest());
    }
}
