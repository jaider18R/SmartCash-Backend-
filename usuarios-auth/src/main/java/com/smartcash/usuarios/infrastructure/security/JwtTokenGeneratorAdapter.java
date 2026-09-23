package com.smartcash.usuarios.infrastructure.security;

import com.smartcash.usuarios.domain.model.Usuario;
import com.smartcash.usuarios.domain.ports.out.TokenGeneratorPort;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;

@Component
public class JwtTokenGeneratorAdapter implements TokenGeneratorPort {

    private final SecretKey secretKey;
    private final long expiracionMs;

    public JwtTokenGeneratorAdapter(
            @Value("${smartcash.jwt.secret}") String secret,
            @Value("${smartcash.jwt.expiracion-ms}") long expiracionMs) {
        this.secretKey = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
        this.expiracionMs = expiracionMs;
    }

    @Override
    public String generarToken(Usuario usuario) {
        Date ahora = new Date();
        Date expiracion = new Date(ahora.getTime() + expiracionMs);

        return Jwts.builder()
                .subject(usuario.getCorreo())
                .claim("id_usuario", usuario.getId().toString())
                .claim("nombre", usuario.getNombre())
                .issuedAt(ahora)
                .expiration(expiracion)
                .signWith(secretKey)
                .compact();
    }
}
