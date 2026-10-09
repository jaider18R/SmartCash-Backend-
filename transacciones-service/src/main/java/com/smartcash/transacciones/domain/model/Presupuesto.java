package com.smartcash.transacciones.domain.model;

import java.math.BigDecimal;
import java.util.UUID;

public class Presupuesto {

    private final UUID id;
    private final UUID idUsuario;
    private final UUID idCategoria;
    private final String nombreCategoria;
    private final BigDecimal montoLimite;
    private final int mes;
    private final int anio;

    public Presupuesto(UUID id, UUID idUsuario, UUID idCategoria, String nombreCategoria, BigDecimal montoLimite, int mes, int anio) {
        this.id = id;
        this.idUsuario = idUsuario;
        this.idCategoria = idCategoria;
        this.nombreCategoria = nombreCategoria;
        this.montoLimite = montoLimite;
        this.mes = mes;
        this.anio = anio;
    }

    public static Presupuesto nuevo(UUID idUsuario, UUID idCategoria, String nombreCategoria, BigDecimal montoLimite, int mes, int anio) {
        return new Presupuesto(null, idUsuario, idCategoria, nombreCategoria, montoLimite, mes, anio);
    }

    public UUID getId() { return id; }
    public UUID getIdUsuario() { return idUsuario; }
    public UUID getIdCategoria() { return idCategoria; }
    public String getNombreCategoria() { return nombreCategoria; }
    public BigDecimal getMontoLimite() { return montoLimite; }
    public int getMes() { return mes; }
    public int getAnio() { return anio; }
}
