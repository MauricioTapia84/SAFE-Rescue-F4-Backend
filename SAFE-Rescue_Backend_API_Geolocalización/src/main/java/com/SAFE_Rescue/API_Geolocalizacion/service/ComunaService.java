package com.SAFE_Rescue.API_Geolocalizacion.service;

import com.SAFE_Rescue.API_Geolocalizacion.modelo.Comuna;
import com.SAFE_Rescue.API_Geolocalizacion.repositoy.ComunaRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.NoSuchElementException;

/**
 * Servicio para la gestión integral de la entidad Comuna.
 * Maneja operaciones CRUD, validación de datos y gestión de jerarquía geográfica (depende de Region).
 */
@Service
public class ComunaService {

    // REPOSITORIO INYECTADO
    @Autowired
    private ComunaRepository comunaRepository;

    // MÉTODOS CRUD PRINCIPALES

    /**
     * Obtiene todas las comunas registradas en el sistema.
     *
     * @return Lista completa de comunas
     */
    public List<Comuna> findAll() {
        return comunaRepository.findAll();
    }

    /**
     * Busca una comuna por su ID único.
     *
     * @param id Identificador de la comuna
     * @return Comuna encontrada
     * @throws NoSuchElementException Si no se encuentra la comuna
     */
    public Comuna findById(Integer id) {
        return comunaRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("No se encontró la comuna con ID: " + id));
    }

    /**
     * Guarda una nueva comuna en el sistema.
     *
     * @param comuna Datos de la comuna a guardar
     * @return Comuna guardada con ID generado
     * @throws IllegalArgumentException Si la comuna no cumple con los parámetros (nulos, vacíos)
     */
    public Comuna save(Comuna comuna) {
        validarComuna(comuna);
        try {
            return comunaRepository.save(comuna);
        } catch (DataIntegrityViolationException e) {
            // Error de integridad: puede ser porque la Region no existe (FK) o datos inválidos.
            throw new IllegalArgumentException("Error de integridad de datos. Verifique que la Región asociada exista y que los valores sean válidos.", e);
        }
    }

    /**
     * Actualiza los datos de una comuna existente.
     *
     * @param comuna Datos actualizados de la comuna
     * @param id     Identificador de la comuna a actualizar
     * @return Comuna actualizada
     * @throws IllegalArgumentException Si la comuna es nula o si los datos no cumplen con los parámetros
     * @throws NoSuchElementException   Si no se encuentra la comuna a actualizar
     */
    public Comuna update(Comuna comuna, Integer id) {
        if (comuna == null) {
            throw new IllegalArgumentException("La entidad Comuna no puede ser nula.");
        }

        validarComuna(comuna);

        Comuna antiguaComuna = comunaRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("Comuna no encontrada con ID: " + id));

        // Actualiza los campos
        antiguaComuna.setNombre(comuna.getNombre());
        antiguaComuna.setCodigoPostal(comuna.getCodigoPostal());
        // Actualiza la Región asociada
        antiguaComuna.setRegion(comuna.getRegion());

        try {
            return comunaRepository.save(antiguaComuna);
        } catch (DataIntegrityViolationException e) {
            throw new IllegalArgumentException("Error de integridad de datos. Verifique que la Región asociada sea válida.", e);
        }
    }

    /**
     * Elimina una comuna del sistema.
     *
     * @param id Identificador de la comuna a eliminar
     * @throws NoSuchElementException Si no se encuentra la comuna
     * @throws IllegalArgumentException Si la comuna tiene direcciones asociadas (violación FK)
     */
    public void delete(Integer id) {
        if (!comunaRepository.existsById(id)) {
            throw new NoSuchElementException("Comuna no encontrada con ID: " + id);
        }
        try {
            comunaRepository.deleteById(id);
        } catch (DataIntegrityViolationException e) {
            // Esto es crucial si la comuna tiene direcciones asociadas
            throw new IllegalArgumentException("No se puede eliminar la comuna porque tiene direcciones asociadas.", e);
        }
    }

    // MÉTODOS PRIVADOS DE VALIDACIÓN

    /**
     * Valida la entidad Comuna.
     *
     * @param comuna Entidad Comuna
     * @throws IllegalArgumentException Si la comuna no cumple con las reglas de validación
     */
    private void validarComuna(Comuna comuna) {
        if (comuna.getNombre() == null || comuna.getNombre().trim().isEmpty()) {
            throw new IllegalArgumentException("El nombre de la comuna es requerido.");
        }

        if (comuna.getRegion() == null) {
            throw new IllegalArgumentException("La comuna debe estar asociada a una entidad Región válida.");
        }

        // Validaciones de longitud (asumiendo 100 caracteres para el nombre)
        if (comuna.getNombre().length() > 100) {
            throw new IllegalArgumentException("El nombre de la comuna excede el máximo de 100 caracteres.");
        }

        // Se puede añadir más validación si el codigoPostal es estrictamente requerido.
    }
}