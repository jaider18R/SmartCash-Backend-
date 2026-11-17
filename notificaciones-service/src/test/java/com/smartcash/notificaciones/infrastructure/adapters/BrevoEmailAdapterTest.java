package com.smartcash.notificaciones.infrastructure.adapters;

import com.smartcash.notificaciones.domain.model.ResultadoEnvio;
import com.sun.net.httpserver.HttpServer;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.*;

class BrevoEmailAdapterTest {

    private HttpServer server;
    private String baseUrl;

    @BeforeEach
    void setUp() throws IOException {
        server = HttpServer.create(new InetSocketAddress(0), 0);
        server.start();
        baseUrl = "http://localhost:" + server.getAddress().getPort() + "/v3/smtp/email";
    }

    @AfterEach
    void tearDown() {
        if (server != null) {
            server.stop(0);
        }
    }

    @Test
    void deberiaEnviarEnModoDemostracionCuandoApiKeyEsMockOVacia() {
        BrevoEmailAdapter adapterMock = new BrevoEmailAdapter(baseUrl, "mock-key-123", "SmartCash", "info@smartcash.com");
        ResultadoEnvio res1 = adapterMock.enviar("user@test.com", "Juan", "Asunto", "<p>Hola</p>");
        assertTrue(res1.exitoso());
        assertTrue(res1.idMensaje().startsWith("mock-"));

        BrevoEmailAdapter adapterNulls = new BrevoEmailAdapter(null, null, null, null);
        ResultadoEnvio res2 = adapterNulls.enviar("user@test.com", null, "Asunto", "<p>Hola</p>");
        assertTrue(res2.exitoso());
    }

    @Test
    void deberiaEnviarExitosamenteConMessageIdDeBrevo() {
        server.createContext("/v3/smtp/email", exchange -> {
            byte[] response = "{\"messageId\":\"<20261028@brevo.com>\"}".getBytes(StandardCharsets.UTF_8);
            exchange.getResponseHeaders().add("Content-Type", "application/json");
            exchange.sendResponseHeaders(201, response.length);
            try (OutputStream os = exchange.getResponseBody()) {
                os.write(response);
            }
        });

        BrevoEmailAdapter adapter = new BrevoEmailAdapter(baseUrl, "xkeysib-real-key", "SmartCash", "info@smartcash.com");
        ResultadoEnvio res = adapter.enviar("user@test.com", "Juan", "Asunto", "<p>Hola</p>");

        assertTrue(res.exitoso());
        assertEquals("<20261028@brevo.com>", res.idMensaje());
        assertEquals("Brevo REST API", res.proveedor());
    }

    @Test
    void deberiaGenerarUuidCuandoRespuestaNoTieneMessageId() {
        server.createContext("/v3/smtp/email", exchange -> {
            byte[] response = "{\"status\":\"ok\"}".getBytes(StandardCharsets.UTF_8);
            exchange.getResponseHeaders().add("Content-Type", "application/json");
            exchange.sendResponseHeaders(200, response.length);
            try (OutputStream os = exchange.getResponseBody()) {
                os.write(response);
            }
        });

        BrevoEmailAdapter adapter = new BrevoEmailAdapter(baseUrl, "xkeysib-real-key", "SmartCash", "info@smartcash.com");
        ResultadoEnvio res = adapter.enviar("user@test.com", null, "Asunto", "<p>Hola</p>");

        assertTrue(res.exitoso());
        assertNotNull(res.idMensaje());
    }

    @Test
    void deberiaManejarExcepcionCuandoFallaLaApiExterna() {
        server.createContext("/v3/smtp/email", exchange -> {
            exchange.sendResponseHeaders(500, -1);
            exchange.close();
        });

        BrevoEmailAdapter adapter = new BrevoEmailAdapter(baseUrl, "xkeysib-real-key", "SmartCash", "info@smartcash.com");
        ResultadoEnvio res = adapter.enviar("user@test.com", "Juan", "Asunto", "<p>Hola</p>");

        assertFalse(res.exitoso());
        assertNull(res.idMensaje());
    }
}
