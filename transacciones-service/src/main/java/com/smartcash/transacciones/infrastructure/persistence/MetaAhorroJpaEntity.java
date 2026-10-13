package com.smartcash.transacciones.infrastructure.persistence;

import jakarta.persistence.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

@Entity
@Table(name = "metas_ahorro")
public class MetaAhorroJpaEntity {

    @Id
    @GeneratedValue
    @Column(name = "id_meta", updatable = false, nullable = false)
    private UUID idMeta;

    @Column(name = "id_usuario", nullable = false)
    private UUID idUsuario;

    @Column(nullable = false, length = 150)
    private String nombre;

    @Column(length = 255)
    private String descripcion;

    @Column(name = "monto_objetivo", nullable = false, precision = 12, scale = 2)
    private BigDecimal montoObjetivo;

    @Column(name = "monto_actual", nullable = false, precision = 12, scale = 2)
    private BigDecimal montoActual;

    @Column(name = "fecha_limite")
    private LocalDate fechaLimite;

    @Column(nullable = false, length = 30)
    private String estado;

    protected MetaAhorroJpaEntity() {}

    public MetaAhorroJpaEntity(UUID idMeta, UUID idUsuario, String nombre, String descripcion,
                               BigDecimal montoObjetivo, BigDecimal montoActual,
                               LocalDate fechaLimite, String estado) {
        this.idMeta = idMeta;
        this.idUsuario = idUsuario;
        this.nombre = nombre;
        this.descripcion = descripcion;
        this.montoObjetivo = montoObjetivo;
        this.montoActual = montoActual;
        this.fechaLimite = fechaLimite;
        this.estado = estado;
    }

    public UUID getIdMeta() { return idMeta; }
    public UUID getIdUsuario() { return idUsuario; }
    public String getNombre() { return nombre; }
    public String getDescripcion() { return descripcion; }
    public BigDecimal getMontoObjetivo() { return montoObjetivo; }
    public BigDecimal getMontoActual() { return montoActual; }
    public LocalDate getFechaLimite() { return fechaLimite; }
    public String getEstado() { return estado; }
}
