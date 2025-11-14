package com.SAFE_Rescue.API_Comunicacion.modelo;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.time.LocalDateTime;

/**
 * Entidad HistorialMensaje: Registra los cambios de estado de un Mensaje o Notificacion.
 * Representa la tabla 'historial_mensaje' en la base de datos.
 * NOTA: Esta entidad no implementa Serializable por requerimiento explícito.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "historial_mensaje")
public class HistorialMensaje {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_historial_mensaje")
    @Schema(description = "Identificador único del registro de historial")
    private Integer idHistorialMensaje;

    /**
     * Fecha y hora en que se registró el cambio de estado.
     * Se asigna automáticamente al persistir y no es actualizable.
     */
    @Column(name = "fecha_historial", nullable = false, updatable = false)
    @Schema(description = "Marca de tiempo del registro de historial")
    private LocalDateTime fechaHistorial;

    /**
     * Descripción breve del cambio ocurrido.
     */
    @Column(name = "detalle", length = 255)
    @Schema(description = "Detalle del cambio de estado (ej: 'Mensaje marcado como leído')")
    private String detalle;

    // --- Relaciones Polimórficas (XOR) ---

    // FK: id_mensaje
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_mensaje", nullable = true)
    private Mensaje mensaje;

    // FK: id_notificacion
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_notificacion", nullable = true)
    private Notificacion notificacion;

    // --- Mapeo de IDs de Estado ---

    /**
     * ID del estado anterior del Mensaje o Notificacion.
     */
    @Column(name = "id_estado_anterior", nullable = false)
    @NotNull(message = "El ID del estado anterior es obligatorio")
    @Schema(description = "ID del estado que tenía el elemento antes del cambio", required = true)
    private Integer idEstadoAnterior;

    /**
     * ID del nuevo estado del Mensaje o Notificacion.
     */
    @Column(name = "id_estado_nuevo", nullable = false)
    @NotNull(message = "El ID del nuevo estado es obligatorio")
    @Schema(description = "ID del estado que tiene el elemento después del cambio", required = true)
    private Integer idEstadoNuevo;

    // Método de callback que se ejecuta antes de persistir la entidad.
    @PrePersist
    protected void onCreate() {
        if (fechaHistorial == null) {
            fechaHistorial = LocalDateTime.now();
        }
    }
}
