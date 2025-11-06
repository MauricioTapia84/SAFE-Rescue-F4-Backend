package com.SAFE_Rescue.API_Geolocalizacion.controller;

import com.SAFE_Rescue.API_Geolocalizacion.modelo.Comuna; // 👈 Entidad correcta
import com.SAFE_Rescue.API_Geolocalizacion.service.ComunaService; // 👈 Servicio correcto
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
 * Controlador REST para la gestión de Comunas.
 * Proporciona endpoints para operaciones CRUD.
 */
@RestController
@RequestMapping("/api-geolocalizacion/v1/comunas")
@Tag(name = "Comunas", description = "Operaciones de CRUD relacionadas con la entidad Comuna")
public class ComunaController {

    @Autowired
    private ComunaService comunaService; // 👈 Servicio inyectado correcto

    // --- OPERACIONES CRUD BÁSICAS ---

    /**
     * Obtiene todas las comunas registradas en el sistema.
     * @return ResponseEntity con lista de comunas o estado NO_CONTENT si no hay registros
     */
    @GetMapping
    @Operation(summary = "Obtener todas las comunas", description = "Obtiene una lista con todas las comunas")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Lista de comunas obtenida exitosamente.",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = Comuna.class))), // Esquema correcto
            @ApiResponse(responseCode = "204", description = "No hay comunas registradas.")
    })
    public ResponseEntity<List<Comuna>> listar() {
        List<Comuna> comunas = comunaService.findAll();
        if (comunas.isEmpty()) {
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.ok(comunas);
    }

    /**
     * Busca una comuna por su ID.
     * @param id ID de la comuna a buscar
     * @return ResponseEntity con la comuna encontrada o mensaje de error
     */
    @GetMapping("/{id}")
    @Operation(summary = "Obtiene una comuna por su ID", description = "Obtiene una comuna al buscarla por su ID")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Comuna encontrada.",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = Comuna.class))), // Esquema correcto
            @ApiResponse(responseCode = "404", description = "Comuna no encontrada.")
    })
    public ResponseEntity<?> buscarComuna(@Parameter(description = "ID de la comuna a buscar", required = true)
                                          @PathVariable int id) {
        Comuna comuna;
        try {
            comuna = comunaService.findById(id);
        } catch (NoSuchElementException e) {
            return new ResponseEntity<>("Comuna no encontrada", HttpStatus.NOT_FOUND);
        }
        return ResponseEntity.ok(comuna);
    }

    /**
     * Crea una nueva comuna.
     * @param comuna Datos de la comuna a crear
     * @return ResponseEntity con mensaje de confirmación o error
     */
    @PostMapping
    @Operation(summary = "Crear una nueva comuna", description = "Crea una nueva comuna en el sistema")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Comuna creada con éxito."),
            @ApiResponse(responseCode = "400", description = "Error en la solicitud (ej: Región no existe o datos faltantes)."),
            @ApiResponse(responseCode = "500", description = "Error interno del servidor.")
    })
    public ResponseEntity<String> agregarComuna(@RequestBody @Parameter(description = "Datos de la comuna a crear", required = true)
                                                Comuna comuna) {
        try {
            comunaService.save(comuna);
            return ResponseEntity.status(HttpStatus.CREATED).body("Comuna creada con éxito.");
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error interno del servidor.");
        }
    }

    /**
     * Actualiza una comuna existente.
     * @param id ID de la comuna a actualizar
     * @param comuna Datos actualizados de la comuna
     * @return ResponseEntity con mensaje de confirmación o error
     */
    @PutMapping("/{id}")
    @Operation(summary = "Actualizar una comuna existente", description = "Actualiza los datos de una comuna por su ID")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Comuna actualizada con éxito."),
            @ApiResponse(responseCode = "404", description = "Comuna no encontrada."),
            @ApiResponse(responseCode = "400", description = "Error en la solicitud."),
            @ApiResponse(responseCode = "500", description = "Error interno del servidor.")
    })
    public ResponseEntity<String> actualizarComuna(@Parameter(description = "ID de la comuna a actualizar", required = true)
                                                   @PathVariable Integer id,
                                                   @RequestBody @Parameter(description = "Datos actualizados de la comuna", required = true)
                                                   Comuna comuna) {
        try {
            comunaService.update(comuna, id);
            return ResponseEntity.ok("Comuna actualizada con éxito");
        } catch (NoSuchElementException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Comuna no encontrada");
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error interno del servidor.");
        }
    }

    /**
     * Elimina una comuna del sistema.
     * @param id ID de la comuna a eliminar
     * @return ResponseEntity con mensaje de confirmación
     */
    @DeleteMapping("/{id}")
    @Operation(summary = "Eliminar una comuna", description = "Elimina una comuna del sistema por su ID")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Comuna eliminada con éxito."),
            @ApiResponse(responseCode = "404", description = "Comuna no encontrada."),
            @ApiResponse(responseCode = "400", description = "Error de dependencia (ej: la comuna tiene direcciones asociadas)."),
            @ApiResponse(responseCode = "500", description = "Error interno del servidor.")
    })
    public ResponseEntity<String> eliminarComuna(@Parameter(description = "ID de la comuna a eliminar", required = true)
                                                 @PathVariable Integer id) {
        try {
            comunaService.delete(id);
            return ResponseEntity.ok("Comuna eliminada con éxito.");
        } catch (NoSuchElementException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Comuna no encontrada");
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error interno del servidor.");
        }
    }
}