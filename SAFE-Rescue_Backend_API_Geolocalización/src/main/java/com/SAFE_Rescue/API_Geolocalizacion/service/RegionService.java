package com.SAFE_Rescue.API_Geolocalizacion.service;

import com.SAFE_Rescue.API_Geolocalizacion.modelo.Region;
import com.SAFE_Rescue.API_Geolocalizacion.repositoy.RegionRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.NoSuchElementException;

/**
 * Servicio para la gestión integral de la entidad Region.
 * Maneja operaciones CRUD, validación de datos y gestión de jerarquía geográfica.
 */
@Service
public class RegionService {

    // REPOSITORIO INYECTADO
    @Autowired
    private RegionRepository regionRepository;

    // MÉTODOS CRUD PRINCIPALES

    /**
     * Obtiene todas las regiones registradas en el sistema.
     *
     * @return Lista completa de regiones
     */
    public List<Region> findAll() {
        return regionRepository.findAll();
    }

    /**
     * Busca una región por su ID único.
     *
     * @param id Identificador de la región
     * @return Región encontrada
     * @throws NoSuchElementException Si no se encuentra la región
     */
    public Region findById(Integer id) {
        return regionRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("No se encontró la región con ID: " + id));
    }

    /**
     * Guarda una nueva región en el sistema.
     *
     * @param region Datos de la región a guardar
     * @return Región guardada con ID generado
     * @throws IllegalArgumentException Si la región no cumple con los parámetros o ya existe (identificación duplicada)
     */
    public Region save(Region region) {
        validarRegion(region);
        try {
            return regionRepository.save(region);
        } catch (DataIntegrityViolationException e) {
            // Error de integridad: puede ser por clave foránea (Pais no existe) o por restricción UNIQUE (identificación duplicada)
            throw new IllegalArgumentException("Error de integridad de datos. Verifique que el País exista y que la identificación de la región no esté duplicada.", e);
        }
    }

    /**
     * Actualiza los datos de una región existente.
     *
     * @param region Datos actualizados de la región
     * @param id     Identificador de la región a actualizar
     * @return Región actualizada
     * @throws IllegalArgumentException Si la región es nula o si los datos no cumplen con los parámetros
     * @throws NoSuchElementException   Si no se encuentra la región a actualizar
     */
    public Region update(Region region, Integer id) {
        if (region == null) {
            throw new IllegalArgumentException("La entidad Región no puede ser nula.");
        }

        validarRegion(region);

        Region antiguaRegion = regionRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("Región no encontrada con ID: " + id));

        // Actualiza los campos
        antiguaRegion.setNombre(region.getNombre());
        antiguaRegion.setIdentificacion(region.getIdentificacion());
        // El País asociado también puede ser actualizado si es necesario (asumiendo que viene en el objeto región)

        try {
            return regionRepository.save(antiguaRegion);
        } catch (DataIntegrityViolationException e) {
            throw new IllegalArgumentException("Error de integridad de datos. El nombre o identificación de la región ya existe o el País no es válido.", e);
        }
    }

    /**
     * Elimina una región del sistema.
     *
     * @param id Identificador de la región a eliminar
     * @throws NoSuchElementException Si no se encuentra la región
     * @throws IllegalArgumentException Si la región tiene comunas asociadas (violación FK)
     */
    public void delete(Integer id) {
        if (!regionRepository.existsById(id)) {
            throw new NoSuchElementException("Región no encontrada con ID: " + id);
        }
        try {
            regionRepository.deleteById(id);
        } catch (DataIntegrityViolationException e) {
            // Esto es crucial para manejar el caso en que la región tiene comunas asociadas (FK)
            throw new IllegalArgumentException("No se puede eliminar la región porque tiene comunas asociadas.", e);
        }
    }

    // MÉTODOS PRIVADOS DE VALIDACIÓN

    /**
     * Valida la entidad Región.
     *
     * @param region Entidad Región
     * @throws IllegalArgumentException Si la región no cumple con las reglas de validación
     */
    private void validarRegion(Region region) {
        if (region.getNombre() == null || region.getNombre().trim().isEmpty()) {
            throw new IllegalArgumentException("El nombre de la región es requerido.");
        }

        if (region.getIdentificacion() == null || region.getIdentificacion().trim().isEmpty()) {
            throw new IllegalArgumentException("La identificación (número/abreviatura) de la región es requerida.");
        }

        // Asumiendo que las longitudes máximas son 100 y 5 respectivamente (como en la entidad)
        if (region.getNombre().length() > 100) {
            throw new IllegalArgumentException("El nombre de la región excede el máximo de 100 caracteres.");
        }

        if (region.getIdentificacion().length() > 5) {
            throw new IllegalArgumentException("La identificación de la región excede el máximo de 5 caracteres.");
        }
    }
}