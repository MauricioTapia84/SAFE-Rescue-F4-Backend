package com.SAFE_Rescue.API_Incidentes.config;

import com.SAFE_Rescue.API_Incidentes.dto.EstadoDTO;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import reactor.core.publisher.Mono;

import java.util.Collections;
import java.util.List;

@Component
public class EstadoClient {

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
     * @param idEstado El ID del estado.
     * @return El objeto EstadoDTO.
     * @throws RuntimeException si el microservicio devuelve un error 4xx o 5xx.
     */
    public EstadoDTO findById(Integer idEstado) {
        return this.webClient.get()
                .uri("/{id}", idEstado)
                .retrieve()

                // --- Manejo de Errores 4XX (Cliente) ---
                .onStatus(
                        status -> status.is4xxClientError(),
                        response -> response.bodyToMono(String.class)
                                .flatMap(body -> Mono.error(new RuntimeException(
                                        "Error 4XX al buscar Estado con ID " + idEstado + ". Detalle del API: " + body)))
                )

                // --- Manejo de Errores 5XX (Servidor) ---
                .onStatus(
                        status -> status.is5xxServerError(),
                        response -> response.bodyToMono(String.class)
                                .flatMap(body -> Mono.error(new RuntimeException(
                                        "Error 5XX del servidor de Estados. Detalle: " + body)))
                )

                // Convierte el cuerpo de la respuesta a la clase EstadoDTO
                .bodyToMono(EstadoDTO.class)
                .block();
    }

    /**
     * Obtiene todos los estados para el proceso de carga de datos (seeding).
     *
     * @return Lista de EstadoDTOs. Retorna lista vacía en caso de error o conexión fallida.
     */
    public List<EstadoDTO> getAllEstados() {
        try {
            return this.webClient.get()
                    .uri("") // Asumimos /estados para obtener todos los recursos
                    .retrieve()

                    // Manejo de errores para devolver una lista vacía y no colapsar el DataLoader
                    .onStatus(
                            status -> status.isError(),
                            response -> {
                                System.err.println("ERROR en EstadoClient.getAllEstados: La API devolvió un código de error: " + response.statusCode());
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
            // Este catch maneja errores de conexión o cualquier excepción lanzada por onStatus si no se maneja en el pipeline.
            System.err.println("ADVERTENCIA: Fallo al conectar con el microservicio de Estados para obtener datos. Se usará fallback (lista vacía). Error: " + e.getMessage());
            return Collections.emptyList(); // Devuelve una lista vacía para que el DataLoader use el fallback.
        }
    }
}