package com.smartcash.transacciones.infrastructure.persistence;

import jakarta.persistence.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

@Entity
@Table(name = "transacciones")
public class TransaccionJpaEntity {

    @Id
    @GeneratedValue
    @Column(name = "id_transaccion", updatable = false, nullable = false)
    private UUID idTransaccion;

    @Column(name = "id_usuario", nullable = false)
    private UUID idUsuario;

    @Column(name = "id_categoria")
    private UUID idCategoria;

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal monto;

    @Column(nullable = false)
    private LocalDate fecha;

    @Column(nullable = false, length = 150)
    private String comercio;

    @Column(name = "tipo_movimiento", nullable = false, length = 20)
    private String tipoMovimiento;

    @Column(name = "confianza_categorizacion")
    private Double confianzaCategorizacion;

    protected TransaccionJpaEntity() {}

    public TransaccionJpaEntity(UUID idTransaccion, UUID idUsuario, UUID idCategoria,
                                 BigDecimal monto, LocalDate fecha, String comercio,
                                 String tipoMovimiento, Double confianzaCategorizacion) {
        this.idTransaccion = idTransaccion;
        this.idUsuario = idUsuario;
        this.idCategoria = idCategoria;
        this.monto = monto;
        this.fecha = fecha;
        this.comercio = comercio;
        this.tipoMovimiento = tipoMovimiento;
        this.confianzaCategorizacion = confianzaCategorizacion;
    }

    public UUID getIdTransaccion() { return idTransaccion; }
    public UUID getIdUsuario() { return idUsuario; }
    public UUID getIdCategoria() { return idCategoria; }
    public BigDecimal getMonto() { return monto; }
    public LocalDate getFecha() { return fecha; }
    public String getComercio() { return comercio; }
    public String getTipoMovimiento() { return tipoMovimiento; }
    public Double getConfianzaCategorizacion() { return confianzaCategorizacion; }
}
