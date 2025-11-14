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
 * Entidad Mensaje: Contiene el contenido de un mensaje dentro de una Conversacion.
 * Representa la tabla 'mensaje' en la base de datos.
 * Esta entidad no extiende RepresentationModel (HATEOAS) siguiendo los requisitos.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "mensaje")
public class Mensaje implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_mensaje")
    private Integer idMensaje;

    // Se inicializa automáticamente al persistir la entidad.
    @Column(name = "fecha_creacion", nullable = false, updatable = false)
    private LocalDateTime fechaCreacion;

    // Usamos @NotBlank para asegurar que no sea nulo ni vacío/solo espacios.
    @NotBlank(message = "El detalle del mensaje no puede estar vacío.")
    @Column(name = "detalle", length = 255) // Aumentado el tamaño del detalle a 255
    @Schema(description = "Contenido textual del mensaje", required = true, example = "Hola, ¿puedes confirmar la recepción?")
    private String detalle;

    /**
     * Relación ManyToOne con la Conversacion.
     * FetchType.LAZY es preferido para evitar cargar la conversación completa innecesariamente.
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_conversacion", nullable = false) // Corregido a id_conversacion
    @NotNull(message = "La conversación asociada es obligatoria")
    @Schema(description = "Objeto de la conversación al que pertenece el mensaje")
    private Conversacion conversacion;

    /**
     * ID del Usuario que envía el mensaje (Emisor).
     * Mapeado como ID simple ya que el Usuario está en otra API.
     */
    @Column(name = "id_usuario_emisor", nullable = false)
    @NotNull(message = "El ID del usuario emisor es obligatorio")
    @Schema(description = "ID del usuario emisor (clave foránea lógica a la API externa de Usuarios)", required = true, example = "456")
    private Integer idUsuarioEmisor;

    /**
     * ID del Estado del mensaje (ej: Enviado, Leído).
     * Mapeado como ID simple ya que el Estado está en otra API.
     */
    @Column(name = "id_estado", nullable = false) // Corregido el nombre de la columna para ser consistente
    @NotNull(message = "El ID del estado del mensaje es obligatorio")
    @Schema(description = "ID del Estado (clave foránea lógica a la API externa de Estados)", required = true, example = "1")
    private Integer idEstado;


    // Método de callback que se ejecuta antes de persistir la entidad.
    @PrePersist
    protected void onCreate() {
        if (fechaCreacion == null) {
            fechaCreacion = LocalDateTime.now();
        }
    }
}