package com.smartcash.usuarios.application.services;

import com.smartcash.usuarios.domain.exceptions.UsuarioYaExisteException;
import com.smartcash.usuarios.domain.model.Usuario;
import com.smartcash.usuarios.domain.ports.in.RegistrarUsuarioUseCase;
import com.smartcash.usuarios.domain.ports.out.PasswordEncoderPort;
import com.smartcash.usuarios.domain.ports.out.RepositorioUsuarioPort;
import org.springframework.stereotype.Service;

/**
 * DIP: esta clase depende UNICAMENTE de interfaces (puertos), nunca de una
 * implementacion concreta (no conoce JPA, no conoce BCrypt directamente).
 * Spring inyecta en tiempo de ejecucion cual adaptador usar.
 *
 * SRP: la unica razon por la que esta clase cambia es si cambia la REGLA
 * de negocio de "como se registra un usuario" (por ejemplo, si se agrega
 * una validacion de dominio nueva).
 */
@Service
public class RegistrarUsuarioService implements RegistrarUsuarioUseCase {

    private final RepositorioUsuarioPort repositorioUsuario;
    private final PasswordEncoderPort passwordEncoder;

    public RegistrarUsuarioService(RepositorioUsuarioPort repositorioUsuario,
                                    PasswordEncoderPort passwordEncoder) {
        this.repositorioUsuario = repositorioUsuario;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public Usuario registrar(String nombre, String correo, String passwordPlano) {
        if (repositorioUsuario.existePorCorreo(correo)) {
            throw new UsuarioYaExisteException(correo);
        }
        String hash = passwordEncoder.encriptar(passwordPlano);
        Usuario nuevoUsuario = Usuario.nuevo(nombre, correo, hash);
        return repositorioUsuario.guardar(nuevoUsuario);
    }
}
