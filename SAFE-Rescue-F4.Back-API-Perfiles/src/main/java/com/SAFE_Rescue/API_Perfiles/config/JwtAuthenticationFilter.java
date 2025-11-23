package com.SAFE_Rescue.API_Perfiles.config;

import com.SAFE_Rescue.API_Perfiles.util.JwtUtil;
import io.jsonwebtoken.Claims;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    @Autowired
    private JwtUtil jwtUtil;

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain
    ) throws ServletException, IOException {

        //  Si ya hay autenticación S2S, no hagas nada
        if (SecurityContextHolder.getContext().getAuthentication() != null &&
                SecurityContextHolder.getContext().getAuthentication().isAuthenticated()) {
            System.out.println(" Autenticación S2S detectada, saltando JWT Filter");
            filterChain.doFilter(request, response);
            return;
        }

        final String authHeader = request.getHeader("Authorization");
        String jwt = null;
        String userId = null;

        try {
            // 1. Extraer el Token del encabezado
            if (authHeader != null && authHeader.startsWith("Bearer ")) {
                jwt = authHeader.substring(7);
                System.out.println(" JWT encontrado en header");

                userId = jwtUtil.extractAllClaims(jwt).getSubject();
                System.out.println(" userId extraído: " + userId);
            }

            // 2. Validar y autenticar
            if (userId != null && SecurityContextHolder.getContext().getAuthentication() == null) {

                if (jwtUtil.validateToken(jwt, Integer.parseInt(userId))) {
                    System.out.println(" Token JWT válido para userId: " + userId);

                    //  Extraer claims
                    Claims claims = jwtUtil.extractAllClaims(jwt);

                    //  Construir autoridades desde el token
                    List<GrantedAuthority> authorities = new ArrayList<>();
                    authorities.add(new SimpleGrantedAuthority("USER"));

                    if (claims.containsKey("tipoPerfil")) {
                        String tipoPerfil = claims.get("tipoPerfil", String.class);
                        authorities.add(new SimpleGrantedAuthority("ROLE_" + tipoPerfil.toUpperCase()));
                        System.out.println(" Autoridad agregada: ROLE_" + tipoPerfil.toUpperCase());
                    }

                    //  Crear autenticación
                    UsernamePasswordAuthenticationToken authToken =
                            new UsernamePasswordAuthenticationToken(
                                    userId,
                                    null,
                                    authorities
                            );

                    authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                    SecurityContextHolder.getContext().setAuthentication(authToken);

                    System.out.println(" Autenticación JWT establecida - Autoridades: " + authorities);
                } else {
                    System.err.println(" Token JWT inválido o expirado para userId: " + userId);
                }
            }
        } catch (Exception e) {
            System.err.println(" Error en JWT Filter: " + e.getMessage());
            e.printStackTrace();
        }

        filterChain.doFilter(request, response);
    }
}