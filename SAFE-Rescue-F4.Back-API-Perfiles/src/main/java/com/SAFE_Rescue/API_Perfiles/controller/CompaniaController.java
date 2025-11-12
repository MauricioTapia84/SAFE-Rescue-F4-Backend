package com.SAFE_Rescue.API_Perfiles.controller;

import com.SAFE_Rescue.API_Perfiles.modelo.Compania;
import com.SAFE_Rescue.API_Perfiles.service.CompaniaService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.NoSuchElementException;

@RestController
@RequestMapping("/api-perfiles/v1/companias")
@Tag(name = "Compañías", description = "Gestión de las entidades de Compañías de Bomberos")
public class CompaniaController {

    @Autowired
    private CompaniaService companiaService;

    // --- GET ALL ---
    @Operation(
            summary = "Obtiene todas las Compañías",
            description = "Devuelve una lista de todas las compañías de bomberos registradas.",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Lista de compañías obtenida exitosamente")
            }
    )
    @GetMapping
    public ResponseEntity<List<Compania>> getAllCompanias() {
        List<Compania> companias = companiaService.findAll();
        return ResponseEntity.ok(companias);
    }

    // --- GET BY ID ---
    @Operation(
            summary = "Obtiene una Compañía por ID",
            description = "Devuelve los detalles de una compañía específica utilizando su ID.",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Compañía encontrada", content = @Content(schema = @Schema(implementation = Compania.class))),
                    @ApiResponse(responseCode = "404", description = "Compañía no encontrada")
            }
    )
    @GetMapping("/{id}")
    public ResponseEntity<Compania> getCompaniaById(@PathVariable Integer id) {
        try {
            Compania compania = companiaService.findById(id);
            return ResponseEntity.ok(compania);
        } catch (NoSuchElementException e) {
            // Lanza una excepción que Spring Boot manejará para retornar un 404 NOT FOUND
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, e.getMessage(), e);
        }
    }

    // --- POST / CREATE ---
    @Operation(
            summary = "Crea una nueva Compañía",
            description = "Registra una nueva compañía en el sistema. Requiere validación de datos.",
            responses = {
                    @ApiResponse(responseCode = "201", description = "Compañía creada exitosamente", content = @Content(schema = @Schema(implementation = Compania.class))),
                    @ApiResponse(responseCode = "400", description = "Datos de entrada inválidos (Ej: nombre duplicado o campos faltantes)")
            }
    )
    @PostMapping
    public ResponseEntity<Compania> createCompania(@Valid @RequestBody Compania compania) {
        try {
            Compania nuevaCompania = companiaService.save(compania);
            // Retorna 201 Created
            return new ResponseEntity<>(nuevaCompania, HttpStatus.CREATED);
        } catch (IllegalArgumentException e) {
            // Captura errores de validación de negocio (nombre duplicado, etc.) del servicio y retorna 400 BAD REQUEST
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, e.getMessage(), e);
        }
    }

    // --- PUT / UPDATE ---
    @Operation(
            summary = "Actualiza una Compañía existente",
            description = "Actualiza completamente una compañía existente por su ID.",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Compañía actualizada exitosamente", content = @Content(schema = @Schema(implementation = Compania.class))),
                    @ApiResponse(responseCode = "400", description = "Datos de entrada inválidos"),
                    @ApiResponse(responseCode = "404", description = "Compañía no encontrada")
            }
    )
    @PutMapping("/{id}")
    public ResponseEntity<Compania> updateCompania(@PathVariable Integer id, @Valid @RequestBody Compania companiaDetails) {
        try {
            Compania companiaActualizada = companiaService.update(companiaDetails, id);
            return ResponseEntity.ok(companiaActualizada);
        } catch (NoSuchElementException e) {
            // La compañía no existe
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, e.getMessage(), e);
        } catch (IllegalArgumentException e) {
            // Datos inválidos o conflicto de integridad (ej: nombre duplicado)
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, e.getMessage(), e);
        }
    }

    // --- DELETE ---
    @Operation(
            summary = "Elimina una Compañía",
            description = "Elimina una compañía por su ID.",
            responses = {
                    @ApiResponse(responseCode = "204", description = "Compañía eliminada exitosamente"),
                    @ApiResponse(responseCode = "404", description = "Compañía no encontrada"),
                    @ApiResponse(responseCode = "409", description = "Conflicto: La compañía tiene registros asociados y no puede ser eliminada")
            }
    )
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteCompania(@PathVariable Integer id) {
        try {
            companiaService.delete(id);
            // Retorna 204 No Content para indicar éxito sin cuerpo de respuesta
            return ResponseEntity.noContent().build();
        } catch (NoSuchElementException e) {
            // La compañía no existe
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, e.getMessage(), e);
        } catch (Exception e) {
            // Captura la DataIntegrityViolationException del servicio y la mapea a 409 CONFLICT
            throw new ResponseStatusException(HttpStatus.CONFLICT, "No se pudo eliminar la compañía. Posiblemente existan referencias activas (Bomberos/Equipos) que dependen de esta compañía.", e);
        }
    }
}