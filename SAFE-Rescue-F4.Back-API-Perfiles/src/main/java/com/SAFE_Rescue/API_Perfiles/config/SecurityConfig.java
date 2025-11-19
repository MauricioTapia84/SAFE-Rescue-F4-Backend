package com.SAFE_Rescue.API_Perfiles.config;

import com.SAFE_Rescue.API_Perfiles.config.JwtAuthenticationFilter; // ⭐ Ver Sección 3
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity // Habilita la seguridad de Spring
@EnableMethodSecurity(prePostEnabled = true) // Permite @PreAuthorize
public class SecurityConfig {

    @Autowired
    private JwtAuthenticationFilter jwtAuthenticationFilter; // Filtro que procesa el JWT

    // --- 2.1. Componentes esenciales ---

    /**
     * Define el PasswordEncoder a utilizar (Recomendado: BCrypt).
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    /**
     * Expone el AuthenticationManager (Necesario para el login manual en AuthController).
     */
    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration authenticationConfiguration) throws Exception {
        return authenticationConfiguration.getAuthenticationManager();
    }

    // --- 2.2. Reglas de Autorización y Filtro ---

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {

        http
                // 1. Deshabilitar CSRF (común en APIs REST stateless)
                .csrf(csrf -> csrf.disable())

                // 2. Configuración de la sesión: Stateless (necesario para JWT)
                // Las sesiones no se crean ni se usan para almacenar estado.
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))

                // 3. Reglas de Autorización: Qué rutas son públicas y cuáles requieren token
                .authorizeHttpRequests(auth -> auth
                        // Permite acceso sin token a las rutas de autenticación y Swagger
                        .requestMatchers("/api-perfiles/v1/auth/**", "/v3/api-docs/**", "/swagger-ui/**").permitAll()

                        // Reglas específicas (ej: solo ADMIN puede ver todos los usuarios)
                        .requestMatchers(HttpMethod.GET, "/api-perfiles/v1/usuarios").hasAuthority("ADMIN")

                        // Las demás rutas requieren autenticación (token válido)
                        .anyRequest().authenticated()
                )

                // 4. Integrar el filtro JWT
                // Se coloca nuestro filtro antes del filtro estándar de autenticación de usuario y contraseña
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }
}