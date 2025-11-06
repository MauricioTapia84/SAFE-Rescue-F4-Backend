package com.SAFE_Rescue.API_Geolocalizacion.service;

import com.SAFE_Rescue.API_Geolocalizacion.modelo.Pais;
import com.SAFE_Rescue.API_Geolocalizacion.repositoy.PaisRepository; // 👈 Asegúrate de crear este Repositorio
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.NoSuchElementException;

/**
 * Servicio para la gestión integral de la entidad Pais.
 * Maneja operaciones CRUD y validación de datos.
 */
@Service
public class PaisService {

    // REPOSITORIO INYECTADO
    @Autowired
    private PaisRepository paisRepository;

    // MÉTODOS CRUD PRINCIPALES

    /**
     * Obtiene todos los países registrados en el sistema.
     *
     * @return Lista completa de países
     */
    public List<Pais> findAll() {
        return paisRepository.findAll();
    }

    /**
     * Busca un país por su ID único.
     *
     * @param id Identificador del país
     * @return País encontrado
     * @throws NoSuchElementException Si no se encuentra el país
     */
    public Pais findById(Integer id) {
        return paisRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("No se encontró el país con ID: " + id));
    }

    /**
     * Guarda un nuevo país en el sistema.
     *
     * @param pais Datos del país a guardar
     * @return País guardado con ID generado
     * @throws IllegalArgumentException Si el país no cumple con los parámetros (nulos, vacíos) o ya existe (código ISO/nombre)
     */
    public Pais save(Pais pais) {
        validarPais(pais);
        try {
            return paisRepository.save(pais);
        } catch (DataIntegrityViolationException e) {
            throw new IllegalArgumentException("Error de integridad de datos. El país ya existe o tiene valores inválidos (nombre/código ISO duplicado).", e);
        }
    }

    /**
     * Actualiza los datos de un país existente.
     *
     * @param pais Datos actualizados del país
     * @param id   Identificador del país a actualizar
     * @return País actualizado
     * @throws IllegalArgumentException Si el país es nulo o si los datos no cumplen con los parámetros
     * @throws NoSuchElementException   Si no se encuentra el país a actualizar
     */
    public Pais update(Pais pais, Integer id) {
        if (pais == null) {
            throw new IllegalArgumentException("La entidad País no puede ser nula.");
        }

        validarPais(pais);

        Pais antiguoPais = paisRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("País no encontrado con ID: " + id));

        // Actualiza los campos
        antiguoPais.setNombre(pais.getNombre());
        antiguoPais.setCodigoIso(pais.getCodigoIso());

        try {
            return paisRepository.save(antiguoPais);
        } catch (DataIntegrityViolationException e) {
            throw new IllegalArgumentException("Error de integridad de datos. El nombre o código ISO del país ya existe.", e);
        }
    }

    /**
     * Elimina un país del sistema.
     *
     * @param id Identificador del país a eliminar
     * @throws NoSuchElementException Si no se encuentra el país
     */
    public void delete(Integer id) {
        if (!paisRepository.existsById(id)) {
            throw new NoSuchElementException("País no encontrado con ID: " + id);
        }
        try {
            paisRepository.deleteById(id);
        } catch (DataIntegrityViolationException e) {
            // Esto es crucial si tienes la restricción de que las Regiones no se pueden eliminar si tienen Comunas asociadas.
            throw new IllegalArgumentException("No se puede eliminar el país porque tiene regiones asociadas.", e);
        }
    }

    // MÉTODOS PRIVADOS DE VALIDACIÓN

    /**
     * Valida la entidad País.
     *
     * @param pais Entidad País
     * @throws IllegalArgumentException Si el país no cumple con las reglas de validación
     */
    private void validarPais(Pais pais) {
        if (pais.getNombre() == null || pais.getNombre().trim().isEmpty()) {
            throw new IllegalArgumentException("El nombre del país es requerido.");
        }

        if (pais.getCodigoIso() == null || pais.getCodigoIso().trim().isEmpty()) {
            throw new IllegalArgumentException("El código ISO del país es requerido.");
        }

        // Asumiendo que el código ISO debe ser de 3 caracteres (ISO 3166-1 alpha-3)
        if (pais.getCodigoIso().length() != 3) {
            throw new IllegalArgumentException("El código ISO debe tener exactamente 3 caracteres.");
        }
    }
}