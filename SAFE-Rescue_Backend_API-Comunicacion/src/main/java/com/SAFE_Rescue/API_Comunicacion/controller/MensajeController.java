package com.SAFE_Rescue.API_Comunicacion.controller;

import com.SAFE_Rescue.API_Comunicacion.modelo.MensajeCreacionDTO;
import com.SAFE_Rescue.API_Comunicacion.modelo.Mensaje;
import com.SAFE_Rescue.API_Comunicacion.service.MensajeService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.util.List;
import java.util.NoSuchElementException;

/**
 * Controlador REST para la gestión de mensajes dentro de las conversaciones.
 * Gestiona la creación, consulta y eliminación de mensajes.
 * * Base URL: /api-comunicaciones/v1
 */
@RestController
@RequestMapping("/api-comunicaciones/v1") // Ruta base ajustada a un nivel superior para jerarquía REST limpia
public class MensajeController {

    private final MensajeService mensajeService;

    @Autowired
    public MensajeController(MensajeService mensajeService) {
        this.mensajeService = mensajeService;
    }

    // =========================================================================
    // ENDPOINTS DE CREACIÓN (POST)
    // =========================================================================

    /**
     * POST /api-comunicaciones/v1/conversaciones/{conversacionId}/mensajes
     * Crea un nuevo mensaje asociado a una conversación específica.
     * Mapea el DTO de entrada a la entidad Mensaje que espera el servicio.
     *
     * @param conversacionId ID de la conversación de destino (desde la URL).
     * @param dto Datos del mensaje a crear (detalle, emisor, estado).
     * @return El mensaje creado con el estado 201 (Created).
     */
    @PostMapping("/conversaciones/{conversacionId}/mensajes")
    public ResponseEntity<Mensaje> crearMensajeEnConversacion(
            @PathVariable Integer conversacionId,
            @Valid @RequestBody MensajeCreacionDTO dto) { // Usa @Valid de jakarta

        // 1. Mapear el DTO a la entidad Mensaje.
        Mensaje nuevoMensaje = new Mensaje();
        nuevoMensaje.setDetalle(dto.getDetalle());
        nuevoMensaje.setIdUsuarioEmisor(dto.getIdUsuarioEmisor());
        nuevoMensaje.setIdEstado(dto.getIdEstado());

        // 2. Llamar al servicio, que vinculará la Conversacion
        Mensaje mensajeCreado = mensajeService.createMessage(conversacionId, nuevoMensaje);

        return new ResponseEntity<>(mensajeCreado, HttpStatus.CREATED);
    }

    // =========================================================================
    // ENDPOINTS DE CONSULTA (GET)
    // =========================================================================

    /**
     * GET /api-comunicaciones/v1/mensajes
     * Obtiene todos los mensajes en el sistema.
     * @return Lista de todos los mensajes.
     */
    @GetMapping("/mensajes")
    public ResponseEntity<List<Mensaje>> obtenerTodosLosMensajes() {
        List<Mensaje> mensajes = mensajeService.findAll();
        return ResponseEntity.ok(mensajes);
    }

    /**
     * GET /api-comunicaciones/v1/mensajes/{idMensaje}
     * Obtiene un mensaje por su ID único.
     * @param idMensaje ID del mensaje.
     * @return El mensaje encontrado.
     */
    @GetMapping("/mensajes/{idMensaje}") // Ruta completa: /api-comunicaciones/v1/mensajes/{idMensaje}
    public ResponseEntity<Mensaje> obtenerMensajePorId(@PathVariable Integer idMensaje) {
        Mensaje mensaje = mensajeService.findById(idMensaje);
        return ResponseEntity.ok(mensaje);
    }

    // =========================================================================
    // ENDPOINTS DE ELIMINACIÓN (DELETE)
    // =========================================================================

    /**
     * DELETE /api-comunicaciones/v1/mensajes/{idMensaje}
     * Elimina un mensaje por su ID.
     * @param idMensaje ID del mensaje a eliminar.
     * @return Respuesta sin contenido con estado 204 (No Content).
     */
    @DeleteMapping("/mensajes/{idMensaje}") // Ruta completa: /api-comunicaciones/v1/mensajes/{idMensaje}
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void eliminarMensaje(@PathVariable Integer idMensaje) {
        mensajeService.delete(idMensaje);
    }

    // =========================================================================
    // GESTIÓN DE EXCEPCIONES
    // =========================================================================

    /**
     * Maneja las excepciones cuando no se encuentra un recurso (ej. Conversación o Mensaje).
     */
    @ExceptionHandler(NoSuchElementException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public String handleNotFoundException(NoSuchElementException ex) {
        return "Recurso no encontrado: " + ex.getMessage();
    }

    /**
     * Maneja las excepciones cuando hay argumentos inválidos (ej. ID de usuario o estado externo no válido).
     */
    @ExceptionHandler(IllegalArgumentException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public String handleBadRequestException(IllegalArgumentException ex) {
        return "Solicitud inválida: " + ex.getMessage();
    }
}