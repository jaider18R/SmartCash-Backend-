package com.smartcash.usuarios.api.dto;

public record LoginResponseDTO(String token, String tipo) {
    public static LoginResponseDTO of(String token) {
        return new LoginResponseDTO(token, "Bearer");
    }
}
