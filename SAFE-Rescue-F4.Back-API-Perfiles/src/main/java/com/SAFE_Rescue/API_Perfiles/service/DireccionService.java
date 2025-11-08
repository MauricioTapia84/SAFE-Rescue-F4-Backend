package com.SAFE_Rescue.API_Perfiles.service;

import com.SAFE_Rescue.API_Perfiles.config.GeolocalizacionClient; // NUEVA INYECCIÓN
import com.SAFE_Rescue.API_Perfiles.modelo.DireccionDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.NoSuchElementException;

/**
 * Servicio adaptador para interactuar con la API de Geolocalización.
 * Este servicio NO tiene repositorio local. Su función es centralizar
 * la comunicación externa a través del GeolocalizacionClient dedicado.
 */
@Service
public class DireccionService {

    // CORRECCIÓN: Inyectamos el cliente dedicado, que contiene la lógica de WebClient
    @Autowired
    private GeolocalizacionClient geolocalizacionClient;

    /**
     * Busca y valida la existencia de una dirección en la API externa de Geolocalización.
     * Si la dirección existe, retorna el DTO.
     * @param id El ID de la dirección.
     * @return DireccionDTO con los datos de la dirección.
     * @throws RuntimeException Si el servicio externo falla (errores 4xx o 5xx).
     */
    public DireccionDTO findById(Integer id) {
        // Toda la lógica de la llamada HTTP, manejo de 4xx/5xx y mapeo a DTO está
        // centralizada y encapsulada dentro de GeolocalizacionClient.
        // Capturamos cualquier error lanzado por el cliente y lo propagamos.
        try {
            return geolocalizacionClient.getDireccionById(id);
        } catch (RuntimeException e) {
            // Se propaga la excepción, pero se podría envolver en una excepción de negocio
            // si se quisiera, aunque la RuntimeException del cliente ya es descriptiva.
            throw e;
        }
    }
}