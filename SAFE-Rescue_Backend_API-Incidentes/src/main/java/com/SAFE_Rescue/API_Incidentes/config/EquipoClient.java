package com.SAFE_Rescue.API_Incidentes.config;

import com.SAFE_Rescue.API_Incidentes.modelo.EquipoDTO; // Asume que esta clase existe
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import reactor.core.publisher.Mono;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

/**
 * Cliente de WebClient para interactuar con el microservicio de Equipos (por ejemplo, Ambulancias, Bomberos, etc.).
 * Permite obtener, guardar y listar los objetos EquipoDTO.
 */
@Component
public class EquipoClient {

    private final WebClient webClient;

    /**
     * Constructor que inicializa el WebClient usando la URL del microservicio de Equipos
     * inyectada desde la configuración.
     * @param equipoServiceUrl La URL base del microservicio de Equipos.
     */
    public EquipoClient(@Value("${equipo.service.url}") String equipoServiceUrl) {
        // La propiedad de configuración debe llamarse 'equipo.service.url'
        this.webClient = WebClient.builder()
                .baseUrl(equipoServiceUrl)
                .build();
    }

    /**
     * Busca un EquipoDTO por su ID único.
     *
     * @param idEquipo El ID del equipo.
     * @return Un Optional que contiene el EquipoDTO si se encuentra.
     */
    public EquipoDTO findById(Integer idEquipo) {
        try {
            EquipoDTO equipoDTO = this.webClient.get()
                    .uri("/{id}", idEquipo)
                    .retrieve()

                    // Manejo de Errores: Si devuelve 404 (Not Found), se retorna Optional.empty()
                    .onStatus(
                            status -> status.is4xxClientError() && status.value() == 404,
                            response -> Mono.empty()
                    )

                    // Manejo de otros errores 4XX o 5XX
                    .onStatus(
                            status -> status.is4xxClientError() || status.is5xxServerError(),
                            response -> response.bodyToMono(String.class)
                                    .flatMap(body -> Mono.error(new RuntimeException(
                                            "Error " + response.statusCode().value() + " al buscar Equipo con ID " + idEquipo + ". Detalle: " + body)))
                    )

                    // Convierte el cuerpo de la respuesta a la clase EquipoDTO
                    .bodyToMono(EquipoDTO.class)
                    .block(); // Bloquea hasta recibir la respuesta (uso sincrónico)

            return equipoDTO;

        } catch (WebClientResponseException e) {
            // Manejo de errores específicos no capturados por onStatus
            throw new RuntimeException("Error de WebClient al buscar Equipo con ID " + idEquipo + ": " + e.getMessage(), e);
        } catch (Exception e) {
            throw new RuntimeException("Error inesperado al buscar Equipo con ID " + idEquipo + ": " + e.getMessage(), e);
        }
    }


    /**
     * Guarda o actualiza un EquipoDTO en el microservicio externo.
     * Se simula la persistencia mediante POST.
     *
     * @param equipo El EquipoDTO a persistir.
     * @return El EquipoDTO guardado (incluyendo ID generado o actualizado).
     * @throws RuntimeException si el microservicio devuelve un error 4xx o 5xx.
     */
    public EquipoDTO save(EquipoDTO equipo) {
        try {
            return this.webClient.post()
                    .uri("") // POST a la URL base (asumido: /equipos)
                    .bodyValue(equipo)
                    .retrieve()

                    // Manejo de Errores 4XX y 5XX (simulación)
                    .onStatus(
                            status -> status.isError(),
                            response -> response.bodyToMono(String.class)
                                    .flatMap(body -> Mono.error(new RuntimeException(
                                            "Error " + response.statusCode().value() + " al guardar Equipo. Detalle: " + body)))
                    )

                    // Convierte el cuerpo de la respuesta a la clase EquipoDTO
                    .bodyToMono(EquipoDTO.class)
                    .block();

        } catch (Exception e) {
            throw new RuntimeException("Error al guardar el Equipo en el microservicio externo: " + e.getMessage(), e);
        }
    }

    /**
     * Obtiene todos los equipos.
     *
     * @return Lista de EquipoDTOs. Retorna lista vacía en caso de error o conexión fallida.
     */
    public List<EquipoDTO> findAll() {
        try {
            return this.webClient.get()
                    .uri("") // Asumimos / para obtener todos los recursos (e.g., /equipos)
                    .retrieve()

                    // Manejo de errores para devolver una lista vacía y no colapsar el proceso
                    .onStatus(
                            status -> status.isError(),
                            response -> {
                                System.err.println("ERROR en EquipoClient.findAll: La API devolvió un código de error: " + response.statusCode());
                                // Devolvemos una excepción para que sea capturada por el catch de abajo
                                return Mono.error(new WebClientResponseException(
                                        response.statusCode().value(),
                                        "Error al obtener equipos",
                                        null, null, null
                                ));
                            }
                    )

                    // Se espera un Flux de EquipoDTOs y se recolecta en una lista
                    .bodyToFlux(EquipoDTO.class)
                    .collectList()
                    .block();

        } catch (Exception e) {
            // Este catch maneja errores de conexión o cualquier excepción lanzada.
            System.err.println("ADVERTENCIA: Fallo al conectar con el microservicio de Equipos para obtener datos. Se usará fallback (lista vacía). Error: " + e.getMessage());
            return Collections.emptyList(); // Devuelve una lista vacía.
        }
    }
}