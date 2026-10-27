package com.smartcash.usuarios.application;

import com.smartcash.usuarios.application.services.SolicitarRecuperacionService;
import com.smartcash.usuarios.domain.model.PasswordResetToken;
import com.smartcash.usuarios.domain.model.Usuario;
import com.smartcash.usuarios.domain.ports.out.NotificacionClientPort;
import com.smartcash.usuarios.domain.ports.out.RepositorioTokenRecuperacionPort;
import com.smartcash.usuarios.domain.ports.out.RepositorioUsuarioPort;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

class SolicitarRecuperacionServiceTest {

    private RepositorioUsuarioPort repositorioUsuarioMock;
    private RepositorioTokenRecuperacionPort repositorioTokenMock;
    private NotificacionClientPort notificacionClientMock;
    private SolicitarRecuperacionService solicitarRecuperacionService;

    @BeforeEach
    void setUp() {
        repositorioUsuarioMock = mock(RepositorioUsuarioPort.class);
        repositorioTokenMock = mock(RepositorioTokenRecuperacionPort.class);
        notificacionClientMock = mock(NotificacionClientPort.class);
        solicitarRecuperacionService = new SolicitarRecuperacionService(
                repositorioUsuarioMock,
                repositorioTokenMock,
                notificacionClientMock,
                "http://localhost:5173"
        );
    }

    @Test
    void deberiaSolicitarRecuperacionExitosamente() {
        String correo = "usuario@smartcash.com";
        Usuario usuario = new Usuario(UUID.randomUUID(), "Usuario", correo, "hash", LocalDateTime.now());

        when(repositorioUsuarioMock.buscarPorCorreo(correo)).thenReturn(Optional.of(usuario));

        solicitarRecuperacionService.solicitar(correo);

        verify(repositorioUsuarioMock, times(1)).buscarPorCorreo(correo);
        verify(repositorioTokenMock, times(1)).guardar(any(PasswordResetToken.class));
        verify(notificacionClientMock, times(1)).enviarRecuperacionPassword(
                eq(correo),
                eq("Usuario"),
                contains("http://localhost:5173/?token=")
        );
    }

    @Test
    void deberiaIgnorarSilenciosamenteSiUsuarioNoExiste() {
        String correo = "noexiste@smartcash.com";
        when(repositorioUsuarioMock.buscarPorCorreo(correo)).thenReturn(Optional.empty());

        solicitarRecuperacionService.solicitar(correo);

        verify(repositorioUsuarioMock, times(1)).buscarPorCorreo(correo);
        verifyNoInteractions(repositorioTokenMock);
        verifyNoInteractions(notificacionClientMock);
    }
}
