package com.smartcash.usuarios.domain.ports.out;

import com.smartcash.usuarios.domain.model.Usuario;

import java.util.Optional;

public interface RepositorioUsuarioPort {
    Usuario guardar(Usuario usuario);
    Optional<Usuario> buscarPorCorreo(String correo);
    Optional<Usuario> buscarPorId(java.util.UUID id);
    boolean existePorCorreo(String correo);
}
