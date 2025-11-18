package com.SAFE_Rescue.API_Donaciones.controller;

import com.SAFE_Rescue.API_Donaciones.modelo.Donacion;
import com.SAFE_Rescue.API_Donaciones.service.DonacionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.NoSuchElementException;

/**
 * Controlador REST para la gestión de donaciones.
 * Proporciona endpoints para operaciones CRUD de la entidad Donacion.
 */
@RestController
@RequestMapping("/api-donaciones/v1/donaciones") // Path actualizado
@Tag(name = "Donaciones", description = "Gestión de la entidad Donacion, que incluye monto, fecha, método de pago y donante (ID externo).")
public class DonacionController {

    @Autowired
    private DonacionService donacionService;

    // OPERACIONES CRUD BÁSICAS

    /**
     * Obtiene todas las donaciones registradas en el sistema.
     * @return ResponseEntity con lista de donaciones o estado NO_CONTENT si no hay registros
     */
    @Operation(summary = "Obtener todas las donaciones", description = "Recupera una lista de todas las donaciones registradas.")
    @ApiResponse(responseCode = "200", description = "Lista de donaciones recuperada con éxito.")
    @ApiResponse(responseCode = "204", description = "No hay donaciones registradas.", content = @Content)
    @GetMapping
    public ResponseEntity<List<Donacion>> listar(){

        List<Donacion> donaciones = donacionService.findAll();
        if(donaciones.isEmpty()){
            return new ResponseEntity<>(HttpStatus.NO_CONTENT);
        }
        return ResponseEntity.ok(donaciones);
    }

    /**
     * Busca una donación por su ID.
     * @param id ID de la donación a buscar
     * @return ResponseEntity con la donación encontrada o mensaje de error
     */
    @Operation(summary = "Buscar donación por ID", description = "Busca y recupera una donación específica usando su identificador.")
    @ApiResponse(responseCode = "200", description = "Donación encontrada con éxito.")
    @ApiResponse(responseCode = "404", description = "Donación no encontrada.", content = @Content)
    @GetMapping("/{id}")
    public ResponseEntity<?> buscarDonacion(
            @Parameter(description = "ID de la donación a buscar", required = true)
            @PathVariable Integer id) {
        Donacion donacion;

        try {
            donacion = donacionService.findById(id);
        }catch(NoSuchElementException e){
            return new ResponseEntity<String>("Donación no encontrada", HttpStatus.NOT_FOUND);
        }

        return ResponseEntity.ok(donacion);

    }

    /**
     * Crea una nueva donación.
     * @param donacion Datos de la donación a crear
     * @return ResponseEntity con mensaje de confirmación o error
     */
    @Operation(summary = "Crear nueva donación", description = "Registra una nueva donación en el sistema. Requiere un 'idDonante' válido y que exista en el microservicio de Usuarios.")
    @ApiResponse(responseCode = "201", description = "Donación creada con éxito.")
    @ApiResponse(responseCode = "400", description = "Error de validación, ID de donante no encontrado, o datos incompletos.")
    @ApiResponse(responseCode = "500", description = "Error interno del servidor.")
    @PostMapping
    public ResponseEntity<String> agregarDonacion(@Valid @RequestBody Donacion donacion) {
        try {
            donacionService.save(donacion);
            return ResponseEntity.status(HttpStatus.CREATED).body("Donación creada con éxito.");
        } catch (IllegalArgumentException e) {
            // Captura errores de validación de negocio (ej: Donante ID no existe)
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Error de validación: " + e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error interno del servidor: " + e.getMessage());
        }
    }

    /**
     * Actualiza una donación existente.
     * @param id ID de la donación a actualizar
     * @param donacion Datos actualizados de la donación
     * @return ResponseEntity con mensaje de confirmación o error
     */
    @Operation(summary = "Actualizar donación", description = "Modifica los datos de una donación existente.")
    @ApiResponse(responseCode = "200", description = "Donación actualizada con éxito.")
    @ApiResponse(responseCode = "404", description = "Donación no encontrada.")
    @ApiResponse(responseCode = "400", description = "Error de validación o ID de referencia no válido.")
    @PutMapping("/{id}")
    public ResponseEntity<String> actualizarDonacion(
            @Parameter(description = "ID de la donación a actualizar", required = true)
            @PathVariable Integer id,
            @Valid @RequestBody Donacion donacion) {
        try {
            donacionService.update(donacion, id);
            return ResponseEntity.ok("Donación actualizada con éxito");
        } catch (NoSuchElementException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body("Donación no encontrada");
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body("Error de validación: " + e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error interno del servidor.");
        }
    }

    /**
     * Elimina una donación del sistema.
     * @param id ID de la donación a eliminar
     * @return ResponseEntity con mensaje de confirmación
     */
    @Operation(summary = "Eliminar donación", description = "Elimina una donación del sistema usando su ID.")
    @ApiResponse(responseCode = "200", description = "Donación eliminada con éxito.")
    @ApiResponse(responseCode = "404", description = "Donación no encontrada.")
    @DeleteMapping("/{id}")
    public ResponseEntity<String> eliminarDonacion(
            @Parameter(description = "ID de la donación a eliminar", required = true)
            @PathVariable Integer id) {
        try {
            donacionService.delete(id);
            return ResponseEntity.ok("Donación eliminada con éxito.");
        } catch (NoSuchElementException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body("Donación no encontrada");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error interno del servidor.");
        }
    }

    // OPERACIONES DE BÚSQUEDA PERSONALIZADA

    /**
     * Busca todas las donaciones realizadas por un donante específico.
     * @param donanteId ID del donante a buscar (ID lógico externo)
     * @return ResponseEntity con lista de donaciones o estado NO_CONTENT
     */
    @Operation(summary = "Buscar donaciones por ID de Donante", description = "Recupera todas las donaciones asociadas a un Donante específico (ID de usuario externo).")
    @ApiResponse(responseCode = "200", description = "Lista de donaciones recuperada con éxito.")
    @ApiResponse(responseCode = "204", description = "No hay donaciones encontradas para ese donante.", content = @Content)
    @GetMapping("/por-donante/{donanteId}")
    public ResponseEntity<List<Donacion>> buscarPorDonante(
            @Parameter(description = "ID del Donante (Usuario) que realizó la donación", required = true)
            @PathVariable Integer donanteId) {

        List<Donacion> donaciones = donacionService.findByDonante(donanteId);
        if(donaciones.isEmpty()){
            return new ResponseEntity<>(HttpStatus.NO_CONTENT);
        }
        return ResponseEntity.ok(donaciones);
    }
}