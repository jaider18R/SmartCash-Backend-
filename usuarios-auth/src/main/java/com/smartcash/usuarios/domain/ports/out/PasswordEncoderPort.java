package com.smartcash.usuarios.domain.ports.out;

public interface PasswordEncoderPort {
    String encriptar(String passwordPlano);
    boolean verificar(String passwordPlano, String passwordHash);
}
