package com.SAFE_Rescue.API_Perfiles.controller;

import com.SAFE_Rescue.API_Perfiles.modelo.TipoEquipo;
import com.SAFE_Rescue.API_Perfiles.service.TipoEquipoService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content; // Importación necesaria
import io.swagger.v3.oas.annotations.media.Schema; // Importación necesaria
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Controlador REST para la gestión de tipos de equipo
 */
@RestController
@RequestMapping("/api-perfiles/v1/tipos-equipo")
@Tag(name = "Tipos de Equipo", description = "Operaciones de CRUD relacionadas con Tipos de Equipo")
public class TipoEquipoController {

    @Autowired
    private TipoEquipoService tipoEquipoService;

    // --- OPERACIONES CRUD BÁSICAS ---

    @GetMapping
    @Operation(summary = "Obtener todos los tipos de equipo", description = "Obtiene una lista con todos los tipos de equipo")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Lista de tipos de equipo obtenida exitosamente.",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = TipoEquipo.class))),
            @ApiResponse(responseCode = "204", description = "No hay tipos de equipo registrados.")
    })
    public ResponseEntity<List<TipoEquipo>> listar() {
        List<TipoEquipo> tiposEquipo = tipoEquipoService.findAll();
        if (tiposEquipo.isEmpty()) {
            return new ResponseEntity<>(HttpStatus.NO_CONTENT);
        }
        return ResponseEntity.ok(tiposEquipo);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Obtiene un tipo de equipo por su ID", description = "Obtiene un tipo de equipo al buscarlo por su ID")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Tipo de equipo encontrado.",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = TipoEquipo.class))),
            @ApiResponse(responseCode = "404", description = "Tipo de equipo no encontrado.")
    })
    public ResponseEntity<TipoEquipo> buscarTipoEquipo(
            @Parameter(description = "ID del tipo de equipo a buscar", required = true)
            @PathVariable Integer id) {

        // findById lanza NoSuchElementException -> Capturado por GlobalExceptionHandler (404)
        TipoEquipo tipoEquipo = tipoEquipoService.findById(id);
        return ResponseEntity.ok(tipoEquipo);
    }

    @PostMapping
    @Operation(summary = "Crear un nuevo tipo de equipo", description = "Crea un nuevo tipo de equipo en el sistema")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Tipo de equipo creado con éxito.",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = TipoEquipo.class))),
            @ApiResponse(responseCode = "400", description = "Error de validación o integridad de datos.")
    })
    public ResponseEntity<TipoEquipo> agregarTipoEquipo(
            @RequestBody @Valid // Dispara las validaciones de TipoEquipo.java
            @Parameter(description = "Datos del tipo de equipo a crear", required = true)
            TipoEquipo tipoEquipo) {

        TipoEquipo nuevoTipo = tipoEquipoService.save(tipoEquipo);
        return ResponseEntity.status(HttpStatus.CREATED).body(nuevoTipo);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Actualizar un tipo de equipo existente", description = "Actualiza los datos de un tipo de equipo por su ID")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Tipo de equipo actualizado con éxito.",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = TipoEquipo.class))),
            @ApiResponse(responseCode = "404", description = "Tipo de equipo no encontrado."),
            @ApiResponse(responseCode = "400", description = "Error en la solicitud (Validación/Integridad).")
    })
    public ResponseEntity<TipoEquipo> actualizarTipoEquipo(
            @Parameter(description = "ID del tipo de equipo a actualizar", required = true)
            @PathVariable Integer id,
            @RequestBody @Valid // Dispara las validaciones
            @Parameter(description = "Datos actualizados del tipo de equipo", required = true)
            TipoEquipo tipoEquipo) {

        TipoEquipo tipoActualizado = tipoEquipoService.update(tipoEquipo, id);
        return ResponseEntity.ok(tipoActualizado);
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Eliminar un tipo de equipo", description = "Elimina un tipo de equipo del sistema por su ID")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Tipo de equipo eliminado con éxito."),
            @ApiResponse(responseCode = "404", description = "Tipo de equipo no encontrado."),
            @ApiResponse(responseCode = "400", description = "Error de referencia (equipos asociados).")
    })
    public ResponseEntity<String> eliminarTipoEquipo(
            @Parameter(description = "ID del tipo de equipo a eliminar", required = true)
            @PathVariable Integer id) {

        tipoEquipoService.delete(id);
        return ResponseEntity.ok("Tipo de equipo eliminado con éxito.");
    }
}