package com.smartcash.transacciones.infrastructure.persistence;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface MetaAhorroJpaRepository extends JpaRepository<MetaAhorroJpaEntity, UUID> {
    List<MetaAhorroJpaEntity> findByIdUsuario(UUID idUsuario);
}
