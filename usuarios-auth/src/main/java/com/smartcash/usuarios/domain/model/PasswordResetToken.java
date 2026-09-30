package com.smartcash.usuarios.domain.model;

import java.time.LocalDateTime;
import java.util.UUID;

public class PasswordResetToken {

    private final UUID id;
    private final String token;
    private final UUID idUsuario;
    private final LocalDateTime fechaExpiracion;
    private final boolean usado;

    public PasswordResetToken(UUID id, String token, UUID idUsuario, LocalDateTime fechaExpiracion, boolean usado) {
        this.id = id;
        this.token = token;
        this.idUsuario = idUsuario;
        this.fechaExpiracion = fechaExpiracion;
        this.usado = usado;
    }

    public static PasswordResetToken nuevo(UUID idUsuario, String token) {
        return new PasswordResetToken(null, token, idUsuario, LocalDateTime.now().plusMinutes(15), false);
    }

    public boolean esValido() {
        return !usado && LocalDateTime.now().isBefore(fechaExpiracion);
    }

    public PasswordResetToken marcarComoUsado() {
        return new PasswordResetToken(this.id, this.token, this.idUsuario, this.fechaExpiracion, true);
    }

    public UUID getId() { return id; }
    public String getToken() { return token; }
    public UUID getIdUsuario() { return idUsuario; }
    public LocalDateTime getFechaExpiracion() { return fechaExpiracion; }
    public boolean isUsado() { return usado; }
}
