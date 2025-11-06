package com.SAFE_Rescue.API_Perfiles.config;

import com.SAFE_Rescue.API_Perfiles.modelo.DireccionDTO;
import com.SAFE_Rescue.API_Perfiles.modelo.EstadoDTO;
import com.SAFE_Rescue.API_Perfiles.modelo.FotoDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.MediaType;
import org.springframework.http.client.MultipartBodyBuilder;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;

import java.io.IOException;
import java.util.NoSuchElementException;
@Configuration
public class WebClienteConfig {

    // Se inyectan los clientes WebClient configurados por Bean para un uso más limpio
    @Autowired
    private WebClient estadoWebClient;

    @Autowired
    private WebClient geolocalizacionWebClient;

    // Usaremos esta URL directamente en el método uploadFoto
    private final String fotoServiceUrl;

    // URLs base de los servicios externos (solo se usan para configurar los Beans)
    private final String estadoServiceUrl;
    private final String geolocalizacionServiceUrl;

    public WebClienteConfig(@Value("${estado.service.url}") String estadoServiceUrl,
                            @Value("${foto.service.url}") String fotoServiceUrl,
                            @Value("${geolocalizacion.service.url}") String geolocalizacionServiceUrl) {

        this.estadoServiceUrl = estadoServiceUrl;
        this.geolocalizacionServiceUrl = geolocalizacionServiceUrl;
        this.fotoServiceUrl = fotoServiceUrl;
    }

    // -------------------------------------------------------------------
    //                       DEFINICIÓN DE BEANS WEBCLIENT
    // -------------------------------------------------------------------

    /**
     * Define el WebClient específico para el servicio de Estados.
     */
    @Bean
    public WebClient estadoWebClient(WebClient.Builder webClientBuilder) {
        return webClientBuilder.baseUrl(this.estadoServiceUrl).build();
    }

    /**
     * Define el WebClient específico para el servicio de Geolocalización (Dirección).
     */
    @Bean
    public WebClient geolocalizacionWebClient(WebClient.Builder webClientBuilder) {
        return webClientBuilder.baseUrl(this.geolocalizacionServiceUrl).build();
    }


    // -------------------------------------------------------------------
    //                       MÉTODOS DE BÚSQUEDA EXTERNA
    // -------------------------------------------------------------------

    /**
     * Obtiene el DTO de Estado desde la API de Estados.
     */
    public EstadoDTO getEstadoById(Integer id) {
        try {
            return this.estadoWebClient.get()
                    .uri("/{id}", id)
                    .retrieve()
                    .bodyToMono(EstadoDTO.class)
                    .block();
        } catch (WebClientResponseException.NotFound e) {
            throw new NoSuchElementException("Estado no encontrado en la API externa (ID: " + id + ")");
        } catch (Exception e) {
            throw new RuntimeException("Error de comunicación con la API de Estados.", e);
        }
    }

    /**
     * Obtiene el DTO de Dirección desde la API de Geolocalización.
     */
    public DireccionDTO getDireccionById(Integer id) {
        try {
            return this.geolocalizacionWebClient.get()
                    .uri("/direcciones/{id}", id) // Ruta específica para Direcciones
                    .retrieve()
                    .bodyToMono(DireccionDTO.class)
                    .block();
        } catch (WebClientResponseException.NotFound e) {
            throw new NoSuchElementException("Dirección no encontrada en la API de Geolocalización (ID: " + id + ")");
        } catch (Exception e) {
            throw new RuntimeException("Error de comunicación con la API de Geolocalización.", e);
        }
    }

    /**
     * Obtiene la URL de una foto específica desde la API de Fotos.
     */
    public String getFotoUrlById(Integer id) {
        // Creamos un cliente para la URL de Fotos ya que no se inyectó como Bean
        WebClient fotoClient = WebClient.builder().baseUrl(this.fotoServiceUrl).build();

        try {
            FotoDTO foto = fotoClient.get()
                    .uri("/{id}", id)
                    .retrieve()
                    .bodyToMono(FotoDTO.class)
                    .block();

            if (foto != null) {
                return foto.getUrl();
            }
            throw new NoSuchElementException("Foto no encontrada o vacía (ID: " + id + ")");

        } catch (WebClientResponseException.NotFound e) {
            throw new NoSuchElementException("Foto no encontrada en la API externa (ID: " + id + ")");
        } catch (Exception e) {
            throw new RuntimeException("Error al obtener la foto de la API externa.", e);
        }
    }

    // -------------------------------------------------------------------
    //                         MÉTODO DE SUBIDA
    // -------------------------------------------------------------------

    /**
     * Sube un archivo de foto a la API externa de Fotos.
     * @return El String retornado por la API (asumido como el ID o la URL final de la foto).
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

            // Creamos un cliente temporal para la URL de Fotos
            WebClient fotoClient = WebClient.builder().baseUrl(this.fotoServiceUrl).build();

            return fotoClient.post()
                    .uri("/upload")
                    .contentType(MediaType.MULTIPART_FORM_DATA)
                    .bodyValue(builder.build())
                    .retrieve()
                    .bodyToMono(String.class)
                    .block();

        } catch (WebClientResponseException e) {
            throw new RuntimeException("Error al comunicarse con la API de fotos. Respuesta: " + e.getStatusCode(), e);
        } catch (IOException e) {
            throw new RuntimeException("Error al leer el archivo de la foto: " + e.getMessage(), e);
        }
    }
}