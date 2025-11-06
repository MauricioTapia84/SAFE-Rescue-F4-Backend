package com.SAFE_Rescue.API_Perfiles.service;

import com.SAFE_Rescue.API_Perfiles.config.WebClienteConfig;
import com.SAFE_Rescue.API_Perfiles.modelo.DireccionDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.NoSuchElementException;

/**
 * Servicio adaptador para interactuar con la API de Geolocalización.
 * Este servicio NO tiene repositorio local. Su función es centralizar
 * la comunicación externa a través de WebClientConfig.
 */
@Service
public class DireccionService {

    @Autowired
    private WebClienteConfig webClienteConfig;

    /**
     * Busca y valida la existencia de una dirección en la API externa de Geolocalización.
     * Si la dirección existe, retorna el DTO. Si no existe, WebClienteConfig lanza NoSuchElementException.
     * @param id El ID de la dirección.
     * @return DireccionDTO con los datos de la dirección.
     * @throws NoSuchElementException Si la dirección no existe en la API externa.
     */
    public DireccionDTO findById(Integer id) {
        // Toda la lógica de la llamada HTTP, manejo de 404 y mapeo a DTO está en WebClienteConfig
        return webClienteConfig.getDireccionById(id);
    }
}