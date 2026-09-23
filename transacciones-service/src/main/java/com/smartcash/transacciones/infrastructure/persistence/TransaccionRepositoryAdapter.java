package com.smartcash.transacciones.infrastructure.persistence;

import com.smartcash.transacciones.domain.model.Transaccion;
import com.smartcash.transacciones.domain.ports.out.RepositorioTransaccionPort;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.UUID;

@Component
public class TransaccionRepositoryAdapter implements RepositorioTransaccionPort {

    private final TransaccionJpaRepository jpaRepository;

    public TransaccionRepositoryAdapter(TransaccionJpaRepository jpaRepository) {
        this.jpaRepository = jpaRepository;
    }

    @Override
    public Transaccion guardar(Transaccion transaccion) {
        TransaccionJpaEntity entity = new TransaccionJpaEntity(
                transaccion.getId(),
                transaccion.getIdUsuario(),
                transaccion.getIdCategoria(),
                transaccion.getMonto(),
                transaccion.getFecha(),
                transaccion.getComercio(),
                transaccion.getTipoMovimiento(),
                transaccion.getConfianzaCategorizacion()
        );
        return toDomain(jpaRepository.save(entity));
    }

    @Override
    public List<Transaccion> listarPorUsuario(UUID idUsuario) {
        return jpaRepository.findByIdUsuario(idUsuario).stream()
                .map(this::toDomain)
                .toList();
    }

    private Transaccion toDomain(TransaccionJpaEntity entity) {
        return new Transaccion(
                entity.getIdTransaccion(),
                entity.getIdUsuario(),
                entity.getIdCategoria(),
                entity.getMonto(),
                entity.getFecha(),
                entity.getComercio(),
                entity.getTipoMovimiento(),
                entity.getConfianzaCategorizacion()
        );
    }
}
