package com.SAFE_Rescue.API_Geolocalizacion.service;

import com.SAFE_Rescue.API_Geolocalizacion.modelo.Cordenadas;
import com.SAFE_Rescue.API_Geolocalizacion.repositoy.CordenadasRepository; // 👈 Asegúrate de crear este Repositorio
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.NoSuchElementException;

/**
 * Servicio para la gestión integral de la entidad Cordenadas.
 * Maneja operaciones CRUD y gestión de excepciones.
 */
@Service
public class CordenadasService {

    // REPOSITORIO INYECTADO
    @Autowired
    private CordenadasRepository cordenadasRepository;

    // MÉTODOS CRUD PRINCIPALES

    /**
     * Obtiene todas las coordenadas de geolocalización registradas.
     *
     * @return Lista completa de geolocalizaciones
     */
    public List<Cordenadas> findAll() {
        return cordenadasRepository.findAll();
    }

    /**
     * Busca una coordenada de geolocalización por su ID único.
     *
     * @param id Identificador de la geolocalización
     * @return Cordenadas encontrada
     * @throws NoSuchElementException Si no se encuentra la geolocalización con el ID
     */
    public Cordenadas findById(Integer id) {
        return cordenadasRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("No se encontró la geolocalización con ID: " + id));
    }

    /**
     * Guarda una nueva coordenada de geolocalización.
     *
     * @param cordenadas Datos de la geolocalización a guardar
     * @return Cordenadas guardada con ID generado
     * @throws IllegalArgumentException Si la geolocalización es nula
     */
    public Cordenadas save(Cordenadas cordenadas) {
        if (cordenadas == null) {
            throw new IllegalArgumentException("La entidad Cordenadas no puede ser nula.");
        }
        try {
            // No se requiere validación por nombre, solo se guarda.
            return cordenadasRepository.save(cordenadas);
        } catch (DataIntegrityViolationException e) {
            // Esto capturaría errores como valores nulos o fuera de rango (aunque ya lo valida la entidad).
            throw new IllegalArgumentException("Error de integridad de datos al guardar la geolocalización.", e);
        }
    }

    /**
     * Actualiza las coordenadas de una geolocalización existente.
     *
     * @param cordenadas Datos actualizados de la geolocalización
     * @param id              Identificador de la geolocalización a actualizar
     * @return Cordenadas actualizada
     * @throws IllegalArgumentException Si la geolocalización es nula
     * @throws NoSuchElementException   Si no se encuentra la geolocalización a actualizar
     */
    public Cordenadas update(Cordenadas cordenadas, Integer id) {
        if (cordenadas == null) {
            throw new IllegalArgumentException("La geolocalización no puede ser nula.");
        }

        // 1. Busca la entidad existente
        Cordenadas antiguaCordenadas = cordenadasRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("Geolocalización no encontrada con ID: " + id));

        // 2. Actualiza solo los campos modificables (Latitud y Longitud)
        antiguaCordenadas.setLatitud(cordenadas.getLatitud());
        antiguaCordenadas.setLongitud(cordenadas.getLongitud());

        try {
            // 3. Guarda y retorna la entidad actualizada
            return cordenadasRepository.save(antiguaCordenadas);
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
        if (!cordenadasRepository.existsById(id)) {
            throw new NoSuchElementException("Geolocalización no encontrada con ID: " + id);
        }
        cordenadasRepository.deleteById(id);
    }
}