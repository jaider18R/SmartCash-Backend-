package com.smartcash.usuarios.domain.exceptions;

public class UsuarioYaExisteException extends RuntimeException {
    public UsuarioYaExisteException(String correo) {
        super("Ya existe un usuario registrado con el correo: " + correo);
    }
}
