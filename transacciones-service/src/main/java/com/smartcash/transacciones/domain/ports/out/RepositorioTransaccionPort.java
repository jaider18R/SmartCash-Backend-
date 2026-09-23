package com.smartcash.transacciones.domain.ports.out;

import com.smartcash.transacciones.domain.model.Transaccion;

import java.util.List;
import java.util.UUID;

public interface RepositorioTransaccionPort {
    Transaccion guardar(Transaccion transaccion);
    List<Transaccion> listarPorUsuario(UUID idUsuario);
}
