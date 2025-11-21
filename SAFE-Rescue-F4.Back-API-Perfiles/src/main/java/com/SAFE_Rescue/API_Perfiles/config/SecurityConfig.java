package com.SAFE_Rescue.API_Perfiles.config;

import com.SAFE_Rescue.API_Perfiles.security.ServiceAuthFilter;
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
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.Arrays;
import java.util.List;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity(prePostEnabled = true)
public class SecurityConfig {

    @Autowired
    private JwtAuthenticationFilter jwtAuthenticationFilter;

    @Autowired
    private ServiceAuthFilter serviceAuthFilter;

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration authenticationConfiguration) throws Exception {
        return authenticationConfiguration.getAuthenticationManager();
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {

        http
                .cors(cors -> cors.configurationSource(corsConfigurationSource())) // 🔥 AGREGAR ESTA LÍNEA
                .csrf(csrf -> csrf.disable())
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth
                        // Rutas públicas
                        .requestMatchers("/api-perfiles/v1/auth/**", "/v3/api-docs/**", "/swagger-ui/**").permitAll()

                        // ** CRÍTICO CORREGIDO: Permitir acceso a ADMIN y ROLE_SERVICE **
                        // Nota: El filtro S2S establece la autoridad como SimpleGrantedAuthority("ROLE_SERVICE"),
                        // pero la regla hasAuthority() la verifica como "ROLE_SERVICE".
                        .requestMatchers(HttpMethod.GET, "/api-perfiles/v1/usuarios")
                        .hasAnyAuthority("ADMIN", "ROLE_SERVICE")

                        // Si Incidentes llama a /usuarios/{id}, también debe tener permiso
                        .requestMatchers(HttpMethod.GET, "/api-perfiles/v1/usuarios/{id}")
                        .hasAnyAuthority("ADMIN", "ROLE_SERVICE")

                        // Opcional: Si mantienes la ruta /s2s para ser más explícito
                        .requestMatchers("/api-perfiles/v1/perfiles/s2s/**")
                        .hasAuthority("ROLE_SERVICE")

                        // Las demás rutas requieren autenticación (token JWT o SERVICE)
                        .anyRequest().authenticated()
                )
                // El filtro S2S se añade primero
                .addFilterBefore(serviceAuthFilter, UsernamePasswordAuthenticationFilter.class)
                // Luego el filtro JWT
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    // 🔥 AGREGAR ESTE MÉTODO PARA CORS
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOriginPatterns(List.of("*"));  // Permite cualquier origen en desarrollo
        configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "OPTIONS", "PATCH"));
        configuration.setAllowedHeaders(List.of("*"));
        configuration.setAllowCredentials(true);
        configuration.setMaxAge(3600L);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }
}