package com.smartcash.transacciones.domain.exceptions;

public class TransaccionInvalidaException extends RuntimeException {
    public TransaccionInvalidaException(String mensaje) {
        super(mensaje);
    }
}
