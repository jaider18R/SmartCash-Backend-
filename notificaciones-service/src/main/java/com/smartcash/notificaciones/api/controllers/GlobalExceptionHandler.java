package com.smartcash.notificaciones.api.controllers;

import com.smartcash.notificaciones.domain.ports.out.RegistroLogPort;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.TypeMismatchException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.ErrorResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @Autowired(required = false)
    private RegistroLogPort registroLog;

    private void registrar(String nivel, String mensaje, Exception ex, HttpServletRequest request, HttpStatus estado) {
        if (registroLog == null) {
            return;
        }
        try {
            String detalle = null;
            if (ex != null) {
                StringWriter sw = new StringWriter();
                ex.printStackTrace(new PrintWriter(sw));
                detalle = sw.toString();
                if (detalle.length() > 4000) {
                    detalle = detalle.substring(0, 4000);
                }
            }
            String usuario = request.getUserPrincipal() != null ? request.getUserPrincipal().getName() : null;
            registroLog.registrar(nivel, mensaje, detalle, request.getRequestURI(), request.getMethod(),
                    estado.value(), usuario);
        } catch (Exception ignorado) {
            // registrar un log nunca debe romper la respuesta al usuario
        }
    }

    /** Cualquier error no previsto: se registra y al usuario se le responde sin detalles tecnicos (RNF-03). */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String, String>> handleErrorNoControlado(Exception ex, HttpServletRequest request) {
        if (ex instanceof ErrorResponse er) {
            HttpStatus estado = HttpStatus.resolve(er.getStatusCode().value());
            String texto = estado != null ? estado.getReasonPhrase() : "Solicitud no valida";
            return ResponseEntity.status(er.getStatusCode()).body(Map.of("error", texto));
        }
        if (ex instanceof HttpMessageNotReadableException || ex instanceof TypeMismatchException) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("error", "Solicitud con formato invalido"));
        }
        registrar("ERROR", "Error no controlado: " + ex.getClass().getSimpleName() + ": " + ex.getMessage(),
                ex, request, HttpStatus.INTERNAL_SERVER_ERROR);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of("error", "Ocurrio un error interno. Intenta de nuevo mas tarde."));
    }
}
