package com.SAFE_Rescue.API_Perfiles.util;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.security.Key;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Base64;

@Component
public class JwtUtil {

    // INYECTAR LA CLAVE DESDE application.properties
    @Value("${jwt.secret}")
    private String jwtSecret;

    // Tiempo de vida del token en milisegundos (10 horas)
    private final long JWT_TOKEN_VALIDITY = 1000 * 60 * 60 * 10;

    /**
     * Obtiene la clave de firma (usando la clave del properties)
     */
    private Key getSigningKey() {
        //  Decodificar la clave base64 y convertir a Key
        byte[] decodedKey = Base64.getDecoder().decode(jwtSecret);
        return Keys.hmacShaKeyFor(decodedKey);
    }

    /**
     * Genera un Token JWT para el usuario especificado.
     */
    public String generateToken(Integer id, String tipoPerfil) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("tipoPerfil", tipoPerfil);

        System.out.println(" Generando token JWT para userId: " + id + ", tipoPerfil: " + tipoPerfil);

        return Jwts.builder()
                .setClaims(claims)
                .setSubject(id.toString())
                .setIssuedAt(new Date(System.currentTimeMillis()))
                .setExpiration(new Date(System.currentTimeMillis() + JWT_TOKEN_VALIDITY))
                .signWith(getSigningKey(), SignatureAlgorithm.HS256)
                .compact();
    }

    /**
     * Obtiene los claims del token.
     */
    public Claims extractAllClaims(String token) {
        try {
            return Jwts.parserBuilder()
                    .setSigningKey(getSigningKey())
                    .build()
                    .parseClaimsJws(token)
                    .getBody();
        } catch (JwtException e) {
            System.err.println(" Error extrayendo claims: " + e.getMessage());
            throw e;
        }
    }

    /**
     * Extrae el subject (userId) del token
     */
    public String extractSubject(String token) {
        return extractAllClaims(token).getSubject();
    }

    /**
     * Valida el token con userId específico
     */
    public boolean validateToken(String token, int userId) {
        try {
            Claims claims = extractAllClaims(token);

            String tokenUserId = claims.getSubject();

            if (!tokenUserId.equals(String.valueOf(userId))) {
                System.err.println(" userId no coincide. Token: " + tokenUserId + ", Esperado: " + userId);
                return false;
            }

            if (claims.getExpiration().before(new Date())) {
                System.err.println(" Token expirado");
                return false;
            }

            System.out.println(" Token válido para userId: " + userId);
            return true;
        } catch (ExpiredJwtException e) {
            System.err.println(" Token expirado: " + e.getMessage());
            return false;
        } catch (JwtException e) {
            System.err.println(" JWT inválido: " + e.getMessage());
            return false;
        }
    }

    /**
     * Valida el token sin verificar userId
     */
    public boolean validateToken(String token) {
        try {
            Jwts.parserBuilder()
                    .setSigningKey(getSigningKey())
                    .build()
                    .parseClaimsJws(token);
            System.out.println(" Token JWT válido");
            return true;
        } catch (ExpiredJwtException e) {
            System.err.println(" Token expirado");
            return false;
        } catch (JwtException e) {
            System.err.println(" JWT inválido: " + e.getMessage());
            return false;
        }
    }
}