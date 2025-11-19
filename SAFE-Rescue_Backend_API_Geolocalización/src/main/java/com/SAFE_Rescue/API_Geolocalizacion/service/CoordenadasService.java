package com.SAFE_Rescue.API_Geolocalizacion.service;

import com.SAFE_Rescue.API_Geolocalizacion.modelo.Coordenadas;
import com.SAFE_Rescue.API_Geolocalizacion.repositoy.CoordenadasRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.NoSuchElementException;

/**
 * Servicio para la gestión integral de la entidad Coordenadas.
 * Maneja operaciones CRUD y gestión de excepciones.
 */
@Service
public class CoordenadasService { // Cambiado de CordenadasService a CoordenadasService

    // REPOSITORIO INYECTADO
    @Autowired
    private CoordenadasRepository coordenadasRepository;

    // MÉTODOS CRUD PRINCIPALES

    /**
     * Obtiene todas las coordenadas registradas.
     *
     * @return Lista completa de coordenadas
     */
    public List<Coordenadas> findAll() { // Cambiado el tipo de retorno
        return coordenadasRepository.findAll();
    }

    /**
     * Busca una coordenada por su ID único.
     *
     * @param id Identificador de las coordenadas
     * @return Coordenadas encontradas
     * @throws NoSuchElementException Si no se encuentran las coordenadas con el ID
     */
    public Coordenadas findById(Integer id) { // Cambiado el tipo de retorno
        return coordenadasRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("No se encontraron las coordenadas con ID: " + id)); // Mensaje actualizado
    }

    /**
     * Guarda nuevas coordenadas.
     *
     * @param coordenadas Datos de las coordenadas a guardar
     * @return Coordenadas guardadas con ID generado
     * @throws IllegalArgumentException Si las coordenadas son nulas
     */
    public Coordenadas save(Coordenadas coordenadas) { // Cambiado parámetro y tipo de retorno
        if (coordenadas == null) {
            throw new IllegalArgumentException("La entidad Coordenadas no puede ser nula."); // Mensaje actualizado
        }
        try {
            return coordenadasRepository.save(coordenadas);
        } catch (DataIntegrityViolationException e) {
            throw new IllegalArgumentException("Error de integridad de datos al guardar las coordenadas.", e); // Mensaje actualizado
        }
    }

    /**
     * Actualiza coordenadas existentes.
     *
     * @param coordenadas Datos actualizados de las coordenadas
     * @param id Identificador de las coordenadas a actualizar
     * @return Coordenadas actualizadas
     * @throws IllegalArgumentException Si las coordenadas son nulas
     * @throws NoSuchElementException Si no se encuentran las coordenadas a actualizar
     */
    public Coordenadas update(Coordenadas coordenadas, Integer id) { // Cambiado parámetro y tipo de retorno
        if (coordenadas == null) {
            throw new IllegalArgumentException("Las coordenadas no pueden ser nulas."); // Mensaje actualizado
        }

        // 1. Busca la entidad existente
        Coordenadas antiguasCoordenadas = coordenadasRepository.findById(id) // Cambiado el nombre de la variable
                .orElseThrow(() -> new NoSuchElementException("Coordenadas no encontradas con ID: " + id)); // Mensaje actualizado

        // 2. Actualiza solo los campos modificables (Latitud y Longitud)
        antiguasCoordenadas.setLatitud(coordenadas.getLatitud());
        antiguasCoordenadas.setLongitud(coordenadas.getLongitud());

        try {
            // 3. Guarda y retorna la entidad actualizada
            return coordenadasRepository.save(antiguasCoordenadas);
        } catch (DataIntegrityViolationException e) {
            throw new IllegalArgumentException("Error al actualizar. Verifique la validez de las coordenadas.", e);
        }
    }

    /**
     * Elimina coordenadas del sistema.
     *
     * @param id Identificador de las coordenadas a eliminar
     * @throws NoSuchElementException Si no se encuentran las coordenadas
     */
    public void delete(Integer id) {
        if (!coordenadasRepository.existsById(id)) {
            throw new NoSuchElementException("Coordenadas no encontradas con ID: " + id); // Mensaje actualizado
        }
        coordenadasRepository.deleteById(id);
    }
}