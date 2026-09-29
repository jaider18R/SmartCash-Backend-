package com.smartcash.usuarios.application.services;

import com.smartcash.usuarios.domain.exceptions.UsuarioYaExisteException;
import com.smartcash.usuarios.domain.model.Usuario;
import com.smartcash.usuarios.domain.ports.in.RegistrarUsuarioUseCase;
import com.smartcash.usuarios.domain.ports.out.PasswordEncoderPort;
import com.smartcash.usuarios.domain.ports.out.RepositorioUsuarioPort;
import org.springframework.stereotype.Service;

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
