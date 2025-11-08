package com.SAFE_Rescue.API_Perfiles.config;

import com.SAFE_Rescue.API_Perfiles.modelo.EstadoDTO;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

@Component
public class EstadoClient {

    private final WebClient webClient;

    /**
     * Constructor que inyecta el WebClient específico para el servicio de Estados.
     * Se usa @Qualifier para asegurar que se inyecte el Bean correcto.
     */
    public EstadoClient(@Qualifier("estadoWebClient") WebClient webClient) {
        this.webClient = webClient;
    }

    /**
     * Obtiene el DTO de Estado desde la API de Estados.
     * @param id El ID del estado.
     * @return El objeto EstadoDTO.
     * @throws RuntimeException si el microservicio devuelve un error 4xx o 5xx.
     */
    public EstadoDTO getEstadoById(Integer id) {
        return this.webClient.get()
                // Asumiendo que la URI es /<id>
                .uri("/{id}", id)
                .retrieve()

                // --- Manejo de Errores 4XX (Cliente) ---
                .onStatus(
                        status -> status.is4xxClientError(),
                        response -> response.bodyToMono(String.class)
                                .flatMap(body -> Mono.error(new RuntimeException(
                                        "Error 4XX al buscar Estado con ID " + id + ". Detalle del API: " + body)))
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
}