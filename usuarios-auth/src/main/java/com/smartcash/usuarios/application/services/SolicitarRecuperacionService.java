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
    private final String frontendUrl;

    public SolicitarRecuperacionService(
            RepositorioUsuarioPort repositorioUsuario,
            RepositorioTokenRecuperacionPort repositorioToken,
            NotificacionClientPort notificacionClient,
            @org.springframework.beans.factory.annotation.Value("${smartcash.frontend.url:http://192.168.1.7:5173}") String frontendUrl) {
        this.repositorioUsuario = repositorioUsuario;
        this.repositorioToken = repositorioToken;
        this.notificacionClient = notificacionClient;
        this.frontendUrl = frontendUrl;
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

        String enlace = frontendUrl + "/?token=" + token;
        notificacionClient.enviarRecuperacionPassword(usuario.getCorreo(), usuario.getNombre(), enlace);
    }
}
