package com.smartcash.usuarios.domain.ports.in;

import com.smartcash.usuarios.domain.model.Usuario;

public interface RegistrarUsuarioUseCase {
    Usuario registrar(String nombre, String correo, String passwordPlano);
}
