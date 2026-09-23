package com.smartcash.usuarios.infrastructure.persistence;

import jakarta.persistence.*;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Entidad JPA. Este es el "detalle" de infraestructura -- vive separada del
 * modelo de dominio (Usuario.java) a proposito. Si manana cambiamos de ORM
 * o de motor de base de datos, esta clase cambia y el dominio NO se entera.
 */
@Entity
@Table(name = "usuarios")
public class UsuarioJpaEntity {

    @Id
    @GeneratedValue
    @Column(name = "id_usuario", updatable = false, nullable = false)
    private UUID idUsuario;

    @Column(nullable = false, length = 100)
    private String nombre;

    @Column(nullable = false, unique = true, length = 150)
    private String correo;

    @Column(name = "password_hash", nullable = false, length = 255)
    private String passwordHash;

    @Column(name = "fecha_registro", nullable = false)
    private LocalDateTime fechaRegistro;

    protected UsuarioJpaEntity() {
        // constructor vacio requerido por JPA
    }

    public UsuarioJpaEntity(UUID idUsuario, String nombre, String correo,
                             String passwordHash, LocalDateTime fechaRegistro) {
        this.idUsuario = idUsuario;
        this.nombre = nombre;
        this.correo = correo;
        this.passwordHash = passwordHash;
        this.fechaRegistro = fechaRegistro;
    }

    public UUID getIdUsuario() { return idUsuario; }
    public String getNombre() { return nombre; }
    public String getCorreo() { return correo; }
    public String getPasswordHash() { return passwordHash; }
    public LocalDateTime getFechaRegistro() { return fechaRegistro; }
}
