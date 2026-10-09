package com.smartcash.transacciones.infrastructure.persistence;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface PresupuestoJpaRepository extends JpaRepository<PresupuestoJpaEntity, UUID> {
    List<PresupuestoJpaEntity> findByIdUsuario(UUID idUsuario);
    Optional<PresupuestoJpaEntity> findByIdUsuarioAndIdCategoriaAndMesAndAnio(UUID idUsuario, UUID idCategoria, int mes, int anio);
}
