package com.smartcash.usuarios.infrastructure.security;

import com.smartcash.usuarios.domain.ports.out.PasswordEncoderPort;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Component;

/**
 * OCP en accion: si mañana queremos cambiar de BCrypt a Argon2, creamos
 * OTRO adaptador que implemente PasswordEncoderPort. Ninguna clase que
 * use el puerto (RegistrarUsuarioService, LoginService) se modifica.
 */
@Component
public class BCryptPasswordEncoderAdapter implements PasswordEncoderPort {

    private final BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();

    @Override
    public String encriptar(String passwordPlano) {
        return encoder.encode(passwordPlano);
    }

    @Override
    public boolean verificar(String passwordPlano, String passwordHash) {
        return encoder.matches(passwordPlano, passwordHash);
    }
}
