package com.smartcash.transacciones.domain.exceptions;

public class MetaAhorroInvalidaException extends RuntimeException {
    public MetaAhorroInvalidaException(String mensaje) {
        super(mensaje);
    }
}
