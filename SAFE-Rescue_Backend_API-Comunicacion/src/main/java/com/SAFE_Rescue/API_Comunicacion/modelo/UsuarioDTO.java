package com.SAFE_Rescue.API_Comunicacion.modelo;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

/**
 * @class UsuarioResponseDTO
 * @brief DTO (Data Transfer Object) utilizado para enviar la información
 * de un usuario al cliente.
 * Excluye datos sensibles como la contraseña y campos de estado interno
 * que no son relevantes para la presentación.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class UsuarioDTO {

    /**
     * Identificador único del usuario.
     */
    @Schema(description = "Identificador único del usuario", example = "1")
    private Integer idUsuario;

    /**
     * Run del usuario.
     */
    @Schema(description = "Run del usuario", example = "12345678")
    private String run;

    /**
     * Dígito verificador del usuario.
     */
    @Schema(description = "Dígito verificador del usuario", example = "K")
    private String dv;

    /**
     * Nombre del usuario.
     */
    @Schema(description = "Nombre del usuario", example = "Juan")
    private String nombre;

    /**
     * Apellido paterno del usuario.
     */
    @Schema(description = "Apellido paterno del usuario", example = "Pérez")
    private String aPaterno;

    /**
     * Apellido materno del usuario.
     */
    @Schema(description = "Apellido materno del usuario", example = "González")
    private String aMaterno;

    /**
     * Fecha de registro del usuario.
     */
    @Schema(description = "Fecha de registro del usuario", example = "2022-01-01")
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
    private LocalDate fechaRegistro;

    /**
     * Teléfono disponible del usuario.
     */
    @Schema(description = "Teléfono del usuario", example = "987654321")
    private String telefono;

    /**
     * Correo del usuario.
     */
    @Schema(description = "Correo del usuario", example = "usuario@ejemplo.com")
    private String correo;

    // --- CLAVES FORÁNEAS LÓGICAS (Microservicios) ---

    /**
     * ID del Estado del usuario (referencia a la API de Estados).
     */
    @Schema(description = "ID del Estado (clave foránea lógica a la API externa de Estados)", example = "1")
    private Integer idEstado;


    /**
     * ID de la Foto de perfil del usuario (referencia a la API de Fotos).
     */
    @Schema(description = "ID de la Foto de perfil (clave foránea lógica a la API externa de Fotos)")
    private Integer idFoto;

    /**
     * Tipo de usuario.
     * En un DTO de respuesta, a menudo es mejor enviar solo el nombre o ID del objeto anidado
     * para evitar estructuras circulares o datos innecesarios.
     * Aquí asumiremos que necesitamos al menos el ID del tipo de usuario.
     */
    @Schema(description = "ID del Tipo de usuario", example = "1")
    private Integer tipoUsuarioId; // Usamos solo el ID del TipoUsuario

}