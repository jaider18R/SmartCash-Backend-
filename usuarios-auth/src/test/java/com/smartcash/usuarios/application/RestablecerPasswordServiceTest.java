package com.smartcash.usuarios.application;

import com.smartcash.usuarios.application.services.RestablecerPasswordService;
import com.smartcash.usuarios.domain.exceptions.TokenInvalidoException;
import com.smartcash.usuarios.domain.model.PasswordResetToken;
import com.smartcash.usuarios.domain.model.Usuario;
import com.smartcash.usuarios.domain.ports.out.PasswordEncoderPort;
import com.smartcash.usuarios.domain.ports.out.RepositorioTokenRecuperacionPort;
import com.smartcash.usuarios.domain.ports.out.RepositorioUsuarioPort;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

class RestablecerPasswordServiceTest {

    private RepositorioTokenRecuperacionPort repositorioTokenMock;
    private RepositorioUsuarioPort repositorioUsuarioMock;
    private PasswordEncoderPort passwordEncoderMock;
    private RestablecerPasswordService restablecerPasswordService;

    @BeforeEach
    void setUp() {
        repositorioTokenMock = mock(RepositorioTokenRecuperacionPort.class);
        repositorioUsuarioMock = mock(RepositorioUsuarioPort.class);
        passwordEncoderMock = mock(PasswordEncoderPort.class);
        restablecerPasswordService = new RestablecerPasswordService(
                repositorioTokenMock,
                repositorioUsuarioMock,
                passwordEncoderMock
        );
    }

    @Test
    void deberiaRestablecerPasswordExitosamente() {
        UUID idUsuario = UUID.randomUUID();
        String tokenStr = "token-valido-123";
        PasswordResetToken resetToken = PasswordResetToken.nuevo(idUsuario, tokenStr);
        Usuario usuario = new Usuario(idUsuario, "Nombre", "test@smartcash.com", "hashViejo", LocalDateTime.now());

        when(repositorioTokenMock.buscarPorToken(tokenStr)).thenReturn(Optional.of(resetToken));
        when(repositorioUsuarioMock.buscarPorId(idUsuario)).thenReturn(Optional.of(usuario));
        when(passwordEncoderMock.encriptar("NuevaPass123*")).thenReturn("nuevoHash");

        restablecerPasswordService.restablecer(tokenStr, "NuevaPass123*");

        verify(passwordEncoderMock, times(1)).encriptar("NuevaPass123*");
        verify(repositorioUsuarioMock, times(1)).guardar(argThat(u -> "nuevoHash".equals(u.getPasswordHash())));
        verify(repositorioTokenMock, times(1)).guardar(argThat(PasswordResetToken::isUsado));
    }

    @Test
    void deberiaRechazarSiTokenNoExiste() {
        when(repositorioTokenMock.buscarPorToken("token-inexistente")).thenReturn(Optional.empty());

        assertThrows(TokenInvalidoException.class, () ->
                restablecerPasswordService.restablecer("token-inexistente", "Pass123*"));

        verifyNoInteractions(passwordEncoderMock);
        verify(repositorioUsuarioMock, never()).guardar(any());
    }

    @Test
    void deberiaRechazarSiTokenYaFueUsado() {
        UUID idUsuario = UUID.randomUUID();
        String tokenStr = "token-usado";
        PasswordResetToken resetToken = PasswordResetToken.nuevo(idUsuario, tokenStr).marcarComoUsado();

        when(repositorioTokenMock.buscarPorToken(tokenStr)).thenReturn(Optional.of(resetToken));

        assertThrows(TokenInvalidoException.class, () ->
                restablecerPasswordService.restablecer(tokenStr, "Pass123*"));

        verifyNoInteractions(passwordEncoderMock);
        verify(repositorioUsuarioMock, never()).guardar(any());
    }
}
