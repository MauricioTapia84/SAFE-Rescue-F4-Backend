package com.SAFE_Rescue.API_Incidentes.service;

import com.SAFE_Rescue.API_Incidentes.modelo.HistorialIncidente;
import com.SAFE_Rescue.API_Incidentes.modelo.Incidente;
import com.SAFE_Rescue.API_Incidentes.repository.HistorialIncidenteRepository;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy; // Importar Lazy
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class HistorialIncidenteService {

    private final HistorialIncidenteRepository historialIncidenteRepository;
    private final IncidenteService incidenteService;

    @Autowired
    public HistorialIncidenteService(
            HistorialIncidenteRepository historialIncidenteRepository,
            @Lazy IncidenteService incidenteService) { // <--- AGREGAR @Lazy AQUÍ
        this.historialIncidenteRepository = historialIncidenteRepository;
        this.incidenteService = incidenteService;
    }

    public List<HistorialIncidente> findAll() {
        return historialIncidenteRepository.findAll();
    }

    public HistorialIncidente registrarCambioEstado(
            Incidente incidente,
            Integer idEstadoAnterior,
            Integer idEstadoNuevo,
            String detalle) {

        if (incidente == null || incidente.getIdIncidente() == null || idEstadoAnterior == null || idEstadoNuevo == null || detalle == null || detalle.trim().isEmpty()) {
            throw new IllegalArgumentException("Datos incompletos para historial.");
        }

        HistorialIncidente registro = new HistorialIncidente();
        registro.setIncidente(incidente);
        registro.setIdEstadoAnterior(idEstadoAnterior);
        registro.setIdEstadoNuevo(idEstadoNuevo);
        registro.setDetalle(detalle);
        registro.setFechaHistorial(LocalDateTime.now());

        return historialIncidenteRepository.save(registro);
    }

    public List<HistorialIncidente> obtenerHistorialPorIncidente(Integer idIncidente) {
        // La validación ahora es segura gracias a @Lazy, evitando el ciclo de dependencias
        if (incidenteService.findById(idIncidente) == null) {
            throw new EntityNotFoundException("Incidente con ID " + idIncidente + " no encontrado.");
        }
        return historialIncidenteRepository.findByIncidenteIdIncidente(idIncidente);
    }
}