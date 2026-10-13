package com.smartcash.transacciones.api.controllers;

import com.smartcash.transacciones.api.dto.CategoriaResponseDTO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/categorias")
@Tag(name = "Categorias", description = "Catalogo de categorias financieras")
public class CategoriaController {

    private static final List<CategoriaResponseDTO> CATEGORIAS = List.of(
            new CategoriaResponseDTO(UUID.fromString("00000000-0000-0000-0000-000000000001"), "Restaurantes y Comida", "#F59E0B", "utensils"),
            new CategoriaResponseDTO(UUID.fromString("00000000-0000-0000-0000-000000000002"), "Mercado y Supermercado", "#10B981", "shopping-cart"),
            new CategoriaResponseDTO(UUID.fromString("00000000-0000-0000-0000-000000000003"), "Servicios y Suscripciones", "#6366F1", "film"),
            new CategoriaResponseDTO(UUID.fromString("00000000-0000-0000-0000-000000000004"), "Transporte y Viajes", "#3B82F6", "car"),
            new CategoriaResponseDTO(UUID.fromString("00000000-0000-0000-0000-000000000005"), "Transferencias y Finanzas", "#8B5CF6", "credit-card"),
            new CategoriaResponseDTO(UUID.fromString("00000000-0000-0000-0000-000000000006"), "Salud y Farmacia", "#EF4444", "heart-pulse")
    );

    @GetMapping
    @Operation(summary = "Obtener el catalogo oficial de categorias con colores e iconos")
    public ResponseEntity<List<CategoriaResponseDTO>> listarCategorias() {
        return ResponseEntity.ok(CATEGORIAS);
    }
}
