package com.SAFE_Rescue.API_Donaciones.config;

import com.SAFE_Rescue.API_Donaciones.modelo.UsuarioDTO;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import reactor.core.publisher.Mono;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

/**
 * Cliente de WebClient para interactuar con el microservicio de Usuarios.
 * Permite obtener, guardar y listar los objetos UsuarioDTO (Ciudadanos y Usuarios Asignados).
 */
@Component
public class UsuarioClient {

    private final WebClient webClient;

    /**
     * Constructor que inicializa el WebClient usando la URL del microservicio de Usuarios
     * inyectada desde la configuración.
     * @param usuarioServiceUrl La URL base del microservicio de Usuarios.
     */
    public UsuarioClient(@Value("${usuario.service.url}") String usuarioServiceUrl) {
        // La propiedad de configuración debe llamarse 'usuario.service.url'
        this.webClient = WebClient.builder()
                .baseUrl(usuarioServiceUrl)
                .build();
    }

    /**
     * Busca un UsuarioDTO por su ID único.
     *
     * @param idUsuario El ID del usuario.
     * @return El UsuarioDTO si se encuentra (o null si hay 404/Not Found, capturado y no relanzado).
     */
    public UsuarioDTO findById(Integer idUsuario) {
        try {
            // Se utiliza blockOptional() para manejar el caso 404 (Not Found) de forma limpia.
            Optional<UsuarioDTO> optionalUsuario = this.webClient.get()
                    .uri("/{id}", idUsuario)
                    .retrieve()

                    // Manejo de 404 (Not Found)
                    .onStatus(
                            status -> status.is4xxClientError() && status.value() == 404,
                            response -> Mono.empty() // Convierte 404 a un Mono vacío (que blockOptional maneja como Optional.empty())
                    )

                    // Manejo de otros errores 4XX o 5XX
                    .onStatus(
                            status -> status.is4xxClientError() || status.is5xxServerError(),
                            response -> response.bodyToMono(String.class)
                                    .flatMap(body -> Mono.error(new RuntimeException(
                                            "Error " + response.statusCode().value() + " al buscar Usuario con ID " + idUsuario + ". Detalle: " + body)))
                    )

                    // Convierte el cuerpo de la respuesta a la clase UsuarioDTO y espera de forma opcional
                    .bodyToMono(UsuarioDTO.class)
                    .blockOptional(); // Bloquea hasta recibir la respuesta, devuelve Optional

            // Retorna el objeto si está presente, o null si fue 404 (para compatibilidad con el DonacionService)
            return optionalUsuario.orElse(null);

        } catch (WebClientResponseException e) {
            // Manejo de errores específicos no capturados por onStatus (ej. otros 4xx o 5xx)
            throw new RuntimeException("Error de WebClient al buscar Usuario con ID " + idUsuario + ": " + e.getMessage(), e);
        } catch (Exception e) {
            throw new RuntimeException("Error inesperado al buscar Usuario con ID " + idUsuario + ": " + e.getMessage(), e);
        }
    }


    /**
     * Guarda o actualiza un UsuarioDTO en el microservicio externo.
     * Se simula la persistencia mediante POST.
     *
     * @param usuario El UsuarioDTO a persistir.
     * @return El UsuarioDTO guardado (incluyendo ID generado o actualizado).
     * @throws RuntimeException si el microservicio devuelve un error 4xx o 5xx.
     */
    public UsuarioDTO save(UsuarioDTO usuario) {
        try {
            return this.webClient.post()
                    .uri("") // POST a la URL base (asumido: /usuarios)
                    .bodyValue(usuario)
                    .retrieve()

                    // Manejo de Errores 4XX y 5XX (simulación)
                    .onStatus(
                            status -> status.isError(),
                            response -> response.bodyToMono(String.class)
                                    .flatMap(body -> Mono.error(new RuntimeException(
                                            "Error " + response.statusCode().value() + " al guardar Usuario. Detalle: " + body)))
                    )

                    // Convierte el cuerpo de la respuesta a la clase UsuarioDTO
                    .bodyToMono(UsuarioDTO.class)
                    .block();

        } catch (Exception e) {
            throw new RuntimeException("Error al guardar el Usuario en el microservicio externo: " + e.getMessage(), e);
        }
    }

    /**
     * Obtiene todos los usuarios.
     *
     * @return Lista de UsuarioDTOs. Retorna lista vacía en caso de error o conexión fallida.
     */
    public List<UsuarioDTO> findAll() {
        try {
            return this.webClient.get()
                    .uri("") // Asumimos / para obtener todos los recursos (e.g., /usuarios)
                    .retrieve()

                    // Manejo de errores para devolver una lista vacía y no colapsar el proceso
                    .onStatus(
                            status -> status.isError(),
                            response -> {
                                System.err.println("ERROR en UsuarioClient.findAll: La API devolvió un código de error: " + response.statusCode());
                                // Devolvemos una excepción para que sea capturada por el catch de abajo
                                return Mono.error(new WebClientResponseException(
                                        response.statusCode().value(),
                                        "Error al obtener usuarios",
                                        null, null, null
                                ));
                            }
                    )

                    // Se espera un Flux de UsuarioDTOs y se recolecta en una lista
                    .bodyToFlux(UsuarioDTO.class)
                    .collectList()
                    .block();

        } catch (Exception e) {
            // Este catch maneja errores de conexión o cualquier excepción lanzada.
            System.err.println("ADVERTENCIA: Fallo al conectar con el microservicio de Usuarios para obtener datos. Se usará fallback (lista vacía). Error: " + e.getMessage());
            return Collections.emptyList(); // Devuelve una lista vacía.
        }
    }
}