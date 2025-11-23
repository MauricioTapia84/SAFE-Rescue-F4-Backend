package com.SAFE_Rescue.API_Incidentes.modelo;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties; // <--- IMPORTANTE
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Entidad que registra específicamente los cambios de estado aplicados a un Incidente.
 * Sirve como registro de auditoría inmutable para la trazabilidad de los estados.
 * Se mapea a la tabla "historial_incidente".
 */
@Entity
@Table(name = "historial_incidente")
@NoArgsConstructor
@AllArgsConstructor
@Data
@Schema(description = "Registro de historial de cambios de estado aplicados a un Incidente")
public class HistorialIncidente {

    /**
     * Identificador único del registro de historial.
     */
    @Id
    @Column(name = "id_historial")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Schema(description = "Identificador único del registro de historial", example = "1")
    private Integer idHistorial;

    // -------------------------------------------------------------------------
    // RELACIÓN CON INCIDENTE
    // -------------------------------------------------------------------------

    /**
     * Relación Muchos-a-Uno con la entidad {@code Incidente}.
     * Indica a qué incidente pertenece este registro de historial.
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_incidente", nullable = false)
    @Schema(description = "Incidente al que se le aplicó el cambio de estado")
    // SOLUCIÓN AL ERROR 500: Ignorar los campos del proxy de Hibernate al serializar
    @JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
    private Incidente incidente;


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
    @Schema(description = "Descripción detallada del evento del historial", example = "El operador Pedro cambió el estado de En Asignación a En Curso.")
    private String detalle;

    // Método de callback que se ejecuta antes de persistir la entidad.
    @PrePersist
    protected void onCreate() {
        if (fechaHistorial == null) {
            fechaHistorial = LocalDateTime.now();
        }
    }
}