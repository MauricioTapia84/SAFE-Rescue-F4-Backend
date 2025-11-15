package com.SAFE_Rescue.API_Comunicacion.service;

import com.SAFE_Rescue.API_Comunicacion.modelo.Mensaje;
import com.SAFE_Rescue.API_Comunicacion.modelo.HistorialMensaje; // Modelo de auditoría
import com.SAFE_Rescue.API_Comunicacion.repository.HistorialMensajeRepository; // Repositorio de auditoría
import jakarta.persistence.EntityNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.NoSuchElementException;

/**
 * Servicio encargado de gestionar y registrar los cambios de estado (auditoría) de los mensajes,
 * así como de proveer la consulta de dichos registros.
 * Este servicio se centra en la AUDITORÍA, no en el CRUD del recurso principal Mensaje.
 */
@Service
public class HistorialMensajeService {

    private final HistorialMensajeRepository historialMensajeRepository;
    // Necesario para validar si el mensaje existe
    private final MensajeService mensajeService;

    @Autowired
    public HistorialMensajeService(
            HistorialMensajeRepository historialMensajeRepository,
            MensajeService mensajeService) {
        this.historialMensajeRepository = historialMensajeRepository;
        this.mensajeService = mensajeService;
    }

    public List<HistorialMensaje> findAll() {
        return historialMensajeRepository.findAll();
    }

    // =========================================================================
    // REGISTRO DE CAMBIOS DE ESTADO
    // =========================================================================

    /**
     * Crea y persiste un nuevo registro de historial de cambio de estado para un MENSAJE.
     * Este método es para uso INTERNO (llamado desde MensajeService cuando ocurre un cambio).
     *
     * @param mensaje Mensaje que sufrió el cambio.
     * @param idEstadoAnterior ID del estado previo.
     * @param idEstadoNuevo ID del nuevo estado.
     * @param detalle Descripción del cambio.
     * @return El registro de historial persistido.
     */
    @Transactional
    public HistorialMensaje registrarCambioEstado(
            Mensaje mensaje,
            Integer idEstadoAnterior,
            Integer idEstadoNuevo,
            String detalle) {

        if (mensaje == null || idEstadoAnterior == null || idEstadoNuevo == null || detalle == null || detalle.trim().isEmpty()) {
            throw new IllegalArgumentException("No se puede registrar el historial para Mensaje: Faltan datos obligatorios.");
        }

        HistorialMensaje registro = new HistorialMensaje();
        registro.setMensaje(mensaje); // Asume que HistorialMensaje tiene una propiedad 'mensaje'
        registro.setIdEstadoAnterior(idEstadoAnterior);
        registro.setIdEstadoNuevo(idEstadoNuevo);
        registro.setDetalle(detalle);
        registro.setFechaHistorial(LocalDateTime.now());

        return historialMensajeRepository.save(registro);
    }

    // =========================================================================
    // CONSULTA DEL HISTORIAL
    // =========================================================================

    /**
     * Obtiene todo el historial de cambios de estado para un MENSAJE específico.
     *
     * @param idMensaje El ID del mensaje cuyo historial se desea consultar.
     * @return Lista de HistorialMensaje ordenados por fecha descendente.
     * @throws EntityNotFoundException Si el mensaje no existe.
     */
    public List<HistorialMensaje> obtenerHistorialPorMensaje(Integer idMensaje) {
        try {
            // 1. Validar la existencia del mensaje usando el servicio principal
            mensajeService.findById(idMensaje);
        } catch (NoSuchElementException e) {
            // Convertir NoSuchElementException a EntityNotFoundException para consistencia
            throw new EntityNotFoundException("Mensaje con ID " + idMensaje + " no encontrado.");
        }

        // 2. Buscar registros por el ID del Mensaje
        // Asume que HistorialMensajeRepository tiene un método findByMensajeIdMensaje
        return historialMensajeRepository.findByMensajeIdMensaje(idMensaje);
    }
}