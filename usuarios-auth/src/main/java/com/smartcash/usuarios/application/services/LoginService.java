package com.smartcash.usuarios.application.services;

import com.smartcash.usuarios.domain.exceptions.CredencialesInvalidasException;
import com.smartcash.usuarios.domain.model.Usuario;
import com.smartcash.usuarios.domain.ports.in.LoginUseCase;
import com.smartcash.usuarios.domain.ports.out.PasswordEncoderPort;
import com.smartcash.usuarios.domain.ports.out.RepositorioUsuarioPort;
import com.smartcash.usuarios.domain.ports.out.TokenGeneratorPort;
import org.springframework.stereotype.Service;

@Service
public class LoginService implements LoginUseCase {

    private final RepositorioUsuarioPort repositorioUsuario;
    private final PasswordEncoderPort passwordEncoder;
    private final TokenGeneratorPort tokenGenerator;

    public LoginService(RepositorioUsuarioPort repositorioUsuario,
                         PasswordEncoderPort passwordEncoder,
                         TokenGeneratorPort tokenGenerator) {
        this.repositorioUsuario = repositorioUsuario;
        this.passwordEncoder = passwordEncoder;
        this.tokenGenerator = tokenGenerator;
    }

    @Override
    public String login(String correo, String passwordPlano) {
        Usuario usuario = repositorioUsuario.buscarPorCorreo(correo)
                .orElseThrow(CredencialesInvalidasException::new);

        if (!passwordEncoder.verificar(passwordPlano, usuario.getPasswordHash())) {
            throw new CredencialesInvalidasException();
        }

        return tokenGenerator.generarToken(usuario);
    }
}
