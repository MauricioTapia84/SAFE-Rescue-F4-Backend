package com.SAFE_Rescue.API_Perfiles.config;

import com.SAFE_Rescue.API_Perfiles.util.JwtUtil;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    @Autowired
    private JwtUtil jwtUtil;

    // Servicio que Spring Security usa para cargar la información del usuario
    @Autowired
    private UserDetailsService userDetailsService;

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain
    ) throws ServletException, IOException {

        // Si ya hay autenticación (establecida por el filtro S2S), no hagas nada.
        if (SecurityContextHolder.getContext().getAuthentication() != null &&
                SecurityContextHolder.getContext().getAuthentication().isAuthenticated()) {
            filterChain.doFilter(request, response);
            return;
        }

        final String authHeader = request.getHeader("Authorization");
        String jwt = null;
        String userId = null; // Usaremos el ID del token como 'username'

        // 1. Extraer el Token del encabezado
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            jwt = authHeader.substring(7);
            try {
                // Asumimos que JwtUtil tiene un método para extraer el subject (ID)
                userId = jwtUtil.extractAllClaims(jwt).getSubject();
            } catch (Exception e) {
                // Manejar token inválido o expirado
                System.err.println("JWT inválido o expirado: " + e.getMessage());
            }
        }

        // 2. Validar y autenticar
        if (userId != null && SecurityContextHolder.getContext().getAuthentication() == null) {

            // Cargar los detalles del usuario
            UserDetails userDetails = this.userDetailsService.loadUserByUsername(userId);

            // Validar el token y el usuario
            if (jwtUtil.validateToken(jwt, Integer.parseInt(userId))) {

                // Crear el objeto de autenticación
                UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(
                        userDetails,
                        null,
                        userDetails.getAuthorities() // Los roles/permisos del usuario
                );

                // Añadir detalles de la petición
                authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));

                // Establecer la autenticación en el contexto de seguridad
                SecurityContextHolder.getContext().setAuthentication(authToken);
            }
        }

        // 3. Continuar con la cadena de filtros
        filterChain.doFilter(request, response);
    }
}