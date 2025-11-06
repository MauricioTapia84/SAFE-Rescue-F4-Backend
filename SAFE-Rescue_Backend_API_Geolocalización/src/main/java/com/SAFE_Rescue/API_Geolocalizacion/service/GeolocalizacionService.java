package com.SAFE_Rescue.API_Geolocalizacion.service;

import com.SAFE_Rescue.API_Geolocalizacion.modelo.Geolocalizacion;
import com.SAFE_Rescue.API_Geolocalizacion.repositoy.GeolocalizacionRepository; // 👈 Asegúrate de crear este Repositorio
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.NoSuchElementException;

/**
 * Servicio para la gestión integral de la entidad Geolocalizacion.
 * Maneja operaciones CRUD y gestión de excepciones.
 */
@Service
public class GeolocalizacionService {

    // REPOSITORIO INYECTADO
    @Autowired
    private GeolocalizacionRepository geolocalizacionRepository;

    // MÉTODOS CRUD PRINCIPALES

    /**
     * Obtiene todas las coordenadas de geolocalización registradas.
     *
     * @return Lista completa de geolocalizaciones
     */
    public List<Geolocalizacion> findAll() {
        return geolocalizacionRepository.findAll();
    }

    /**
     * Busca una coordenada de geolocalización por su ID único.
     *
     * @param id Identificador de la geolocalización
     * @return Geolocalizacion encontrada
     * @throws NoSuchElementException Si no se encuentra la geolocalización con el ID
     */
    public Geolocalizacion findById(Integer id) {
        return geolocalizacionRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("No se encontró la geolocalización con ID: " + id));
    }

    /**
     * Guarda una nueva coordenada de geolocalización.
     *
     * @param geolocalizacion Datos de la geolocalización a guardar
     * @return Geolocalizacion guardada con ID generado
     * @throws IllegalArgumentException Si la geolocalización es nula
     */
    public Geolocalizacion save(Geolocalizacion geolocalizacion) {
        if (geolocalizacion == null) {
            throw new IllegalArgumentException("La entidad Geolocalizacion no puede ser nula.");
        }
        try {
            // No se requiere validación por nombre, solo se guarda.
            return geolocalizacionRepository.save(geolocalizacion);
        } catch (DataIntegrityViolationException e) {
            // Esto capturaría errores como valores nulos o fuera de rango (aunque ya lo valida la entidad).
            throw new IllegalArgumentException("Error de integridad de datos al guardar la geolocalización.", e);
        }
    }

    /**
     * Actualiza las coordenadas de una geolocalización existente.
     *
     * @param geolocalizacion Datos actualizados de la geolocalización
     * @param id              Identificador de la geolocalización a actualizar
     * @return Geolocalizacion actualizada
     * @throws IllegalArgumentException Si la geolocalización es nula
     * @throws NoSuchElementException   Si no se encuentra la geolocalización a actualizar
     */
    public Geolocalizacion update(Geolocalizacion geolocalizacion, Integer id) {
        if (geolocalizacion == null) {
            throw new IllegalArgumentException("La geolocalización no puede ser nula.");
        }

        // 1. Busca la entidad existente
        Geolocalizacion antiguaGeolocalizacion = geolocalizacionRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("Geolocalización no encontrada con ID: " + id));

        // 2. Actualiza solo los campos modificables (Latitud y Longitud)
        antiguaGeolocalizacion.setLatitud(geolocalizacion.getLatitud());
        antiguaGeolocalizacion.setLongitud(geolocalizacion.getLongitud());

        try {
            // 3. Guarda y retorna la entidad actualizada
            return geolocalizacionRepository.save(antiguaGeolocalizacion);
        } catch (DataIntegrityViolationException e) {
            throw new IllegalArgumentException("Error al actualizar. Verifique la validez de las coordenadas.", e);
        }
    }

    /**
     * Elimina una geolocalización del sistema.
     *
     * @param id Identificador de la geolocalización a eliminar
     * @throws NoSuchElementException Si no se encuentra la geolocalización
     */
    public void delete(Integer id) {
        if (!geolocalizacionRepository.existsById(id)) {
            throw new NoSuchElementException("Geolocalización no encontrada con ID: " + id);
        }
        geolocalizacionRepository.deleteById(id);
    }
}