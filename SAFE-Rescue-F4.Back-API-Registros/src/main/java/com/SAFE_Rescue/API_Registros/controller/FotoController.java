package com.SAFE_Rescue.API_Registros.controller;

import com.SAFE_Rescue.API_Registros.modelo.Foto;
import com.SAFE_Rescue.API_Registros.service.FotoService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import org.apache.tika.Tika;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.NoSuchElementException;

/**
 * Controlador REST para la gestión de fotos de perfil.
 * Proporciona endpoints para operaciones CRUD.
 */
@RestController
@RequestMapping("/api-registros/v1/fotos")
@Tag(name = "Fotos", description = "Operaciones de CRUD relacionadas con Fotos")
public class FotoController {

    @Autowired
    private FotoService fotoService;

    @PostMapping("/upload")
    @Operation(summary = "Subir una foto", description = "Sube una foto en formato multipart/form-data")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Foto subida exitosamente.",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = Foto.class))),
            @ApiResponse(responseCode = "400", description = "Error al subir la foto."),
            @ApiResponse(responseCode = "500", description = "Error interno del servidor.")
    })
    public ResponseEntity<?> uploadFoto(
            @RequestParam("file") @Parameter(description = "Archivo de foto", required = true)
            MultipartFile file) {
        try {
            System.out.println(" [FotoController] Recibiendo upload de foto: " + file.getOriginalFilename());
            System.out.println("   Tamaño: " + file.getSize() + " bytes");

            if (file.isEmpty()) {
                System.err.println(" Archivo vacío");
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body("El archivo está vacío");
            }

            //  Validar tamaño (máximo 5MB)
            long maxSize = 5 * 1024 * 1024; // 5MB
            if (file.getSize() > maxSize) {
                System.err.println(" Archivo demasiado grande: " + file.getSize() + " bytes");
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body("El archivo no debe superar 5MB. Tamaño actual: " + (file.getSize() / 1024 / 1024) + "MB");
            }

            // Convertir archivo a bytes
            byte[] fotoBytes = file.getBytes();
            String filename = file.getOriginalFilename();

            //  Determinar el tipo MIME
            String contentType = determineContentTypeWithTika(fotoBytes, filename);

            System.out.println("   Content-Type determinado: " + contentType);

            if (contentType == null) {
                System.err.println(" Tipo de archivo no soportado: " + filename);
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body("Solo se aceptan imágenes (JPG, PNG, GIF, WebP, BMP, SVG)");
            }

            //  Guardar archivo en el sistema de archivos
            String savedFilePath = fotoService.guardarArchivoFisico(fotoBytes, filename);
            System.out.println("   Archivo guardado en: " + savedFilePath);

            //  Crear objeto Foto SIN los bytes (solo metadatos y URL)
            Foto foto = new Foto();
            foto.setUrl(savedFilePath);                // Ruta del archivo
            foto.setDatos(null);                       // NO guardar bytes en BD
            foto.setTipo(contentType);
            foto.setTamanio((int) file.getSize());
            foto.setFechaSubida(LocalDateTime.now());
            foto.setDescripcion("Foto de perfil");

            // Guardar metadatos en la BD
            Foto fotoGuardada = fotoService.save(foto);

            System.out.println(" [FotoController] Foto guardada - ID: " + fotoGuardada.getIdFoto());
            System.out.println("   URL: " + fotoGuardada.getUrl());
            System.out.println("   Tipo: " + fotoGuardada.getTipo());
            System.out.println("   Tamaño: " + fotoGuardada.getTamanio() + " bytes");

            return ResponseEntity.status(HttpStatus.CREATED).body(fotoGuardada);

        } catch (IOException e) {
            System.err.println(" Error al leer archivo: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error al procesar el archivo: " + e.getMessage());
        } catch (Exception e) {
            System.err.println(" Error al subir foto: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error interno del servidor: " + e.getMessage());
        }
    }

    //  Método mejorado: Usa Tika primero, luego fallback a extensión
    private String determineContentTypeWithTika(byte[] fileBytes, String filename) {
        try {
            Tika tika = new Tika();
            String detectedType = tika.detect(fileBytes);

            System.out.println("   MIME type detectado por Tika: " + detectedType);

            // Validar que sea una imagen
            if (detectedType != null && detectedType.startsWith("image/")) {
                return detectedType;
            }
        } catch (Exception e) {
            System.out.println(" Tika no disponible, usando detección por extensión");
        }

        // Fallback: Determinar por extensión
        return determineContentTypeByExtension(filename);
    }

    //  Método auxiliar: Determinar tipo MIME desde extensión
    private String determineContentTypeByExtension(String filename) {
        if (filename == null) {
            return null;
        }

        String lowerFilename = filename.toLowerCase();

        if (lowerFilename.endsWith(".jpg") || lowerFilename.endsWith(".jpeg")) {
            return "image/jpeg";
        } else if (lowerFilename.endsWith(".png")) {
            return "image/png";
        } else if (lowerFilename.endsWith(".gif")) {
            return "image/gif";
        } else if (lowerFilename.endsWith(".webp")) {
            return "image/webp";
        } else if (lowerFilename.endsWith(".bmp")) {
            return "image/bmp";
        } else if (lowerFilename.endsWith(".svg")) {
            return "image/svg+xml";
        }

        return null;
    }

    // ================== OPERACIONES CRUD BÁSICAS ==================

    /**
     * Obtiene todas las fotos registradas en el sistema.
     */
    @GetMapping
    @Operation(summary = "Obtener todas las fotos", description = "Obtiene una lista con todas las fotos.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Lista de fotos obtenida exitosamente.",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = Foto.class))),
            @ApiResponse(responseCode = "204", description = "No hay fotos registradas.")
    })
    public ResponseEntity<List<Foto>> listar() {
        List<Foto> fotos = fotoService.findAll();
        if (fotos.isEmpty()) {
            return new ResponseEntity<>(HttpStatus.NO_CONTENT);
        }
        return ResponseEntity.ok(fotos);
    }

    /**
     * Busca una foto por su ID.
     */
    @GetMapping("/{id}")
    @Operation(summary = "Obtener una foto por su ID", description = "Obtiene una foto al buscarla por su ID.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Foto encontrada.",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = Foto.class))),
            @ApiResponse(responseCode = "404", description = "Foto no encontrada.")
    })
    public ResponseEntity<?> buscarFoto(@Parameter(description = "ID de la foto a buscar", required = true)
                                        @PathVariable int id) {
        try {
            Foto foto = fotoService.findById(id);
            return ResponseEntity.ok(foto);
        } catch (NoSuchElementException e) {
            return new ResponseEntity<>("Foto no encontrada", HttpStatus.NOT_FOUND);
        }
    }

    /**
     * Crea una nueva foto (JSON).
     */
    @PostMapping
    @Operation(summary = "Crear una nueva foto", description = "Crea una nueva foto en el sistema (JSON).")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Foto creada con éxito.",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = Foto.class))),
            @ApiResponse(responseCode = "400", description = "Error en la solicitud...")
    })
    public ResponseEntity<?> agregarFoto(@RequestBody @Parameter(description = "Datos de la foto a crear", required = true)
                                         Foto foto) {
        try {
            Foto fotoGuardada = fotoService.save(foto);
            return ResponseEntity.status(HttpStatus.CREATED).body(fotoGuardada);
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error interno del servidor.");
        }
    }

    /**
     * Actualiza una foto existente.
     */
    @PutMapping("/{id}")
    @Operation(summary = "Actualizar una foto existente", description = "Actualiza los datos de una foto por su ID.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Foto actualizada con éxito."),
            @ApiResponse(responseCode = "404", description = "Foto no encontrada."),
            @ApiResponse(responseCode = "400", description = "Error en la solicitud."),
            @ApiResponse(responseCode = "500", description = "Error interno del servidor.")
    })
    public ResponseEntity<String> actualizarFoto(@Parameter(description = "ID de la foto a actualizar", required = true)
                                                 @PathVariable Integer id,
                                                 @RequestBody @Parameter(description = "Datos actualizados de la foto", required = true)
                                                 Foto foto) {
        try {
            fotoService.update(foto, id);
            return ResponseEntity.ok("Foto actualizada con éxito.");
        } catch (NoSuchElementException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Foto no encontrada.");
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error interno del servidor.");
        }
    }

    /**
     * Elimina una foto del sistema.
     */
    @DeleteMapping("/{id}")
    @Operation(summary = "Eliminar una foto", description = "Elimina una foto del sistema por su ID.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Foto eliminada con éxito."),
            @ApiResponse(responseCode = "404", description = "Foto no encontrada."),
            @ApiResponse(responseCode = "400", description = "No se puede eliminar una foto en uso."),
            @ApiResponse(responseCode = "500", description = "Error interno del servidor.")
    })
    public ResponseEntity<String> eliminarFoto(@Parameter(description = "ID de la foto a eliminar", required = true)
                                               @PathVariable Integer id) {
        try {
            fotoService.delete(id);
            return ResponseEntity.ok("Foto eliminada con éxito.");
        } catch (NoSuchElementException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Foto no encontrada.");
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error interno del servidor.");
        }
    }
}