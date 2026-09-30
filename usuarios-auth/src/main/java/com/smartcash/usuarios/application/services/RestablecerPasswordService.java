package com.smartcash.usuarios.application.services;

import com.smartcash.usuarios.domain.exceptions.TokenInvalidoException;
import com.smartcash.usuarios.domain.model.PasswordResetToken;
import com.smartcash.usuarios.domain.model.Usuario;
import com.smartcash.usuarios.domain.ports.in.RestablecerPasswordUseCase;
import com.smartcash.usuarios.domain.ports.out.PasswordEncoderPort;
import com.smartcash.usuarios.domain.ports.out.RepositorioTokenRecuperacionPort;
import com.smartcash.usuarios.domain.ports.out.RepositorioUsuarioPort;
import org.springframework.stereotype.Service;

@Service
public class RestablecerPasswordService implements RestablecerPasswordUseCase {

    private final RepositorioTokenRecuperacionPort repositorioToken;
    private final RepositorioUsuarioPort repositorioUsuario;
    private final PasswordEncoderPort passwordEncoder;

    public RestablecerPasswordService(
            RepositorioTokenRecuperacionPort repositorioToken,
            RepositorioUsuarioPort repositorioUsuario,
            PasswordEncoderPort passwordEncoder) {
        this.repositorioToken = repositorioToken;
        this.repositorioUsuario = repositorioUsuario;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void restablecer(String token, String nuevaPassword) {
        PasswordResetToken resetToken = repositorioToken.buscarPorToken(token)
                .orElseThrow(() -> new TokenInvalidoException("El enlace de recuperacion es invalido o ya expiro"));

        if (!resetToken.esValido()) {
            throw new TokenInvalidoException("El enlace de recuperacion es invalido o ya expiro");
        }

        Usuario usuario = repositorioUsuario.buscarPorId(resetToken.getIdUsuario())
                .orElseThrow(() -> new TokenInvalidoException("Usuario no encontrado"));

        String nuevoHash = passwordEncoder.encriptar(nuevaPassword);
        Usuario usuarioActualizado = usuario.conPasswordHash(nuevoHash);

        repositorioUsuario.guardar(usuarioActualizado);
        repositorioToken.guardar(resetToken.marcarComoUsado());
    }
}
