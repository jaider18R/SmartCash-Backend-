package com.smartcash.usuarios.api.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record RecuperarPasswordRequestDTO(
        @NotBlank(message = "El correo es obligatorio")
        @Email(message = "El formato de correo no es valido")
        String correo
) {}
