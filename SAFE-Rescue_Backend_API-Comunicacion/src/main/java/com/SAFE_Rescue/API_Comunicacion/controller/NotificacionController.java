package com.SAFE_Rescue.API_Comunicacion.controller;

import com.SAFE_Rescue.API_Comunicacion.modelo.NotificacionCreacionDTO;
import com.SAFE_Rescue.API_Comunicacion.modelo.Conversacion;
import com.SAFE_Rescue.API_Comunicacion.modelo.Notificacion;
import com.SAFE_Rescue.API_Comunicacion.service.ConversacionService;
import com.SAFE_Rescue.API_Comunicacion.service.NotificacionService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.util.HashMap;
import java.util.Map;
import java.util.NoSuchElementException;

/**
 * Controlador REST para la gestión de Notificaciones.
 * Base URL: /api-comunicaciones/v1
 */
@RestController
@RequestMapping("/api-comunicaciones/v1")
public class NotificacionController {

    private final NotificacionService notificacionService;
    private final ConversacionService conversacionService;

    @Autowired
    public NotificacionController(NotificacionService notificacionService, ConversacionService conversacionService) {
        this.notificacionService = notificacionService;
        this.conversacionService = conversacionService;
    }

    // =========================================================================
    // ENDPOINTS DE CREACIÓN (POST)
    // =========================================================================

    /**
     * POST /api-comunicaciones/v1/notificaciones
     * Crea una nueva notificación.
     *
     * @param dto Datos de la notificación a crear.
     * @return La notificación creada con el estado 201 (Created).
     */
    @PostMapping("/notificaciones")
    public ResponseEntity<Notificacion> crearNotificacion(@Valid @RequestBody NotificacionCreacionDTO dto) {

        // 1. Resolver la entidad Conversacion a partir del ID en el DTO (es opcional)
        // La excepción NoSuchElementException si la Conversacion no existe se maneja
        // en el bloque @ExceptionHandler.
        Conversacion conversacion = null;
        Integer idConversacion = dto.getIdConversacion();

        if (idConversacion != null) {
            // Llama al servicio. Si no se encuentra, la excepción será manejada como 404.
            conversacion = conversacionService.findById(idConversacion);
        }

        // 2. Llamar al servicio de creación
        Notificacion nuevaNotificacion = notificacionService.crearNotificacion(
                dto.getIdUsuarioReceptor(),
                dto.getDetalle(),
                conversacion // Pasa la Conversacion o null
        );

        return new ResponseEntity<>(nuevaNotificacion, HttpStatus.CREATED);
    }

    // =========================================================================
    // ENDPOINTS DE CONSULTA (GET)
    // =========================================================================

    /**
     * GET /api-comunicaciones/v1/notificaciones/usuario/{idUsuarioReceptor}/pendientes
     * Recupera notificaciones pendientes para un usuario, con paginación.
     *
     * @param idUsuarioReceptor ID del usuario que debe recibir las notificaciones.
     * @param pageable Configuración de paginación (tamaño, página, orden).
     * @return Página de notificaciones pendientes (Estado 200 OK).
     */
    @GetMapping("/notificaciones/usuario/{idUsuarioReceptor}/pendientes")
    public ResponseEntity<Page<Notificacion>> obtenerNotificacionesPendientes(
            @PathVariable String idUsuarioReceptor,
            @PageableDefault(size = 10, sort = "fechaCreacion") Pageable pageable) {

        Page<Notificacion> notificaciones = notificacionService.cargarNotificacionesPendientes(idUsuarioReceptor, pageable);
        return ResponseEntity.ok(notificaciones);
    }

    // =========================================================================
    // ENDPOINTS DE ACTUALIZACIÓN DE ESTADO (PATCH)
    // =========================================================================

    /**
     * PATCH /api-comunicaciones/v1/notificaciones/{idNotificacion}/leida
     * Marca una notificación específica como leída (estado 9).
     *
     * @param idNotificacion ID de la notificación a actualizar.
     * @return La notificación actualizada (Estado 200 OK).
     */
    @PatchMapping("/notificaciones/{idNotificacion}/leida")
    public ResponseEntity<Notificacion> marcarNotificacionComoLeida(@PathVariable Integer idNotificacion) {
        Notificacion notificacionActualizada = notificacionService.marcarComoLeida(idNotificacion);
        return ResponseEntity.ok(notificacionActualizada);
    }

    /**
     * PATCH /api-comunicaciones/v1/notificaciones/usuario/{idUsuarioReceptor}/leidas
     * Marca todas las notificaciones pendientes de un usuario como leídas.
     *
     * @param idUsuarioReceptor ID del usuario cuyas notificaciones serán marcadas.
     * @return Mensaje de éxito con el contador de notificaciones marcadas (Estado 200 OK).
     */
    @PatchMapping("/notificaciones/usuario/{idUsuarioReceptor}/leidas")
    public ResponseEntity<String> marcarTodasNotificacionesComoLeidas(@PathVariable String idUsuarioReceptor) {
        int count = notificacionService.marcarTodasComoLeidas(idUsuarioReceptor);
        return ResponseEntity.ok("Se marcaron " + count + " notificaciones como leídas para el usuario " + idUsuarioReceptor);
    }


    // =========================================================================
    // GESTIÓN DE EXCEPCIONES
    // =========================================================================

    /**
     * Maneja las excepciones cuando la validación del DTO falla (@Valid).
     * Devuelve 400 BAD REQUEST con detalles de los errores.
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public Map<String, String> handleValidationExceptions(MethodArgumentNotValidException ex) {
        Map<String, String> errors = new HashMap<>();
        ex.getBindingResult().getAllErrors().forEach(error -> {
            String fieldName = ((FieldError) error).getField();
            String errorMessage = error.getDefaultMessage();
            errors.put(fieldName, errorMessage);
        });
        return errors;
    }

    /**
     * Maneja las excepciones cuando no se encuentra un recurso (404 NOT FOUND).
     * Esto incluye casos donde el ID de la conversación en el DTO no existe.
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

    /**
     * Maneja errores internos o fallos de comunicación con microservicios (500 INTERNAL SERVER ERROR).
     */
    @ExceptionHandler(RuntimeException.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public String handleRuntimeException(RuntimeException ex) {
        return "Error interno del servidor o fallo de comunicación con microservicios: " + ex.getMessage();
    }
}