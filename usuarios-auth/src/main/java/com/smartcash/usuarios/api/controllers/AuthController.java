package com.smartcash.usuarios.api.controllers;

import com.smartcash.usuarios.api.dto.*;
import com.smartcash.usuarios.api.mappers.UsuarioMapper;
import com.smartcash.usuarios.domain.model.Usuario;
import com.smartcash.usuarios.domain.ports.in.LoginUseCase;
import com.smartcash.usuarios.domain.ports.in.RegistrarUsuarioUseCase;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/auth")
public class AuthController {

    private final RegistrarUsuarioUseCase registrarUsuarioUseCase;
    private final LoginUseCase loginUseCase;
    private final com.smartcash.usuarios.domain.ports.in.SolicitarRecuperacionUseCase solicitarRecuperacionUseCase;
    private final com.smartcash.usuarios.domain.ports.in.RestablecerPasswordUseCase restablecerPasswordUseCase;

    public AuthController(RegistrarUsuarioUseCase registrarUsuarioUseCase,
                           LoginUseCase loginUseCase,
                           com.smartcash.usuarios.domain.ports.in.SolicitarRecuperacionUseCase solicitarRecuperacionUseCase,
                           com.smartcash.usuarios.domain.ports.in.RestablecerPasswordUseCase restablecerPasswordUseCase) {
        this.registrarUsuarioUseCase = registrarUsuarioUseCase;
        this.loginUseCase = loginUseCase;
        this.solicitarRecuperacionUseCase = solicitarRecuperacionUseCase;
        this.restablecerPasswordUseCase = restablecerPasswordUseCase;
    }

    @PostMapping("/registro")
    public ResponseEntity<UsuarioResponseDTO> registrar(@Valid @RequestBody RegistroRequestDTO request) {
        Usuario usuario = registrarUsuarioUseCase.registrar(
                request.nombre(), request.correo(), request.password());
        return ResponseEntity.status(HttpStatus.CREATED).body(UsuarioMapper.toResponseDTO(usuario));
    }

    @PostMapping("/login")
    public ResponseEntity<LoginResponseDTO> login(@Valid @RequestBody LoginRequestDTO request) {
        String token = loginUseCase.login(request.correo(), request.password());
        return ResponseEntity.ok(LoginResponseDTO.of(token));
    }

    @PostMapping("/recuperar-password")
    public ResponseEntity<MensajeResponseDTO> solicitarRecuperacion(@Valid @RequestBody RecuperarPasswordRequestDTO request) {
        solicitarRecuperacionUseCase.solicitar(request.correo());
        return ResponseEntity.ok(new MensajeResponseDTO("Si el correo esta registrado, recibiras las instrucciones en tu bandeja de entrada."));
    }

    @PostMapping("/restablecer-password")
    public ResponseEntity<MensajeResponseDTO> restablecerPassword(@Valid @RequestBody RestablecerPasswordRequestDTO request) {
        restablecerPasswordUseCase.restablecer(request.token(), request.nuevaPassword());
        return ResponseEntity.ok(new MensajeResponseDTO("Contrasena actualizada con exito. Ya puedes iniciar sesion."));
    }
}
