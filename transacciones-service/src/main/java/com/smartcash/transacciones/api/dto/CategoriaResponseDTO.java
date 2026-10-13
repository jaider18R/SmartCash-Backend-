package com.smartcash.transacciones.api.dto;

import java.util.UUID;

public record CategoriaResponseDTO(
        UUID idCategoria,
        String nombre,
        String color,
        String icono
) {}
