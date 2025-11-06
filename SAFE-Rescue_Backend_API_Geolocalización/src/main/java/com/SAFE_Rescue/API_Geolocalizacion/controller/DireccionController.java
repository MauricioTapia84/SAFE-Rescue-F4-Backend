package com.SAFE_Rescue.API_Geolocalizacion.controller;

import com.SAFE_Rescue.API_Geolocalizacion.modelo.Direccion; // 👈 Entidad correcta
import com.SAFE_Rescue.API_Geolocalizacion.service.DireccionService; // 👈 Servicio correcto
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.NoSuchElementException;

/**
 * Controlador REST para la gestión de Direcciones.
 * Proporciona endpoints para operaciones CRUD.
 */
@RestController
@RequestMapping("/api-geolocalizacion/v1/direcciones")
@Tag(name = "Direcciones", description = "Operaciones de CRUD relacionadas con la entidad Dirección")
public class DireccionController {

    @Autowired
    private DireccionService direccionService; // 👈 Servicio inyectado correcto

    // --- OPERACIONES CRUD BÁSICAS ---

    /**
     * Obtiene todas las direcciones registradas en el sistema.
     * @return ResponseEntity con lista de direcciones o estado NO_CONTENT si no hay registros
     */
    @GetMapping
    @Operation(summary = "Obtener todas las direcciones", description = "Obtiene una lista con todas las direcciones")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Lista de direcciones obtenida exitosamente.",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = Direccion.class))), // Esquema correcto
            @ApiResponse(responseCode = "204", description = "No hay direcciones registradas.")
    })
    public ResponseEntity<List<Direccion>> listar() {
        List<Direccion> direcciones = direccionService.findAll();
        if (direcciones.isEmpty()) {
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.ok(direcciones);
    }

    /**
     * Busca una dirección por su ID.
     * @param id ID de la dirección a buscar
     * @return ResponseEntity con la dirección encontrada o mensaje de error
     */
    @GetMapping("/{id}")
    @Operation(summary = "Obtiene una dirección por su ID", description = "Obtiene una dirección al buscarla por su ID")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Dirección encontrada.",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = Direccion.class))), // Esquema correcto
            @ApiResponse(responseCode = "404", description = "Dirección no encontrada.")
    })
    public ResponseEntity<?> buscarDireccion(@Parameter(description = "ID de la dirección a buscar", required = true)
                                             @PathVariable int id) {
        Direccion direccion;
        try {
            direccion = direccionService.findById(id);
        } catch (NoSuchElementException e) {
            return new ResponseEntity<>("Dirección no encontrada", HttpStatus.NOT_FOUND);
        }
        return ResponseEntity.ok(direccion);
    }

    /**
     * Crea una nueva dirección.
     * @param direccion Datos de la dirección a crear
     * @return ResponseEntity con mensaje de confirmación o error
     */
    @PostMapping
    @Operation(summary = "Crear una nueva dirección", description = "Crea una nueva dirección en el sistema")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Dirección creada con éxito."),
            @ApiResponse(responseCode = "400", description = "Error en la solicitud (ej: Comuna o Geolocalización no existen, o datos incompletos)."),
            @ApiResponse(responseCode = "500", description = "Error interno del servidor.")
    })
    public ResponseEntity<String> agregarDireccion(@RequestBody @Parameter(description = "Datos de la dirección a crear", required = true)
                                                   Direccion direccion) {
        try {
            direccionService.save(direccion);
            return ResponseEntity.status(HttpStatus.CREATED).body("Dirección creada con éxito.");
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error interno del servidor.");
        }
    }

    /**
     * Actualiza una dirección existente.
     * @param id ID de la dirección a actualizar
     * @param direccion Datos actualizados de la dirección
     * @return ResponseEntity con mensaje de confirmación o error
     */
    @PutMapping("/{id}")
    @Operation(summary = "Actualizar una dirección existente", description = "Actualiza los datos de una dirección por su ID")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Dirección actualizada con éxito."),
            @ApiResponse(responseCode = "404", description = "Dirección no encontrada."),
            @ApiResponse(responseCode = "400", description = "Error en la solicitud."),
            @ApiResponse(responseCode = "500", description = "Error interno del servidor.")
    })
    public ResponseEntity<String> actualizarDireccion(@Parameter(description = "ID de la dirección a actualizar", required = true)
                                                      @PathVariable Integer id,
                                                      @RequestBody @Parameter(description = "Datos actualizados de la dirección", required = true)
                                                      Direccion direccion) {
        try {
            direccionService.update(direccion, id);
            return ResponseEntity.ok("Dirección actualizada con éxito");
        } catch (NoSuchElementException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Dirección no encontrada");
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error interno del servidor.");
        }
    }

    /**
     * Elimina una dirección del sistema.
     * @param id ID de la dirección a eliminar
     * @return ResponseEntity con mensaje de confirmación
     */
    @DeleteMapping("/{id}")
    @Operation(summary = "Eliminar una dirección", description = "Elimina una dirección del sistema por su ID")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Dirección eliminada con éxito."),
            @ApiResponse(responseCode = "404", description = "Dirección no encontrada."),
            @ApiResponse(responseCode = "500", description = "Error interno del servidor.")
    })
    public ResponseEntity<String> eliminarDireccion(@Parameter(description = "ID de la dirección a eliminar", required = true)
                                                    @PathVariable Integer id) {
        try {
            direccionService.delete(id);
            return ResponseEntity.ok("Dirección eliminada con éxito.");
        } catch (NoSuchElementException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Dirección no encontrada");
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error interno del servidor.");
        }
    }
}