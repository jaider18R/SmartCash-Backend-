package com.smartcash.transacciones.domain.model;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

public class Transaccion {

    private final UUID id;
    private final UUID idUsuario;
    private final UUID idCategoria;          // puede ser null: aun no categorizada
    private final BigDecimal monto;
    private final LocalDate fecha;
    private final String comercio;
    private final String tipoMovimiento;      // "ingreso" | "gasto"
    private final Double confianzaCategorizacion; // puede ser null

    public Transaccion(UUID id, UUID idUsuario, UUID idCategoria, BigDecimal monto,
                        LocalDate fecha, String comercio, String tipoMovimiento,
                        Double confianzaCategorizacion) {
        this.id = id;
        this.idUsuario = idUsuario;
        this.idCategoria = idCategoria;
        this.monto = monto;
        this.fecha = fecha;
        this.comercio = comercio;
        this.tipoMovimiento = tipoMovimiento;
        this.confianzaCategorizacion = confianzaCategorizacion;
    }

    public static Transaccion nueva(UUID idUsuario, BigDecimal monto, LocalDate fecha,
                                     String comercio, String tipoMovimiento) {
        return new Transaccion(null, idUsuario, null, monto, fecha, comercio, tipoMovimiento, null);
    }

    /** Devuelve una copia inmutable con la categoria ya asignada (el dominio nunca "muta" en sitio). */
    public Transaccion conCategoria(UUID idCategoria, double confianza) {
        return new Transaccion(this.id, this.idUsuario, idCategoria, this.monto,
                this.fecha, this.comercio, this.tipoMovimiento, confianza);
    }

    public UUID getId() { return id; }
    public UUID getIdUsuario() { return idUsuario; }
    public UUID getIdCategoria() { return idCategoria; }
    public BigDecimal getMonto() { return monto; }
    public LocalDate getFecha() { return fecha; }
    public String getComercio() { return comercio; }
    public String getTipoMovimiento() { return tipoMovimiento; }
    public Double getConfianzaCategorizacion() { return confianzaCategorizacion; }
}
