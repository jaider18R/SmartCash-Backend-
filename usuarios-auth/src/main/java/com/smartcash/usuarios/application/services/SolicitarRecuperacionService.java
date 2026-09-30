package com.smartcash.usuarios.application.services;

import com.smartcash.usuarios.domain.model.PasswordResetToken;
import com.smartcash.usuarios.domain.model.Usuario;
import com.smartcash.usuarios.domain.ports.in.SolicitarRecuperacionUseCase;
import com.smartcash.usuarios.domain.ports.out.NotificacionClientPort;
import com.smartcash.usuarios.domain.ports.out.RepositorioTokenRecuperacionPort;
import com.smartcash.usuarios.domain.ports.out.RepositorioUsuarioPort;
import org.springframework.stereotype.Service;

import java.util.Optional;
import java.util.UUID;

@Service
public class SolicitarRecuperacionService implements SolicitarRecuperacionUseCase {

    private final RepositorioUsuarioPort repositorioUsuario;
    private final RepositorioTokenRecuperacionPort repositorioToken;
    private final NotificacionClientPort notificacionClient;

    public SolicitarRecuperacionService(
            RepositorioUsuarioPort repositorioUsuario,
            RepositorioTokenRecuperacionPort repositorioToken,
            NotificacionClientPort notificacionClient) {
        this.repositorioUsuario = repositorioUsuario;
        this.repositorioToken = repositorioToken;
        this.notificacionClient = notificacionClient;
    }

    @Override
    public void solicitar(String correo) {
        Optional<Usuario> usuarioOpt = repositorioUsuario.buscarPorCorreo(correo);
        if (usuarioOpt.isEmpty()) {
            return;
        }

        Usuario usuario = usuarioOpt.get();
        String token = UUID.randomUUID().toString();
        PasswordResetToken resetToken = PasswordResetToken.nuevo(usuario.getId(), token);
        repositorioToken.guardar(resetToken);

        String enlace = "http://localhost:3000/restablecer-password?token=" + token;
        notificacionClient.enviarRecuperacionPassword(usuario.getCorreo(), usuario.getNombre(), enlace);
    }
}
