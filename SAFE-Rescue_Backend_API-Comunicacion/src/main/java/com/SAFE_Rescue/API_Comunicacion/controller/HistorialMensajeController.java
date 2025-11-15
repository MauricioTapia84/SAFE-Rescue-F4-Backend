package com.SAFE_Rescue.API_Comunicacion.controller;

import com.SAFE_Rescue.API_Comunicacion.modelo.HistorialMensaje;
import com.SAFE_Rescue.API_Comunicacion.service.HistorialMensajeService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.Arrays;

/**
 * Controlador REST para la consulta del historial de cambios (auditoría) de los mensajes.
 * Sigue el patrón del HistorialUsuarioController para rutas de auditoría.
 */
@RestController
@RequestMapping("/api-comunicaciones/v1") // Ruta base genérica como en HistorialUsuarioController
@Tag(name = "Historial de Mensajes", description = "Endpoints para consultar el registro de auditoría de cambios en los Mensajes.")
public class HistorialMensajeController {

    private final HistorialMensajeService historialMensajeService; // Sustituir por el servicio real

    @Autowired
    public HistorialMensajeController(HistorialMensajeService historialMensajeService) {
        this.historialMensajeService = historialMensajeService;
    }

    // =========================================================================
    // ENDPOINTS GENERALES
    // =========================================================================

    /**
     * Obtiene todos los registros de historial de todos los mensajes.
     * Endpoint: GET /api-comunicaciones/v1/historial-mensajes
     *
     * @return Lista de todos los registros de historial.
     */
    @GetMapping("/historial-mensajes")
    @Operation(summary = "Obtiene todos los registros de historial de todos los mensajes",
            description = "Retorna una lista completa de todos los eventos de historial registrados para todos los mensajes.")
    public ResponseEntity<List<HistorialMensaje>> getAllHistorialMensajes() {
        List<HistorialMensaje> historial = historialMensajeService.findAll();

        if (historial.isEmpty()) {
            return ResponseEntity.noContent().build();
        }

        return ResponseEntity.ok(historial);
    }

    // =========================================================================
    // ENDPOINT ESPECÍFICO POR MENSAJE
    // =========================================================================

    /**
     * Obtiene el historial completo de cambios para un MENSAJE específico.
     *
     * Endpoint: GET /api-comunicaciones/v1/mensajes/{idMensaje}/historial
     *
     * ESTA RUTA REEMPLAZA a la ruta anterior que causaba conflicto y ahora usa la convención
     * de sub-recurso similar a la del HistorialUsuarioController.
     *
     * @param idMensaje ID del mensaje cuyo historial se desea consultar.
     * @return Lista de registros de historial ordenados por fecha.
     */
    @GetMapping("/mensajes/{idMensaje}/historial")
    @Operation(summary = "Obtiene el historial de cambios de estado para un mensaje específico",
            description = "Retorna una lista de todos los registros de auditoría de cambios del mensaje, ordenados por fecha descendente.")
    public ResponseEntity<List<HistorialMensaje>> obtenerHistorialPorMensaje(@PathVariable Integer idMensaje) {
        // Se asume que este método ya existe en el servicio.
        List<HistorialMensaje> historial = historialMensajeService.obtenerHistorialPorMensaje(idMensaje);

        if (historial.isEmpty()) {
            return ResponseEntity.noContent().build();
        }

        return ResponseEntity.ok(historial);
    }
}