package com.smartcash.usuarios.domain.ports.out;

import com.smartcash.usuarios.domain.model.PasswordResetToken;

import java.util.Optional;

public interface RepositorioTokenRecuperacionPort {
    PasswordResetToken guardar(PasswordResetToken token);
    Optional<PasswordResetToken> buscarPorToken(String token);
}
