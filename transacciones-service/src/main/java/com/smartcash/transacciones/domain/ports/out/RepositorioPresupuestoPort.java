package com.smartcash.transacciones.domain.ports.out;

import com.smartcash.transacciones.domain.model.Presupuesto;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface RepositorioPresupuestoPort {
    Presupuesto guardar(Presupuesto presupuesto);
    List<Presupuesto> listarPorUsuario(UUID idUsuario);
    Optional<Presupuesto> buscarPorUsuarioYCategoriaYPeriodo(UUID idUsuario, UUID idCategoria, int mes, int anio);
}
