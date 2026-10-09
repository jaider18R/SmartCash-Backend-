package com.smartcash.transacciones.domain.ports.in;

import com.smartcash.transacciones.domain.model.Presupuesto;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

public interface GestionarPresupuestoUseCase {
    Presupuesto crearOActualizar(UUID idUsuario, UUID idCategoria, String nombreCategoria, BigDecimal montoLimite, int mes, int anio);
    List<Presupuesto> listarPorUsuario(UUID idUsuario);
}
