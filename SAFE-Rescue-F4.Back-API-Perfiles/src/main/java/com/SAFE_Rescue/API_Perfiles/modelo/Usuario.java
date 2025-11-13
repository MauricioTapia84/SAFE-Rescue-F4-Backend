package com.SAFE_Rescue.API_Perfiles.modelo;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Entidad que representa un usuario en el sistema.
 * Contiene información sobre la composición y estado del usuario,
 * referenciando datos de Foto y Estado mediante IDs (Claves Foráneas Lógicas).
 */
@Entity
@Table(name = "usuario")
@NoArgsConstructor
@AllArgsConstructor
@Data
@Inheritance(strategy = InheritanceType.JOINED)
public class Usuario {

    /**
     * Identificador único del usuario.
     */
    @Id
    @Column(name = "id_usuario")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Schema(description = "Identificador único del usuario", example = "1")
    private Integer idUsuario;

    /**
     * Run del usuario.
     */
    @Column(unique = true, length = 8, nullable = false)
    @Schema(description = "Run del usuario", example = "12345678", required = true)
    @NotBlank(message = "El Run es obligatorio")
    @Size(min = 7, max = 8, message = "El Run debe tener entre 7 y 8 dígitos")
    private String run;

    /**
     * Dígito verificador del usuario.
     */
    @Column(length = 1, nullable = false)
    @Schema(description = "Dígito verificador del usuario", example = "K", required = true)
    @NotBlank(message = "El Dígito verificador es obligatorio")
    @Size(min = 1, max = 1, message = "El Dígito verificador debe ser 1 carácter")
    private String dv;

    /**
     * Nombre descriptivo del usuario.
     */
    @Column(length = 50, nullable = false)
    @Schema(description = "Nombre del usuario", example = "Juan", required = true)
    @NotBlank(message = "El nombre es obligatorio")
    @Size(max = 50, message = "El nombre no puede exceder los 50 caracteres")
    private String nombre;

    /**
     * Apellido paterno descriptivo del usuario.
     */
    @Column(name = "a_paterno", length = 50, nullable = false)
    @Schema(description = "Apellido paterno del usuario", example = "Pérez", required = true)
    @NotBlank(message = "El apellido paterno es obligatorio")
    @Size(max = 50, message = "El apellido paterno no puede exceder los 50 caracteres")
    private String aPaterno;

    /**
     * Apellido materno descriptivo del usuario.
     */
    @Column(name = "a_materno", length = 50, nullable = false)
    @Schema(description = "Apellido materno del usuario", example = "González", required = true)
    @NotBlank(message = "El apellido materno es obligatorio")
    @Size(max = 50, message = "El apellido materno no puede exceder los 50 caracteres")
    private String aMaterno;

    /**
     * Fecha de registro del usuario.
     */
    @Column(name = "fecha_registro", nullable = false)
    @Schema(description = "Fecha de registro del usuario", example = "2022-01-01", required = true)
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
    @NotNull(message = "La fecha de registro es obligatoria")
    private LocalDateTime fechaRegistro;

    /**
     * Teléfono disponible del usuario.
     */
    @Column(unique = true, length = 9, nullable = false)
    @Schema(description = "Teléfono del usuario", example = "987654321", required = true)
    @NotBlank(message = "El teléfono es obligatorio")
    @Size(max = 9, message = "El teléfono debe tener 9 dígitos")
    private String telefono;

    /**
     * Correo del usuario.
     */
    @Column(unique = true, length = 80, nullable = false)
    @Schema(description = "Correo del usuario", example = "usuario@ejemplo.com", required = true)
    @NotBlank(message = "El correo es obligatorio")
    @Email(message = "El correo debe tener un formato válido.")
    @Size(max = 80, message = "El correo no puede exceder los 80 caracteres")
    private String correo;

    /**
     * Contraseña del usuario.
     */
    @Column(length = 70, nullable = false)
    @Schema(description = "Contraseña del usuario", example = "hash-seguro-de-contraseña", required = true)
    @NotBlank(message = "La contraseña es obligatoria")
    @Size(max = 70, message = "La longitud de la contraseña es inválida")
    private String contrasenia;

    /**
     * Intentos fallidos del usuario al iniciar sesión.
     */
    @Column(name = "intentos_fallidos", nullable = false)
    @Schema(description = "Número de intentos fallidos de inicio de sesión", example = "0")
    private Integer intentosFallidos = 0;

    /**
     * Razón de baneo.
     */
    @Column(name = "razon_baneo", length = 100, nullable = true)
    @Schema(description = "Razón de baneo del usuario", example = "Spam")
    @Size(max = 100, message = "La razón de baneo no puede exceder los 100 caracteres")
    private String razonBaneo;

    /**
     * Días de baneo.
     */
    @Column(name = "dias_baneo", nullable = true)
    @Schema(description = "Número de días de baneo", example = "0")
    private Integer diasBaneo;

    // --- CLAVES FORÁNEAS LÓGICAS (Microservicios) ---

    /**
     * ID del Estado del usuario (referencia a la API de Estados).
     */
    @Column(name = "estado_id", nullable = false)
    @NotNull(message = "El ID del estado del usuario es obligatorio")
    @Schema(description = "ID del Estado (clave foránea lógica a la API externa de Estados)", required = true, example = "1")
    private Integer idEstado;


    /**
     * ID de la Foto de perfil del usuario (referencia a la API de Fotos).
     */
    @Column(name = "id_foto", nullable = true)
    @Schema(description = "ID de la Foto de perfil (clave foránea lógica a la API externa de Fotos)")
    private Integer idFoto;

    /**
     * Tipo usuario.
     */
    @ManyToOne
    @JoinColumn(name = "tipo_usuario_id", referencedColumnName = "id_tipo_usuario", nullable = false)
    @NotNull(message = "El tipo de usuario es obligatorio")
    @Schema(description = "Tipo de usuario asociado al usuario")
    private TipoUsuario tipoUsuario;
}