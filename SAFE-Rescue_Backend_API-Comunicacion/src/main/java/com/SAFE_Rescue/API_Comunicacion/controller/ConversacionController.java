package com.SAFE_Rescue.API_Comunicacion.controller;

import com.SAFE_Rescue.API_Comunicacion.modelo.Conversacion;
import com.SAFE_Rescue.API_Comunicacion.modelo.Mensaje;
import com.SAFE_Rescue.API_Comunicacion.service.ConversacionService;
import com.SAFE_Rescue.API_Comunicacion.service.MensajeService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.NoSuchElementException;

/**
 * Controlador REST para la gestión de la entidad Conversacion.
 * Gestiona la creación, consulta y eliminación de hilos de conversación.
 */
@RestController
@RequestMapping("/api-comunicaciones/v1/conversaciones")
public class ConversacionController {

    private final ConversacionService conversacionService;
    private final MensajeService mensajeService;

    @Autowired
    public ConversacionController(ConversacionService conversacionService, MensajeService mensajeService) {
        this.conversacionService = conversacionService;
        this.mensajeService = mensajeService;
    }

    // =========================================================================
    // ENDPOINTS DE CREACIÓN (POST)
    // =========================================================================

    /**
     * POST /api-comunicaciones/v1/conversaciones
     * Inicia un nuevo hilo de conversación, utilizando la Entidad directamente como cuerpo de la solicitud.
     *
     * @param conversacion Entidad Conversacion con los campos 'tipo' y 'nombre' (opcional).
     * @return La conversación creada con el estado 201 (Created).
     *
     * !!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!
     * !!! ADVERTENCIA DE PROYECTO ESCOLAR: Usar la entidad directamente como @RequestBody en el  !!!
     * !!! método POST es una MALA PRÁCTICA en producción (Vulnerabilidad de Asignación Masiva). !!!
     * !!! Siempre se recomienda usar un DTO (Data Transfer Object) para el cuerpo de la petición.!!!
     * !!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!
     */
    @PostMapping
    public ResponseEntity<Conversacion> iniciarConversacion(@RequestBody Conversacion conversacion) {
        // En lugar de usar el DTO, extraemos los campos directamente de la entidad
        Conversacion nuevaConversacion = conversacionService.iniciarNuevaConversacion(
                conversacion.getTipo(),
                conversacion.getNombre()
        );
        return new ResponseEntity<>(nuevaConversacion, HttpStatus.CREATED);
    }

    // =========================================================================
    // ENDPOINTS DE CONSULTA (GET)
    // =========================================================================

    /**
     * GET /api-comunicaciones/v1/conversaciones
     * Obtiene una lista de todas las conversaciones.
     * @return Lista de todas las conversaciones.
     */
    @GetMapping
    public ResponseEntity<List<Conversacion>> obtenerTodasLasConversaciones() {
        List<Conversacion> conversaciones = conversacionService.findAll();
        return ResponseEntity.ok(conversaciones);
    }

    /**
     * GET /api-comunicaciones/v1/conversaciones/{idConversacion}
     * Busca una conversación por su identificador único.
     * @param idConversacion ID de la conversación a buscar.
     * @return El objeto Conversacion si es encontrado.
     */
    @GetMapping("/{idConversacion}")
    public ResponseEntity<Conversacion> obtenerConversacionPorId(@PathVariable Integer idConversacion) {
        Conversacion conversacion = conversacionService.findById(idConversacion);
        return ResponseEntity.ok(conversacion);
    }

    /**
     * GET /api-comunicaciones/v1/conversaciones/{idConversacion}/mensajes
     * Obtiene todos los mensajes de una conversación específica.
     * @param idConversacion ID de la conversación.
     * @return Lista de mensajes ordenados cronológicamente.
     */
    @GetMapping("/{idConversacion}/mensajes")
    public ResponseEntity<List<Mensaje>> obtenerMensajesPorConversacion(@PathVariable Integer idConversacion) {
        List<Mensaje> mensajes = mensajeService.findMessagesByConversation(idConversacion);
        return ResponseEntity.ok(mensajes);
    }

    // =========================================================================
    // ENDPOINTS DE ELIMINACIÓN (DELETE)
    // =========================================================================

    /**
     * DELETE /api-comunicaciones/v1/conversaciones/{idConversacion}
     * Elimina una conversación por su identificador único.
     * @param idConversacion ID de la conversación a eliminar.
     */
    @DeleteMapping("/{idConversacion}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void eliminarConversacion(@PathVariable Integer idConversacion) {
        conversacionService.delete(idConversacion);
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
     * Maneja las excepciones cuando hay argumentos inválidos (400 BAD REQUEST).
     */
    @ExceptionHandler(IllegalArgumentException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public String handleBadRequestException(IllegalArgumentException ex) {
        return "Solicitud inválida: " + ex.getMessage();
    }
}