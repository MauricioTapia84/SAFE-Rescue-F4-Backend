package com.SAFE_Rescue.API_Comunicacion.controller;

import com.SAFE_Rescue.API_Comunicacion.modelo.Mensaje;
import com.SAFE_Rescue.API_Comunicacion.service.MensajeService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;

// Importaciones para OpenAPI/Swagger
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

/**
 * Controlador REST para la gestión de Mensajes enviados.
 * Actualizado para NO usar HATEOAS y mapeado a los métodos del MensajeService.
 */
@RestController
@RequestMapping("/api-comunicacion/v1/mensajes")
@Tag(name = "Mensajes", description = "Operaciones de CRUD relacionadas con mensajes enviados.")
public class MensajeController {

    private final MensajeService mensajeService;

    /**
     * Constructor para inyección de dependencias del servicio de mensajes.
     * @param mensajeService El servicio que maneja la lógica de negocio de los mensajes.
     */
    @Autowired
    public MensajeController(MensajeService mensajeService) {
        this.mensajeService = mensajeService;
    }

    /**
     * DTO para la solicitud de creación de un mensaje.
     * Requiere el ID del borrador original, el ID de la conversación y el ID del emisor.
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    static class CrearMensajeRequest {
        @Schema(description = "ID del borrador original a partir del cual se crea el mensaje (para obtener el contenido)", example = "1", required = true)
        private int idBorradorOriginal;

        @Schema(description = "ID de la conversación a la que pertenece este mensaje", example = "5", required = true)
        private int idConversacion;

        @Schema(description = "ID del usuario que envía el mensaje (Emisor)", example = "101", required = true)
        private Integer idEmisor;
    }

    /**
     * Crea un nuevo mensaje a partir de un borrador, asociándolo a una conversación y un emisor.
     *
     * @param request Objeto con el ID del borrador, ID de la conversación y el ID del emisor.
     * @return El mensaje creado con estado 201 Created.
     */
    @PostMapping
    @Operation(summary = "Crear un nuevo mensaje desde un borrador", description = "Crea un mensaje asociándolo a una conversación y un emisor, marcando el borrador original como enviado.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Mensaje creado con éxito.",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = Mensaje.class))),
            @ApiResponse(responseCode = "400", description = "Error en la solicitud, datos inválidos o borrador ya enviado."),
            @ApiResponse(responseCode = "404", description = "Recurso (Borrador o Conversación) no encontrado."),
            @ApiResponse(responseCode = "409", description = "El borrador original ya ha sido enviado."),
            @ApiResponse(responseCode = "500", description = "Error interno del servidor.")
    })
    public ResponseEntity<Mensaje> crearMensaje(
            @RequestBody @Parameter(description = "Datos para crear un mensaje (ID del borrador, ID de conversación y ID del emisor)", required = true)
            CrearMensajeRequest request) {
        try {
            Mensaje nuevoMensaje = mensajeService.crearMensajeDesdeBorradorEnConversacion(
                    request.getIdBorradorOriginal(),
                    request.getIdConversacion(),
                    request.getIdEmisor()
            );
            return new ResponseEntity<>(nuevoMensaje, HttpStatus.CREATED);
        } catch (NoSuchElementException e) {
            // Borrador o Conversación no encontrado
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        } catch (IllegalStateException e) {
            // Borrador ya enviado (simulado)
            return new ResponseEntity<>(HttpStatus.CONFLICT);
        } catch (RuntimeException e) {
            // Para otros errores generales de negocio/validación del servicio
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        } catch (Exception e) {
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    /**
     * Obtiene una lista de todos los mensajes enviados.
     * Mapeado a mensajeService.obtenerTodosLosMensajes().
     * @return Lista de mensajes con estado 200 OK.
     */
    @GetMapping
    @Operation(summary = "Obtener todos los mensajes enviados", description = "Obtiene una lista con todos los mensajes.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Lista de mensajes obtenida exitosamente.",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = Mensaje.class))),
            @ApiResponse(responseCode = "204", description = "No hay mensajes registrados.")
    })
    public ResponseEntity<List<Mensaje>> obtenerTodosLosMensajes() {
        // Mapeado a obtenerTodosLosMensajes()
        List<Mensaje> mensajes = mensajeService.obtenerTodosLosMensajes();

        if (mensajes.isEmpty()) {
            return new ResponseEntity<>(HttpStatus.NO_CONTENT);
        }

        return new ResponseEntity<>(mensajes, HttpStatus.OK);
    }

    /**
     * Obtiene un mensaje por su ID.
     * Mapeado a mensajeService.obtenerMensajePorId(id).
     * @param id El ID del mensaje a buscar.
     * @return El mensaje encontrado con estado 200 OK, o 404 Not Found si no existe.
     */
    @GetMapping("/{id}")
    @Operation(summary = "Obtiene un mensaje por su ID", description = "Obtiene un mensaje específico por su ID.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Mensaje encontrado.",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = Mensaje.class))),
            @ApiResponse(responseCode = "404", description = "Mensaje no encontrado.")
    })
    public ResponseEntity<Mensaje> obtenerMensajePorId(
            @Parameter(description = "ID del mensaje a buscar", required = true)
            @PathVariable int id) {
        // Mapeado a obtenerMensajePorId(id)
        Optional<Mensaje> mensaje = mensajeService.obtenerMensajePorId(id);
        return mensaje.map(m -> new ResponseEntity<>(m, HttpStatus.OK))
                .orElseGet(() -> new ResponseEntity<>(HttpStatus.NOT_FOUND));
    }

    /**
     * Elimina un mensaje por su ID.
     * Mapeado a mensajeService.eliminarMensaje(id).
     * @param id El ID del mensaje a eliminar.
     * @return Estado 204 No Content si la eliminación es exitosa, o 404 Not Found si el mensaje no existe.
     */
    @DeleteMapping("/{id}")
    @Operation(summary = "Eliminar un mensaje", description = "Elimina un mensaje del sistema por su ID.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Mensaje eliminado con éxito."),
            @ApiResponse(responseCode = "404", description = "Mensaje no encontrado."),
            @ApiResponse(responseCode = "500", description = "Error interno del servidor.")
    })
    public ResponseEntity<Void> eliminarMensaje(
            @Parameter(description = "ID del mensaje a eliminar", required = true)
            @PathVariable int id) {
        try {
            // Mapeado a eliminarMensaje(id)
            mensajeService.eliminarMensaje(id);
            return new ResponseEntity<>(HttpStatus.NO_CONTENT); // 204 No Content
        } catch (NoSuchElementException e) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND); // Mensaje no encontrado
        } catch (Exception e) {
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
}