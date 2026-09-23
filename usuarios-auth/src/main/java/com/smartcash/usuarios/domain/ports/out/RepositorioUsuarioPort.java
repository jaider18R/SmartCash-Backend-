package com.smartcash.usuarios.domain.ports.out;

import com.smartcash.usuarios.domain.model.Usuario;

import java.util.Optional;

/**
 * Puerto de salida (secundario). El dominio pide esto, sin saber si detras hay
 * PostgreSQL, MongoDB o un mapa en memoria. ISP: solo los metodos que el dominio
 * realmente necesita, nada de un "repositorio generico" sobrecargado.
 */
public interface RepositorioUsuarioPort {
    Usuario guardar(Usuario usuario);
    Optional<Usuario> buscarPorCorreo(String correo);
    boolean existePorCorreo(String correo);
}
