package com.SAFE_Rescue.API_Geolocalizacion.controller;

import com.SAFE_Rescue.API_Geolocalizacion.modelo.Cordenadas;
import com.SAFE_Rescue.API_Geolocalizacion.service.CordenadasService; // 👈 Servicio correcto
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
 * Controlador REST para la gestión de Geolocalizaciones (coordenadas Lat/Lng).
 * Proporciona endpoints para operaciones CRUD.
 */
@RestController
// Ruta base adaptada
@RequestMapping("/api-geolocalizacion/v1/localizaciones")
// Tag adaptado
@Tag(name = "Geolocalizaciones", description = "Operaciones de CRUD relacionadas con coordenadas de Geolocalización")
public class CordenadasController {

    // Servicio inyectado adaptado
    @Autowired
    private CordenadasService cordenadasService;

    // --- OPERACIONES CRUD BÁSICAS ---

    /**
     * Obtiene todas las geolocalizaciones registradas en el sistema.
     * @return ResponseEntity con lista de geolocalizaciones o estado NO_CONTENT si no hay registros
     */
    @GetMapping
    @Operation(summary = "Obtener todas las geolocalizaciones", description = "Obtiene una lista con todas las coordenadas de latitud y longitud.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Lista de geolocalizaciones obtenida exitosamente.",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = Cordenadas.class))), // Esquema correcto
            @ApiResponse(responseCode = "204", description = "No hay geolocalizaciones registradas.")
    })
    public ResponseEntity<List<Cordenadas>> listar() {
        List<Cordenadas> geolocalizaciones = cordenadasService.findAll();
        if (geolocalizaciones.isEmpty()) {
            return new ResponseEntity<>(HttpStatus.NO_CONTENT);
        }
        return ResponseEntity.ok(geolocalizaciones);
    }

    /**
     * Busca una geolocalización por su ID.
     * @param id ID de la geolocalización a buscar
     * @return ResponseEntity con la geolocalización encontrada o mensaje de error
     */
    @GetMapping("/{id}")
    @Operation(summary = "Obtiene una geolocalización por su ID", description = "Obtiene una coordenada al buscarla por su ID")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Geolocalización encontrada.",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = Cordenadas.class))), // Esquema correcto
            @ApiResponse(responseCode = "404", description = "Geolocalización no encontrada.")
    })
    public ResponseEntity<?> buscarGeolocalizacion(@Parameter(description = "ID de la geolocalización a buscar", required = true)
                                                   @PathVariable int id) {
        Cordenadas cordenadas;
        try {
            cordenadas = cordenadasService.findById(id);
        } catch (NoSuchElementException e) {
            return new ResponseEntity<>("Geolocalización no encontrada", HttpStatus.NOT_FOUND);
        }
        return ResponseEntity.ok(cordenadas);
    }

    /**
     * Crea una nueva geolocalización.
     * @param cordenadas Datos de la geolocalización a crear
     * @return ResponseEntity con mensaje de confirmación o error
     */
    @PostMapping
    @Operation(summary = "Crear una nueva geolocalización", description = "Crea una nueva coordenada (Lat/Lng) en el sistema")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Geolocalización creada con éxito."),
            @ApiResponse(responseCode = "400", description = "Error en la solicitud (ej: Latitud/Longitud fuera de rango)."),
            @ApiResponse(responseCode = "500", description = "Error interno del servidor.")
    })
    public ResponseEntity<String> agregarGeolocalizacion(@RequestBody @Parameter(description = "Datos de la geolocalización a crear", required = true)
                                                         Cordenadas cordenadas) {
        try {
            cordenadasService.save(cordenadas);
            return ResponseEntity.status(HttpStatus.CREATED).body("Geolocalización creada con éxito.");
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error interno del servidor.");
        }
    }

    /**
     * Actualiza una geolocalización existente.
     * @param id ID de la geolocalización a actualizar
     * @param cordenadas Datos actualizados de la geolocalización
     * @return ResponseEntity con mensaje de confirmación o error
     */
    @PutMapping("/{id}")
    @Operation(summary = "Actualizar una geolocalización existente", description = "Actualiza los datos de una coordenada por su ID")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Geolocalización actualizada con éxito."),
            @ApiResponse(responseCode = "404", description = "Geolocalización no encontrada."),
            @ApiResponse(responseCode = "400", description = "Error en la solicitud."),
            @ApiResponse(responseCode = "500", description = "Error interno del servidor.")
    })
    public ResponseEntity<String> actualizarGeolocalizacion(@Parameter(description = "ID de la geolocalización a actualizar", required = true)
                                                            @PathVariable Integer id,
                                                            @RequestBody @Parameter(description = "Datos actualizados de la geolocalización", required = true)
                                                            Cordenadas cordenadas) {
        try {
            cordenadasService.update(cordenadas, id);
            return ResponseEntity.ok("Geolocalización actualizada con éxito");
        } catch (NoSuchElementException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Geolocalización no encontrada");
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error interno del servidor.");
        }
    }

    /**
     * Elimina una geolocalización del sistema.
     * @param id ID de la geolocalización a eliminar
     * @return ResponseEntity con mensaje de confirmación
     */
    @DeleteMapping("/{id}")
    @Operation(summary = "Eliminar una geolocalización", description = "Elimina una coordenada del sistema por su ID")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Geolocalización eliminada con éxito."),
            @ApiResponse(responseCode = "404", description = "Geolocalización no encontrada."),
            @ApiResponse(responseCode = "400", description = "Error de dependencia (por ejemplo, si está asociada a una Dirección)."),
            @ApiResponse(responseCode = "500", description = "Error interno del servidor.")
    })
    public ResponseEntity<String> eliminarGeolocalizacion(@Parameter(description = "ID de la geolocalización a eliminar", required = true)
                                                          @PathVariable Integer id) {
        try {
            cordenadasService.delete(id);
            return ResponseEntity.ok("Geolocalización eliminada con éxito.");
        } catch (NoSuchElementException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Geolocalización no encontrada");
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error interno del servidor.");
        }
    }
}