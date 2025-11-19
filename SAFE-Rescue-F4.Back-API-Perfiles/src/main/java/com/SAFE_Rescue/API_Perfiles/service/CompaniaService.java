package com.SAFE_Rescue.API_Perfiles.service;

import com.SAFE_Rescue.API_Perfiles.config.GeolocalizacionClient;
import com.SAFE_Rescue.API_Perfiles.modelo.Compania;
import com.SAFE_Rescue.API_Perfiles.repository.CompaniaRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.NoSuchElementException;

/**
 * Servicio para la gestión de compañías de bomberos.
 */
@Service
public class CompaniaService {

    @Autowired
    private CompaniaRepository companiaRepository;

    // Se inyecta DireccionService para validar que la dirección referenciada exista en la API externa.
    @Autowired
    private GeolocalizacionClient geolocalizacionClient;

    // Límite de caracteres definido en la entidad para el nombre
    private static final int NOMBRE_MAX_LENGTH = 50;


    /**
     * Obtiene todas las compañías registradas.
     * @return Lista de todas las compañías.
     */
    public List<Compania> findAll() {
        return companiaRepository.findAll();
    }

    /**
     * Busca una compañía por su ID.
     * @param id Identificador de la compañía.
     * @return La entidad Compania si se encuentra.
     * @throws NoSuchElementException si la compañía no existe.
     */
    public Compania findById(Integer id) {
        return companiaRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("Compañía no encontrada con ID: " + id));
    }

    /**
     * Guarda una nueva compañía en la base de datos.
     * Realiza validaciones previas para asegurar la integridad de los datos.
     * @param compania Objeto Compania a guardar.
     * @return La compañía guardada.
     * @throws IllegalArgumentException si la validación falla (nombre inválido, duplicado, o ID de dirección inexistente).
     */
    public Compania save(Compania compania) {
        validarCompania(compania);
        // El campo 'nombre' debe ser único. DataIntegrityViolationException capturará si es duplicado.
        try {
            return companiaRepository.save(compania);
        } catch (DataIntegrityViolationException e) {
            throw new IllegalArgumentException("Error al guardar la compañía: El nombre de la compañía ya existe o faltan datos obligatorios.", e);
        }
    }

    /**
     * Actualiza una compañía existente.
     * @param companiaDetails Objeto Compania con los nuevos datos.
     * @param id Identificador de la compañía a actualizar.
     * @return La compañía actualizada.
     * @throws NoSuchElementException si la compañía no existe.
     * @throws IllegalArgumentException si la validación falla (nombre inválido, duplicado, o ID de dirección inexistente).
     */
    public Compania update(Compania companiaDetails, Integer id) {
        // 1. Validar los datos de entrada, incluyendo la existencia de la dirección
        validarCompania(companiaDetails);

        // 2. Buscar la entidad existente
        Compania companiaExistente = findById(id);

        // 3. Aplicar las actualizaciones
        companiaExistente.setNombre(companiaDetails.getNombre());

        // **CORRECCIÓN** Se asigna el ID de la dirección (clave foránea lógica)
        companiaExistente.setIdDireccion(companiaDetails.getIdDireccion());

        // 4. Guardar y manejar la excepción de integridad (e.g., nombre duplicado)
        try {
            return companiaRepository.save(companiaExistente);
        } catch (DataIntegrityViolationException e) {
            throw new IllegalArgumentException("Error al actualizar la compañía: El nombre de la compañía ya existe.", e);
        }
    }

    /**
     * Elimina una compañía por su ID.
     * @param id Identificador de la compañía a eliminar.
     * @throws NoSuchElementException si la compañía no existe.
     * @throws DataIntegrityViolationException si hay referencias activas (bomberos, equipos) que impiden la eliminación.
     */
    public void delete(Integer id) {
        if (!companiaRepository.existsById(id)) {
            throw new NoSuchElementException("No se puede eliminar. Compañía no encontrada con ID: " + id);
        }
        try {
            companiaRepository.deleteById(id);
        } catch (DataIntegrityViolationException e) {
            // Esto ocurre si la compañía tiene entidades asociadas (e.g., Bomberos, Equipos)
            throw new DataIntegrityViolationException("No se puede eliminar la compañía con ID " + id + " porque tiene registros asociados (Bomberos o Equipos).", e);
        }
    }


    /**
     * Método de validación interna para la entidad Compania.
     * Valida la existencia del ID de Dirección usando DireccionService.
     * @param compania Entidad a validar.
     * @throws IllegalArgumentException si algún campo obligatorio o de formato no es válido.
     */
    private void validarCompania(Compania compania) {
        if (compania.getNombre() == null || compania.getNombre().isBlank()) {
            throw new IllegalArgumentException("El nombre de la compañía es obligatorio.");
        }
        if (compania.getNombre().length() > NOMBRE_MAX_LENGTH) {
            throw new IllegalArgumentException("El nombre no puede exceder los " + NOMBRE_MAX_LENGTH + " caracteres.");
        }

        // **CORRECCIÓN** Validar el ID de Dirección (clave foránea lógica)
        if (compania.getIdDireccion() == null) {
            throw new IllegalArgumentException("El ID de la dirección de la compañía es obligatorio.");
        }

        // Validamos la existencia del ID de Dirección usando el servicio.
        try {
            // Se asume que DireccionService utiliza WebClientConfig para verificar la existencia en la API externa.
            geolocalizacionClient.getDireccionById(compania.getIdDireccion());
        } catch (NoSuchElementException e) {
            throw new IllegalArgumentException("El ID de dirección asociado (" + compania.getIdDireccion() + ") no existe en la API externa de Geolocalización.", e);
        }
    }
}