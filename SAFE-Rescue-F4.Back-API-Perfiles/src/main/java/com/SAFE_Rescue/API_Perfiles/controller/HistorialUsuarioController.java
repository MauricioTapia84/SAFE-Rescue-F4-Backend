package com.SAFE_Rescue.API_Perfiles.controller;

import com.SAFE_Rescue.API_Perfiles.modelo.HistorialUsuario;
import com.SAFE_Rescue.API_Perfiles.service.HistorialUsuarioService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Controlador REST para la consulta del historial de cambios de estado de los perfiles (usuarios y equipos).
 * Los registros se crean internamente por el servicio de negocio, este controlador
 * sólo expone la funcionalidad de consulta (GET).
 */
@RestController
@RequestMapping("/api-perfiles/v1")
@Tag(name = "Historial de Perfiles", description = "Endpoints para consultar el registro de auditoría de cambios de estado de Usuarios y Equipos")
public class HistorialUsuarioController {

    private final HistorialUsuarioService historialUsuarioService;

    @Autowired
    public HistorialUsuarioController(HistorialUsuarioService historialUsuarioService) {
        this.historialUsuarioService = historialUsuarioService;
    }

    // =========================================================================
    // ENDPOINTS GENERALES
    // =========================================================================

    /**
     * Obtiene todos los registros de historial de todos los perfiles (usuarios y equipos).
     * Endpoint: GET /api/v1/historial
     *
     * @return Lista de todos los registros de historial.
     */
    @GetMapping("/historial")
    @Operation(summary = "Obtiene todos los registros de historial de todos los perfiles",
            description = "Retorna una lista completa de todos los eventos de historial registrados.")
    public ResponseEntity<List<HistorialUsuario>> getAllHistorial() {
        List<HistorialUsuario> historial = historialUsuarioService.findAll();

        if (historial.isEmpty()) {
            return ResponseEntity.noContent().build();
        }

        return ResponseEntity.ok(historial);
    }

    // =========================================================================
    // ENDPOINTS ESPECÍFICOS POR PERFIL
    // =========================================================================

    /**
     * Obtiene el historial completo de cambios de estado para un USUARIO específico.
     *
     * Endpoint: GET /api/v1/usuarios/{idUsuario}/historial
     *
     * @param idUsuario ID del usuario cuyo historial se desea consultar.
     * @return Lista de registros de historial ordenados por fecha (el más reciente primero).
     */
    @GetMapping("/usuarios/{idUsuario}/historial")
    @Operation(summary = "Obtiene el historial de cambios de estado para un usuario específico",
            description = "Retorna una lista de todos los registros de cambios de estado del usuario, ordenados por fecha descendente.")
    public ResponseEntity<List<HistorialUsuario>> getHistorialPorUsuario(@PathVariable Integer idUsuario) {
        List<HistorialUsuario> historial = historialUsuarioService.obtenerHistorialPorUsuario(idUsuario);

        if (historial.isEmpty()) {
            return ResponseEntity.noContent().build();
        }

        return ResponseEntity.ok(historial);
    }

    /**
     * Obtiene el historial completo de cambios de estado para un EQUIPO específico.
     *
     * Endpoint: GET /api/v1/equipos/{idEquipo}/historial
     *
     * @param idEquipo ID del equipo cuyo historial se desea consultar.
     * @return Lista de registros de historial ordenados por fecha (el más reciente primero).
     */
    @GetMapping("/equipos/{idEquipo}/historial") // NUEVO ENDPOINT
    @Operation(summary = "Obtiene el historial de cambios de estado para un equipo específico",
            description = "Retorna una lista de todos los registros de cambios de estado del equipo, ordenados por fecha descendente.")
    public ResponseEntity<List<HistorialUsuario>> getHistorialPorEquipo(@PathVariable Integer idEquipo) {
        List<HistorialUsuario> historial = historialUsuarioService.obtenerHistorialPorEquipo(idEquipo);

        if (historial.isEmpty()) {
            return ResponseEntity.noContent().build();
        }

        return ResponseEntity.ok(historial);
    }
}