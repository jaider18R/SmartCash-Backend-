package com.smartcash.transacciones.infrastructure.persistence;

import com.smartcash.transacciones.domain.model.Presupuesto;
import com.smartcash.transacciones.domain.ports.out.RepositorioPresupuestoPort;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Component
public class PresupuestoRepositoryAdapter implements RepositorioPresupuestoPort {

    private final PresupuestoJpaRepository jpaRepository;

    public PresupuestoRepositoryAdapter(PresupuestoJpaRepository jpaRepository) {
        this.jpaRepository = jpaRepository;
    }

    @Override
    public Presupuesto guardar(Presupuesto presupuesto) {
        PresupuestoJpaEntity entity = new PresupuestoJpaEntity(
                presupuesto.getId(),
                presupuesto.getIdUsuario(),
                presupuesto.getIdCategoria(),
                presupuesto.getNombreCategoria(),
                presupuesto.getMontoLimite(),
                presupuesto.getMes(),
                presupuesto.getAnio()
        );
        return toDomain(jpaRepository.save(entity));
    }

    @Override
    public List<Presupuesto> listarPorUsuario(UUID idUsuario) {
        return jpaRepository.findByIdUsuario(idUsuario).stream()
                .map(this::toDomain)
                .toList();
    }

    @Override
    public Optional<Presupuesto> buscarPorUsuarioYCategoriaYPeriodo(UUID idUsuario, UUID idCategoria, int mes, int anio) {
        return jpaRepository.findByIdUsuarioAndIdCategoriaAndMesAndAnio(idUsuario, idCategoria, mes, anio)
                .map(this::toDomain);
    }

    private Presupuesto toDomain(PresupuestoJpaEntity entity) {
        return new Presupuesto(
                entity.getIdPresupuesto(),
                entity.getIdUsuario(),
                entity.getIdCategoria(),
                entity.getNombreCategoria(),
                entity.getMontoLimite(),
                entity.getMes(),
                entity.getAnio()
        );
    }
}
