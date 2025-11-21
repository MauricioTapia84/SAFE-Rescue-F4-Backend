package com.SAFE_Rescue.API_Incidentes.controller;

import com.SAFE_Rescue.API_Incidentes.modelo.HistorialIncidente;
import com.SAFE_Rescue.API_Incidentes.service.HistorialIncidenteService; // Importamos el nuevo servicio
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * Controlador REST para la consulta del historial de cambios de estado de los incidentes.
 * Los registros se crean internamente por el servicio de negocio, este controlador
 * sólo expone la funcionalidad de consulta (GET).
 */
@RestController
@RequestMapping("/api-incidentes/v1")
@Tag(name = "Historial de Incidentes", description = "Endpoints para consultar el registro de auditoría de cambios de estado de un Incidente.")
public class HistorialIncidenteController {

    private final HistorialIncidenteService historialIncidenteService;

    @Autowired
    public HistorialIncidenteController(HistorialIncidenteService historialIncidenteService) {
        this.historialIncidenteService = historialIncidenteService;
    }

    // =========================================================================
    // ENDPOINTS GENERALES (AUDITORÍA COMPLETA)
    // =========================================================================

    /**
     * Obtiene todos los registros de historial de todos los incidentes.
     * Endpoint: GET /api/v1/historial/incidentes
     *
     * @return Lista de todos los registros de historial de incidentes.
     */
    @GetMapping("/historial/incidentes")
    @Operation(summary = "Obtiene todos los registros de historial de todos los incidentes",
            description = "Retorna una lista completa de todos los eventos de historial registrados.")
    public ResponseEntity<List<HistorialIncidente>> getAllHistorial() {
        List<HistorialIncidente> historial = historialIncidenteService.findAll();

        if (historial.isEmpty()) {
            return ResponseEntity.noContent().build();
        }

        return ResponseEntity.ok(historial);
    }

    // =========================================================================
    // ENDPOINT ESPECÍFICO POR INCIDENTE
    // =========================================================================

    /**
     * Obtiene el historial completo de cambios de estado para un INCIDENTE específico.
     *
     * Endpoint: GET /api/v1/incidentes/{idIncidente}/historial
     *
     * @param idIncidente ID del incidente cuyo historial se desea consultar.
     * @return Lista de registros de historial ordenados por fecha (el más reciente primero).
     */
    @GetMapping("/incidentes/{idIncidente}/historial")
    @Operation(summary = "Obtiene el historial de cambios de estado para un incidente específico",
            description = "Retorna una lista de todos los registros de cambios de estado del incidente, ordenados por fecha descendente.")
    public ResponseEntity<List<HistorialIncidente>> getHistorialPorIncidente(@PathVariable Integer idIncidente) {
        List<HistorialIncidente> historial = historialIncidenteService.obtenerHistorialPorIncidente(idIncidente);

        if (historial.isEmpty()) {
            return ResponseEntity.noContent().build();
        }

        return ResponseEntity.ok(historial);
    }
}