package com.smartcash.usuarios.api.dto;

import jakarta.validation.constraints.NotBlank;

public record LoginRequestDTO(
        @NotBlank String correo,
        @NotBlank String password
) {}
