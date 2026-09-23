package com.smartcash.usuarios.infrastructure.persistence;

import com.smartcash.usuarios.domain.model.Usuario;
import com.smartcash.usuarios.domain.ports.out.RepositorioUsuarioPort;
import org.springframework.stereotype.Component;

import java.util.Optional;

/**
 * Adaptador de salida: implementa el puerto RepositorioUsuarioPort usando JPA.
 * Traduce entre el modelo de dominio (Usuario) y la entidad de persistencia
 * (UsuarioJpaEntity). Es el UNICO lugar del sistema que conoce ambos mundos.
 */
@Component
public class UsuarioRepositoryAdapter implements RepositorioUsuarioPort {

    private final UsuarioJpaRepository jpaRepository;

    public UsuarioRepositoryAdapter(UsuarioJpaRepository jpaRepository) {
        this.jpaRepository = jpaRepository;
    }

    @Override
    public Usuario guardar(Usuario usuario) {
        UsuarioJpaEntity entity = new UsuarioJpaEntity(
                usuario.getId(),
                usuario.getNombre(),
                usuario.getCorreo(),
                usuario.getPasswordHash(),
                usuario.getFechaRegistro()
        );
        UsuarioJpaEntity guardado = jpaRepository.save(entity);
        return toDomain(guardado);
    }

    @Override
    public Optional<Usuario> buscarPorCorreo(String correo) {
        return jpaRepository.findByCorreo(correo).map(this::toDomain);
    }

    @Override
    public boolean existePorCorreo(String correo) {
        return jpaRepository.existsByCorreo(correo);
    }

    private Usuario toDomain(UsuarioJpaEntity entity) {
        return new Usuario(
                entity.getIdUsuario(),
                entity.getNombre(),
                entity.getCorreo(),
                entity.getPasswordHash(),
                entity.getFechaRegistro()
        );
    }
}
