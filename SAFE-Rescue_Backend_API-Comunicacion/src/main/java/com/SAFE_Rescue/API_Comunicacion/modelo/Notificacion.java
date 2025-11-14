package com.SAFE_Rescue.API_Comunicacion.modelo;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * Entidad Notificacion: Registra eventos para usuarios y mantiene el historial.
 * Representa la tabla 'notificacion' en la base de datos.
 * Implementa Serializable para permitir el uso en contextos de caché o red.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "notificacion")
public class Notificacion implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_notificacion")
    @Schema(description = "Identificador único de la notificación")
    private Integer idNotificacion;

    /**
     * Fecha y hora de creación de la notificación.
     * Se asigna automáticamente al persistir y no es actualizable.
     */
    @Column(name = "fecha_creacion", nullable = false, updatable = false)
    @Schema(description = "Marca de tiempo de la creación de la notificación")
    private LocalDateTime fechaCreacion;

    /**
     * Descripción detallada del evento (ej: "Nuevo mensaje", "Alerta de emergencia").
     */
    @NotBlank(message = "El detalle de la notificación es obligatorio")
    @Column(name = "detalle", length = 255, nullable = false)
    @Schema(description = "Descripción detallada de la notificación", required = true, example = "Nuevo mensaje en chat grupal")
    private String detalle;

    // --- Relaciones Many-to-One ---

    /**
     * Relación con la Conversacion asociada a esta notificación.
     * La columna id_conversacion debe existir en la tabla 'notificacion'.
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_conversacion", nullable = false)
    @NotNull(message = "La conversación asociada es obligatoria")
    private Conversacion conversacion;

    // --- Mapeo de IDs (asumiendo que idUsuarioReceptor e idEstado son IDs de otras entidades) ---

    /**
     * ID del usuario que debe recibir esta notificación.
     * Mapeo directo a ID para evitar cargar la entidad Usuario completa.
     * Se asume que es un String, que es el tipo estándar para IDs de usuarios en sistemas modernos.
     */
    @Column(name = "id_usuario_receptor", length = 255, nullable = false)
    @NotBlank(message = "El ID del usuario receptor es obligatorio")
    @Schema(description = "ID del usuario que recibe la notificación", required = true)
    private String idUsuarioReceptor;

    /**
     * ID del estado actual de la notificación (ej: 1=Pendiente, 2=Leída).
     * Mapeo directo a ID para evitar cargar la entidad Estado completa.
     */
    @Column(name = "id_estado", nullable = false)
    @NotNull(message = "El ID del estado es obligatorio")
    @Schema(description = "ID que indica el estado de la notificación (ej: 1=Pendiente)", required = true)
    private Integer idEstado;

    // Método de callback que se ejecuta antes de persistir la entidad.
    @PrePersist
    protected void onCreate() {
        if (fechaCreacion == null) {
            fechaCreacion = LocalDateTime.now();
        }
    }
}