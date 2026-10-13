package com.smartcash.transacciones.domain.ports.in;

import com.smartcash.transacciones.domain.model.MetaAhorro;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

public interface GestionarMetaAhorroUseCase {
    MetaAhorro crearMeta(UUID idUsuario, String nombre, String descripcion, BigDecimal montoObjetivo, LocalDate fechaLimite);
    List<MetaAhorro> listarPorUsuario(UUID idUsuario);
    MetaAhorro abonarMeta(UUID idUsuario, UUID idMeta, BigDecimal monto);
    MetaAhorro retirarMeta(UUID idUsuario, UUID idMeta, BigDecimal monto);
    void eliminarMeta(UUID idUsuario, UUID idMeta);
}
