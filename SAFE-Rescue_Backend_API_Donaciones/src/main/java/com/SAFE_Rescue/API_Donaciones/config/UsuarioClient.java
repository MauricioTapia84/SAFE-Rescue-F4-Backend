package com.SAFE_Rescue.API_Donaciones.config;

import com.SAFE_Rescue.API_Donaciones.modelo.UsuarioDTO;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import reactor.core.publisher.Mono;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

/**
 * Cliente de WebClient para interactuar con el microservicio de Usuarios (API_Perfiles).
 * La cabecera de autenticación Service-to-Service se aplica globalmente en el constructor
 * usando defaultHeaders, garantizando que el token interno se envíe automáticamente en cada llamada.
 */
@Component
public class UsuarioClient {

    private final WebClient webClient;

    /**
     * Constructor que inicializa el WebClient, inyectando la URL y el Token de Autorización.
     * Este enfoque elimina la necesidad de aplicar las cabeceras manualmente en cada método.
     * * @param usuarioServiceUrl La URL base del microservicio de Perfiles (ej. http://localhost:8082/api/v1).
     * @param internalAuthToken El token secreto para la comunicación interna entre microservicios (propiedad ${internal.auth.token}).
     */
    public UsuarioClient(
            @Value("${usuario.service.url}") String usuarioServiceUrl,
            @Value("${internal.auth.token}") String internalAuthToken) {

        // 1. Verificación Crítica: Si esta propiedad falta en el application.properties, Spring
        // lanzará el error "Could not resolve placeholder". ¡Asegúrate de que exista!

        // 2. Aplicación del Token: Usamos defaultHeaders para incluir el token Bearer en
        // todas las peticiones creadas por este WebClient.
        this.webClient = WebClient.builder()
                .baseUrl(usuarioServiceUrl)
                .defaultHeader("Accept", "application/json")
                .defaultHeaders(headers -> headers.setBearerAuth(internalAuthToken))
                .build();
    }


    /**
     * Busca un UsuarioDTO por su ID único.
     * Endpoint asumido: GET /usuarios/{id}
     *
     * @param idUsuario El ID del usuario.
     * @return Un Optional que contiene el UsuarioDTO si se encuentra. Retorna Optional.empty() si el usuario no existe (404).
     */
    public Optional<UsuarioDTO> findById(Integer idUsuario) {
        try {
            return this.webClient.get()
                    .uri("/{id}", idUsuario)
                    .retrieve()

                    // Manejo de 404 (Not Found): Convierte el error 404 en un Mono vacío.
                    .onStatus(
                            status -> status.is4xxClientError() && status.value() == HttpStatus.NOT_FOUND.value(),
                            response -> Mono.empty()
                    )

                    // Manejo de otros errores 4XX o 5XX
                    .onStatus(
                            status -> status.is4xxClientError() || status.is5xxServerError(),
                            response -> response.bodyToMono(String.class)
                                    .flatMap(body -> Mono.error(new RuntimeException(
                                            "Error " + response.statusCode().value() + " al buscar Usuario con ID " + idUsuario + ". Detalle: " + body)))
                    )

                    .bodyToMono(UsuarioDTO.class)
                    .blockOptional();

        } catch (WebClientResponseException e) {
            // Este catch es para errores que ocurren fuera del onStatus (ej. conexión) o si se lanza una WebClientResponseException
            // por un error que no sea 404 y que no fue manejado por el .onStatus.
            if (e.getStatusCode().value() == HttpStatus.NOT_FOUND.value()) {
                return Optional.empty();
            }
            throw new RuntimeException("Error de WebClient al buscar Usuario con ID " + idUsuario + ": " + e.getMessage(), e);
        } catch (Exception e) {
            throw new RuntimeException("Error inesperado al buscar Usuario con ID " + idUsuario + ": " + e.getMessage(), e);
        }
    }


    /**
     * Guarda o actualiza un UsuarioDTO en el microservicio externo.
     * Endpoint asumido: POST /usuarios
     *
     * @param usuario El UsuarioDTO a persistir.
     * @return El UsuarioDTO guardado (incluyendo ID generado o actualizado).
     */
    public UsuarioDTO save(UsuarioDTO usuario) {
        try {
            return this.webClient.post()
                    .uri("")
                    .bodyValue(usuario)
                    .retrieve()

                    // Manejo de Errores 4XX y 5XX
                    .onStatus(
                            status -> status.isError(),
                            response -> response.bodyToMono(String.class)
                                    .flatMap(body -> Mono.error(new RuntimeException(
                                            "Error " + response.statusCode().value() + " al guardar Usuario. Detalle: " + body)))
                    )

                    .bodyToMono(UsuarioDTO.class)
                    .block();

        } catch (Exception e) {
            throw new RuntimeException("Error al guardar el Usuario en el microservicio externo: " + e.getMessage(), e);
        }
    }

    /**
     * Obtiene todos los usuarios.
     * Endpoint asumido: GET /usuarios
     *
     * @return Lista de UsuarioDTOs. Retorna lista vacía en caso de error o conexión fallida.
     */
    public List<UsuarioDTO> findAll() {
        try {
            return this.webClient.get()
                    .uri("")
                    .retrieve()

                    .onStatus(
                            status -> status.isError(),
                            response -> {
                                System.err.println("ERROR en UsuarioClient.findAll: La API devolvió un código de error: " + response.statusCode());
                                // Lanza una excepción para que sea capturada por el catch externo
                                return Mono.error(new WebClientResponseException(
                                        response.statusCode().value(),
                                        "Error al obtener usuarios",
                                        null, null, null
                                ));
                            }
                    )

                    .bodyToFlux(UsuarioDTO.class)
                    .collectList()
                    .block();

        } catch (Exception e) {
            // En caso de fallas de conexión o errores HTTP no manejados
            System.err.println("ADVERTENCIA: Fallo al conectar con el microservicio de Usuarios para obtener datos. Se usará fallback (lista vacía). Error: " + e.getMessage());
            return Collections.emptyList();
        }
    }
}