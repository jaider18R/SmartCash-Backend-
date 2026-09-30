package com.smartcash.usuarios.infrastructure.persistence;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "tokens_recuperacion")
public class PasswordResetTokenJpaEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id_token")
    private UUID idToken;

    @Column(name = "token", nullable = false, unique = true)
    private String token;

    @Column(name = "id_usuario", nullable = false)
    private UUID idUsuario;

    @Column(name = "fecha_expiracion", nullable = false)
    private LocalDateTime fechaExpiracion;

    @Column(name = "usado", nullable = false)
    private boolean usado;

    public PasswordResetTokenJpaEntity() {}

    public PasswordResetTokenJpaEntity(UUID idToken, String token, UUID idUsuario, LocalDateTime fechaExpiracion, boolean usado) {
        this.idToken = idToken;
        this.token = token;
        this.idUsuario = idUsuario;
        this.fechaExpiracion = fechaExpiracion;
        this.usado = usado;
    }

    public UUID getIdToken() { return idToken; }
    public void setIdToken(UUID idToken) { this.idToken = idToken; }
    public String getToken() { return token; }
    public void setToken(String token) { this.token = token; }
    public UUID getIdUsuario() { return idUsuario; }
    public void setIdUsuario(UUID idUsuario) { this.idUsuario = idUsuario; }
    public LocalDateTime getFechaExpiracion() { return fechaExpiracion; }
    public void setFechaExpiracion(LocalDateTime fechaExpiracion) { this.fechaExpiracion = fechaExpiracion; }
    public boolean isUsado() { return usado; }
    public void setUsado(boolean usado) { this.usado = usado; }
}
