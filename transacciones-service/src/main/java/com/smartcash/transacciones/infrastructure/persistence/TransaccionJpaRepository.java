package com.smartcash.transacciones.infrastructure.persistence;

import org.springframework.data.jpa.repository.JpaRepository;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

public interface TransaccionJpaRepository extends JpaRepository<TransaccionJpaEntity, UUID> {
    List<TransaccionJpaEntity> findByIdUsuario(UUID idUsuario);

    @Query("SELECT COALESCE(SUM(t.monto), 0) FROM TransaccionJpaEntity t WHERE t.idUsuario = :idUsuario AND t.idCategoria = :idCategoria AND t.tipoMovimiento = 'gasto' AND t.fecha BETWEEN :desde AND :hasta")
    BigDecimal sumMontoGastosPorUsuarioCategoriaYPeriodo(@Param("idUsuario") UUID idUsuario,
                                                         @Param("idCategoria") UUID idCategoria,
                                                         @Param("desde") LocalDate desde,
                                                         @Param("hasta") LocalDate hasta);
}

