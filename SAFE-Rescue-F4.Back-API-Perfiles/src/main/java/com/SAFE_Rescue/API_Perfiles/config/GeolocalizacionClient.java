package com.SAFE_Rescue.API_Perfiles.config;

import com.SAFE_Rescue.API_Perfiles.modelo.DireccionDTO;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

@Component
public class GeolocalizacionClient {

    private final WebClient webClient;

    /**
     * Constructor que inyecta el WebClient que fue definido y calificado
     * en WebClientConfig.java.
     */
    public GeolocalizacionClient(@Qualifier("geolocalizacionWebClient") WebClient webClient) {
        this.webClient = webClient;
    }

    /**
     * Obtiene los datos de dirección de un ID específico, mapeándolos al DireccionDTO.
     * @param id El ID de la dirección (Integer, según el DTO).
     * @return El objeto DireccionDTO.
     * @throws RuntimeException si el microservicio devuelve un error 4xx o 5xx.
     */
    public DireccionDTO getDireccionById(Integer id) {
        return this.webClient.get()
                .uri("/{id}", id)
                .retrieve()


                // --- Manejo de Errores 4XX (Cliente) ---
                // Usamos la expresión lambda 'status -> status.is4xxClientError()'
                .onStatus(
                        status -> status.is4xxClientError(),
                        response -> response.bodyToMono(String.class)
                                .flatMap(body -> Mono.error(new RuntimeException(
                                        "Error 4XX al buscar Dirección con ID " + id + ". Detalle del API: " + body)))
                )

                // --- Manejo de Errores 5XX (Servidor) ---
                // Usamos la expresión lambda 'status -> status.is5xxServerError()'
                .onStatus(
                        status -> status.is5xxServerError(),
                        response -> response.bodyToMono(String.class)
                                .flatMap(body -> Mono.error(new RuntimeException(
                                        "Error 5XX del servidor de Geolocalización. Detalle: " + body)))
                )

                .bodyToMono(DireccionDTO.class)
                .block();
    }
}