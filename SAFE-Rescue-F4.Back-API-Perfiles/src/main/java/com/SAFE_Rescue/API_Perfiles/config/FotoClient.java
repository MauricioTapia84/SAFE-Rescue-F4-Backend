package com.SAFE_Rescue.API_Perfiles.config;

import com.SAFE_Rescue.API_Perfiles.modelo.FotoDTO;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.client.MultipartBodyBuilder;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.io.IOException;

@Component
public class FotoClient {

    private final WebClient webClient;

    /**
     * Constructor que inyecta el WebClient específico para el servicio de Fotos.
     */
    public FotoClient(@Qualifier("fotoWebClient") WebClient webClient) {
        this.webClient = webClient;
    }

    /**
     * Manejador de errores común para 4XX y 5XX.
     * @param status El estado HTTP (4XX o 5XX).
     * @param id El ID del recurso (si aplica, si no es una cadena descriptiva).
     * @param serviceName Nombre del servicio afectado.
     * @return Mono<Throwable> con la excepción envuelta.
     */
    private <T> Mono<Throwable> handleError(HttpStatus status, String id, String serviceName) {
        String errorType = status.is4xxClientError() ? "4XX (Cliente)" : "5XX (Servidor)";
        return Mono.error(new RuntimeException(
                String.format("Error %s al comunicarse con el API de %s para ID %s. Código: %d",
                        errorType, serviceName, id, status.value())));
    }

    /**
     * Obtiene la URL de una foto específica desde la API de Fotos.
     * @param id El ID de la foto.
     * @return La URL de la foto.
     * @throws RuntimeException si el microservicio devuelve un error 4xx o 5xx.
     */
    public String getFotoUrlById(Integer id) {
        return this.webClient.get()
                // Asumiendo que la URI es /<id>
                .uri("/{id}", id)
                .retrieve()

                // --- Manejo de Errores 4XX (Cliente) ---
                .onStatus(
                        status -> status.is4xxClientError(),
                        response -> handleError((HttpStatus) response.statusCode(), id.toString(), "Fotos")
                )

                // --- Manejo de Errores 5XX (Servidor) ---
                .onStatus(
                        status -> status.is5xxServerError(),
                        response -> handleError((HttpStatus) response.statusCode(), id.toString(), "Fotos")
                )

                // Convierte el cuerpo de la respuesta a la clase FotoDTO
                .bodyToMono(FotoDTO.class)
                .map(FotoDTO::getUrl) // Mapea directamente al URL
                .block();
    }

    /**
     * Sube un archivo de foto a la API externa de Fotos.
     * @param archivo Archivo MultipartFile a subir.
     * @return El String retornado por la API (asumido como el ID o la URL final).
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
            // Asumimos que la API espera el archivo con la clave "file"
            builder.part("file", resource, MediaType.MULTIPART_FORM_DATA);

            return this.webClient.post()
                    .uri("/upload") // Asumiendo que la URI de subida es /upload
                    .contentType(MediaType.MULTIPART_FORM_DATA)
                    .bodyValue(builder.build())
                    .retrieve()

                    // --- Manejo de Errores 4XX y 5XX para Subida ---
                    .onStatus(
                            status -> status.isError(), // Cubre ambos 4xx y 5xx
                            response -> handleError((HttpStatus) response.statusCode(), "Subida", "Fotos")
                    )

                    .bodyToMono(String.class) // Asumimos que devuelve un String (ID o URL)
                    .block();

        } catch (IOException e) {
            throw new RuntimeException("Error al leer el archivo de la foto para subirlo.", e);
        }
    }
}