package com.SAFE_Rescue.API_Geolocalizacion.controller;

import com.SAFE_Rescue.API_Geolocalizacion.modelo.Coordenadas;
import com.SAFE_Rescue.API_Geolocalizacion.service.CoordenadasService; // 👈 Servicio actualizado
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
 * Controlador REST para la gestión de Coordenadas (latitud y longitud).
 * Proporciona endpoints para operaciones CRUD.
 */
@RestController
// Ruta base actualizada (opcional, puedes mantenerla si prefieres)
@RequestMapping("/api-geolocalizacion/v1/coordenadas")
// Tag actualizado
@Tag(name = "Coordenadas", description = "Operaciones de CRUD relacionadas con coordenadas de latitud y longitud")
public class CoordenadasController { // Cambiado de CordenadasController a CoordenadasController

    // Servicio inyectado actualizado
    @Autowired
    private CoordenadasService coordenadasService; // Cambiado el nombre del servicio

    // --- OPERACIONES CRUD BÁSICAS ---

    /**
     * Obtiene todas las coordenadas registradas en el sistema.
     * @return ResponseEntity con lista de coordenadas o estado NO_CONTENT si no hay registros
     */
    @GetMapping
    @Operation(summary = "Obtener todas las coordenadas", description = "Obtiene una lista con todas las coordenadas de latitud y longitud.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Lista de coordenadas obtenida exitosamente.",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = Coordenadas.class))), // Esquema actualizado
            @ApiResponse(responseCode = "204", description = "No hay coordenadas registradas.") // Descripción actualizada
    })
    public ResponseEntity<List<Coordenadas>> listar() { // Tipo de retorno actualizado
        List<Coordenadas> coordenadas = coordenadasService.findAll(); // Variable y servicio actualizados
        if (coordenadas.isEmpty()) {
            return new ResponseEntity<>(HttpStatus.NO_CONTENT);
        }
        return ResponseEntity.ok(coordenadas);
    }

    /**
     * Busca coordenadas por su ID.
     * @param id ID de las coordenadas a buscar
     * @return ResponseEntity con las coordenadas encontradas o mensaje de error
     */
    @GetMapping("/{id}")
    @Operation(summary = "Obtiene coordenadas por su ID", description = "Obtiene coordenadas al buscarlas por su ID")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Coordenadas encontradas.", // Descripción actualizada
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = Coordenadas.class))), // Esquema actualizado
            @ApiResponse(responseCode = "404", description = "Coordenadas no encontradas.") // Descripción actualizada
    })
    public ResponseEntity<?> buscarCoordenadas(@Parameter(description = "ID de las coordenadas a buscar", required = true) // Nombre y descripción actualizados
                                               @PathVariable int id) {
        Coordenadas coordenadas; // Variable actualizada
        try {
            coordenadas = coordenadasService.findById(id); // Servicio actualizado
        } catch (NoSuchElementException e) {
            return new ResponseEntity<>("Coordenadas no encontradas", HttpStatus.NOT_FOUND); // Mensaje actualizado
        }
        return ResponseEntity.ok(coordenadas);
    }

    /**
     * Crea nuevas coordenadas.
     * @param coordenadas Datos de las coordenadas a crear
     * @return ResponseEntity con mensaje de confirmación o error
     */
    @PostMapping
    @Operation(summary = "Crear nuevas coordenadas", description = "Crea nuevas coordenadas (Lat/Lng) en el sistema") // Descripción actualizada
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Coordenadas creadas con éxito."), // Descripción actualizada
            @ApiResponse(responseCode = "400", description = "Error en la solicitud (ej: Latitud/Longitud fuera de rango)."),
            @ApiResponse(responseCode = "500", description = "Error interno del servidor.")
    })
    public ResponseEntity<String> agregarCoordenadas(@RequestBody @Parameter(description = "Datos de las coordenadas a crear", required = true) // Nombre y descripción actualizados
                                                     Coordenadas coordenadas) { // Parámetro actualizado
        try {
            coordenadasService.save(coordenadas); // Servicio actualizado
            return ResponseEntity.status(HttpStatus.CREATED).body("Coordenadas creadas con éxito."); // Mensaje actualizado
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error interno del servidor.");
        }
    }

    /**
     * Actualiza coordenadas existentes.
     * @param id ID de las coordenadas a actualizar
     * @param coordenadas Datos actualizados de las coordenadas
     * @return ResponseEntity con mensaje de confirmación o error
     */
    @PutMapping("/{id}")
    @Operation(summary = "Actualizar coordenadas existentes", description = "Actualiza los datos de coordenadas por su ID") // Descripción actualizada
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Coordenadas actualizadas con éxito."), // Descripción actualizada
            @ApiResponse(responseCode = "404", description = "Coordenadas no encontradas."), // Descripción actualizada
            @ApiResponse(responseCode = "400", description = "Error en la solicitud."),
            @ApiResponse(responseCode = "500", description = "Error interno del servidor.")
    })
    public ResponseEntity<String> actualizarCoordenadas(@Parameter(description = "ID de las coordenadas a actualizar", required = true) // Nombre y descripción actualizados
                                                        @PathVariable Integer id,
                                                        @RequestBody @Parameter(description = "Datos actualizados de las coordenadas", required = true) // Descripción actualizada
                                                        Coordenadas coordenadas) { // Parámetro actualizado
        try {
            coordenadasService.update(coordenadas, id); // Servicio actualizado
            return ResponseEntity.ok("Coordenadas actualizadas con éxito"); // Mensaje actualizado
        } catch (NoSuchElementException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Coordenadas no encontradas"); // Mensaje actualizado
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error interno del servidor.");
        }
    }

    /**
     * Elimina coordenadas del sistema.
     * @param id ID de las coordenadas a eliminar
     * @return ResponseEntity con mensaje de confirmación
     */
    @DeleteMapping("/{id}")
    @Operation(summary = "Eliminar coordenadas", description = "Elimina coordenadas del sistema por su ID") // Descripción actualizada
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Coordenadas eliminadas con éxito."), // Descripción actualizada
            @ApiResponse(responseCode = "404", description = "Coordenadas no encontradas."), // Descripción actualizada
            @ApiResponse(responseCode = "400", description = "Error de dependencia (por ejemplo, si están asociadas a una Dirección)."),
            @ApiResponse(responseCode = "500", description = "Error interno del servidor.")
    })
    public ResponseEntity<String> eliminarCoordenadas(@Parameter(description = "ID de las coordenadas a eliminar", required = true) // Nombre y descripción actualizados
                                                      @PathVariable Integer id) {
        try {
            coordenadasService.delete(id); // Servicio actualizado
            return ResponseEntity.ok("Coordenadas eliminadas con éxito."); // Mensaje actualizado
        } catch (NoSuchElementException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Coordenadas no encontradas"); // Mensaje actualizado
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error interno del servidor.");
        }
    }
}