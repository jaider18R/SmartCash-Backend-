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

/**
 * Capa API: SOLO traduce HTTP <-> casos de uso. No contiene logica de negocio.
 * SRP estricto: si la regla de negocio cambia, este archivo no se toca;
 * si cambia el formato de entrada/salida HTTP, este es el unico que se toca.
 */
@RestController
@RequestMapping("/auth")
public class AuthController {

    private final RegistrarUsuarioUseCase registrarUsuarioUseCase;
    private final LoginUseCase loginUseCase;

    public AuthController(RegistrarUsuarioUseCase registrarUsuarioUseCase,
                           LoginUseCase loginUseCase) {
        this.registrarUsuarioUseCase = registrarUsuarioUseCase;
        this.loginUseCase = loginUseCase;
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
}
