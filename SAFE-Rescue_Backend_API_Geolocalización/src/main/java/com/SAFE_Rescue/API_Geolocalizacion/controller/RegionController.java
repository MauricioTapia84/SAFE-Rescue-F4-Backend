package com.SAFE_Rescue.API_Geolocalizacion.controller;

import com.SAFE_Rescue.API_Geolocalizacion.modelo.Region; // 👈 Entidad correcta
import com.SAFE_Rescue.API_Geolocalizacion.service.RegionService; // 👈 Servicio correcto
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
 * Controlador REST para la gestión de Regiones (entidades geográficas).
 * Proporciona endpoints para operaciones CRUD.
 */
@RestController
@RequestMapping("/api-geolocalizacion/v1/regiones")
@Tag(name = "Regiones", description = "Operaciones de CRUD relacionadas con la entidad Región")
public class RegionController {

    @Autowired
    private RegionService regionService;

    // --- OPERACIONES CRUD BÁSICAS ---

    /**
     * Obtiene todas las regiones registradas en el sistema.
     * @return ResponseEntity con lista de regiones o estado NO_CONTENT si no hay registros
     */
    @GetMapping
    @Operation(summary = "Obtener todas las regiones", description = "Obtiene una lista con todas las regiones")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Lista de regiones obtenida exitosamente.",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = Region.class))), // Esquema correcto
            @ApiResponse(responseCode = "204", description = "No hay regiones registradas.")
    })
    public ResponseEntity<List<Region>> listar() {
        List<Region> regiones = regionService.findAll();
        if (regiones.isEmpty()) {
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.ok(regiones);
    }

    /**
     * Busca una región por su ID.
     * @param id ID de la región a buscar
     * @return ResponseEntity con la región encontrada o mensaje de error
     */
    @GetMapping("/{id}")
    @Operation(summary = "Obtiene una región por su ID", description = "Obtiene una región al buscarla por su ID")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Región encontrada.",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = Region.class))), // Esquema correcto
            @ApiResponse(responseCode = "404", description = "Región no encontrada.")
    })
    public ResponseEntity<?> buscarRegion(@Parameter(description = "ID de la región a buscar", required = true)
                                          @PathVariable int id) {
        Region region;
        try {
            region = regionService.findById(id);
        } catch (NoSuchElementException e) {
            return new ResponseEntity<>("Región no encontrada", HttpStatus.NOT_FOUND);
        }
        return ResponseEntity.ok(region);
    }

    /**
     * Crea una nueva región.
     * @param region Datos de la región a crear
     * @return ResponseEntity con mensaje de confirmación o error
     */
    @PostMapping
    @Operation(summary = "Crear una nueva región", description = "Crea una nueva región en el sistema")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Región creada con éxito."),
            @ApiResponse(responseCode = "400", description = "Error en la solicitud (ej: Nombre/Identificación duplicada o País no válido)."),
            @ApiResponse(responseCode = "500", description = "Error interno del servidor.")
    })
    public ResponseEntity<String> agregarRegion(@RequestBody @Parameter(description = "Datos de la región a crear", required = true)
                                                Region region) {
        try {
            regionService.save(region);
            return ResponseEntity.status(HttpStatus.CREATED).body("Región creada con éxito.");
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error interno del servidor.");
        }
    }

    /**
     * Actualiza una región existente.
     * @param id ID de la región a actualizar
     * @param region Datos actualizados de la región
     * @return ResponseEntity con mensaje de confirmación o error
     */
    @PutMapping("/{id}")
    @Operation(summary = "Actualizar una región existente", description = "Actualiza los datos de una región por su ID")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Región actualizada con éxito."),
            @ApiResponse(responseCode = "404", description = "Región no encontrada."),
            @ApiResponse(responseCode = "400", description = "Error en la solicitud."),
            @ApiResponse(responseCode = "500", description = "Error interno del servidor.")
    })
    public ResponseEntity<String> actualizarRegion(@Parameter(description = "ID de la región a actualizar", required = true)
                                                   @PathVariable Integer id,
                                                   @RequestBody @Parameter(description = "Datos actualizados de la región", required = true)
                                                   Region region) {
        try {
            regionService.update(region, id);
            return ResponseEntity.ok("Región actualizada con éxito");
        } catch (NoSuchElementException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Región no encontrada");
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error interno del servidor.");
        }
    }

    /**
     * Elimina una región del sistema.
     * @param id ID de la región a eliminar
     * @return ResponseEntity con mensaje de confirmación
     */
    @DeleteMapping("/{id}")
    @Operation(summary = "Eliminar una región", description = "Elimina una región del sistema por su ID")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Región eliminada con éxito."),
            @ApiResponse(responseCode = "404", description = "Región no encontrada."),
            @ApiResponse(responseCode = "400", description = "Error de dependencia (ej: la región tiene comunas asociadas)."),
            @ApiResponse(responseCode = "500", description = "Error interno del servidor.")
    })
    public ResponseEntity<String> eliminarRegion(@Parameter(description = "ID de la región a eliminar", required = true)
                                                 @PathVariable Integer id) {
        try {
            regionService.delete(id);
            return ResponseEntity.ok("Región eliminada con éxito.");
        } catch (NoSuchElementException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Región no encontrada");
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error interno del servidor.");
        }
    }
}