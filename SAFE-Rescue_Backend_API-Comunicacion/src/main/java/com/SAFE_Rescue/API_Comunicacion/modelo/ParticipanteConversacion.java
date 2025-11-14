package com.SAFE_Rescue.API_Comunicacion.modelo;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * Entidad de relación que mapea un Usuario o Entidad (idUsuario) a una Conversacion (idConversacion).
 * Permite manejar la relación Muchos a Muchos entre los participantes y las conversaciones.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "participante_conversacion")
public class ParticipanteConversacion implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_participante_conv")
    @Schema(description = "Identificador único de la asignación participante-conversación")
    private Integer idParticipanteConv;

    /**
     * ID del usuario, entidad o recurso que participa en la conversación.
     * Asumimos que es una clave foránea a otra tabla de usuarios/entidades.
     */
    @Column(name = "id_usuario", nullable = false)
    @Schema(description = "ID del usuario/entidad que participa en la conversación", required = true)
    private Integer idUsuario;

    /**
     * Clave foránea que referencia a la conversación.
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_conversacion", nullable = false)
    @Schema(description = "Conversación a la que pertenece el participante")
    private Conversacion conversacion;

    /**
     * Fecha y hora en que el participante se unió a la conversación.
     */
    @Column(name = "fecha_union", nullable = false, updatable = false)
    @Schema(description = "Marca de tiempo de la unión del participante a la conversación")
    private LocalDateTime fechaUnion;

    @PrePersist
    protected void onCreate() {
        if (fechaUnion == null) {
            fechaUnion = LocalDateTime.now();
        }
    }
}