package com.SAFE_Rescue.API_Perfiles.controller;

import com.SAFE_Rescue.API_Perfiles.dto.UsuarioFotoRequest;
import com.SAFE_Rescue.API_Perfiles.dto.UsuarioPatchRequest;
import com.SAFE_Rescue.API_Perfiles.modelo.Usuario;
import com.SAFE_Rescue.API_Perfiles.service.UsuarioService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.NoSuchElementException;

/**
 * Controlador REST para la gestión de usuarios
 * Proporciona endpoints para operaciones CRUD y gestión de fotos de usuarios
 */
@RestController
@RequestMapping("/api-perfiles/v1/usuarios")
@Tag(name = "Usuarios", description = "Operaciones de CRUD relacionadas con Usuarios")
public class UsuarioController {

    @Autowired
    private UsuarioService usuarioService;

    // --- OPERACIONES CRUD BÁSICAS ---

    @GetMapping
    @Operation(summary = "Obtener todos los usuarios", description = "Obtiene una lista con todos los usuarios")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Lista de usuarios obtenida exitosamente.",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = Usuario.class))),
            @ApiResponse(responseCode = "204", description = "No hay usuarios registrados.")
    })
    public ResponseEntity<List<Usuario>> listar() {
        List<Usuario> usuarios = usuarioService.findAll();
        if (usuarios.isEmpty()) {
            return new ResponseEntity<>(HttpStatus.NO_CONTENT);
        }
        return ResponseEntity.ok(usuarios);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Obtiene un usuario por su ID", description = "Obtiene un usuario al buscarlo por su ID")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Usuario encontrado.",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = Usuario.class))),
            @ApiResponse(responseCode = "404", description = "Usuario no encontrado.")
    })
    public ResponseEntity<Usuario> buscarUsuario(
            @Parameter(description = "ID del usuario a buscar", required = true)
            @PathVariable Integer id) {

        Usuario usuario = usuarioService.findById(id);
        return ResponseEntity.ok(usuario);
    }

    // --- NUEVO ENDPOINT: BUSCAR POR NOMBRE DE USUARIO ---
    @GetMapping("/buscar/{username}")
    @Operation(summary = "Buscar usuario por nombre de usuario", description = "Obtiene los datos de un usuario dado su nick")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Usuario encontrado"),
            @ApiResponse(responseCode = "404", description = "Usuario no encontrado")
    })
    public ResponseEntity<Usuario> buscarPorNombreUsuario(
            @Parameter(description = "Nombre de usuario (Nick)", required = true)
            @PathVariable String username) {

        Usuario usuario = usuarioService.findByNombreUsuario(username);
        return ResponseEntity.ok(usuario);
    }
    // ---------------------------------------------------

    @PostMapping
    @Operation(summary = "Crear un nuevo usuario", description = "Crea un nuevo usuario en el sistema")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Usuario creado con éxito.",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = Usuario.class))),
            @ApiResponse(responseCode = "400", description = "Error en la solicitud (Validación, RUN/Correo existente, TipoUsuario/Estado no existe).")
    })
    public ResponseEntity<Usuario> agregarUsuario(
            @RequestBody @Valid
            @Parameter(description = "Datos del usuario a crear", required = true)
            Usuario usuario) {

        Usuario nuevoUsuario = usuarioService.save(usuario);
        return ResponseEntity.status(HttpStatus.CREATED).body(nuevoUsuario);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Actualizar un usuario existente", description = "Actualiza los datos de un usuario por su ID")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Usuario actualizado con éxito.",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = Usuario.class))),
            @ApiResponse(responseCode = "404", description = "Usuario no encontrado."),
            @ApiResponse(responseCode = "400", description = "Error en la solicitud (Validación/Integridad/Relaciones).")
    })
    public ResponseEntity<Usuario> actualizarUsuario(
            @Parameter(description = "ID del usuario a actualizar", required = true)
            @PathVariable Integer id,
            @RequestBody @Valid
            @Parameter(description = "Datos actualizados del usuario", required = true)
            Usuario usuario) {

        Usuario usuarioActualizado = usuarioService.update(usuario, id);
        return ResponseEntity.ok(usuarioActualizado);
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Eliminar un usuario", description = "Elimina un usuario del sistema por su ID")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Usuario eliminado con éxito."),
            @ApiResponse(responseCode = "404", description = "Usuario no encontrado."),
            @ApiResponse(responseCode = "400", description = "Error en la solicitud (Referencia Activa).")
    })
    public ResponseEntity<String> eliminarUsuario(
            @Parameter(description = "ID del usuario a eliminar", required = true)
            @PathVariable Integer id) {

        usuarioService.delete(id);
        return ResponseEntity.ok("Usuario eliminado con éxito.");
    }

    // --- GESTIÓN DE FOTOS ---

    @PostMapping("/{id}/subir-foto")
    @Operation(summary = "Sube foto de perfil y retorna el usuario actualizado", description = "Sube un archivo de imagen, lo asocia al usuario y retorna el objeto Usuario actualizado.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Foto subida y usuario retornado con éxito.",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = Usuario.class))),
            @ApiResponse(responseCode = "400", description = "Error al subir la foto o archivo inválido."),
            @ApiResponse(responseCode = "404", description = "Usuario no encontrado."),
            @ApiResponse(responseCode = "500", description = "Error al comunicarse con el servicio de fotos o al actualizar la DB.")
    })
    public ResponseEntity<Usuario> subirFotoUsuario(
            @Parameter(description = "ID del usuario al que se asociará la foto", required = true)
            @PathVariable Integer id,
            @Parameter(description = "Archivo de imagen a subir", required = true)
            @RequestParam("foto") MultipartFile archivo) {

        System.out.println(" [UsuarioController] Recibiendo subida de foto para userId: " + id);
        System.out.println("   Archivo: " + archivo.getOriginalFilename());
        System.out.println("   Tamaño: " + archivo.getSize() + " bytes");

        try {
            if (archivo.isEmpty()) {
                System.err.println(" Archivo vacío");
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
            }

            //  Llamar servicio que retorna el Usuario actualizado
            Usuario usuarioActualizado = usuarioService.subirYActualizarFotoUsuario(id, archivo);

            System.out.println(" [UsuarioController] Foto subida exitosamente");
            System.out.println("   Usuario actualizado - ID foto: " + usuarioActualizado.getIdFoto());

            return ResponseEntity.ok(usuarioActualizado);

        } catch (RuntimeException e) {
            System.err.println(" [UsuarioController] Error: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        } catch (Exception e) {
            System.err.println(" [UsuarioController] Error inesperado: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Actualización parcial de usuario (PATCH)
     */
    @PatchMapping("/{id}")
    public ResponseEntity<Usuario> actualizarParcialUsuario(
            @PathVariable Integer id,
            @RequestBody Usuario usuarioParcial) {

        System.out.println("📝 [UsuarioController] PATCH recibido para usuario ID: " + id);
        System.out.println("   Datos: " + usuarioParcial);

        try {
            Usuario usuarioActualizado = usuarioService.actualizarParcialmente(id, usuarioParcial);
            return ResponseEntity.ok(usuarioActualizado);

        } catch (NoSuchElementException e) {
            System.out.println("❌ Usuario no encontrado: " + id);
            return ResponseEntity.notFound().build();

        } catch (IllegalArgumentException e) {
            System.out.println("❌ Error de validación: " + e.getMessage());
            return ResponseEntity.badRequest().build();

        } catch (Exception e) {
            System.out.println("❌ Error inesperado: " + e.getMessage());
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * Endpoint específico para actualizar solo la foto (PATCH)
     */
    @PatchMapping("/{id}/foto")
    public ResponseEntity<Usuario> actualizarFotoUsuario(
            @PathVariable Integer id,
            @RequestBody UsuarioFotoRequest fotoRequest) {

        System.out.println("🖼 [UsuarioController] PATCH foto recibido para usuario ID: " + id);
        System.out.println("   idFoto: " + fotoRequest.getIdFoto());

        try {
            Usuario usuarioActualizado = usuarioService.actualizarSoloFoto(id, fotoRequest.getIdFoto());
            return ResponseEntity.ok(usuarioActualizado);

        } catch (NoSuchElementException e) {
            System.out.println(" Usuario no encontrado: " + id);
            return ResponseEntity.notFound().build();

        } catch (Exception e) {
            System.out.println(" Error actualizando foto: " + e.getMessage());
            return ResponseEntity.internalServerError().build();
        }
    }


}