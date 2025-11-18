package com.SAFE_Rescue.API_Donaciones.service;

import com.SAFE_Rescue.API_Donaciones.config.UsuarioClient;
import com.SAFE_Rescue.API_Donaciones.modelo.Donacion;
import com.SAFE_Rescue.API_Donaciones.repository.DonacionRepository;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.NoSuchElementException;

/**
 * @class DonacionService
 * @brief Servicio para la gestión integral de Donaciones.
 * Maneja operaciones CRUD y validación de la existencia de entidades externas (Donante).
 */
@Service
@Transactional
public class DonacionService {

    // REPOSITORIOS LOCALES (JPA) INYECTADOS
    @Autowired private DonacionRepository donacionRepository;

    // CLIENTES/SERVICIOS INYECTADOS (APIs externas)
    // Se utiliza para verificar la existencia del Donante mediante su ID.
    @Autowired private UsuarioClient usuarioClient;

    // MÉTODOS CRUD PRINCIPALES

    /**
     * Obtiene todas las Donaciones registradas en el sistema.
     * @return Lista completa de Donaciones
     */
    public List<Donacion> findAll() {
        return donacionRepository.findAll();
    }

    /**
     * Busca una Donacion por su ID único.
     * @param id Identificador de la Donacion
     * @return Donacion encontrado
     * @throws NoSuchElementException Si no se encuentra la donación
     */
    public Donacion findById(Integer id) {
        return donacionRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("No se encontró Donacion con ID: " + id));
    }

    /**
     * Guarda una nueva donación en el sistema.
     * Realiza validaciones y verifica la existencia del Donante externo.
     * @param donacion Datos de la donación a guardar
     * @return Donacion guardada con ID generado
     * @throws IllegalArgumentException Si la referencia del Donante no existe.
     */
    public Donacion save(Donacion donacion) {
        try {
            // 1. Validar existencia del Donante (referencia externa)
            validarExistenciaDonante(donacion.getIdDonante());

            // 2. Establecer fecha de donación si no está presente
            if (donacion.getFechaDonacion() == null) {
                donacion.setFechaDonacion(LocalDateTime.now());
            }

            // 3. Guardar la Donacion en la base de datos local
            return donacionRepository.save(donacion);
        } catch (NoSuchElementException e) {
            // Captura si la validación de existencia del donante falló
            throw new IllegalArgumentException("Error de referencia: " + e.getMessage(), e);
        } catch (Exception e) {
            // Captura errores genéricos
            throw new RuntimeException("Error al guardar la donación: " + e.getMessage(), e);
        }
    }

    /**
     * Actualiza los datos de una donación existente.
     *
     * @param donacion Datos actualizados de la donación
     * @param id Identificador de la donación a actualizar
     * @return Donacion actualizada
     * @throws IllegalArgumentException Si el Donante ID no existe.
     */
    public Donacion update(Donacion donacion, Integer id) {
        if (donacion == null) {
            throw new IllegalArgumentException("La donación no puede ser nula");
        }

        Donacion donacionExistente = findById(id);

        try {
            // 1. Actualizar campos modificables (monto, método, homenaje)
            if (donacion.getMonto() != null) {
                donacionExistente.setMonto(donacion.getMonto());
            }
            if (donacion.getMetodoPago() != null) {
                donacionExistente.setMetodoPago(donacion.getMetodoPago());
            }
            // Los campos opcionales pueden ser actualizados a null si se desea
            if (donacion.getTipoHomenaje() != null) {
                donacionExistente.setTipoHomenaje(donacion.getTipoHomenaje());
            }
            if (donacion.getDetalleHomenaje() != null) {
                donacionExistente.setDetalleHomenaje(donacion.getDetalleHomenaje());
            }

            // Si se intenta cambiar el Donante, validar su existencia
            if (donacion.getIdDonante() != null && !donacion.getIdDonante().equals(donacionExistente.getIdDonante())) {
                validarExistenciaDonante(donacion.getIdDonante());
                donacionExistente.setIdDonante(donacion.getIdDonante());
            }

            // 2. Guardar la donación actualizada
            return donacionRepository.save(donacionExistente);
        } catch (NoSuchElementException e) {
            throw new IllegalArgumentException("Error de referencia en la actualización: " + e.getMessage(), e);
        } catch (Exception e) {
            throw new RuntimeException("Error al actualizar donación con ID " + id + ": " + e.getMessage(), e);
        }
    }

    /**
     * Elimina una donación del sistema.
     * @param id Identificador de la donación a eliminar
     * @throws NoSuchElementException Si no se encuentra la donación
     */
    public void delete(Integer id) {
        Donacion donacion = findById(id);
        donacionRepository.delete(donacion);
    }

    // --- MÉTODOS PRIVADOS DE VALIDACIÓN ---

    /**
     * Valida la existencia del Donante mediante la API externa (UsuarioClient).
     * @param idDonante El ID del donante a buscar.
     * @throws NoSuchElementException Si el Donante no es encontrado.
     */
    private void validarExistenciaDonante(Integer idDonante) {
        if (idDonante == null) {
            throw new IllegalArgumentException("El ID del donante es obligatorio.");
        }

        // Asumiendo que usuarioClient.findById(id) devuelve el objeto Donante/Usuario (no nulo) si existe.
        Object donanteFound = usuarioClient.findById(idDonante);

        if (donanteFound == null) {
            throw new IllegalArgumentException("Donante no encontrado con ID: " + idDonante);
        }
    }

    /**
     * Busca todas las donaciones realizadas por un donante.
     * @param idDonante ID del donante
     * @return Lista de donaciones
     */
    public List<Donacion> findByDonante(Integer idDonante) {
        return donacionRepository.findByIdDonante(idDonante);
    }
}