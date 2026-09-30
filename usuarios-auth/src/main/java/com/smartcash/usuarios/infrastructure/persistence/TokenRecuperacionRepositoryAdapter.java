package com.smartcash.usuarios.infrastructure.persistence;

import com.smartcash.usuarios.domain.model.PasswordResetToken;
import com.smartcash.usuarios.domain.ports.out.RepositorioTokenRecuperacionPort;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
public class TokenRecuperacionRepositoryAdapter implements RepositorioTokenRecuperacionPort {

    private final PasswordResetTokenJpaRepository jpaRepository;

    public TokenRecuperacionRepositoryAdapter(PasswordResetTokenJpaRepository jpaRepository) {
        this.jpaRepository = jpaRepository;
    }

    @Override
    public PasswordResetToken guardar(PasswordResetToken token) {
        PasswordResetTokenJpaEntity entity = new PasswordResetTokenJpaEntity(
                token.getId(),
                token.getToken(),
                token.getIdUsuario(),
                token.getFechaExpiracion(),
                token.isUsado()
        );
        PasswordResetTokenJpaEntity guardado = jpaRepository.save(entity);
        return toDomain(guardado);
    }

    @Override
    public Optional<PasswordResetToken> buscarPorToken(String token) {
        return jpaRepository.findByToken(token).map(this::toDomain);
    }

    private PasswordResetToken toDomain(PasswordResetTokenJpaEntity entity) {
        return new PasswordResetToken(
                entity.getIdToken(),
                entity.getToken(),
                entity.getIdUsuario(),
                entity.getFechaExpiracion(),
                entity.isUsado()
        );
    }
}
