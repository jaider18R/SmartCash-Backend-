package com.smartcash.usuarios.domain.ports.in;

import com.smartcash.usuarios.domain.model.Usuario;

/**
 * Puerto de entrada (primario). Define QUE hace el sistema, no COMO.
 * SRP: esta interfaz tiene una unica razon de cambio -> la regla de "registrar usuario".
 */
public interface RegistrarUsuarioUseCase {
    Usuario registrar(String nombre, String correo, String passwordPlano);
}
