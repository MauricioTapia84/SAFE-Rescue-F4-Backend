package com.SAFE_Rescue.API_Geolocalizacion.controller;

import com.SAFE_Rescue.API_Geolocalizacion.modelo.Pais; // 👈 Entidad correcta
import com.SAFE_Rescue.API_Geolocalizacion.service.PaisService; // 👈 Servicio correcto
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
 * Controlador REST para la gestión de Países.
 * Proporciona endpoints para operaciones CRUD.
 */
@RestController
@RequestMapping("/api-geolocalizacion/v1/paises")
@Tag(name = "Países", description = "Operaciones de CRUD relacionadas con la entidad País")
public class PaisController {

    @Autowired
    private PaisService paisService; // 👈 Servicio inyectado correcto

    // --- OPERACIONES CRUD BÁSICAS ---

    /**
     * Obtiene todos los países registrados en el sistema.
     * @return ResponseEntity con lista de países o estado NO_CONTENT si no hay registros
     */
    @GetMapping
    @Operation(summary = "Obtener todos los países", description = "Obtiene una lista con todos los países")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Lista de países obtenida exitosamente.",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = Pais.class))), // Esquema correcto
            @ApiResponse(responseCode = "204", description = "No hay países registrados.")
    })
    public ResponseEntity<List<Pais>> listar() {
        List<Pais> paises = paisService.findAll();
        if (paises.isEmpty()) {
            // Usa el builder conciso para 204
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.ok(paises);
    }

    /**
     * Busca un país por su ID.
     * @param id ID del país a buscar
     * @return ResponseEntity con el país encontrado o mensaje de error
     */
    @GetMapping("/{id}")
    @Operation(summary = "Obtiene un país por su ID", description = "Obtiene un país al buscarlo por su ID")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "País encontrado.",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = Pais.class))), // Esquema correcto
            @ApiResponse(responseCode = "404", description = "País no encontrado.")
    })
    public ResponseEntity<?> buscarPais(@Parameter(description = "ID del país a buscar", required = true)
                                        @PathVariable int id) {
        Pais pais;
        try {
            pais = paisService.findById(id);
        } catch (NoSuchElementException e) {
            return new ResponseEntity<>("País no encontrado", HttpStatus.NOT_FOUND);
        }
        return ResponseEntity.ok(pais);
    }

    /**
     * Crea un nuevo país.
     * @param pais Datos del país a crear
     * @return ResponseEntity con mensaje de confirmación o error
     */
    @PostMapping
    @Operation(summary = "Crear un nuevo país", description = "Crea un nuevo país en el sistema")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "País creado con éxito."),
            @ApiResponse(responseCode = "400", description = "Error en la solicitud (ej: código ISO duplicado o datos faltantes)."),
            @ApiResponse(responseCode = "500", description = "Error interno del servidor.")
    })
    public ResponseEntity<String> agregarPais(@RequestBody @Parameter(description = "Datos del país a crear", required = true)
                                              Pais pais) {
        try {
            paisService.save(pais);
            return ResponseEntity.status(HttpStatus.CREATED).body("País creado con éxito.");
        } catch (RuntimeException e) { // Captura IllegalArgumentException del Service
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error interno del servidor.");
        }
    }

    /**
     * Actualiza un país existente.
     * @param id ID del país a actualizar
     * @param pais Datos actualizados del país
     * @return ResponseEntity con mensaje de confirmación o error
     */
    @PutMapping("/{id}")
    @Operation(summary = "Actualizar un país existente", description = "Actualiza los datos de un país por su ID")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "País actualizado con éxito."),
            @ApiResponse(responseCode = "404", description = "País no encontrado."),
            @ApiResponse(responseCode = "400", description = "Error en la solicitud."),
            @ApiResponse(responseCode = "500", description = "Error interno del servidor.")
    })
    public ResponseEntity<String> actualizarPais(@Parameter(description = "ID del país a actualizar", required = true)
                                                 @PathVariable Integer id,
                                                 @RequestBody @Parameter(description = "Datos actualizados del país", required = true)
                                                 Pais pais) {
        try {
            paisService.update(pais, id);
            return ResponseEntity.ok("País actualizado con éxito");
        } catch (NoSuchElementException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("País no encontrado");
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error interno del servidor.");
        }
    }

    /**
     * Elimina un país del sistema.
     * @param id ID del país a eliminar
     * @return ResponseEntity con mensaje de confirmación
     */
    @DeleteMapping("/{id}")
    @Operation(summary = "Eliminar un país", description = "Elimina un país del sistema por su ID")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "País eliminado con éxito."),
            @ApiResponse(responseCode = "404", description = "País no encontrado."),
            @ApiResponse(responseCode = "400", description = "Error de dependencia (ej: el país tiene regiones asociadas)."),
            @ApiResponse(responseCode = "500", description = "Error interno del servidor.")
    })
    public ResponseEntity<String> eliminarPais(@Parameter(description = "ID del país a eliminar", required = true)
                                               @PathVariable Integer id) {
        try {
            paisService.delete(id);
            return ResponseEntity.ok("País eliminado con éxito.");
        } catch (NoSuchElementException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("País no encontrado");
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error interno del servidor.");
        }
    }
}