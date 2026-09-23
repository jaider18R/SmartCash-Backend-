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

@RestController
@RequestMapping("/transacciones")
public class TransaccionController {

    private final RegistrarTransaccionUseCase registrarTransaccionUseCase;

    public TransaccionController(RegistrarTransaccionUseCase registrarTransaccionUseCase) {
        this.registrarTransaccionUseCase = registrarTransaccionUseCase;
    }

    @PostMapping
    public ResponseEntity<TransaccionResponseDTO> registrar(@Valid @RequestBody TransaccionRequestDTO request) {
        Transaccion transaccion = registrarTransaccionUseCase.registrar(
                request.idUsuario(),
                request.monto(),
                request.fecha(),
                request.comercio(),
                request.tipoMovimiento()
        );
        return ResponseEntity.status(HttpStatus.CREATED).body(TransaccionMapper.toResponseDTO(transaccion));
    }
}
