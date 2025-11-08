package com.SAFE_Rescue.API_Perfiles.config;

import org.springframework.beans.factory.annotation.Qualifier; // Importación necesaria
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.client.WebClient;

@Configuration
public class WebClientConfig {

    private final String fotoServiceUrl;
    private final String estadoServiceUrl;
    private final String geolocalizacionServiceUrl;

    public WebClientConfig(@Value("${estado.service.url}") String estadoServiceUrl,
                           @Value("${foto.service.url}") String fotoServiceUrl,
                           @Value("${geolocalizacion.service.url}") String geolocalizacionServiceUrl) {
        this.estadoServiceUrl = estadoServiceUrl;
        this.geolocalizacionServiceUrl = geolocalizacionServiceUrl;
        this.fotoServiceUrl = fotoServiceUrl;
    }

    // Usamos @Qualifier para ser explícitos sobre el nombre del bean.
    @Bean
    @Qualifier("estadoWebClient")
    public WebClient estadoWebClient(WebClient.Builder webClientBuilder) {
        return webClientBuilder.baseUrl(this.estadoServiceUrl).build();
    }

    // Usamos @Qualifier para ser explícitos sobre el nombre del bean.
    @Bean
    @Qualifier("geolocalizacionWebClient")
    public WebClient geolocalizacionWebClient(WebClient.Builder webClientBuilder) {
        return webClientBuilder.baseUrl(this.geolocalizacionServiceUrl).build();
    }

    // Usamos @Qualifier para ser explícitos sobre el nombre del bean.
    @Bean
    @Qualifier("fotoWebClient")
    public WebClient fotoWebClient(WebClient.Builder webClientBuilder) {
        return webClientBuilder.baseUrl(this.fotoServiceUrl).build();
    }
}