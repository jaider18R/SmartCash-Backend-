package com.smartcash.usuarios.domain.ports.out;

public interface NotificacionClientPort {
    void enviarRecuperacionPassword(String correo, String nombre, String enlace);
}
