package com.smartcash.usuarios.domain.ports.out;

import com.smartcash.usuarios.domain.model.Usuario;

public interface TokenGeneratorPort {
    String generarToken(Usuario usuario);
}
