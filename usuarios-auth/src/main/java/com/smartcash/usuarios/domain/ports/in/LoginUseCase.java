package com.smartcash.usuarios.domain.ports.in;

public interface LoginUseCase {
    /**
     * @return token JWT firmado, listo para devolver al cliente.
     */
    String login(String correo, String passwordPlano);
}
