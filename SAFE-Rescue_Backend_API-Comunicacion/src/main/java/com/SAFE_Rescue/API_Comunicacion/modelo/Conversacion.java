package com.SAFE_Rescue.API_Comunicacion.modelo;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.List;
import java.util.ArrayList;

/**
 * Entidad Conversacion: Agrupa mensajes y participantes.
 * Representa la tabla 'conversacion' en la base de datos.
 * Implementa Serializable para permitir el uso en contextos de caché o red.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "conversacion")
public class Conversacion implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_conversacion")
    @Schema(description = "Identificador único de la conversación")
    private Integer idConversacion;

    /**
     * Define si es una conversación privada (uno a uno), grupal, o de emergencia, etc.
     */
    @NotBlank(message = "El tipo de conversación es obligatorio")
    @Column(name = "tipo", length = 50, nullable = false)
    @Schema(description = "Tipo de conversación (ej: 'Privada', 'Grupo', 'Emergencia')", required = true, example = "Privada")
    private String tipo;

    /**
     * Nombre visible para la conversación (útil para grupos).
     */
    @Column(name = "nombre", length = 100, nullable = true) // Puede ser nulo para conversaciones privadas
    @Schema(description = "Nombre opcional de la conversación (para grupos o identificación)")
    private String nombre;

    /**
     * Fecha y hora de creación de la conversación.
     * No debe ser actualizable después de su creación.
     */
    @Column(name = "fecha_creacion", nullable = false, updatable = false)
    @Schema(description = "Marca de tiempo de la creación de la conversación")
    private LocalDateTime fechaCreacion;


    // Método de callback que se ejecuta antes de persistir la entidad.
    @PrePersist
    protected void onCreate() {
        if (fechaCreacion == null) {
            fechaCreacion = LocalDateTime.now();
        }
    }
}