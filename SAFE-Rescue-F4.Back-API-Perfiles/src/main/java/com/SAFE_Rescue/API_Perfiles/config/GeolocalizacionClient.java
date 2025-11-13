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
 * Utiliza WebClient para comunicación asíncrona/reactiva (aunque aquí se usa de forma síncrona con .block()).
 */
@Component
public class GeolocalizacionClient {

    private final WebClient webClient;

    public GeolocalizacionClient(@Value("${geolocalizacion.service.url}") String geolocalizacionServiceUrl) {
        // Configuramos el WebClient con la URL base del microservicio de Geolocalización
        this.webClient = WebClient.builder()
                .baseUrl(geolocalizacionServiceUrl)
                .build();
    }

    /**
     * Obtiene una sola Dirección por su ID.
     * @param id El ID de la dirección.
     * @return DireccionDTO si se encuentra.
     */
    public DireccionDTO getDireccionById(Integer id) {
        try {
            return this.webClient.get()
                    // Usamos una ruta específica para una colección REST
                    .uri("/{id}", id)
                    .retrieve()
                    // Si recibimos 4xx (ej. 404 Not Found), lanzamos una excepción específica
                    .onStatus(
                            status -> status.is4xxClientError(),
                            response -> response.bodyToMono(String.class)
                                    .map(body -> new RuntimeException("Direccion no encontrada (ID: " + id + ") en MS-Geolocalizacion"))
                    )
                    .bodyToMono(DireccionDTO.class) // Se espera un DireccionDTO
                    .block(); // Bloquea hasta que la respuesta esté disponible
        } catch (RuntimeException e) {
            // Re-lanza las excepciones específicas para ser manejadas por el servicio llamante
            throw e;
        } catch (Exception e) {
            // Maneja fallos de conexión o errores 5xx no específicos
            throw new IllegalStateException("Error al comunicarse con MS-Geolocalizacion para obtener dirección: " + e.getMessage(), e);
        }
    }

    /**
     * Guarda o actualiza una Dirección en el microservicio de Geolocalización.
     * Si DireccionDTO tiene ID, se asume un intento de actualización.
     * Si DireccionDTO no tiene ID, se asume una creación.
     * @param direccionDTO El DTO de la dirección a guardar/actualizar.
     * @return El DireccionDTO persistido, incluyendo el ID asignado si fue una creación.
     */
    public DireccionDTO guardarDireccion(DireccionDTO direccionDTO) {
        // Declaramos el tipo correcto: RequestHeadersSpec. Este objeto ya tiene el body
        // y está listo para llamar a .retrieve().
        WebClient.RequestHeadersSpec<?> requestSpec;

        // La URL de destino será la base de la colección
        final String uriPath = "";

        if (direccionDTO.getIdDireccion() == null) {
            // CREACIÓN: Usar POST
            requestSpec = this.webClient.post()
                    .uri(uriPath)
                    .bodyValue(direccionDTO); // Retorna RequestHeadersSpec
        } else {
            // ACTUALIZACIÓN: Usar PUT
            requestSpec = this.webClient.put()
                    .uri(uriPath)
                    .bodyValue(direccionDTO); // Retorna RequestHeadersSpec
        }

        try {
            return requestSpec.retrieve()
                    // Manejo de errores 4xx/5xx del servicio externo
                    .onStatus(
                            status -> status.isError(),
                            response -> response.bodyToMono(String.class)
                                    .map(body -> new IllegalStateException("Error al guardar dirección en MS-Geolocalizacion. Código: " + response.statusCode() + ", Mensaje: " + body))
                    )
                    .bodyToMono(DireccionDTO.class)
                    .block(); // Bloqueamos la ejecución para el servicio síncrono

        } catch (WebClientResponseException e) {
            // Captura errores específicos lanzados por onStatus o problemas de conexión
            throw new IllegalStateException("Fallo de comunicación con MS-Geolocalizacion: " + e.getMessage(), e);
        } catch (Exception e) {
            // Fallo general (ej. error de deserialización, timeout)
            throw new IllegalStateException("Error inesperado al intentar guardar la dirección: " + e.getMessage(), e);
        }
    }


    /**
     * Obtiene todas las direcciones para el proceso de carga de datos (seeding).
     * @return Lista de DireccionDTOs. Retorna lista vacía en caso de error o conexión fallida.
     */
    public List<DireccionDTO> getAllDirecciones() {
        try {
            return this.webClient.get()
                    // Usamos una ruta específica para una colección REST
                    .uri("")
                    .retrieve()
                    // Manejo de errores para devolver una lista vacía y no colapsar el DataLoader
                    .onStatus(
                            status -> status.isError(),
                            response -> {
                                System.out.println("ERROR en GeolocalizacionClient.getAllDirecciones: La API devolvió un código de error: " + response.statusCode());
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
            System.out.println("ADVERTENCIA: Fallo al conectar con el microservicio de Geolocalización para obtener Direcciones. Se usará fallback con IDs simulados. Error: " + e.getMessage());
            return Collections.emptyList(); // Devuelve una lista vacía para que el DataLoader use el fallback.
        }
    }
}