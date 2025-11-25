package com.SAFE_Rescue.API_Incidentes.controller;

import com.SAFE_Rescue.API_Incidentes.dto.ActualizarFotoRequest;
import com.SAFE_Rescue.API_Incidentes.dto.IncidentePatchRequest;
import com.SAFE_Rescue.API_Incidentes.modelo.Incidente;
import com.SAFE_Rescue.API_Incidentes.service.IncidenteService;
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
import java.util.Map;
import java.util.NoSuchElementException;

/**
 * Controlador REST para la gestión de incidentes
 * Proporciona endpoints para operaciones CRUD y gestión de relaciones de incidentes
 */
@RestController
@RequestMapping("/api-incidentes/v1/incidentes")
@Tag(name = "Incidentes", description = "Gestión de la entidad Incidente y sus relaciones")
public class IncidenteController {

    @Autowired
    private IncidenteService incidenteService;

    // OPERACIONES CRUD BÁSICAS

    /**
     * Obtiene todos los incidentes registrados en el sistema.
     * @return ResponseEntity con lista de incidentes o estado NO_CONTENT si no hay registros
     */
    @Operation(summary = "Obtener todos los incidentes", description = "Recupera una lista de todos los incidentes registrados.")
    @ApiResponse(responseCode = "200", description = "Lista de incidentes recuperada con éxito.")
    @ApiResponse(responseCode = "204", description = "No hay incidentes registrados.", content = @Content)
    @GetMapping
    public ResponseEntity<List<Incidente>> listar(){

        List<Incidente> incidentes = incidenteService.findAll();
        if(incidentes.isEmpty()){
            return new ResponseEntity<>(HttpStatus.NO_CONTENT);
        }
        return ResponseEntity.ok(incidentes);
    }

    /**
     * Busca un incidente por su ID.
     * @param id ID del incidente a buscar
     * @return ResponseEntity con el incidente encontrado o mensaje de error
     */
    @Operation(summary = "Buscar incidente por ID", description = "Busca y recupera un incidente específico usando su identificador.")
    @ApiResponse(responseCode = "200", description = "Incidente encontrado con éxito.")
    @ApiResponse(responseCode = "404", description = "Incidente no encontrado.", content = @Content)
    @GetMapping("/{id}")
    public ResponseEntity<?> buscarIncidente(
            @Parameter(description = "ID del incidente a buscar", required = true)
            @PathVariable Integer id) {
        Incidente incidente;

        try {
            incidente = incidenteService.findById(id);
        }catch(NoSuchElementException e){
            return new ResponseEntity<String>("Incidente no encontrado", HttpStatus.NOT_FOUND);
        }

        return ResponseEntity.ok(incidente);

    }

