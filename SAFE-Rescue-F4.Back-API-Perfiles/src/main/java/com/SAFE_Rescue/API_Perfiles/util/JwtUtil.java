package com.SAFE_Rescue.API_Perfiles.util;

import io.jsonwebtoken.Jwts; // <-- NECESARIA
import io.jsonwebtoken.SignatureAlgorithm; // <-- NECESARIA
import io.jsonwebtoken.security.Keys; // <-- RECOMENDADO para manejo de claves
import org.springframework.stereotype.Component;

import java.security.Key;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

@Component
public class JwtUtil {

    private final Key signingKey = Keys.secretKeyFor(SignatureAlgorithm.HS256); // O usar la cadena de arriba:

    // Tiempo de vida del token en milisegundos (10 horas)
    private final long JWT_TOKEN_VALIDITY = 1000 * 60 * 60 * 10;

    /**
     * Genera un Token JWT para el usuario especificado.
     */
    public String generateToken(Integer id, String tipoPerfil) {

        // Define los claims (id, tipoPerfil, etc.)
        Map<String, Object> claims = new HashMap<>();
        claims.put("tipoPerfil", tipoPerfil);

        return Jwts.builder()
                .setClaims(claims)
                .setSubject(id.toString()) // El ID del usuario (el 'subject' del token)
                .setIssuedAt(new Date(System.currentTimeMillis()))
                .setExpiration(new Date(System.currentTimeMillis() + JWT_TOKEN_VALIDITY))
                .signWith(signingKey, SignatureAlgorithm.HS256) // Usar el objeto Key
                .compact();
    }

    // ----------------------------------------------------------------------
    // ⭐ OBJETOS ADICIONALES NECESARIOS (Mínimo)
    // ----------------------------------------------------------------------

    /**
     * Obtiene los claims (cuerpo) del token.
     */
    public io.jsonwebtoken.Claims extractAllClaims(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(signingKey)
                .build()
                .parseClaimsJws(token)
                .getBody();
    }

    /**
     * Valida si el token es válido (no expirado y correctamente firmado).
     */
    public Boolean validateToken(String token, Integer userId) {
        final Integer tokenUserId = Integer.parseInt(extractAllClaims(token).getSubject());
        final Date expiration = extractAllClaims(token).getExpiration();

        return (tokenUserId.equals(userId) && expiration.after(new Date()));
    }
}