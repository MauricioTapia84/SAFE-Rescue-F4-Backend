package com.SAFE_Rescue.API_Incidentes.modelo;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Entidad que registra un Incidente reportado en el sistema, centralizando la información
 * de ubicación, persona, tipo y estado mediante claves foráneas lógicas (IDs).
 */
@Entity
@Table(name = "incidente") // Nombre de la tabla en la base de datos
@NoArgsConstructor // Genera constructor sin argumentos
@AllArgsConstructor // Genera constructor con todos los argumentos
@Data // Genera getters, setters, toString, equals y hashCode
public class Incidente {

    /**
     * Identificador único autoincremental del incidente.
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY) // Auto-incremental
    private Integer idIncidente; // Cambiado a Integer para coincidir con el patrón de Usuario

    @Column(length = 50, nullable = false)
    @NotBlank(message = "El título del incidente es obligatorio.") // Validación de aplicación
    @Size(max = 50, message = "El título no puede exceder los 50 caracteres.") // Validación de aplicación
    private String titulo;

    @Column(length = 400, nullable = true)
    @Size(max = 400, message = "El detalle no puede exceder los 400 caracteres.") // Validación de aplicación
    private String detalle;

    /**
     * Fecha y hora exacta en que se creó el registro de incidente.
     */
    @Column(name = "fecha_incidente", nullable = false)
    @NotNull(message = "La fecha de registro es obligatoria.") // Validación de aplicación
    @Schema(description = "Fecha y hora en que se registró el incidente", example = "2025-09-09T10:30:00")
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss") // Incluye fecha y hora
    private LocalDateTime fechaRegistro;

    // --- CLAVE FORÁNEA JPA (ASUMIDO: Es una entidad local como TipoUsuario en el ejemplo) ---

    /**
     * Tipo de incidente (Relación JPA si es una entidad local).
     */
    @ManyToOne
    @JoinColumn(name = "tipo_incidente_id", referencedColumnName = "idTipoIncidente", nullable = false)
    @NotNull(message = "El tipo de incidente es obligatorio.")
    private TipoIncidente tipoIncidente;

    // --- CLAVES FORÁNEAS LÓGICAS (Microservicios/DTOs) ---

    /**
     * ID de la Dirección/Ubicación del incidente (Referencia lógica).
     */
    @Column(name = "ubicacion_id", nullable = false) // Mapeo al nombre de columna original
    @NotNull(message = "La ID de la ubicación del incidente es obligatoria.")
    @Schema(description = "ID de la Ubicación (Clave foránea lógica)", required = true, example = "5")
    private Integer idDireccion; // Reemplaza DireccionDTO

    /**
     * ID del Usuario que reporta el incidente (Referencia lógica al ciudadano).
     */
    @Column(name = "ciudadano_id", nullable = false) // Mapeo al nombre de columna original
    @NotNull(message = "La ID del ciudadano que reporta es obligatoria.")
    @Schema(description = "ID del Ciudadano (Clave foránea lógica)", required = true, example = "10")
    private Integer idCiudadano; // Reemplaza UsuarioDTO

    /**
     * ID del Estado actual del incidente (Referencia lógica).
     */
    @Column(name = "estado_incidente_id", nullable = false) // Mapeo al nombre de columna original
    @NotNull(message = "La ID del estado del incidente es obligatoria.")
    @Schema(description = "ID del Estado del Incidente (Clave foránea lógica)", required = true, example = "1")
    private Integer idEstadoIncidente; // Reemplaza EstadoDTO

    /**
     * ID del Usuario responsable/asignado al incidente (Referencia lógica).
     */
    @Column(name = "usuario_asignado_id", nullable = true) // Nuevo nombre de columna
    @Schema(description = "ID del Usuario responsable/asignado (Clave foránea lógica)", required = false, example = "15")
    private Integer idUsuarioAsignado; // Cambiado de 'UsuarioDto' y representa al usuario.
}