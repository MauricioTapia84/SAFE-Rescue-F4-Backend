package com.SAFE_Rescue.API_Perfiles.config;

import com.SAFE_Rescue.API_Perfiles.dto.FotoDTO;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.client.MultipartBodyBuilder;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import reactor.core.publisher.Mono;

import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.Random;

@Service
public class FotoClient {

    private final WebClient webClient;
    private final Random random = new Random();
    private static final String SERVICE_NAME = "Fotos";

    /**
     * Constructor que inicializa el WebClient usando la URL del microservicio de Fotos.
     */
    public FotoClient(@Value("${foto.service.url}") String fotoServiceUrl) {
        this.webClient = WebClient.builder()
                .baseUrl(fotoServiceUrl)
                .build();
    }

    /**
     * Manejador de errores común para 4XX y 5XX. Retorna un Mono que lanza una excepción.
     */
    private Mono<Throwable> handleError(HttpStatus status, String id) {
        return Mono.error(new RuntimeException(
                String.format("Error %s al comunicarse con el API de %s para ID %s. Código: %d",
                        status.is4xxClientError() ? "4XX (Cliente)" : "5XX (Servidor)", SERVICE_NAME, id, status.value())));
    }

    /**
     * Obtiene la URL de una foto específica desde la API de Fotos.
     */
    public String getFotoUrlById(Integer id) {
        try {
            return this.webClient.get()
                    .uri("/{id}", id) // Endpoint: GET /api-registros/v1/fotos/{id}
                    .retrieve()
                    .onStatus(
                            status -> status.isError(),
                            response -> handleError((HttpStatus) response.statusCode(), id.toString())
                    )
                    .bodyToMono(FotoDTO.class)
                    .map(FotoDTO::getUrl)
                    .block();
        } catch (Exception e) {
            System.err.printf("🚨 ERROR: Fallo al obtener la URL de Foto para ID %d. Mensaje: %s%n", id, e.getMessage());
            return "url_simulada_por_error";
        }
    }

    /**
     * Obtiene todas las fotos para propósitos de seeding o listado.
     */
    public List<FotoDTO> getAllFotos() {
        try {
            return this.webClient.get()
                    .uri("")
                    .retrieve() // Endpoint: GET /api-registros/v1/fotos

                    .onStatus(
                            status -> status.isError(),
                            response -> {
                                System.err.printf("ERROR en FotoClient.getAllFotos: La API devolvió un código de error: %d%n", response.statusCode().value());
                                return Mono.error(new WebClientResponseException(
                                        response.statusCode().value(), "Error al obtener fotos", null, null, null
                                ));
                            }
                    )
                    .bodyToFlux(FotoDTO.class)
                    .collectList()
                    .block();
        } catch (Exception e) {
            System.err.println("🟡 WARN: Fallo de conexión o error en getAllFotos. Retornando lista vacía. Error: " + e.getMessage());
            return Collections.emptyList();
        }
    }

    /**
     * Sube un archivo de foto a una API externa utilizando multipart/form-data.
     */
    public String uploadFoto(MultipartFile archivo) {
        if (archivo.isEmpty()) {
            throw new IllegalArgumentException("El archivo no puede estar vacío.");
        }

        try {
            ByteArrayResource resource = new ByteArrayResource(archivo.getBytes()) {
                @Override
                public String getFilename() {
                    return archivo.getOriginalFilename();
                }
            };

            MultipartBodyBuilder builder = new MultipartBodyBuilder();
            builder.part("file", resource, MediaType.MULTIPART_FORM_DATA);

            return this.webClient.post()
                    .uri("/upload")
                    .contentType(MediaType.MULTIPART_FORM_DATA)
                    .bodyValue(builder.build())
                    .retrieve()
                    .onStatus(
                            status -> status.isError(),
                            response -> handleError((HttpStatus) response.statusCode(), "Subida")
                    )
                    .bodyToMono(String.class)
                    .block();

        } catch (IOException e) {
            throw new RuntimeException("Error al leer el archivo de la foto para subirlo.", e);
        }
    }


    /**
     * Obtiene todas las fotos existentes de la API y selecciona un ID aleatorio.
     * Retorna un ID de fallback si la API está caída o si no hay fotos.
     * * @return Un idFoto existente o un ID de fallback.
     */
    public Integer getRandomExistingFotoId() {
        final Integer fallbackId = 1001; // ID de fallback

        try {
            List<FotoDTO> fotos = this.getAllFotos(); // Llama al método existente

            if (fotos != null && !fotos.isEmpty()) {
                // Selecciona un DTO aleatorio de la lista
                FotoDTO fotoAleatoria = fotos.get(random.nextInt(fotos.size()));

                // Retorna el ID, con fallback a '1' si el ID es nulo por alguna razón.
                return fotoAleatoria.getIdFoto() != null ? fotoAleatoria.getIdFoto() : 1;
            } else {
                System.err.println("🟡 WARN: API de Fotos está levantada, pero la lista de fotos está vacía. Usando ID de fallback: " + fallbackId);
                return fallbackId;
            }
        } catch (Exception e) {
            // Captura cualquier error de conexión o respuesta del getAllFotos()
            System.err.printf("🚨 FATAL WARN: Fallo al obtener lista de fotos. Retornando ID simulado: %d. Error: %s%n", fallbackId, e.getMessage());
            return fallbackId;
        }
    }
}