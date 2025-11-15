package com.SAFE_Rescue.API_Comunicacion.controller;

import com.SAFE_Rescue.API_Comunicacion.modelo.ParticipanteConversacion;
import com.SAFE_Rescue.API_Comunicacion.service.ParticipanteConvService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.NoSuchElementException;

/**
 * Controlador REST para gestionar la adición y remoción de participantes en las conversaciones.
 */
@RestController
@RequestMapping("/api-comunicaciones/v1/participantes")
public class ParticipanteConvController {

    private final ParticipanteConvService participanteConvService;

    @Autowired
    public ParticipanteConvController(ParticipanteConvService participanteConvService) {
        this.participanteConvService = participanteConvService;
    }

    // =========================================================================
    // ENDPOINTS DE ASIGNACIÓN (POST)
    // =========================================================================

    /**
     * POST /api-comunicaciones/v1/participantes/{idConversacion}/usuario/{idUsuario}
     * Asigna un usuario a una conversación.
     *
     * @param idConversacion ID de la conversación.
     * @param idUsuario ID del usuario a unir.
     * @return La asignación de participante creada con estado 201 (Created).
     */
    @PostMapping("/{idConversacion}/usuario/{idUsuario}")
    public ResponseEntity<ParticipanteConversacion> unirParticipante(
            @PathVariable Integer idConversacion,
            @PathVariable Integer idUsuario) {

        ParticipanteConversacion nuevaAsignacion = participanteConvService.unirParticipanteAConversacion(idConversacion, idUsuario);
        return new ResponseEntity<>(nuevaAsignacion, HttpStatus.CREATED);
    }

    // =========================================================================
    // ENDPOINTS DE CONSULTA (GET)
    // =========================================================================

    /**
     * GET /api-comunicaciones/v1/participantes/conversacion/{idConversacion}
     * Obtiene todos los participantes de una conversación.
     * @param idConversacion ID de la conversación.
     * @return Lista de ParticipanteConversacion.
     */
    @GetMapping("/conversacion/{idConversacion}")
    public ResponseEntity<List<ParticipanteConversacion>> obtenerParticipantesPorConversacion(@PathVariable Integer idConversacion) {
        List<ParticipanteConversacion> participantes = participanteConvService.findParticipantesByConversacion(idConversacion);
        return ResponseEntity.ok(participantes);
    }

    /**
     * GET /api-comunicaciones/v1/participantes/usuario/{idUsuario}
     * Obtiene todas las conversaciones en las que participa un usuario dado.
     * @param idUsuario ID del usuario.
     * @return Lista de ParticipanteConversacion (que incluye el ID de la conversación).
     */
    @GetMapping("/usuario/{idUsuario}")
    public ResponseEntity<List<ParticipanteConversacion>> obtenerConversacionesPorUsuario(@PathVariable Integer idUsuario) {
        List<ParticipanteConversacion> conversaciones = participanteConvService.findConversacionesByParticipante(idUsuario);
        return ResponseEntity.ok(conversaciones);
    }

    // =========================================================================
    // ENDPOINTS DE DESASIGNACIÓN (DELETE)
    // =========================================================================

    /**
     * DELETE /api-comunicaciones/v1/participantes/{idConversacion}/usuario/{idUsuario}
     * Desasigna (elimina) un usuario de una conversación específica.
     *
     * @param idConversacion ID de la conversación.
     * @param idUsuario ID del participante a desasignar.
     */
    @DeleteMapping("/{idConversacion}/usuario/{idUsuario}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void eliminarParticipante(
            @PathVariable Integer idConversacion,
            @PathVariable Integer idUsuario) {
        participanteConvService.eliminarParticipanteDeConversacion(idConversacion, idUsuario);
    }

    // =========================================================================
    // GESTIÓN DE EXCEPCIONES
    // =========================================================================

    /**
     * Maneja las excepciones cuando no se encuentra un recurso (404 NOT FOUND).
     */
    @ExceptionHandler(NoSuchElementException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public String handleNotFoundException(NoSuchElementException ex) {
        return "Recurso no encontrado: " + ex.getMessage();
    }

    /**
     * Maneja las excepciones de estado ilegal (409 CONFLICT), por ejemplo, cuando se intenta añadir un participante ya existente.
     */
    @ExceptionHandler(IllegalStateException.class)
    @ResponseStatus(HttpStatus.CONFLICT)
    public String handleConflictException(IllegalStateException ex) {
        return "Conflicto de estado: " + ex.getMessage();
    }

    /**
     * Maneja las excepciones de argumentos inválidos (400 BAD REQUEST), por ejemplo, ID de usuario no válido.
     */
    @ExceptionHandler(IllegalArgumentException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public String handleBadRequestException(IllegalArgumentException ex) {
        return "Solicitud inválida: " + ex.getMessage();
    }
}