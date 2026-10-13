package com.smartcash.transacciones.domain.ports.out;

import com.smartcash.transacciones.domain.model.MetaAhorro;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface RepositorioMetaAhorroPort {
    MetaAhorro guardar(MetaAhorro metaAhorro);
    Optional<MetaAhorro> buscarPorId(UUID idMeta);
    List<MetaAhorro> listarPorUsuario(UUID idUsuario);
    void eliminar(UUID idMeta);
}
