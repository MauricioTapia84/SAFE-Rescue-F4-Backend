package com.SAFE_Rescue.API_Perfiles.controller;

import com.SAFE_Rescue.API_Perfiles.modelo.TipoUsuario;
import com.SAFE_Rescue.API_Perfiles.service.TipoUsuarioService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content; // Importación necesaria
import io.swagger.v3.oas.annotations.media.Schema; // Importación necesaria
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Controlador REST para la gestión de tipos de usuario
 */
@RestController
@RequestMapping("/api-perfiles/v1/tipos-usuario")
@Tag(name = "Tipos de Usuario", description = "Operaciones de CRUD relacionadas con Tipos de Usuario")
public class TipoUsuarioController {

    @Autowired
    private TipoUsuarioService tipoUsuarioService;

    // --- OPERACIONES CRUD BÁSICAS ---

    @GetMapping
    @Operation(summary = "Obtener todos los tipos de usuario", description = "Obtiene una lista con todos los tipos de usuario")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Lista de tipos de usuario obtenida exitosamente.",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = TipoUsuario.class))),
            @ApiResponse(responseCode = "204", description = "No hay tipos de usuario registrados.")
    })
    public ResponseEntity<List<TipoUsuario>> listar() {
        List<TipoUsuario> tiposUsuario = tipoUsuarioService.findAll();
        if (tiposUsuario.isEmpty()) {
            return new ResponseEntity<>(HttpStatus.NO_CONTENT);
        }
        return ResponseEntity.ok(tiposUsuario);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Obtiene un tipo de usuario por su ID", description = "Obtiene un tipo de usuario al buscarlo por su ID")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Tipo de usuario encontrado.",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = TipoUsuario.class))),
            @ApiResponse(responseCode = "404", description = "Tipo de usuario no encontrado.")
    })
    public ResponseEntity<TipoUsuario> buscarTipoUsuario(
            @Parameter(description = "ID del tipo de usuario a buscar", required = true)
            @PathVariable Integer id) {

        // findById lanza NoSuchElementException -> Capturado por GlobalExceptionHandler (404)
        TipoUsuario tipoUsuario = tipoUsuarioService.findById(id);
        return ResponseEntity.ok(tipoUsuario);
    }

    @PostMapping
    @Operation(summary = "Crear un nuevo tipo de usuario", description = "Crea un nuevo tipo de usuario en el sistema")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Tipo de usuario creado con éxito.",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = TipoUsuario.class))),
            @ApiResponse(responseCode = "400", description = "Error de validación o integridad de datos.")
    })
    public ResponseEntity<TipoUsuario> agregarTipoUsuario(
            @RequestBody @Valid // Dispara las validaciones de TipoUsuario.java
            @Parameter(description = "Datos del tipo de usuario a crear", required = true)
            TipoUsuario tipoUsuario) {

        TipoUsuario nuevoTipo = tipoUsuarioService.save(tipoUsuario);
        return ResponseEntity.status(HttpStatus.CREATED).body(nuevoTipo);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Actualizar un tipo de usuario existente", description = "Actualiza los datos de un tipo de usuario por su ID")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Tipo de usuario actualizado con éxito.",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = TipoUsuario.class))),
            @ApiResponse(responseCode = "404", description = "Tipo de usuario no encontrado."),
            @ApiResponse(responseCode = "400", description = "Error en la solicitud (Validación/Integridad).")
    })
    public ResponseEntity<TipoUsuario> actualizarTipoUsuario(
            @Parameter(description = "ID del tipo de usuario a actualizar", required = true)
            @PathVariable Integer id,
            @RequestBody @Valid // Dispara las validaciones
            @Parameter(description = "Datos actualizados del tipo de usuario", required = true)
            TipoUsuario tipoUsuario) {

        TipoUsuario tipoActualizado = tipoUsuarioService.update(tipoUsuario, id);
        return ResponseEntity.ok(tipoActualizado);
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Eliminar un tipo de usuario", description = "Elimina un tipo de usuario del sistema por su ID")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Tipo de usuario eliminado con éxito."),
            @ApiResponse(responseCode = "404", description = "Tipo de usuario no encontrado."),
            @ApiResponse(responseCode = "400", description = "Error de referencia (usuarios asociados).")
    })
    public ResponseEntity<String> eliminarTipoUsuario(
            @Parameter(description = "ID del tipo de usuario a eliminar", required = true)
            @PathVariable Integer id) {

        tipoUsuarioService.delete(id);
        return ResponseEntity.ok("Tipo de usuario eliminado con éxito.");
    }
}