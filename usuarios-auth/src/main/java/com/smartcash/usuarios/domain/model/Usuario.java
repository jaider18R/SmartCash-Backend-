package com.smartcash.usuarios.domain.model;

import java.time.LocalDateTime;
import java.util.UUID;

public class Usuario {

    private final UUID id;
    private final String nombre;
    private final String correo;
    private final String passwordHash;
    private final LocalDateTime fechaRegistro;

    public Usuario(UUID id, String nombre, String correo, String passwordHash, LocalDateTime fechaRegistro) {
        this.id = id;
        this.nombre = nombre;
        this.correo = correo;
        this.passwordHash = passwordHash;
        this.fechaRegistro = fechaRegistro;
    }

    public static Usuario nuevo(String nombre, String correo, String passwordHash) {
        return new Usuario(null, nombre, correo, passwordHash, LocalDateTime.now());
    }

    public UUID getId() { return id; }
    public String getNombre() { return nombre; }
    public String getCorreo() { return correo; }
    public String getPasswordHash() { return passwordHash; }
    public LocalDateTime getFechaRegistro() { return fechaRegistro; }

    public Usuario conPasswordHash(String nuevoPasswordHash) {
        return new Usuario(this.id, this.nombre, this.correo, nuevoPasswordHash, this.fechaRegistro);
    }
}
