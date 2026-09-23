package com.smartcash.usuarios.api.mappers;

import com.smartcash.usuarios.api.dto.UsuarioResponseDTO;
import com.smartcash.usuarios.domain.model.Usuario;

public class UsuarioMapper {

    private UsuarioMapper() {}

    public static UsuarioResponseDTO toResponseDTO(Usuario usuario) {
        return new UsuarioResponseDTO(
                usuario.getId(),
                usuario.getNombre(),
                usuario.getCorreo(),
                usuario.getFechaRegistro()
        );
    }
}
