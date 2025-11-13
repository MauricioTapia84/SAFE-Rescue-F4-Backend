package com.SAFE_Rescue.API_Perfiles.modelo;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Entidad que registra específicamente los cambios de estado aplicados a un perfil de Usuario o Equipo.
 * Sirve como registro de auditoría inmutable para la trazabilidad de los estados.
 * Se mapea a la tabla "historial_perfil" (renombrada para reflejar su nuevo alcance).
 */
@Entity
@Table(name = "historial_perfil") // Renombrado para ser más genérico
@NoArgsConstructor
@AllArgsConstructor
@Data
@Schema(description = "Registro de historial de cambios de estado aplicados a un usuario o equipo")
public class HistorialUsuario { // Se mantiene el nombre de la clase para simplificar, aunque ahora es multi-perfil

    /**
     * Identificador único del registro de historial.
     */
    @Id
    @Column(name = "id_historial")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Schema(description = "Identificador único del registro de historial", example = "1")
    private Integer idHistorial;

    // -------------------------------------------------------------------------
    // RELACIONES CON PERFILES (Solo una debe estar presente)
    // -------------------------------------------------------------------------

    /**
     * Relación Muchos-a-Uno con la entidad {@code Usuario}.
     * Es nullable ya que el registro podría ser para un equipo.
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_usuario", nullable = true) // Ahora es opcional
    @Schema(description = "Usuario al que se le aplicó el cambio de estado (puede ser nulo)")
    private Usuario usuario;

    /**
     * Relación Muchos-a-Uno con la entidad {@code Equipo}.
     * Indica a qué equipo se le aplicó el cambio de estado.
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_equipo", nullable = true) // Nuevo campo opcional
    @Schema(description = "Equipo al que se le aplicó el cambio de estado (puede ser nulo)")
    private Equipo equipo;


    // -------------------------------------------------------------------------
    // CLAVES LÓGICAS (IDs de Estados del Microservicio Externo)
    // -------------------------------------------------------------------------

    /**
     * ID del estado anterior (clave lógica del microservicio de Estado).
     */
    @Column(name = "id_estado_anterior", nullable = false)
    @Schema(description = "ID del estado anterior (clave lógica del microservicio de Estado)", example = "1")
    private Integer idEstadoAnterior;

    /**
     * ID del nuevo estado (clave lógica del microservicio de Estado).
     */
    @Column(name = "id_estado_nuevo", nullable = false)
    @Schema(description = "ID del nuevo estado (clave lógica del microservicio de Estado)", example = "2")
    private Integer idEstadoNuevo;


    // -------------------------------------------------------------------------
    // METADATOS DEL REGISTRO
    // -------------------------------------------------------------------------

    /**
     * Fecha y hora exacta en que se creó el registro de historial.
     */
    @Column(name = "fecha_historial", nullable = false, updatable = false)
    @Schema(description = "Fecha y hora en que se registró el historial", example = "2025-09-09T10:30:00")
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime fechaHistorial;

    /**
     * Descripción detallada del evento registrado (ej: Razón del cambio de estado).
     */
    @Column(name = "detalle", length = 250, nullable = false)
    @Schema(description = "Descripción detallada del evento del historial", example = "El administrador Juan Pérez cambió el estado de Inactivo a Activo por solicitud.")
    private String detalle;
}