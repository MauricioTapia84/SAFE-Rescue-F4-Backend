package com.SAFE_Rescue.API_Incidentes.service;

import com.SAFE_Rescue.API_Incidentes.modelo.HistorialIncidente; // Nueva entidad de historial
import com.SAFE_Rescue.API_Incidentes.modelo.Incidente; // Entidad principal a auditar
import com.SAFE_Rescue.API_Incidentes.repository.HistorialIncidenteRepository; // Nuevo Repositorio
import jakarta.persistence.EntityNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Servicio encargado de gestionar y registrar los cambios de estado de los Incidentes
 * en la tabla de historial (auditoría), así como de proveer la consulta
 * de dichos registros.
 */
@Service
public class HistorialIncidenteService {

    private final HistorialIncidenteRepository historialIncidenteRepository;
    // Necesario para validar si el Incidente existe antes de registrar/consultar su historial.
    private final IncidenteService incidenteService;


    @Autowired
    public HistorialIncidenteService(
            HistorialIncidenteRepository historialIncidenteRepository,
            IncidenteService incidenteService) {
        this.historialIncidenteRepository = historialIncidenteRepository;
        this.incidenteService = incidenteService;
    }

    /**
     * Obtiene todos los registros de historial de todos los incidentes.
     * @return Lista de HistorialIncidentes.
     */
    public List<HistorialIncidente> findAll() {
        return historialIncidenteRepository.findAll();
    }

    // =========================================================================
    // REGISTRO DE CAMBIOS DE ESTADO
    // =========================================================================

    /**
     * Crea y persiste un nuevo registro de historial de cambio de estado para un INCIDENTE.
     * Este método es para uso INTERNO (llamado desde IncidenteService).
     */
    public HistorialIncidente registrarCambioEstado(
            Incidente incidente,
            Integer idEstadoAnterior,
            Integer idEstadoNuevo,
            String detalle) {

        if (incidente == null || incidente.getIdIncidente() == null || idEstadoAnterior == null || idEstadoNuevo == null || detalle == null || detalle.trim().isEmpty()) {
            throw new IllegalArgumentException("No se puede registrar el historial para el Incidente: Faltan datos obligatorios o el incidente es nulo.");
        }

        HistorialIncidente registro = new HistorialIncidente();
        registro.setIncidente(incidente);
        registro.setIdEstadoAnterior(idEstadoAnterior);
        registro.setIdEstadoNuevo(idEstadoNuevo);
        registro.setDetalle(detalle);
        registro.setFechaHistorial(LocalDateTime.now());

        return historialIncidenteRepository.save(registro);
    }


    // =========================================================================
    // CONSULTA DEL HISTORIAL
    // =========================================================================

    /**
     * Obtiene todo el historial de cambios de estado para un INCIDENTE específico.
     *
     * @param idIncidente El ID del incidente cuyo historial se desea consultar.
     * @return Lista de HistorialIncidentes ordenados por fecha descendente.
     * @throws EntityNotFoundException Si el Incidente no existe.
     */
    public List<HistorialIncidente> obtenerHistorialPorIncidente(Integer idIncidente) {
        // Verifica si el incidente existe. Asumo que findById en IncidenteService lanza una excepción
        // o devuelve null si no lo encuentra. Si devuelve un Optional vacío, se debe manejar apropiadamente.
        if (incidenteService.findById(idIncidente) == null) {
            throw new EntityNotFoundException("Incidente con ID " + idIncidente + " no encontrado.");
        }

        // Asumo que se usa un método de Spring Data JPA en el repositorio
        return historialIncidenteRepository.findByIncidenteIdIncidente(idIncidente);
    }
}