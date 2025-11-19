package com.SAFE_Rescue.API_Incidentes.config;

import com.SAFE_Rescue.API_Incidentes.modelo.UsuarioDTO;
import org.springframework.beans.factory.annotation.Value;
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
 * usando defaultHeaders, eliminando la necesidad del método applyHeaders.
 */
@Component
public class UsuarioClient {

    private final WebClient webClient;

    /**
     * Constructor que inicializa el WebClient, inyectando la URL y el Token de Autorización.
     * @param usuarioServiceUrl La URL base del microservicio de Perfiles.
     * @param internalAuthToken El token secreto para la comunicación interna entre microservicios.
     */
    public UsuarioClient(
            @Value("${usuario.service.url}") String usuarioServiceUrl,
            @Value("${internal.auth.token}") String internalAuthToken) {

        // ⭐ CORRECCIÓN: Aplicamos el token de autorización interno como una cabecera por defecto
        // en todas las peticiones realizadas por este cliente.
        this.webClient = WebClient.builder()
                .baseUrl(usuarioServiceUrl)
                .defaultHeader("Accept", "application/json")
                .defaultHeaders(headers -> headers.setBearerAuth(internalAuthToken)) // Solución al error de 'uri()'
                .build();
    }

    /**
     * MÉTODO ELIMINADO: La autenticación ahora se aplica en el constructor.
     */
    // private WebClient.RequestHeadersSpec<?> applyHeaders(WebClient.RequestHeadersSpec<?> spec) { ... }


    /**
     * Busca un UsuarioDTO por su ID único.
     * Endpoint asumido: GET /api/v1/usuarios/{id}
     *
     * @param idUsuario El ID del usuario.
     * @return Un Optional que contiene el UsuarioDTO si se encuentra.
     */
    public Optional<UsuarioDTO> findById(Integer idUsuario) {
        try {
            // El token ya está incluido gracias al constructor
            return this.webClient.get()
                    .uri("/usuarios/{id}", idUsuario)
                    .retrieve()

                    // Manejo de 404 (Not Found)
                    .onStatus(
                            status -> status.is4xxClientError() && status.value() == 404,
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
            throw new RuntimeException("Error de WebClient al buscar Usuario con ID " + idUsuario + ": " + e.getMessage(), e);
        } catch (Exception e) {
            throw new RuntimeException("Error inesperado al buscar Usuario con ID " + idUsuario + ": " + e.getMessage(), e);
        }
    }


    /**
     * Guarda o actualiza un UsuarioDTO en el microservicio externo.
     * Endpoint asumido: POST /api/v1/usuarios
     *
     * @param usuario El UsuarioDTO a persistir.
     * @return El UsuarioDTO guardado (incluyendo ID generado o actualizado).
     */
    public UsuarioDTO save(UsuarioDTO usuario) {
        try {
            // El token ya está incluido gracias al constructor
            return this.webClient.post()
                    .uri("/usuarios")
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
     * Endpoint asumido: GET /api/v1/usuarios
     *
     * @return Lista de UsuarioDTOs. Retorna lista vacía en caso de error o conexión fallida.
     */
    public List<UsuarioDTO> findAll() {
        try {
            // El token ya está incluido gracias al constructor
            return this.webClient.get()
                    .uri("/usuarios")
                    .retrieve()

                    .onStatus(
                            status -> status.isError(),
                            response -> {
                                System.err.println("ERROR en UsuarioClient.findAll: La API devolvió un código de error: " + response.statusCode());
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
            System.err.println("ADVERTENCIA: Fallo al conectar con el microservicio de Usuarios para obtener datos. Se usará fallback (lista vacía). Error: " + e.getMessage());
            return Collections.emptyList();
        }
    }
}