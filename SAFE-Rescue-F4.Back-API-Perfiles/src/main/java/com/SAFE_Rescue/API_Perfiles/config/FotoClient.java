package com.SAFE_Rescue.API_Perfiles.config;

import com.SAFE_Rescue.API_Perfiles.dto.FotoDTO;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.BodyInserters;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.Random;

@Component
public class FotoClient {

    private final WebClient webClient;
    private final String fotoServiceUrl;
    private final Random random = new Random();

    public FotoClient(WebClient.Builder webClientBuilder,
                      @Value("${foto.service.url}") String fotoServiceUrl) {
        this.webClient = webClientBuilder.build();
        this.fotoServiceUrl = fotoServiceUrl;
    }

    /**
     * Sube una foto y retorna solo el ID de la foto
     */
    public Integer uploadFoto(byte[] fotoBytes, String nombreOriginal) {
        System.out.println(" [FotoClient] Subiendo foto: " + nombreOriginal + " (" + fotoBytes.length + " bytes)");
        System.out.println("   URL: " + fotoServiceUrl + "/upload");

        try {
            ByteArrayResource resource = new ByteArrayResource(fotoBytes) {
                @Override
                public String getFilename() {
                    return nombreOriginal;
                }
            };

            //  Obtener el objeto Foto completo
            FotoDTO fotoResponse = this.webClient.post()
                    .uri(fotoServiceUrl + "/upload")
                    .contentType(MediaType.MULTIPART_FORM_DATA)
                    .body(BodyInserters.fromMultipartData("file", resource))
                    .retrieve()
                    .onStatus(status -> !status.is2xxSuccessful(),
                            clientResponse -> clientResponse.bodyToMono(String.class)
                                    .flatMap(body -> {
                                        System.err.println(" [FotoClient] Error " + clientResponse.statusCode().value());
                                        System.err.println("   Respuesta: " + body);
                                        return Mono.error(new RuntimeException(
                                                "Error " + clientResponse.statusCode().value() +
                                                        ": " + body
                                        ));
                                    })
                    )
                    .bodyToMono(FotoDTO.class)
                    .block();

            //  Validar que la respuesta tenga ID
            if (fotoResponse == null || fotoResponse.getIdFoto() == null) {
                System.err.println(" [FotoClient] Respuesta inválida: " + fotoResponse);
                throw new RuntimeException("La API de fotos no retornó un ID válido");
            }

            System.out.println(" [FotoClient] Foto subida exitosamente - ID: " + fotoResponse.getIdFoto());
            System.out.println("   URL: " + fotoResponse.getUrl());
            System.out.println("   Tipo: " + fotoResponse.getTipo());

            //  Retornar solo el ID
            return fotoResponse.getIdFoto();

        } catch (Exception e) {
            System.err.println(" [FotoClient] Error al subir foto: " + e.getMessage());
            e.printStackTrace();
            throw new RuntimeException("Error al subir la foto: " + e.getMessage(), e);
        }
    }

    /**
     *  Obtiene una foto aleatoria existente para el DataLoader
     */
    public Integer getRandomExistingFotoId() {
        System.out.println(" [FotoClient] Obteniendo lista de fotos existentes...");

        try {
            // Obtener todas las fotos
            List<FotoDTO> fotos = this.webClient.get()
                    .uri(fotoServiceUrl)  // Endpoint GET /api-registros/v1/fotos
                    .retrieve()
                    .onStatus(status -> !status.is2xxSuccessful(),
                            clientResponse -> clientResponse.bodyToMono(String.class)
                                    .flatMap(body -> {
                                        System.err.println(" [FotoClient] Error " + clientResponse.statusCode().value());
                                        return Mono.error(new RuntimeException(
                                                "Error al obtener fotos: " + clientResponse.statusCode().value()
                                        ));
                                    })
                    )
                    .bodyToFlux(FotoDTO.class)
                    .collectList()
                    .block();

            if (fotos == null || fotos.isEmpty()) {
                System.out.println(" [FotoClient] No hay fotos disponibles. Usando ID de fallback: 1");
                return 1; // Fallback
            }

            // Seleccionar foto aleatoria
            FotoDTO fotoAleatoria = fotos.get(random.nextInt(fotos.size()));
            Integer fotoId = fotoAleatoria.getIdFoto();

            System.out.println(" [FotoClient] Foto aleatoria seleccionada - ID: " + fotoId);
            return fotoId;

        } catch (Exception e) {
            System.err.println(" [FotoClient] Error al obtener foto aleatoria: " + e.getMessage());
            System.out.println("   Usando ID de fallback: 1");
            return 1; // Fallback seguro
        }
    }

    /**
     * Obtiene una foto por ID
     */
    public byte[] getFoto(Integer idFoto) {
        System.out.println(" [FotoClient] Obteniendo foto: " + idFoto);

        try {
            byte[] response = this.webClient.get()
                    .uri(fotoServiceUrl + "/{id}", idFoto)
                    .retrieve()
                    .onStatus(status -> !status.is2xxSuccessful(),
                            clientResponse -> clientResponse.bodyToMono(String.class)
                                    .flatMap(body -> Mono.error(new RuntimeException(
                                            "Error " + clientResponse.statusCode().value()
                                    )))
                    )
                    .bodyToMono(byte[].class)
                    .block();

            System.out.println(" [FotoClient] Foto obtenida: " + (response != null ? response.length : 0) + " bytes");
            return response;

        } catch (Exception e) {
            System.err.println(" [FotoClient] Error al obtener foto: " + e.getMessage());
            throw new RuntimeException("Error al obtener la foto", e);
        }
    }

    /**
     * Elimina una foto por ID
     */
    public void deleteFoto(Integer idFoto) {
        System.out.println(" [FotoClient] Eliminando foto: " + idFoto);

        try {
            this.webClient.delete()
                    .uri(fotoServiceUrl + "/{id}", idFoto)
                    .retrieve()
                    .onStatus(status -> !status.is2xxSuccessful(),
                            clientResponse -> clientResponse.bodyToMono(String.class)
                                    .flatMap(body -> Mono.error(new RuntimeException(
                                            "Error " + clientResponse.statusCode().value()
                                    )))
                    )
                    .bodyToMono(Void.class)
                    .block();

            System.out.println(" [FotoClient] Foto eliminada");

        } catch (Exception e) {
            System.err.println(" [FotoClient] Error al eliminar foto: " + e.getMessage());
            throw new RuntimeException("Error al eliminar la foto", e);
        }
    }
}