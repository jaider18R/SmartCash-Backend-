package com.smartcash.transacciones.api.controllers;

import com.smartcash.transacciones.api.dto.MetaAhorroRequestDTO;
import com.smartcash.transacciones.api.dto.MetaAhorroResponseDTO;
import com.smartcash.transacciones.api.dto.MovimientoMetaRequestDTO;
import com.smartcash.transacciones.domain.model.MetaAhorro;
import com.smartcash.transacciones.domain.ports.in.GestionarMetaAhorroUseCase;
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
@RequestMapping("/api/metas-ahorro")
@Tag(name = "Metas de Ahorro", description = "Endpoints para la gestion de objetivos de ahorro y bolsillos virtuales")
public class MetaAhorroController {

    private final GestionarMetaAhorroUseCase gestionarMetaAhorroUseCase;

    public MetaAhorroController(GestionarMetaAhorroUseCase gestionarMetaAhorroUseCase) {
        this.gestionarMetaAhorroUseCase = gestionarMetaAhorroUseCase;
    }

    @PostMapping
    @Operation(summary = "Crear un nuevo objetivo o meta de ahorro")
    public ResponseEntity<MetaAhorroResponseDTO> crearMeta(
            @Valid @RequestBody MetaAhorroRequestDTO request,
            Authentication authentication) {
        UUID idUsuario = UUID.fromString((String) authentication.getPrincipal());
        MetaAhorro meta = gestionarMetaAhorroUseCase.crearMeta(
                idUsuario,
                request.nombre(),
                request.descripcion(),
                request.montoObjetivo(),
                request.fechaLimite()
        );
        return ResponseEntity.status(HttpStatus.CREATED).body(MetaAhorroResponseDTO.de(meta));
    }

    @GetMapping
    @Operation(summary = "Listar todas las metas de ahorro del usuario")
    public ResponseEntity<List<MetaAhorroResponseDTO>> listar(Authentication authentication) {
        UUID idUsuario = UUID.fromString((String) authentication.getPrincipal());
        List<MetaAhorroResponseDTO> respuesta = gestionarMetaAhorroUseCase.listarPorUsuario(idUsuario).stream()
                .map(MetaAhorroResponseDTO::de)
                .toList();
        return ResponseEntity.ok(respuesta);
    }

    @PostMapping("/{idMeta}/abonar")
    @Operation(summary = "Apartar o transferir dinero virtualmente hacia una meta de ahorro")
    public ResponseEntity<MetaAhorroResponseDTO> abonar(
            @PathVariable UUID idMeta,
            @Valid @RequestBody MovimientoMetaRequestDTO request,
            Authentication authentication) {
        UUID idUsuario = UUID.fromString((String) authentication.getPrincipal());
        MetaAhorro meta = gestionarMetaAhorroUseCase.abonarMeta(idUsuario, idMeta, request.monto());
        return ResponseEntity.ok(MetaAhorroResponseDTO.de(meta));
    }

    @PostMapping("/{idMeta}/retirar")
    @Operation(summary = "Retirar o desocupar dinero virtualmente de una meta de ahorro")
    public ResponseEntity<MetaAhorroResponseDTO> retirar(
            @PathVariable UUID idMeta,
            @Valid @RequestBody MovimientoMetaRequestDTO request,
            Authentication authentication) {
        UUID idUsuario = UUID.fromString((String) authentication.getPrincipal());
        MetaAhorro meta = gestionarMetaAhorroUseCase.retirarMeta(idUsuario, idMeta, request.monto());
        return ResponseEntity.ok(MetaAhorroResponseDTO.de(meta));
    }

    @DeleteMapping("/{idMeta}")
    @Operation(summary = "Eliminar una meta de ahorro")
    public ResponseEntity<Void> eliminar(
            @PathVariable UUID idMeta,
            Authentication authentication) {
        UUID idUsuario = UUID.fromString((String) authentication.getPrincipal());
        gestionarMetaAhorroUseCase.eliminarMeta(idUsuario, idMeta);
        return ResponseEntity.noContent().build();
    }
}
