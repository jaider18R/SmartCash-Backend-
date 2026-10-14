package com.smartcash.transacciones.api.controllers;

import com.smartcash.transacciones.api.dto.TransaccionRequestDTO;
import com.smartcash.transacciones.api.dto.TransaccionResponseDTO;
import com.smartcash.transacciones.api.mappers.TransaccionMapper;
import com.smartcash.transacciones.domain.model.Transaccion;
import com.smartcash.transacciones.domain.ports.in.RegistrarTransaccionUseCase;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/transacciones")
public class TransaccionController {

    private final RegistrarTransaccionUseCase registrarTransaccionUseCase;

    public TransaccionController(RegistrarTransaccionUseCase registrarTransaccionUseCase) {
        this.registrarTransaccionUseCase = registrarTransaccionUseCase;
    }

    @PostMapping
    public ResponseEntity<TransaccionResponseDTO> registrar(
            @Valid @RequestBody TransaccionRequestDTO request,
            org.springframework.security.core.Authentication authentication) {
        UUID idUsuario = (authentication != null && authentication.getPrincipal() instanceof String p && !p.isBlank())
                ? UUID.fromString(p)
                : request.idUsuario();

        Transaccion transaccion = registrarTransaccionUseCase.registrar(
                idUsuario,
                request.monto(),
                request.fecha(),
                request.comercio(),
                request.tipoMovimiento()
        );
        return ResponseEntity.status(HttpStatus.CREATED).body(TransaccionMapper.toResponseDTO(transaccion));
    }


    @GetMapping
    public ResponseEntity<java.util.List<TransaccionResponseDTO>> listar(
            @RequestParam(required = false) String tipo,
            @RequestParam(required = false) UUID categoriaId,
            @RequestParam(required = false) java.time.LocalDate desde,
            @RequestParam(required = false) java.time.LocalDate hasta,
            @RequestParam(required = false) String buscar,
            org.springframework.security.core.Authentication authentication) {
        UUID idUsuario = UUID.fromString((String) authentication.getPrincipal());
        
        java.util.List<TransaccionResponseDTO> transacciones = registrarTransaccionUseCase.listarPorUsuario(idUsuario)
                .stream()
                .filter(t -> tipo == null || t.getTipoMovimiento().equalsIgnoreCase(tipo))
                .filter(t -> categoriaId == null || categoriaId.equals(t.getIdCategoria()))
                .filter(t -> desde == null || !t.getFecha().isBefore(desde))
                .filter(t -> hasta == null || !t.getFecha().isAfter(hasta))
                .filter(t -> buscar == null || t.getComercio().toLowerCase().contains(buscar.toLowerCase()))
                .map(TransaccionMapper::toResponseDTO)
                .toList();
        return ResponseEntity.ok(transacciones);
    }

    @PutMapping("/{id}")
    public ResponseEntity<TransaccionResponseDTO> actualizar(
            @PathVariable UUID id,
            @Valid @RequestBody com.smartcash.transacciones.api.dto.ActualizarTransaccionRequestDTO request,
            org.springframework.security.core.Authentication authentication) {
        UUID idUsuario = UUID.fromString((String) authentication.getPrincipal());
        Transaccion actualizada = registrarTransaccionUseCase.actualizar(
                idUsuario,
                id,
                request.monto(),
                request.fecha(),
                request.comercio(),
                request.tipoMovimiento(),
                request.idCategoria()
        );
        return ResponseEntity.ok(TransaccionMapper.toResponseDTO(actualizada));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminar(
            @PathVariable UUID id,
            org.springframework.security.core.Authentication authentication) {
        UUID idUsuario = UUID.fromString((String) authentication.getPrincipal());
        registrarTransaccionUseCase.eliminar(idUsuario, id);
        return ResponseEntity.noContent().build();
    }
}


