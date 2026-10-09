package com.smartcash.transacciones.infrastructure.persistence;

import jakarta.persistence.*;

import java.math.BigDecimal;
import java.util.UUID;

@Entity
@Table(name = "presupuestos")
public class PresupuestoJpaEntity {

    @Id
    @GeneratedValue
    @Column(name = "id_presupuesto", updatable = false, nullable = false)
    private UUID idPresupuesto;

    @Column(name = "id_usuario", nullable = false)
    private UUID idUsuario;

    @Column(name = "id_categoria", nullable = false)
    private UUID idCategoria;

    @Column(name = "nombre_categoria", nullable = false, length = 100)
    private String nombreCategoria;

    @Column(name = "monto_limite", nullable = false, precision = 12, scale = 2)
    private BigDecimal montoLimite;

    @Column(nullable = false)
    private int mes;

    @Column(nullable = false)
    private int anio;

    protected PresupuestoJpaEntity() {}

    public PresupuestoJpaEntity(UUID idPresupuesto, UUID idUsuario, UUID idCategoria,
                                String nombreCategoria, BigDecimal montoLimite, int mes, int anio) {
        this.idPresupuesto = idPresupuesto;
        this.idUsuario = idUsuario;
        this.idCategoria = idCategoria;
        this.nombreCategoria = nombreCategoria;
        this.montoLimite = montoLimite;
        this.mes = mes;
        this.anio = anio;
    }

    public UUID getIdPresupuesto() { return idPresupuesto; }
    public UUID getIdUsuario() { return idUsuario; }
    public UUID getIdCategoria() { return idCategoria; }
    public String getNombreCategoria() { return nombreCategoria; }
    public BigDecimal getMontoLimite() { return montoLimite; }
    public int getMes() { return mes; }
    public int getAnio() { return anio; }
}
