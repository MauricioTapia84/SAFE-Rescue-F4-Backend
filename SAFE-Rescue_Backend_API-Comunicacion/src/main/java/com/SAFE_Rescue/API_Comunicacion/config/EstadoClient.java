package com.SAFE_Rescue.API_Comunicacion.config;

import com.SAFE_Rescue.API_Comunicacion.dto.EstadoDTO;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import reactor.core.publisher.Mono;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

// Importamos el Logger estándar de Spring
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Cliente de WebClient para interactuar con el microservicio de Estados.
 * Permite obtener el catálogo de Estados (ej. Enviado, Leído).
 */
@Component
public class EstadoClient {

    private static final Logger log = LoggerFactory.getLogger(EstadoClient.class);

    private final WebClient webClient;

    /**
     * Constructor que inicializa el WebClient usando la URL del microservicio de Estados
     * inyectada desde la configuración (application.properties o similares).
     * @param estadoServiceUrl La URL base del microservicio de Estados.
     */
    public EstadoClient(@Value("${estado.service.url}") String estadoServiceUrl) {
        // Se construye el WebClient usando la URL proporcionada.
        this.webClient = WebClient.builder()
                .baseUrl(estadoServiceUrl)
                .build();
    }

    /**
     * Obtiene el DTO de Estado desde la API de Estados por su ID.
     * Maneja 404 como un Optional vacío.
     * @param idEstado El ID del estado.
     * @return Un Optional que contiene el EstadoDTO si se encuentra.
     */
    public Optional<EstadoDTO> findById(Integer idEstado) {
        try {
            return this.webClient.get()
                    .uri("/{id}", idEstado)
                    .retrieve()

                    // --- Manejo de 404 (Not Found) ---
                    .onStatus(
                            status -> status.is4xxClientError() && status.value() == 404,
                            response -> Mono.empty() // Convierte 404 a un Mono vacío
                    )

                    // --- Manejo de otros Errores 4XX o 5XX ---
                    .onStatus(
                            status -> status.isError(), // Captura 4XX (no 404) y 5XX
                            response -> response.bodyToMono(String.class)
                                    .flatMap(body -> Mono.error(new RuntimeException(
                                            "Error " + response.statusCode().value() + " al buscar Estado con ID " + idEstado + ". Detalle del API: " + body)))
                    )

                    // Convierte el cuerpo de la respuesta a la clase EstadoDTO y bloquea opcionalmente
                    .bodyToMono(EstadoDTO.class)
                    .blockOptional(); // Bloquea y retorna Optional

        } catch (WebClientResponseException e) {
            log.error("Error de WebClient al buscar Estado con ID {}: {}", idEstado, e.getMessage(), e);
            throw new RuntimeException("Error de WebClient al buscar Estado con ID " + idEstado + ": " + e.getMessage(), e);
        } catch (Exception e) {
            log.error("Error inesperado al buscar Estado con ID {}: {}", idEstado, e.getMessage(), e);
            throw new RuntimeException("Error inesperado al buscar Estado con ID " + idEstado + ": " + e.getMessage(), e);
        }
    }

    /**
     * Obtiene todos los estados para el proceso de carga de datos (seeding).
     *
     * @return Lista de EstadoDTOs. Retorna lista vacía en caso de error o conexión fallida.
     */
    public List<EstadoDTO> getAllEstados() {
        try {
            return this.webClient.get()
                    .uri("/estados") // Path más explícito si fuera necesario
                    .retrieve()

                    // Manejo de errores para devolver una lista vacía y no colapsar el DataLoader
                    .onStatus(
                            status -> status.isError(),
                            response -> {
                                log.warn("ERROR en EstadoClient.getAllEstados: La API devolvió un código de error: {}", response.statusCode());
                                // Devolvemos una excepción para que sea capturada por el catch de abajo
                                return Mono.error(new WebClientResponseException(
                                        response.statusCode().value(),
                                        "Error al obtener estados",
                                        null, null, null
                                ));
                            }
                    )

                    // Se espera un Flux de EstadoDTOs y se recolecta en una lista
                    .bodyToFlux(EstadoDTO.class)
                    .collectList()
                    .block();

        } catch (Exception e) {
            // Este catch maneja errores de conexión o cualquier excepción lanzada.
            log.warn("ADVERTENCIA: Fallo al conectar con el microservicio de Estados para obtener datos. Se usará fallback (lista vacía). Error: {}", e.getMessage());
            return Collections.emptyList(); // Devuelve una lista vacía para que el DataLoader use el fallback.
        }
    }
}