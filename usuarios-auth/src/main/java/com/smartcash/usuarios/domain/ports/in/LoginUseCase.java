package com.smartcash.usuarios.domain.ports.in;

public interface LoginUseCase {

    String login(String correo, String passwordPlano);
}
