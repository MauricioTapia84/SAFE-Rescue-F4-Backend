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
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.List;
import java.util.NoSuchElementException;

/**
 * Controlador REST para la gestión de fotos de perfil.
 * Proporciona endpoints para operaciones CRUD y servicio de archivos.
 */
@RestController
@RequestMapping("/api-registros/v1/fotos")
@Tag(name = "Fotos", description = "Operaciones de CRUD relacionadas con Fotos")
public class FotoController {

    @Autowired
    private FotoService fotoService;

    private final Path fileStorageLocation = Paths.get("uploads/fotos").toAbsolutePath().normalize();

    // ================== ENDPOINTS PARA SERVIR ARCHIVOS ==================

    /**
     * Servir foto por nombre de archivo
     */
    @GetMapping("/archivo/{filename:.+}")
    @Operation(summary = "Servir archivo de foto", description = "Sirve el archivo físico de la foto por su nombre")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Archivo servido exitosamente"),
            @ApiResponse(responseCode = "404", description = "Archivo no encontrado"),
            @ApiResponse(responseCode = "500", description = "Error al servir el archivo")
    })
    public ResponseEntity<Resource> servirFoto(
            @Parameter(description = "Nombre del archivo de la foto", required = true)
            @PathVariable String filename) {
        try {
            Path filePath = this.fileStorageLocation.resolve(filename).normalize();
            Resource resource = new UrlResource(filePath.toUri());

            if (resource.exists() && resource.isReadable()) {
                // Determinar el tipo de contenido
                String contentType = Files.probeContentType(filePath);
                if (contentType == null) {
                    contentType = "application/octet-stream";
                }

                return ResponseEntity.ok()
                        .contentType(MediaType.parseMediaType(contentType))
                        .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"" + resource.getFilename() + "\"")
                        .body(resource);
            } else {
                return ResponseEntity.notFound().build();
            }
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * Servir foto por ID de la base de datos
     */
    @GetMapping("/{id}/archivo")
    @Operation(summary = "Servir archivo de foto por ID", description = "Sirve el archivo físico de la foto buscando por ID en la BD")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Archivo servido exitosamente"),
            @ApiResponse(responseCode = "404", description = "Foto no encontrada"),
            @ApiResponse(responseCode = "500", description = "Error al servir el archivo")
    })
    public ResponseEntity<Resource> servirFotoPorId(
            @Parameter(description = "ID de la foto en la base de datos", required = true)
            @PathVariable Integer id) {
        try {
            Foto foto = fotoService.findById(id);
            String rutaArchivo = foto.getUrl();

            // Extraer nombre del archivo de la ruta completa
            String filename = extraerNombreArchivo(rutaArchivo);

            Path filePath = this.fileStorageLocation.resolve(filename).normalize();
            Resource resource = new UrlResource(filePath.toUri());

            if (resource.exists() && resource.isReadable()) {
                String contentType = Files.probeContentType(filePath);
                if (contentType == null) {
                    // Fallback al tipo guardado en la BD
                    contentType = foto.getTipo() != null ? foto.getTipo() : "application/octet-stream";
                }

                return ResponseEntity.ok()
                        .contentType(MediaType.parseMediaType(contentType))
                        .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"" + filename + "\"")
                        .body(resource);
            } else {
                return ResponseEntity.notFound().build();
            }
        } catch (NoSuchElementException e) {
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * Obtener URL pública para una foto por ID
     */
    @GetMapping("/{id}/url")
    @Operation(summary = "Obtener URL pública de foto", description = "Obtiene la URL pública para acceder a la foto")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "URL obtenida exitosamente"),
            @ApiResponse(responseCode = "404", description = "Foto no encontrada")
    })
    public ResponseEntity<String> obtenerUrlPublica(
            @Parameter(description = "ID de la foto", required = true)
            @PathVariable Integer id) {
        try {
            Foto foto = fotoService.findById(id);
            String urlPublica = construirUrlPublica(foto.getUrl());
            return ResponseEntity.ok(urlPublica);
        } catch (NoSuchElementException e) {
            return ResponseEntity.notFound().build();
        }
    }

    // ================== MÉTODOS AUXILIARES ==================

    private String extraerNombreArchivo(String rutaArchivo) {
        if (rutaArchivo != null && rutaArchivo.contains("/")) {
            return rutaArchivo.substring(rutaArchivo.lastIndexOf("/") + 1);
        }
        return rutaArchivo;
    }

    private String construirUrlPublica(String rutaArchivo) {
        if (rutaArchivo == null || rutaArchivo.isEmpty()) {
            return null;
        }
        String nombreArchivo = extraerNombreArchivo(rutaArchivo);
        return "/api-registros/v1/fotos/archivo/" + nombreArchivo;
    }

    // ================== OPERACIONES CRUD EXISTENTES (MANTENIDAS) ==================

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

            // Agregar URL pública a la respuesta
            fotoGuardada.setUrl(construirUrlPublica(fotoGuardada.getUrl()));

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
        // Agregar URLs públicas a cada foto
        fotos.forEach(foto -> foto.setUrl(construirUrlPublica(foto.getUrl())));
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
            // Agregar URL pública
            foto.setUrl(construirUrlPublica(foto.getUrl()));
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
            // Agregar URL pública
            fotoGuardada.setUrl(construirUrlPublica(fotoGuardada.getUrl()));
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