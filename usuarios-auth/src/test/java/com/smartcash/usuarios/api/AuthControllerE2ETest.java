package com.smartcash.usuarios.api;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.smartcash.usuarios.api.controllers.AuthController;
import com.smartcash.usuarios.api.controllers.GlobalExceptionHandler;
import com.smartcash.usuarios.api.dto.LoginRequestDTO;
import com.smartcash.usuarios.api.dto.RecuperarPasswordRequestDTO;
import com.smartcash.usuarios.api.dto.RegistroRequestDTO;
import com.smartcash.usuarios.api.dto.RestablecerPasswordRequestDTO;
import com.smartcash.usuarios.domain.exceptions.CredencialesInvalidasException;
import com.smartcash.usuarios.domain.exceptions.TokenInvalidoException;
import com.smartcash.usuarios.domain.exceptions.UsuarioYaExisteException;
import com.smartcash.usuarios.domain.model.Usuario;
import com.smartcash.usuarios.domain.ports.in.LoginUseCase;
import com.smartcash.usuarios.domain.ports.in.RegistrarUsuarioUseCase;
import com.smartcash.usuarios.domain.ports.in.RestablecerPasswordUseCase;
import com.smartcash.usuarios.domain.ports.in.SolicitarRecuperacionUseCase;
import com.smartcash.usuarios.infrastructure.security.BCryptPasswordEncoderAdapter;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.time.LocalDateTime;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

class AuthControllerE2ETest {

    private MockMvc mockMvc;
    private ObjectMapper objectMapper;
    private RegistrarUsuarioUseCase registrarUseCase;
    private LoginUseCase loginUseCase;
    private SolicitarRecuperacionUseCase solicitarRecuperacionUseCase;
    private RestablecerPasswordUseCase restablecerPasswordUseCase;

    @BeforeEach
    void setUp() {
        registrarUseCase = mock(RegistrarUsuarioUseCase.class);
        loginUseCase = mock(LoginUseCase.class);
        solicitarRecuperacionUseCase = mock(SolicitarRecuperacionUseCase.class);
        restablecerPasswordUseCase = mock(RestablecerPasswordUseCase.class);

        AuthController controller = new AuthController(
                registrarUseCase,
                loginUseCase,
                solicitarRecuperacionUseCase,
                restablecerPasswordUseCase
        );

        mockMvc = MockMvcBuilders.standaloneSetup(controller)
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
        objectMapper = new ObjectMapper();
    }

    @Test
    void e2eRegistroUsuarioExitosoRetorna201() throws Exception {
        UUID id = UUID.randomUUID();
        Usuario usuario = new Usuario(id, "Alejandro", "alejandro@smartcash.com", "hash", LocalDateTime.now());
        when(registrarUseCase.registrar("Alejandro", "alejandro@smartcash.com", "Password123*"))
                .thenReturn(usuario);

        RegistroRequestDTO request = new RegistroRequestDTO("Alejandro", "alejandro@smartcash.com", "Password123*");

        mockMvc.perform(post("/auth/registro")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.idUsuario").value(id.toString()))
                .andExpect(jsonPath("$.nombre").value("Alejandro"))
                .andExpect(jsonPath("$.correo").value("alejandro@smartcash.com"));
    }

    @Test
    void e2eRegistroUsuarioDuplicadoRetorna409() throws Exception {
        when(registrarUseCase.registrar(anyString(), anyString(), anyString()))
                .thenThrow(new UsuarioYaExisteException("duplicado@smartcash.com"));

        RegistroRequestDTO request = new RegistroRequestDTO("Test", "duplicado@smartcash.com", "Password123*");

        mockMvc.perform(post("/auth/registro")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.error").exists());
    }

    @Test
    void e2eRegistroConDatosInvalidosRetorna400() throws Exception {
        RegistroRequestDTO request = new RegistroRequestDTO("", "correo-invalido", "123");

        mockMvc.perform(post("/auth/registro")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").exists());
    }

    @Test
    void e2eLoginExitosoRetorna200YToken() throws Exception {
        when(loginUseCase.login("alejandro@smartcash.com", "Password123*"))
                .thenReturn("eyJhbGciOiJIUzI1NiJ9.token.e2e");

        LoginRequestDTO request = new LoginRequestDTO("alejandro@smartcash.com", "Password123*");

        mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").value("eyJhbGciOiJIUzI1NiJ9.token.e2e"))
                .andExpect(jsonPath("$.tipo").value("Bearer"));
    }

    @Test
    void e2eLoginCredencialesInvalidasRetorna401() throws Exception {
        when(loginUseCase.login(anyString(), anyString()))
                .thenThrow(new CredencialesInvalidasException());

        LoginRequestDTO request = new LoginRequestDTO("alejandro@smartcash.com", "WrongPass");

        mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.error").exists());
    }

    @Test
    void e2eSolicitarRecuperacionPasswordRetorna200() throws Exception {
        RecuperarPasswordRequestDTO request = new RecuperarPasswordRequestDTO("alejandro@smartcash.com");

        mockMvc.perform(post("/auth/recuperar-password")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.mensaje").exists());

        verify(solicitarRecuperacionUseCase, times(1)).solicitar("alejandro@smartcash.com");
    }

    @Test
    void e2eRestablecerPasswordExitosoRetorna200() throws Exception {
        RestablecerPasswordRequestDTO request = new RestablecerPasswordRequestDTO("token-123", "NuevaPassword123*");

        mockMvc.perform(post("/auth/restablecer-password")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.mensaje").exists());

        verify(restablecerPasswordUseCase, times(1)).restablecer("token-123", "NuevaPassword123*");
    }

    @Test
    void e2eRestablecerPasswordTokenInvalidoRetorna400() throws Exception {
        doThrow(new TokenInvalidoException("Token expirado"))
                .when(restablecerPasswordUseCase).restablecer(anyString(), anyString());

        RestablecerPasswordRequestDTO request = new RestablecerPasswordRequestDTO("token-expirado", "NuevaPassword123*");

        mockMvc.perform(post("/auth/restablecer-password")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Token expirado"));
    }

    @Test
    void e2eBCryptPasswordEncoderCicloCompleto() {
        BCryptPasswordEncoderAdapter adapter = new BCryptPasswordEncoderAdapter();
        String hash = adapter.encriptar("MiClaveSegura2026");
        assertNotNull(hash);
        assertTrue(adapter.verificar("MiClaveSegura2026", hash));
        assertFalse(adapter.verificar("OtraClave", hash));
    }
}
