package com.smartcash.usuarios.api.dto;

import java.time.LocalDateTime;
import java.util.UUID;

public record UsuarioResponseDTO(
        UUID idUsuario,
        String nombre,
        String correo,
        LocalDateTime fechaRegistro
) {}
