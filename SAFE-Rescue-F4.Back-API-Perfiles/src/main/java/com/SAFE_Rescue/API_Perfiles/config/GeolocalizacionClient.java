package com.SAFE_Rescue.API_Perfiles.config;

import com.SAFE_Rescue.API_Perfiles.modelo.DireccionDTO;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import reactor.core.publisher.Mono;

import java.util.Collections;
import java.util.List;

/**
 * Cliente para interactuar con el microservicio de Geolocalización.
 * Asume que el endpoint base devuelve una lista de DireccionDTOs y
 * el endpoint /{id} devuelve un solo DireccionDTO.
 */
@Component
public class GeolocalizacionClient {

    private final WebClient webClient;

    public GeolocalizacionClient(@Value("${geolocalizacion.service.url}") String geolocalizacionServiceUrl) {
        this.webClient = WebClient.builder()
                .baseUrl(geolocalizacionServiceUrl)
                .build();
    }

    /**
     * Obtiene una sola Dirección por su ID.
     * @param id El ID de la dirección.
     * @return DireccionDTO si se encuentra.
     */
    public DireccionDTO getDireccionById(Long id) {
        return this.webClient.get()
                .uri("/{id}", id)
                .retrieve()
                .onStatus(
                        status -> status.is4xxClientError(),
                        response -> response.bodyToMono(String.class)
                                .map(body -> new RuntimeException("Direccion no encontrada (ID: " + id + ")"))
                )
                .bodyToMono(DireccionDTO.class) // Se espera un DireccionDTO
                .block();
    }

    /**
     * Obtiene todas las direcciones para el proceso de carga de datos (seeding).
     * @return Lista de DireccionDTOs. Retorna lista vacía en caso de error o conexión fallida.
     */
    public List<DireccionDTO> getAllDirecciones() {
        try {
            return this.webClient.get()
                    .uri("")
                    .retrieve()


                    // Manejo de errores para devolver una lista vacía y no colapsar el DataLoader
                    .onStatus(
                            status -> status.isError(),
                            response -> {
                                System.err.println("ERROR en GeolocalizacionClient.getAllDirecciones: La API devolvió un código de error: " + response.statusCode());
                                // Devolvemos una excepción para que sea capturada por el catch de abajo
                                return Mono.error(new WebClientResponseException(
                                        response.statusCode().value(),
                                        "Error al obtener direcciones",
                                        null, null, null
                                ));
                            }
                    )

                    // Se espera un Flux de DireccionDTOs y se recolecta en una lista
                    .bodyToFlux(DireccionDTO.class)
                    .collectList()
                    .block();

        } catch (Exception e) {
            System.err.println("ADVERTENCIA: Fallo al conectar con el microservicio de Geolocalización para obtener Direcciones. Se usará fallback con IDs simulados. Error: " + e.getMessage());
            return Collections.emptyList(); // Devuelve una lista vacía para que el DataLoader use el fallback.
        }
    }
}