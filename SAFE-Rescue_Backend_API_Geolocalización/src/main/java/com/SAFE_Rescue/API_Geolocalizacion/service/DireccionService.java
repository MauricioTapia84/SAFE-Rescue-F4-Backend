package com.SAFE_Rescue.API_Geolocalizacion.service;

import com.SAFE_Rescue.API_Geolocalizacion.modelo.Comuna;
import com.SAFE_Rescue.API_Geolocalizacion.modelo.Coordenadas;
import com.SAFE_Rescue.API_Geolocalizacion.modelo.Direccion;
import com.SAFE_Rescue.API_Geolocalizacion.repositoy.ComunaRepository;
import com.SAFE_Rescue.API_Geolocalizacion.repositoy.CoordenadasRepository;
import com.SAFE_Rescue.API_Geolocalizacion.repositoy.DireccionRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.NoSuchElementException;

/**
 * Servicio para la gestión integral de la entidad Direccion.
 * Maneja operaciones CRUD y valida las relaciones con Comuna y Coordenadas.
 */
@Service
public class DireccionService {

    // REPOSITORIOS INYECTADOS
    @Autowired
    private DireccionRepository direccionRepository;

    @Autowired
    private CoordenadasRepository coordenadasRepository;

    @Autowired
    private ComunaRepository comunaRepository;


    // MÉTODOS CRUD PRINCIPALES

    /**
     * Obtiene todas las direcciones registradas en el sistema.
     *
     * @return Lista completa de direcciones
     */
    public List<Direccion> findAll() {
        return direccionRepository.findAll();
    }

    /**
     * Busca una dirección por su ID único.
     *
     * @param id Identificador de la dirección
     * @return Dirección encontrada
     * @throws NoSuchElementException Si no se encuentra la dirección
     */
    public Direccion findById(Integer id) {
        return direccionRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("No se encontró la dirección con ID: " + id));
    }

    /**
     * Guarda una nueva dirección en el sistema.
     *
     * CORRECCIÓN CRÍTICA: Maneja entidades 'detached' (Coordenadas/Comuna) buscándolas primero
     * para evitar el error "detached entity passed to persist".
     *
     * @param direccion Datos de la dirección a guardar
     * @return Dirección guardada con ID generado
     * @throws IllegalArgumentException Si la dirección no cumple con los parámetros
     */
    @Transactional
    public Direccion save(Direccion direccion) {
        validarDireccion(direccion);

        // 1. Gestionar Coordenadas Detached
        // Si vienen coordenadas con ID, las buscamos en BD para asociar la instancia "managed"
        if (direccion.getCoordenadas() != null && direccion.getCoordenadas().getIdCoordenadas() != null) {
            Coordenadas coordenadasManaged = coordenadasRepository.findById(direccion.getCoordenadas().getIdCoordenadas())
                    .orElseThrow(() -> new RuntimeException("Coordenadas no encontradas con ID: " + direccion.getCoordenadas().getIdCoordenadas()));
            direccion.setCoordenadas(coordenadasManaged);
        }

        // 2. Gestionar Comuna Detached
        // Si viene comuna con ID, la buscamos en BD
        if (direccion.getComuna() != null && direccion.getComuna().getIdComuna() != null) {
            Comuna comunaManaged = comunaRepository.findById(direccion.getComuna().getIdComuna())
                    .orElseThrow(() -> new RuntimeException("Comuna no encontrada con ID: " + direccion.getComuna().getIdComuna()));
            direccion.setComuna(comunaManaged);
        }

        try {
            return direccionRepository.save(direccion);
        } catch (DataIntegrityViolationException e) {
            throw new IllegalArgumentException("Error de integridad de datos al guardar Dirección. Verifique datos.", e);
        }
    }

    /**
     * Actualiza los datos de una dirección existente.
     *
     * @param direccion Datos actualizados de la dirección
     * @param id        Identificador de la dirección a actualizar
     * @return Dirección actualizada
     * @throws IllegalArgumentException Si la dirección es nula o si los datos no cumplen con los parámetros
     * @throws NoSuchElementException   Si no se encuentra la dirección a actualizar
     */
    @Transactional
    public Direccion update(Direccion direccion, Integer id) {
        if (direccion == null) {
            throw new IllegalArgumentException("La entidad Dirección no puede ser nula.");
        }

        validarDireccion(direccion);

        Direccion antiguaDireccion = direccionRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("Dirección no encontrada con ID: " + id));

        // Actualiza los campos específicos
        antiguaDireccion.setCalle(direccion.getCalle());
        antiguaDireccion.setNumero(direccion.getNumero());
        antiguaDireccion.setComplemento(direccion.getComplemento());
        antiguaDireccion.setVilla(direccion.getVilla());

        // Actualiza las relaciones (aplicando la misma lógica de managed entities)
        if (direccion.getComuna() != null && direccion.getComuna().getIdComuna() != null) {
            Comuna comunaManaged = comunaRepository.findById(direccion.getComuna().getIdComuna())
                    .orElseThrow(() -> new RuntimeException("Comuna no encontrada con ID: " + direccion.getComuna().getIdComuna()));
            antiguaDireccion.setComuna(comunaManaged);
        }

        if (direccion.getCoordenadas() != null && direccion.getCoordenadas().getIdCoordenadas() != null) {
            Coordenadas coordenadasManaged = coordenadasRepository.findById(direccion.getCoordenadas().getIdCoordenadas())
                    .orElseThrow(() -> new RuntimeException("Coordenadas no encontradas con ID: " + direccion.getCoordenadas().getIdCoordenadas()));
            antiguaDireccion.setCoordenadas(coordenadasManaged);
        }

        try {
            return direccionRepository.save(antiguaDireccion);
        } catch (DataIntegrityViolationException e) {
            throw new IllegalArgumentException("Error de integridad de datos. Verifique que las entidades asociadas sean válidas.", e);
        }
    }

    /**
     * Elimina una dirección del sistema.
     *
     * @param id Identificador de la dirección a eliminar
     * @throws NoSuchElementException Si no se encuentra la dirección
     */
    public void delete(Integer id) {
        if (!direccionRepository.existsById(id)) {
            throw new NoSuchElementException("Dirección no encontrada con ID: " + id);
        }
        direccionRepository.deleteById(id);
    }

    // MÉTODOS PRIVADOS DE VALIDACIÓN

    /**
     * Valida la entidad Dirección.
     *
     * @param direccion Entidad Dirección
     * @throws IllegalArgumentException Si la dirección no cumple con las reglas de validación
     */
    private void validarDireccion(Direccion direccion) {
        if (direccion.getCalle() == null || direccion.getCalle().trim().isEmpty()) {
            throw new IllegalArgumentException("El nombre de la calle es requerido.");
        }

        if (direccion.getNumero() == null || direccion.getNumero().trim().isEmpty()) {
            throw new IllegalArgumentException("La numeración de la dirección es requerida.");
        }

        if (direccion.getComuna() == null) {
            throw new IllegalArgumentException("La dirección debe estar asociada a una Comuna válida.");
        }

        if (direccion.getCoordenadas() == null) {
            throw new IllegalArgumentException("La dirección debe estar asociada a una Geolocalización válida (Lat/Lng).");
        }

        if (direccion.getCalle().length() > 150) {
            throw new IllegalArgumentException("El nombre de la calle excede el máximo de 150 caracteres.");
        }
    }
}