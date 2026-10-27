package com.smartcash.usuarios.application;

import com.smartcash.usuarios.application.services.LoginService;
import com.smartcash.usuarios.domain.exceptions.CredencialesInvalidasException;
import com.smartcash.usuarios.domain.model.Usuario;
import com.smartcash.usuarios.domain.ports.out.PasswordEncoderPort;
import com.smartcash.usuarios.domain.ports.out.RepositorioUsuarioPort;
import com.smartcash.usuarios.domain.ports.out.TokenGeneratorPort;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class LoginServiceTest {

    private RepositorioUsuarioPort repositorioUsuarioMock;
    private PasswordEncoderPort passwordEncoderMock;
    private TokenGeneratorPort tokenGeneratorMock;
    private LoginService loginService;

    @BeforeEach
    void setUp() {
        repositorioUsuarioMock = mock(RepositorioUsuarioPort.class);
        passwordEncoderMock = mock(PasswordEncoderPort.class);
        tokenGeneratorMock = mock(TokenGeneratorPort.class);
        loginService = new LoginService(repositorioUsuarioMock, passwordEncoderMock, tokenGeneratorMock);
    }

    @Test
    void deberiaHacerLoginExitoso() {
        String correo = "test@smartcash.com";
        String passwordPlano = "Password123*";
        String passwordHash = "$2a$10$hashSimulado";
        Usuario usuario = new Usuario(UUID.randomUUID(), "Usuario Test", correo, passwordHash, LocalDateTime.now());

        when(repositorioUsuarioMock.buscarPorCorreo(correo)).thenReturn(Optional.of(usuario));
        when(passwordEncoderMock.verificar(passwordPlano, passwordHash)).thenReturn(true);
        when(tokenGeneratorMock.generarToken(usuario)).thenReturn("jwt.token.valido");

        String token = loginService.login(correo, passwordPlano);

        assertNotNull(token);
        assertEquals("jwt.token.valido", token);
        verify(repositorioUsuarioMock, times(1)).buscarPorCorreo(correo);
        verify(passwordEncoderMock, times(1)).verificar(passwordPlano, passwordHash);
        verify(tokenGeneratorMock, times(1)).generarToken(usuario);
    }

    @Test
    void deberiaLanzarExcepcionCuandoUsuarioNoExiste() {
        String correo = "inexistente@smartcash.com";
        when(repositorioUsuarioMock.buscarPorCorreo(correo)).thenReturn(Optional.empty());

        assertThrows(CredencialesInvalidasException.class, () ->
                loginService.login(correo, "Password123*"));

        verify(repositorioUsuarioMock, times(1)).buscarPorCorreo(correo);
        verifyNoInteractions(passwordEncoderMock);
        verifyNoInteractions(tokenGeneratorMock);
    }

    @Test
    void deberiaLanzarExcepcionCuandoPasswordEsIncorrecto() {
        String correo = "test@smartcash.com";
        String passwordPlano = "PasswordIncorrecto";
        String passwordHash = "$2a$10$hashSimulado";
        Usuario usuario = new Usuario(UUID.randomUUID(), "Usuario Test", correo, passwordHash, LocalDateTime.now());

        when(repositorioUsuarioMock.buscarPorCorreo(correo)).thenReturn(Optional.of(usuario));
        when(passwordEncoderMock.verificar(passwordPlano, passwordHash)).thenReturn(false);

        assertThrows(CredencialesInvalidasException.class, () ->
                loginService.login(correo, passwordPlano));

        verify(repositorioUsuarioMock, times(1)).buscarPorCorreo(correo);
        verify(passwordEncoderMock, times(1)).verificar(passwordPlano, passwordHash);
        verifyNoInteractions(tokenGeneratorMock);
    }
}
