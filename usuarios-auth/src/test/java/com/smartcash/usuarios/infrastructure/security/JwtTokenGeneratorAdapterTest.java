package com.smartcash.usuarios.infrastructure.security;

import com.smartcash.usuarios.domain.model.Usuario;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class JwtTokenGeneratorAdapterTest {

    private final String secret = "12345678901234567890123456789012_secret_key_32_bytes";
    private final long expiracionMs = 3600000;
    private JwtTokenGeneratorAdapter tokenGenerator;

    @BeforeEach
    void setUp() {
        tokenGenerator = new JwtTokenGeneratorAdapter(secret, expiracionMs);
    }

    @Test
    void deberiaGenerarTokenJwtValidoConClaims() {
        UUID id = UUID.randomUUID();
        Usuario usuario = new Usuario(id, "Carlos Perez", "carlos@smartcash.com", "hash", LocalDateTime.now());

        String token = tokenGenerator.generarToken(usuario);

        assertNotNull(token);
        assertFalse(token.isBlank());

        SecretKey key = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
        Claims claims = Jwts.parser()
                .verifyWith(key)
                .build()
                .parseSignedClaims(token)
                .getPayload();

        assertEquals("carlos@smartcash.com", claims.getSubject());
        assertEquals(id.toString(), claims.get("id_usuario", String.class));
        assertEquals("Carlos Perez", claims.get("nombre", String.class));
        assertNotNull(claims.getExpiration());
        assertTrue(claims.getExpiration().getTime() > System.currentTimeMillis());
    }
}
