package com.smartcash.usuarios.infrastructure;

import com.smartcash.usuarios.domain.model.PasswordResetToken;
import com.smartcash.usuarios.domain.model.Usuario;
import com.smartcash.usuarios.infrastructure.clients.NotificacionRestClientAdapter;
import com.smartcash.usuarios.infrastructure.persistence.*;
import com.smartcash.usuarios.infrastructure.security.SecurityConfig;
import com.sun.net.httpserver.HttpServer;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class InfrastructureAdaptersTest {

    @Test
    void deberiaCubrirUsuarioRepositoryAdapter() {
        UsuarioJpaRepository jpaRepo = mock(UsuarioJpaRepository.class);
        UsuarioRepositoryAdapter adapter = new UsuarioRepositoryAdapter(jpaRepo);

        UUID id = UUID.randomUUID();
        LocalDateTime now = LocalDateTime.now();
        Usuario domainUser = new Usuario(id, "Ana", "ana@smartcash.com", "hash123", now);
        UsuarioJpaEntity entity = new UsuarioJpaEntity(id, "Ana", "ana@smartcash.com", "hash123", now);

        when(jpaRepo.save(any(UsuarioJpaEntity.class))).thenReturn(entity);
        when(jpaRepo.findByCorreo("ana@smartcash.com")).thenReturn(Optional.of(entity));
        when(jpaRepo.findById(id)).thenReturn(Optional.of(entity));
        when(jpaRepo.existsByCorreo("ana@smartcash.com")).thenReturn(true);

        Usuario guardado = adapter.guardar(domainUser);
        assertEquals(id, guardado.getId());
        assertEquals("Ana", guardado.getNombre());

        Optional<Usuario> porCorreo = adapter.buscarPorCorreo("ana@smartcash.com");
        assertTrue(porCorreo.isPresent());
        assertEquals("ana@smartcash.com", porCorreo.get().getCorreo());

        Optional<Usuario> porId = adapter.buscarPorId(id);
        assertTrue(porId.isPresent());
        assertEquals(id, porId.get().getId());

        assertTrue(adapter.existePorCorreo("ana@smartcash.com"));
    }

    @Test
    void deberiaCubrirTokenRecuperacionRepositoryAdapter() {
        PasswordResetTokenJpaRepository jpaRepo = mock(PasswordResetTokenJpaRepository.class);
        TokenRecuperacionRepositoryAdapter adapter = new TokenRecuperacionRepositoryAdapter(jpaRepo);

        UUID id = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        LocalDateTime exp = LocalDateTime.now().plusMinutes(15);
        PasswordResetToken token = new PasswordResetToken(id, "tok-123", userId, exp, false);
        PasswordResetTokenJpaEntity entity = new PasswordResetTokenJpaEntity(id, "tok-123", userId, exp, false);

        when(jpaRepo.save(any(PasswordResetTokenJpaEntity.class))).thenReturn(entity);
        when(jpaRepo.findByToken("tok-123")).thenReturn(Optional.of(entity));

        PasswordResetToken guardado = adapter.guardar(token);
        assertEquals(id, guardado.getId());
        assertEquals("tok-123", guardado.getToken());
        assertEquals(userId, guardado.getIdUsuario());
        assertEquals(exp, guardado.getFechaExpiracion());
        assertFalse(guardado.isUsado());

        Optional<PasswordResetToken> encontrado = adapter.buscarPorToken("tok-123");
        assertTrue(encontrado.isPresent());
        assertTrue(encontrado.get().esValido());

        PasswordResetToken usado = encontrado.get().marcarComoUsado();
        assertFalse(usado.esValido());

        PasswordResetToken expirado = new PasswordResetToken(id, "tok-exp", userId, LocalDateTime.now().minusMinutes(5), false);
        assertFalse(expirado.esValido());

        PasswordResetTokenJpaEntity emptyEntity = new PasswordResetTokenJpaEntity();
        emptyEntity.setIdToken(id);
        emptyEntity.setToken("t");
        emptyEntity.setIdUsuario(userId);
        emptyEntity.setFechaExpiracion(exp);
        emptyEntity.setUsado(true);
        assertTrue(emptyEntity.isUsado());
    }

    @Test
    void deberiaCubrirNotificacionRestClientAdapterExitoYFallo() throws IOException {
        HttpServer server = HttpServer.create(new InetSocketAddress(0), 0);
        server.createContext("/api/notificaciones/email", exchange -> {
            exchange.sendResponseHeaders(200, -1);
            exchange.close();
        });
        server.start();

        try {
            String url = "http://localhost:" + server.getAddress().getPort();
            NotificacionRestClientAdapter adapter = new NotificacionRestClientAdapter(url);
            assertDoesNotThrow(() -> adapter.enviarRecuperacionPassword("user@test.com", "Pedro", "http://link"));
            assertDoesNotThrow(() -> adapter.enviarRecuperacionPassword("user@test.com", null, "http://link"));
        } finally {
            server.stop(0);
        }

        NotificacionRestClientAdapter adapterFallo = new NotificacionRestClientAdapter("http://localhost:1");
        assertDoesNotThrow(() -> adapterFallo.enviarRecuperacionPassword("user@test.com", "Pedro", "http://link"));
    }

    @Test
    void deberiaCubrirSecurityConfigCors() {
        SecurityConfig config = new SecurityConfig();
        CorsConfigurationSource source = config.corsConfigurationSource();
        assertNotNull(source);
        CorsConfiguration cors = source.getCorsConfiguration(new MockHttpServletRequest());
        assertNotNull(cors);
        assertTrue(Boolean.TRUE.equals(cors.getAllowCredentials()));
    }
}