    /**
     * Crea un nuevo incidente.
     * MODIFICADO: Ahora devuelve el objeto Incidente creado (con ID) en lugar de un String.
     */
    @PostMapping
    @Operation(summary = "Crear nuevo incidente", description = "Registra un nuevo incidente en el sistema. Requiere IDs válidos para TipoIncidente, Estado, Ciudadano y Dirección. El Usuario Asignado es opcional.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Incidente creado con éxito."),
            @ApiResponse(responseCode = "400", description = "Error de validación o ID de referencia no encontrado."),
            @ApiResponse(responseCode = "500", description = "Error interno del servidor.")
    })
    // Cambiamos ResponseEntity<String> a ResponseEntity<?>
    public ResponseEntity<?> agregarIncidente(@RequestBody Incidente incidente) {
        try {
            // 1. Guardamos y CAPTURAMOS el objeto
            Incidente nuevoIncidente = incidenteService.save(incidente);

            // 2. Devolvemos el objeto completo (con su ID)
            return ResponseEntity.status(HttpStatus.CREATED).body(nuevoIncidente);

        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error interno del servidor.");
        }
    }

    /**
     * Actualiza un incidente existente.
     * @param id ID del incidente a actualizar
     * @param incidente Datos actualizados del incidente
     * @return ResponseEntity con mensaje de confirmación o error
     */
    @Operation(summary = "Actualizar incidente", description = "Modifica los datos de un incidente existente.")
    @ApiResponse(responseCode = "200", description = "Incidente actualizado con éxito.")
    @ApiResponse(responseCode = "404", description = "Incidente no encontrado.")
    @ApiResponse(responseCode = "400", description = "Error de validación o ID de referencia no válido.")
    @PutMapping("/{id}")
    public ResponseEntity<String> actualizarIncidente(
            @Parameter(description = "ID del incidente a actualizar", required = true)
            @PathVariable Integer id,
            @RequestBody Incidente incidente) {
        try {
            Incidente nuevoIncidente = incidenteService.update(incidente, id);
            return ResponseEntity.ok("Actualizado con éxito");
        } catch (NoSuchElementException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body("Incidente no encontrado");
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error interno del servidor.");
        }
    }

    /**
     * Elimina un incidente del sistema.
     * @param id ID del incidente a eliminar
     * @return ResponseEntity con mensaje de confirmación
     */
    @Operation(summary = "Eliminar incidente", description = "Elimina un incidente del sistema usando su ID.")
    @ApiResponse(responseCode = "200", description = "Incidente eliminado con éxito.")
    @ApiResponse(responseCode = "404", description = "Incidente no encontrado.")
    @DeleteMapping("/{id}")
    public ResponseEntity<String> eliminarIncidente(
            @Parameter(description = "ID del incidente a eliminar", required = true)
            @PathVariable Integer id) {
        try {
            incidenteService.delete(id);
            return ResponseEntity.ok("Incidente eliminado con éxito.");
        } catch (NoSuchElementException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body("Incidente no encontrado");
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error interno del servidor.");
        }
    }


    // GESTIÓN DE RELACIONES

    /**
     * Asigna un ciudadano un incidente.
     * @param incidenteId ID del incidente
     * @param ciudadanoId del ciudadano
     * @return ResponseEntity con mensaje de confirmación o error
     */
    @Operation(summary = "Asignar Ciudadano", description = "Asigna un Ciudadano existente (ID externo) a un incidente.")
    @ApiResponse(responseCode = "200", description = "Ciudadano asignado con éxito.")
    @ApiResponse(responseCode = "404", description = "Incidente o Ciudadano no encontrado.")
    @PostMapping("/{incidenteId}/asignar-ciudadano/{ciudadanoId}")
    public ResponseEntity<String> asignacCiudadano(
            @Parameter(description = "ID del incidente", required = true) @PathVariable Integer incidenteId,
            @Parameter(description = "ID del ciudadano", required = true) @PathVariable Integer ciudadanoId) {
        try {
            incidenteService.asignarCiudadano(incidenteId,ciudadanoId);
            return ResponseEntity.ok("Ciudadano asignado al Incidente exitosamente");
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        }
    }

    /**
     * Asigna un estado de incidente a un incidente
     * @param incidenteId ID del incidente
     * @param estadoIncidenteId ID del estado de incidente a asignar
     * @return ResponseEntity con mensaje de confirmación o error
     */
    @Operation(summary = "Asignar Estado de Incidente", description = "Asigna un Estado existente (ID externo) a un incidente.")
    @ApiResponse(responseCode = "200", description = "Estado asignado con éxito.")
    @ApiResponse(responseCode = "404", description = "Incidente o Estado no encontrado.")
    @PostMapping("/{incidenteId}/asignar-estado-incidente/{estadoIncidenteId}")
    public ResponseEntity<String> asignarEstadoIncidente(
            @Parameter(description = "ID del incidente", required = true) @PathVariable Integer incidenteId,
            @Parameter(description = "ID del estado de incidente", required = true) @PathVariable Integer estadoIncidenteId) {
        try {
            incidenteService.asignarEstadoIncidente(incidenteId,estadoIncidenteId);
            return ResponseEntity.ok("Estado Incidente asignado al Incidente exitosamente");
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        }
    }

    /**
     * Asigna un tipo de incidente a un incidente
     * @param incidenteId ID del incidente
     * @param tipoIncidenteId ID del tipo de incidente a asignar
     * @return ResponseEntity con mensaje de confirmación o error
     */
    @Operation(summary = "Asignar Tipo de Incidente", description = "Asigna un Tipo de Incidente existente (ID local) a un incidente.")
    @ApiResponse(responseCode = "200", description = "Tipo de Incidente asignado con éxito.")
    @ApiResponse(responseCode = "404", description = "Incidente o Tipo de Incidente no encontrado.")
    @PostMapping("/{incidenteId}/asignar-tipo-incidente/{tipoIncidenteId}")
    public ResponseEntity<String> asignarTipoIncidente(
            @Parameter(description = "ID del incidente", required = true) @PathVariable Integer incidenteId,
            @Parameter(description = "ID del tipo de incidente", required = true) @PathVariable Integer tipoIncidenteId) {
        try {
            incidenteService.asignarTipoIncidente(incidenteId,tipoIncidenteId);
            return ResponseEntity.ok("Tipo Incidente asignado al Incidente exitosamente");
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        }
    }

    // NUEVO ENDPOINT PARA ASIGNAR USUARIO RESPONSABLE
    /**
     * Asigna un Usuario Responsable a un incidente.
     * @param incidenteId ID del incidente
     * @param usuarioId ID del usuario a asignar
     * @return ResponseEntity con mensaje de confirmación o error
     */
    @Operation(summary = "Asignar Usuario Responsable", description = "Asigna un Usuario existente (ID externo) como responsable del incidente.")
    @ApiResponse(responseCode = "200", description = "Usuario asignado con éxito.")
    @ApiResponse(responseCode = "404", description = "Incidente o Usuario no encontrado.")
    @PostMapping("/{incidenteId}/asignar-usuario-asignado/{usuarioId}")
    public ResponseEntity<String> asignarUsuarioAsignado(
            @Parameter(description = "ID del incidente", required = true) @PathVariable Integer incidenteId,
            @Parameter(description = "ID del usuario responsable", required = true) @PathVariable Integer usuarioId) {
        try {
            incidenteService.asignarUsuarioAsignado(incidenteId, usuarioId);
            return ResponseEntity.ok("Usuario responsable asignado al Incidente exitosamente");
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        }
    }

    // ELIMINADO: Endpoint 'asignarEquipo'

    /**
     * Crea una nueva dirección en el microservicio de Geolocalización y la asigna al incidente.
     * @param incidenteId ID del incidente al que asignar la dirección
     * @param ubicacionJson Payload JSON con los datos de la dirección a crear
     * @return ResponseEntity con el incidente actualizado o error
     */
    @Operation(summary = "Crear y Asignar Nueva Ubicación (Dirección)",
            description = "Envía el JSON de la dirección al microservicio de Geolocalización para crearla, y luego asigna el ID retornado al incidente especificado.")
    @ApiResponse(responseCode = "200", description = "Dirección creada y asignada al incidente con éxito.",
            content = @Content(schema = @Schema(implementation = Incidente.class)))
    @ApiResponse(responseCode = "404", description = "Incidente no encontrado.")
    @ApiResponse(responseCode = "400", description = "Error en el formato JSON de la dirección o falla del microservicio de Geolocalización.")
    @PostMapping("/{incidenteId}/agregar-ubicacion")
    public ResponseEntity<?> agregarUbicacionAIncidente(
            @Parameter(description = "ID del incidente al que asignar la nueva dirección", required = true)
            @PathVariable Integer incidenteId,
            @Parameter(description = "JSON con los datos de la nueva dirección (calle, altura, coordenadas, etc.)", required = true)
            @RequestBody String ubicacionJson) {
        try {
            Incidente incidenteActualizado = incidenteService.agregarUbicacionAIncidente(incidenteId, ubicacionJson);
            return ResponseEntity.ok(incidenteActualizado);
        } catch (NoSuchElementException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Incidente no encontrado.");
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Error al crear o asignar la dirección: " + e.getMessage());
        }
    }


    /**
     * Asigna una Dirección (Ubicación) a un incidente existente mediante IDs.
     * @param incidenteId ID del incidente
     * @param direccionId ID de la DireccionDTO
     * @return ResponseEntity con mensaje de confirmación o error
     */
    @Operation(summary = "Asignar Dirección existente", description = "Asigna una Dirección existente (ID externo) a un incidente.")
    @ApiResponse(responseCode = "200", description = "Dirección asignada con éxito.")
    @ApiResponse(responseCode = "404", description = "Incidente o Dirección no encontrada.")
    @PostMapping("/{incidenteId}/asignar-direccion/{direccionId}")
    public ResponseEntity<String> asignarDireccion(
            @Parameter(description = "ID del incidente", required = true) @PathVariable Integer incidenteId,
            @Parameter(description = "ID de la dirección (ubicación)", required = true) @PathVariable Integer direccionId) {
        try {
            incidenteService.asignarDireccion(incidenteId, direccionId);
            return ResponseEntity.ok("Dirección asignada al incidente exitosamente");
        } catch (RuntimeException e) {
            // Cambiado a usar direccionId para claridad en la ruta y el método
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        }
    }

    /**
     * Actualización parcial de incidente (PATCH)
     */
    @Operation(summary = "Actualización parcial de incidente", description = "Actualiza solo los campos proporcionados del incidente.")
    @ApiResponse(responseCode = "200", description = "Incidente actualizado parcialmente con éxito.")
    @ApiResponse(responseCode = "404", description = "Incidente no encontrado.")
    @ApiResponse(responseCode = "400", description = "Error de validación o ID de referencia no válido.")
    @PatchMapping("/{id}")
    public ResponseEntity<?> actualizarParcialIncidente(
            @Parameter(description = "ID del incidente a actualizar", required = true)
            @PathVariable Integer id,
            @RequestBody IncidentePatchRequest patchRequest) {

        System.out.println(" [IncidenteController] PATCH recibido para incidente ID: " + id);
        System.out.println("   Datos: " + patchRequest);

        try {
            // Convertir DTO a entidad
            Incidente incidenteParcial = new Incidente();
            incidenteParcial.setTitulo(patchRequest.getTitulo());
            incidenteParcial.setDetalle(patchRequest.getDetalle());
            incidenteParcial.setRegion(patchRequest.getRegion());
            incidenteParcial.setComuna(patchRequest.getComuna());
            incidenteParcial.setDireccion(patchRequest.getDireccion());
            incidenteParcial.setIdDireccion(patchRequest.getIdDireccion());
            incidenteParcial.setIdCiudadano(patchRequest.getIdCiudadano());
            incidenteParcial.setIdEstadoIncidente(patchRequest.getIdEstadoIncidente());
            incidenteParcial.setIdUsuarioAsignado(patchRequest.getIdUsuarioAsignado());
            incidenteParcial.setTipoIncidente(patchRequest.getTipoIncidente());

            Incidente incidenteActualizado = incidenteService.actualizarParcialmente(id, incidenteParcial);
            return ResponseEntity.ok(incidenteActualizado);

        } catch (NoSuchElementException e) {
            System.out.println(" Incidente no encontrado: " + id);
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Incidente no encontrado");

        } catch (IllegalArgumentException e) {
            System.out.println(" Error de validación: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());

        } catch (Exception e) {
            System.out.println(" Error inesperado: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error interno del servidor");
        }
    }

    /**
     * Endpoint para actualizar por idFoto
     */
    @PatchMapping("/{id}/foto")
    public ResponseEntity<?> actualizarFotoIncidente(
            @PathVariable Long id,
            @RequestBody ActualizarFotoRequest request) {

        try {
            Incidente incidenteActualizado = incidenteService.actualizarFotoIncidente(Math.toIntExact(id), Math.toIntExact(request.getIdFoto()));

            return ResponseEntity.ok().body(Map.of(
                    "mensaje", "Foto del incidente actualizada correctamente",
                    "idIncidente", incidenteActualizado.getIdIncidente(),
                    "idFoto", incidenteActualizado.getIdFoto()
            ));

        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("error", e.getMessage()));
        }
    }

    /**
     * Endpoint para compatibilidad - actualizar por URL de imagen
     */
    @PatchMapping("/{id}/imagen")
    public ResponseEntity<?> actualizarImagenIncidente(
            @PathVariable Long id,
            @RequestBody String imagenUrl) {

        try {
            Incidente incidenteActualizado = incidenteService.actualizarImagenIncidente(Math.toIntExact(id), imagenUrl);

            return ResponseEntity.ok().body(Map.of(
                    "mensaje", "Imagen del incidente actualizada correctamente",
                    "idIncidente", incidenteActualizado.getIdIncidente(),
                    "idFoto", incidenteActualizado.getIdFoto()
            ));

        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("error", e.getMessage()));
        }
    }

}