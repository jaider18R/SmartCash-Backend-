package com.smartcash.transacciones.application.services;

import com.smartcash.transacciones.domain.exceptions.MetaAhorroInvalidaException;
import com.smartcash.transacciones.domain.model.MetaAhorro;
import com.smartcash.transacciones.domain.ports.in.GestionarMetaAhorroUseCase;
import com.smartcash.transacciones.domain.ports.out.RepositorioMetaAhorroPort;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Service
public class MetaAhorroService implements GestionarMetaAhorroUseCase {

    private final RepositorioMetaAhorroPort repositorioMetaAhorro;

    public MetaAhorroService(RepositorioMetaAhorroPort repositorioMetaAhorro) {
        this.repositorioMetaAhorro = repositorioMetaAhorro;
    }

    @Override
    public MetaAhorro crearMeta(UUID idUsuario, String nombre, String descripcion,
                                BigDecimal montoObjetivo, LocalDate fechaLimite) {
        MetaAhorro nueva = MetaAhorro.crear(idUsuario, nombre, descripcion, montoObjetivo, fechaLimite);
        return repositorioMetaAhorro.guardar(nueva);
    }

    @Override
    public List<MetaAhorro> listarPorUsuario(UUID idUsuario) {
        return repositorioMetaAhorro.listarPorUsuario(idUsuario);
    }

    @Override
    public MetaAhorro abonarMeta(UUID idUsuario, UUID idMeta, BigDecimal monto) {
        MetaAhorro meta = obtenerMetaYValidarUsuario(idUsuario, idMeta);
        MetaAhorro actualizada = meta.abonar(monto);
        return repositorioMetaAhorro.guardar(actualizada);
    }

    @Override
    public MetaAhorro retirarMeta(UUID idUsuario, UUID idMeta, BigDecimal monto) {
        MetaAhorro meta = obtenerMetaYValidarUsuario(idUsuario, idMeta);
        MetaAhorro actualizada = meta.retirar(monto);
        return repositorioMetaAhorro.guardar(actualizada);
    }

    @Override
    public void eliminarMeta(UUID idUsuario, UUID idMeta) {
        obtenerMetaYValidarUsuario(idUsuario, idMeta);
        repositorioMetaAhorro.eliminar(idMeta);
    }

    private MetaAhorro obtenerMetaYValidarUsuario(UUID idUsuario, UUID idMeta) {
        MetaAhorro meta = repositorioMetaAhorro.buscarPorId(idMeta)
                .orElseThrow(() -> new MetaAhorroInvalidaException("Meta de ahorro no encontrada"));

        if (!meta.getIdUsuario().equals(idUsuario)) {
            throw new MetaAhorroInvalidaException("No tiene permisos para modificar esta meta");
        }

        return meta;
    }
}
