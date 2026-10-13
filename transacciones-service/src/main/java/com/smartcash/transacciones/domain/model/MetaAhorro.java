package com.smartcash.transacciones.domain.model;

import com.smartcash.transacciones.domain.exceptions.MetaAhorroInvalidaException;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

public class MetaAhorro {

    private final UUID id;
    private final UUID idUsuario;
    private final String nombre;
    private final String descripcion;
    private final BigDecimal montoObjetivo;
    private final BigDecimal montoActual;
    private final LocalDate fechaLimite;
    private final String estado;

    public MetaAhorro(UUID id, UUID idUsuario, String nombre, String descripcion,
                      BigDecimal montoObjetivo, BigDecimal montoActual,
                      LocalDate fechaLimite, String estado) {
        this.id = id;
        this.idUsuario = idUsuario;
        this.nombre = nombre;
        this.descripcion = descripcion;
        this.montoObjetivo = montoObjetivo;
        this.montoActual = montoActual;
        this.fechaLimite = fechaLimite;
        this.estado = estado;
    }

    public static MetaAhorro crear(UUID idUsuario, String nombre, String descripcion,
                                   BigDecimal montoObjetivo, LocalDate fechaLimite) {
        if (idUsuario == null) {
            throw new MetaAhorroInvalidaException("El id_usuario es obligatorio");
        }
        if (nombre == null || nombre.isBlank()) {
            throw new MetaAhorroInvalidaException("El nombre de la meta es obligatorio");
        }
        if (montoObjetivo == null || montoObjetivo.compareTo(BigDecimal.ZERO) <= 0) {
            throw new MetaAhorroInvalidaException("El monto objetivo debe ser mayor a cero");
        }
        if (fechaLimite != null && fechaLimite.isBefore(LocalDate.now())) {
            throw new MetaAhorroInvalidaException("La fecha límite no puede estar en el pasado");
        }
        return new MetaAhorro(null, idUsuario, nombre, descripcion, montoObjetivo, BigDecimal.ZERO, fechaLimite, "EN_PROGRESO");
    }

    public MetaAhorro abonar(BigDecimal monto) {
        if (monto == null || monto.compareTo(BigDecimal.ZERO) <= 0) {
            throw new MetaAhorroInvalidaException("El monto a transferir debe ser mayor a cero");
        }
        BigDecimal nuevoMonto = this.montoActual.add(monto);
        String nuevoEstado = nuevoMonto.compareTo(this.montoObjetivo) >= 0 ? "COMPLETADA" : "EN_PROGRESO";
        return new MetaAhorro(this.id, this.idUsuario, this.nombre, this.descripcion,
                this.montoObjetivo, nuevoMonto, this.fechaLimite, nuevoEstado);
    }

    public MetaAhorro retirar(BigDecimal monto) {
        if (monto == null || monto.compareTo(BigDecimal.ZERO) <= 0) {
            throw new MetaAhorroInvalidaException("El monto a retirar debe ser mayor a cero");
        }
        if (monto.compareTo(this.montoActual) > 0) {
            throw new MetaAhorroInvalidaException("El monto a retirar no puede superar el saldo actual ahorrado");
        }
        BigDecimal nuevoMonto = this.montoActual.subtract(monto);
        String nuevoEstado = nuevoMonto.compareTo(this.montoObjetivo) >= 0 ? "COMPLETADA" : "EN_PROGRESO";
        return new MetaAhorro(this.id, this.idUsuario, this.nombre, this.descripcion,
                this.montoObjetivo, nuevoMonto, this.fechaLimite, nuevoEstado);
    }

    public UUID getId() { return id; }
    public UUID getIdUsuario() { return idUsuario; }
    public String getNombre() { return nombre; }
    public String getDescripcion() { return descripcion; }
    public BigDecimal getMontoObjetivo() { return montoObjetivo; }
    public BigDecimal getMontoActual() { return montoActual; }
    public LocalDate getFechaLimite() { return fechaLimite; }
    public String getEstado() { return estado; }
}
