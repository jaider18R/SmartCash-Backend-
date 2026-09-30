package com.smartcash.notificaciones.api.controllers;

import com.smartcash.notificaciones.api.dto.NotificacionEmailRequestDTO;
import com.smartcash.notificaciones.api.dto.NotificacionResponseDTO;
import com.smartcash.notificaciones.domain.model.ResultadoEnvio;
import com.smartcash.notificaciones.domain.ports.in.EnviarNotificacionUseCase;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/notificaciones")
@Tag(name = "Notificaciones", description = "Endpoints para el envio de correos y alertas del sistema")
public class NotificacionController {

    private final EnviarNotificacionUseCase enviarNotificacionUseCase;

    public NotificacionController(EnviarNotificacionUseCase enviarNotificacionUseCase) {
        this.enviarNotificacionUseCase = enviarNotificacionUseCase;
    }

    @PostMapping("/email")
    @Operation(summary = "Enviar correo electronico consumiendo API externa")
    public ResponseEntity<NotificacionResponseDTO> enviarEmail(@Valid @RequestBody NotificacionEmailRequestDTO request) {
        ResultadoEnvio resultado = enviarNotificacionUseCase.enviar(
                request.destinatario(),
                request.nombre(),
                request.tipo(),
                request.asunto(),
                request.parametros()
        );
        return ResponseEntity.ok(NotificacionResponseDTO.de(resultado));
    }

    @GetMapping("/health")
    @Operation(summary = "Verificar estado del microservicio de notificaciones")
    public ResponseEntity<Map<String, String>> health() {
        return ResponseEntity.ok(Map.of(
                "status", "UP",
                "service", "notificaciones-service",
                "port", "8083"
        ));
    }
}
