package com.SAFE_Rescue.API_Perfiles.controller;

import com.SAFE_Rescue.API_Perfiles.modelo.Bombero;
import com.SAFE_Rescue.API_Perfiles.service.BomberoService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid; // ¡CRÍTICO! Necesario para la validación del modelo
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Controlador REST para la gestión de bomberos (entidad hija de Usuario).
 */
@RestController
@RequestMapping("/api-perfiles/v1/bomberos")
@Tag(name = "Bomberos", description = "Operaciones CRUD y específicas relacionadas con la entidad Bombero")
public class BomberoController {

    @Autowired
    private BomberoService bomberoService;

    // --- OPERACIONES CRUD BÁSICAS ---

    @GetMapping
    @Operation(summary = "Obtener todos los bomberos", description = "Obtiene una lista con todos los bomberos")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Lista de bomberos obtenida exitosamente.",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = Bombero.class))),
            @ApiResponse(responseCode = "204", description = "No hay bomberos registrados.")
    })
    public ResponseEntity<List<Bombero>> listar() {
        List<Bombero> bomberos = bomberoService.findAll();
        if (bomberos.isEmpty()) {
            return new ResponseEntity<>(HttpStatus.NO_CONTENT);
        }
        return ResponseEntity.ok(bomberos);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Obtiene un bombero por su ID", description = "Obtiene un bombero al buscarlo por su ID")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Bombero encontrado.",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = Bombero.class))),
            @ApiResponse(responseCode = "404", description = "Bombero no encontrado.")
    })
    public ResponseEntity<Bombero> buscarBombero(@PathVariable Integer id) {
        // findById lanza NoSuchElementException -> Capturado por GlobalExceptionHandler (404)
        Bombero bombero = bomberoService.findById(id);
        return ResponseEntity.ok(bombero);
    }

    @PostMapping
    @Operation(summary = "Crear un nuevo bombero", description = "Crea un nuevo bombero en el sistema")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Bombero creado con éxito."),
            @ApiResponse(responseCode = "400", description = "Error en la solicitud (Validación, RUN/Correo existente, Equipo/TipoUsuario/Estado no existe).")
    })
    public ResponseEntity<Bombero> agregarBombero(
            @RequestBody @Valid // CRÍTICO: Dispara las validaciones de Usuario y Bombero
            @Parameter(description = "Datos del bombero a crear", required = true)
            Bombero bombero) {

        Bombero nuevoBombero = bomberoService.save(bombero);
        return ResponseEntity.status(HttpStatus.CREATED).body(nuevoBombero);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Actualizar un bombero existente", description = "Actualiza los datos de un bombero por su ID")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Bombero actualizado con éxito."),
            @ApiResponse(responseCode = "404", description = "Bombero no encontrado."),
            @ApiResponse(responseCode = "400", description = "Error en la solicitud (Validación, RUN/Correo existente, Equipo/TipoUsuario/Estado no existe).")
    })
    public ResponseEntity<Bombero> actualizarBombero(
            @PathVariable Integer id,
            @RequestBody @Valid // CRÍTICO: Dispara las validaciones
            @Parameter(description = "Datos actualizados del bombero", required = true)
            Bombero bombero) {

        Bombero bomberoActualizado = bomberoService.update(bombero, id);
        return ResponseEntity.ok(bomberoActualizado);
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Eliminar un bombero", description = "Elimina un bombero del sistema por su ID")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Bombero eliminado con éxito."),
            @ApiResponse(responseCode = "404", description = "Bombero no encontrado."),
            @ApiResponse(responseCode = "400", description = "Error de referencia (el Usuario/Bombero tiene referencias activas, ej. es líder de un equipo).")
    })
    public ResponseEntity<String> eliminarBombero(@PathVariable Integer id) {
        // El servicio lanza NoSuchElementException (404) o IllegalStateException (400)
        bomberoService.delete(id);
        return ResponseEntity.ok("Bombero eliminado con éxito.");
    }
}