package com.smartcash.transacciones.api.controllers;

import com.smartcash.transacciones.api.dto.PresupuestoRequestDTO;
import com.smartcash.transacciones.api.dto.PresupuestoResponseDTO;
import com.smartcash.transacciones.domain.model.Presupuesto;
import com.smartcash.transacciones.domain.ports.in.GestionarPresupuestoUseCase;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/presupuestos")
@Tag(name = "Presupuestos", description = "Endpoints para la gestion de presupuestos mensuales por categoria")
public class PresupuestoController {

    private final GestionarPresupuestoUseCase gestionarPresupuestoUseCase;

    public PresupuestoController(GestionarPresupuestoUseCase gestionarPresupuestoUseCase) {
        this.gestionarPresupuestoUseCase = gestionarPresupuestoUseCase;
    }

    @PostMapping
    @Operation(summary = "Crear o actualizar un presupuesto mensual para una categoria")
    public ResponseEntity<PresupuestoResponseDTO> crearOActualizar(
            @Valid @RequestBody PresupuestoRequestDTO request,
            Authentication authentication) {
        UUID idUsuario = UUID.fromString((String) authentication.getPrincipal());
        Presupuesto presupuesto = gestionarPresupuestoUseCase.crearOActualizar(
                idUsuario,
                request.idCategoria(),
                request.nombreCategoria(),
                request.montoLimite(),
                request.mes(),
                request.anio()
        );
        return ResponseEntity.status(HttpStatus.CREATED).body(PresupuestoResponseDTO.de(presupuesto));
    }

    @GetMapping
    @Operation(summary = "Listar los presupuestos del usuario autenticado")
    public ResponseEntity<List<PresupuestoResponseDTO>> listar(Authentication authentication) {
        UUID idUsuario = UUID.fromString((String) authentication.getPrincipal());
        List<PresupuestoResponseDTO> respuesta = gestionarPresupuestoUseCase.listarPorUsuario(idUsuario).stream()
                .map(PresupuestoResponseDTO::de)
                .toList();
        return ResponseEntity.ok(respuesta);
    }
}
