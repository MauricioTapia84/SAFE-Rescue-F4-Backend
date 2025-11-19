package com.SAFE_Rescue.API_Perfiles.util;

import java.lang.reflect.Field;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import java.util.Date;
import java.util.HashMap;
import java.security.Key;
import io.jsonwebtoken.security.SignatureException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

// No necesitamos @ExtendWith(MockitoExtension.class) ya que no hay mocks
public class JwtUtilTest {

    private JwtUtil jwtUtil;
    private final Integer USER_ID = 101;
    private final String TIPO_PERFIL = "BOMBERO";

    @BeforeEach
    void setUp() {
        // Inicializamos la instancia real para probarla
        jwtUtil = new JwtUtil();

    }

    // =================================================================
    // PRUEBAS DE GENERACIÓN DE TOKEN
    // =================================================================

    @Test
    void generateToken_ShouldReturnValidToken() {
        // Ejecución
        String token = jwtUtil.generateToken(USER_ID, TIPO_PERFIL);

        // Verificación 1: El token no debe ser nulo ni vacío
        assertNotNull(token);
        assertFalse(token.isEmpty());

        // Verificación 2: El token debe tener 3 partes (header, payload, signature)
        assertEquals(3, token.split("\\.").length, "Un JWT debe tener 3 partes separadas por puntos.");
    }

    @Test
    void extractAllClaims_ShouldContainCorrectClaims() {
        // 1. Generar token
        String token = jwtUtil.generateToken(USER_ID, TIPO_PERFIL);

        // 2. Extraer claims
        io.jsonwebtoken.Claims claims = jwtUtil.extractAllClaims(token);

        // Verificación 1: Subject (ID del usuario)
        assertEquals(USER_ID.toString(), claims.getSubject(), "El subject debe coincidir con el ID del usuario.");

        // Verificación 2: Custom Claim (Tipo de Perfil)
        assertEquals(TIPO_PERFIL, claims.get("tipoPerfil"), "El claim 'tipoPerfil' debe ser correcto.");

        // Verificación 3: Fecha de emisión (Issued At)
        assertNotNull(claims.getIssuedAt(), "Debe tener fecha de emisión.");

        // Verificación 4: Fecha de expiración (Expiration)
        assertNotNull(claims.getExpiration(), "Debe tener fecha de expiración.");
        assertTrue(claims.getExpiration().after(new Date()), "El token debe expirar en el futuro.");
    }

    // =================================================================
    // PRUEBAS DE VALIDACIÓN DE TOKEN
    // =================================================================

    @Test
    void validateToken_ShouldReturnTrueForValidTokenAndUser() {
        // Generar un token válido
        String token = jwtUtil.generateToken(USER_ID, TIPO_PERFIL);

        // Validar el token contra el mismo ID
        Boolean isValid = jwtUtil.validateToken(token, USER_ID);

        // Verificación
        assertTrue(isValid, "Un token recién generado y válido debe pasar la validación.");
    }

    @Test
    void validateToken_ShouldReturnFalseForMismatchedUserId() {
        // Generar token para USER_ID (101)
        String token = jwtUtil.generateToken(USER_ID, TIPO_PERFIL);

        // Intentar validar contra un ID diferente (102)
        Boolean isValid = jwtUtil.validateToken(token, 102);

        // Verificación
        assertFalse(isValid, "La validación debe fallar si el ID del token no coincide con el ID esperado.");
    }

    @Test
    void validateToken_ShouldThrowExceptionForExpiredToken() throws Exception {

        // 1. Crear una instancia anónima de JwtUtil para generar el token expirado.
        JwtUtil shortLivedJwtUtil = new JwtUtil() {

            // Sobrescribimos generateToken para forzar la expiración.
            // NOTA: No podemos usar 'signingKey' directamente aquí sin Reflection o sin un constructor.
            // Usaremos un truco: generaremos el token con la clave de la clase base.
            @Override
            public String generateToken(Integer id, String tipoPerfil) {
                // Generamos un token que expiró hace 1 minuto
                return Jwts.builder()
                        .setClaims(new HashMap<String, Object>() {{ put("tipoPerfil", tipoPerfil); }})
                        .setSubject(id.toString())
                        .setIssuedAt(new Date(System.currentTimeMillis() - 60000))
                        .setExpiration(new Date(System.currentTimeMillis() - 1))
                        // Aquí, necesitamos la 'signingKey' del objeto principal 'jwtUtil'
                        // Pero la clase anónima solo ve su propia versión (privada).
                        // Lo solucionaremos INYECTANDO la clave de 'jwtUtil' en el constructor
                        // o, más simple para este caso, usando la propia Key de la clase base.
                        // ***El truco es usar Reflection ANTES de generar el token.***
                        .signWith(this.getSigningKeyReflected(), SignatureAlgorithm.HS256) // Usaremos un método helper
                        .compact();
            }

            // Método helper local para acceder al signingKey privado usando Reflection
            // Esto es necesario porque 'signingKey' en la clase anónima es su propia instancia privada.
            private Key getSigningKeyReflected() {
                try {
                    Field signingKeyField = JwtUtil.class.getDeclaredField("signingKey");
                    signingKeyField.setAccessible(true);
                    return (Key) signingKeyField.get(jwtUtil); // Obtiene la clave de la instancia principal 'jwtUtil'
                } catch (Exception e) {
                    throw new RuntimeException("Error al acceder al signingKey mediante Reflection", e);
                }
            }
        };

        // 2. Generar el token expirado usando la misma clave que 'jwtUtil'
        String expiredToken = shortLivedJwtUtil.generateToken(USER_ID, TIPO_PERFIL);

        // 3. Verificar que la validación falla con ExpiredJwtException
        // El método validateToken llama a extractAllClaims, que lanza la excepción.
        assertThrows(ExpiredJwtException.class, () -> {
            jwtUtil.validateToken(expiredToken, USER_ID);
        }, "Debe lanzar ExpiredJwtException al intentar validar un token que ha expirado.");
    }

    @Test
    void extractAllClaims_ShouldThrowExceptionForInvalidSignature() {
        // Generar un token válido
        String validToken = jwtUtil.generateToken(USER_ID, TIPO_PERFIL);

        // Corromper la firma del token (cambiar la última letra)
        String corruptedToken = validToken.substring(0, validToken.length() - 1) + "X";

        // Intentar extraer claims debe fallar por firma inválida
        assertThrows(SignatureException.class, () -> {
            jwtUtil.extractAllClaims(corruptedToken);
        }, "Debe lanzar SignatureException si la firma no es válida.");
    }
}