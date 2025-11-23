package com.SAFE_Rescue.API_Perfiles.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;

/**
 * Filtro de seguridad que valida el Token Secreto Compartido (S2S).
 * - Si el token es el secreto, establece el rol ROLE_SERVICE y permite el paso.
 * - Si no, permite que la petición continúe al siguiente filtro (JWT).
 */
@Component
public class ServiceAuthFilter extends OncePerRequestFilter {

    @Value("${service.auth.secret}")
    private String systemAuthSecret;

    private static final String BEARER_PREFIX = "Bearer";

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {

        String authorizationHeader = request.getHeader("Authorization");

        // *** DIAGNÓSTICO: Muestra el encabezado recibido para depuración ***
        // System.out.println("DEBUG S2S: Cabecera Authorization recibida: " + authorizationHeader);

        // 1. Verificar si la cabecera es nula o no empieza con Bearer
        if (authorizationHeader == null || !authorizationHeader.startsWith(BEARER_PREFIX)) {
            // Si no tiene Bearer (o es nula), no es un S2S token ni un JWT válido (aún).
            // Permitimos que el siguiente filtro (JWT Filter) lo maneje.
            filterChain.doFilter(request, response);
            return;
        }

        // 2. Extraer el token
        String providedToken = authorizationHeader.substring(BEARER_PREFIX.length()).trim();

        // 3. VALIDACIÓN S2S: Si el token coincide con el secreto
        if (providedToken.equals(systemAuthSecret)) {

            // *** CRÍTICO: Establecer la autenticación en el SecurityContextHolder ***
            UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(
                    "internal_service", // Principal que representa a la API de Incidentes
                    null,
                    List.of(new SimpleGrantedAuthority("ROLE_SERVICE")) // Rol/Autoridad S2S
            );

            SecurityContextHolder.getContext().setAuthentication(authentication);

            // El servicio ya está autenticado. Continuamos la cadena de filtros.
            filterChain.doFilter(request, response);
            return;
        }

        // 4. Si la cabecera existe pero no es el secreto S2S
        // (Podría ser un JWT o un token S2S incorrecto).
        // Simplemente dejamos que el siguiente filtro (JWT Filter) intente validarlo.
        filterChain.doFilter(request, response);
    }
}