package com.smartcash.usuarios.api.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record RestablecerPasswordRequestDTO(
        @NotBlank(message = "El token es obligatorio")
        String token,

        @NotBlank(message = "La nueva contrasena es obligatoria")
        @Size(min = 8, message = "La nueva contrasena debe tener al menos 8 caracteres")
        String nuevaPassword
) {}
