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
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))
                .csrf(csrf -> csrf.disable())
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth
                        //  Rutas públicas
                        .requestMatchers("/api-perfiles/v1/auth/**", "/v3/api-docs/**", "/swagger-ui/**").permitAll()

                        //  OBTENER USUARIOS - S2S (ROLE_SERVICE) O ADMIN
                        .requestMatchers(HttpMethod.GET, "/api-perfiles/v1/usuarios")
                        .hasAnyAuthority("ADMIN", "ROLE_SERVICE")

                        //  OBTENER CIUDADANOS - S2S (ROLE_SERVICE) O ADMIN
                        .requestMatchers(HttpMethod.GET, "/api-perfiles/v1/ciudadanos/**")
                        .hasAnyAuthority("ADMIN", "ROLE_SERVICE")

                        //  OBTENER BOMBEROS - S2S (ROLE_SERVICE) O ADMIN
                        .requestMatchers(HttpMethod.GET, "/api-perfiles/v1/bomberos/**")
                        .hasAnyAuthority("ADMIN", "ROLE_SERVICE")

                        //  ACTUALIZAR CIUDADANOS - S2S (ROLE_SERVICE) O ADMIN
                        .requestMatchers(HttpMethod.PUT, "/api-perfiles/v1/ciudadanos/**")
                        .hasAnyAuthority("ADMIN", "ROLE_SERVICE")

                        //  ACTUALIZAR BOMBEROS - S2S (ROLE_SERVICE) O ADMIN
                        .requestMatchers(HttpMethod.PUT, "/api-perfiles/v1/bomberos/**")
                        .hasAnyAuthority("ADMIN", "ROLE_SERVICE")

                        //  OBTENER USUARIO POR ID - S2S (ROLE_SERVICE) O ADMIN
                        .requestMatchers(HttpMethod.GET, "/api-perfiles/v1/usuarios/{id}")
                        .hasAnyAuthority("ADMIN", "ROLE_SERVICE")

                        //  RUTAS S2S EXPLÍCITAS
                        .requestMatchers("/api-perfiles/v1/perfiles/s2s/**")
                        .hasAuthority("ROLE_SERVICE")

                        //  FOTO DE USUARIO - CUALQUIER USUARIO AUTENTICADO (JWT O S2S)
                        // Los usuarios pueden modificar su propia foto
                        .requestMatchers(HttpMethod.POST, "/api-perfiles/v1/usuarios/{id}/subir-foto")
                        .authenticated()  //  Cualquier usuario autenticado

                        .requestMatchers(HttpMethod.DELETE, "/api-perfiles/v1/usuarios/{id}/foto")
                        .authenticated()  //  Cualquier usuario autenticado

                        .requestMatchers(HttpMethod.GET, "/api-perfiles/v1/usuarios/{id}/foto")
                        .authenticated()  //  Agregar GET también

                        //  ACTUALIZAR USUARIO - CUALQUIER USUARIO AUTENTICADO PUEDE ACTUALIZAR SU PERFIL
                        .requestMatchers(HttpMethod.PUT, "/api-perfiles/v1/usuarios/{id}")
                        .authenticated()

                        //  Las demás rutas requieren autenticación
                        .anyRequest().authenticated()
                )
                //  Orden importante: Primero S2S, luego JWT
                .addFilterBefore(serviceAuthFilter, UsernamePasswordAuthenticationFilter.class)
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOriginPatterns(List.of("*"));
        configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "OPTIONS", "PATCH"));
        configuration.setAllowedHeaders(List.of("*"));
        configuration.setAllowCredentials(true);
        configuration.setMaxAge(3600L);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }
}