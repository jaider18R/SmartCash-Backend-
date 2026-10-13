package com.smartcash.transacciones.infrastructure.persistence;

import com.smartcash.transacciones.domain.model.MetaAhorro;
import com.smartcash.transacciones.domain.ports.out.RepositorioMetaAhorroPort;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Component
public class MetaAhorroRepositoryAdapter implements RepositorioMetaAhorroPort {

    private final MetaAhorroJpaRepository jpaRepository;

    public MetaAhorroRepositoryAdapter(MetaAhorroJpaRepository jpaRepository) {
        this.jpaRepository = jpaRepository;
    }

    @Override
    public MetaAhorro guardar(MetaAhorro metaAhorro) {
        MetaAhorroJpaEntity entity = new MetaAhorroJpaEntity(
                metaAhorro.getId(),
                metaAhorro.getIdUsuario(),
                metaAhorro.getNombre(),
                metaAhorro.getDescripcion(),
                metaAhorro.getMontoObjetivo(),
                metaAhorro.getMontoActual(),
                metaAhorro.getFechaLimite(),
                metaAhorro.getEstado()
        );
        return toDomain(jpaRepository.save(entity));
    }

    @Override
    public Optional<MetaAhorro> buscarPorId(UUID idMeta) {
        return jpaRepository.findById(idMeta).map(this::toDomain);
    }

    @Override
    public List<MetaAhorro> listarPorUsuario(UUID idUsuario) {
        return jpaRepository.findByIdUsuario(idUsuario).stream()
                .map(this::toDomain)
                .toList();
    }

    @Override
    public void eliminar(UUID idMeta) {
        jpaRepository.deleteById(idMeta);
    }

    private MetaAhorro toDomain(MetaAhorroJpaEntity entity) {
        return new MetaAhorro(
                entity.getIdMeta(),
                entity.getIdUsuario(),
                entity.getNombre(),
                entity.getDescripcion(),
                entity.getMontoObjetivo(),
                entity.getMontoActual(),
                entity.getFechaLimite(),
                entity.getEstado()
        );
    }
}
