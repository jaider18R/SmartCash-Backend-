package com.smartcash.usuarios.domain.ports.in;

public interface RestablecerPasswordUseCase {
    void restablecer(String token, String nuevaPassword);
}
