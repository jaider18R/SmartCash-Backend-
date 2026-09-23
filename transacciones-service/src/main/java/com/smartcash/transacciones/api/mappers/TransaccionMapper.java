package com.smartcash.transacciones.api.mappers;

import com.smartcash.transacciones.api.dto.TransaccionResponseDTO;
import com.smartcash.transacciones.domain.model.Transaccion;

public class TransaccionMapper {

    private TransaccionMapper() {}

    public static TransaccionResponseDTO toResponseDTO(Transaccion transaccion) {
        return new TransaccionResponseDTO(
                transaccion.getId(),
                transaccion.getIdUsuario(),
                transaccion.getIdCategoria(),
                transaccion.getMonto(),
                transaccion.getFecha(),
                transaccion.getComercio(),
                transaccion.getTipoMovimiento(),
                transaccion.getConfianzaCategorizacion()
        );
    }
}
