package com.smartcash.usuarios.application;

import com.smartcash.usuarios.application.services.RegistrarUsuarioService;
import com.smartcash.usuarios.domain.exceptions.UsuarioYaExisteException;
import com.smartcash.usuarios.domain.model.Usuario;
import com.smartcash.usuarios.domain.ports.out.PasswordEncoderPort;
import com.smartcash.usuarios.domain.ports.out.RepositorioUsuarioPort;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class RegistrarUsuarioServiceTest {

    private RepositorioUsuarioPort repositorioUsuarioMock;
    private PasswordEncoderPort passwordEncoderMock;
    private RegistrarUsuarioService registrarUsuarioService;

    @BeforeEach
    void setUp() {
        repositorioUsuarioMock = mock(RepositorioUsuarioPort.class);
        passwordEncoderMock = mock(PasswordEncoderPort.class);
        registrarUsuarioService = new RegistrarUsuarioService(repositorioUsuarioMock, passwordEncoderMock);
    }

    @Test
    void deberiaRegistrarUsuarioExitosamente() {
        String nombre = "Alejandro";
        String correo = "alejandro@smartcash.com";
        String passwordPlano = "SecurePass123";
        String hashGenerado = "$2a$10$hashSeguro";

        when(repositorioUsuarioMock.existePorCorreo(correo)).thenReturn(false);
        when(passwordEncoderMock.encriptar(passwordPlano)).thenReturn(hashGenerado);
        when(repositorioUsuarioMock.guardar(any(Usuario.class))).thenAnswer(inv -> inv.getArgument(0));

        Usuario usuarioCreado = registrarUsuarioService.registrar(nombre, correo, passwordPlano);

        assertNotNull(usuarioCreado);
        assertEquals(nombre, usuarioCreado.getNombre());
        assertEquals(correo, usuarioCreado.getCorreo());
        assertEquals(hashGenerado, usuarioCreado.getPasswordHash());
        verify(repositorioUsuarioMock, times(1)).existePorCorreo(correo);
        verify(passwordEncoderMock, times(1)).encriptar(passwordPlano);
        verify(repositorioUsuarioMock, times(1)).guardar(any(Usuario.class));
    }

    @Test
    void deberiaLanzarExcepcionCuandoCorreoYaEstaRegistrado() {
        String correo = "repetido@smartcash.com";
        when(repositorioUsuarioMock.existePorCorreo(correo)).thenReturn(true);

        assertThrows(UsuarioYaExisteException.class, () ->
                registrarUsuarioService.registrar("Nombre", correo, "pass123"));

        verify(repositorioUsuarioMock, times(1)).existePorCorreo(correo);
        verifyNoInteractions(passwordEncoderMock);
        verify(repositorioUsuarioMock, never()).guardar(any());
    }
}
