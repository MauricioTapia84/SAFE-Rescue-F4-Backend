package com.SAFE_Rescue.API_Perfiles.modelo;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull; // Necesitas esta importación
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Entidad que representa un equipo en el sistema.
 * Contiene información sobre la composición y el estado del equipo.
 */
@Entity
@Table(name = "equipo")
@NoArgsConstructor
@AllArgsConstructor
@Data
public class Equipo {

    /**
     * Identificador único del equipo.
     */
    @Id
    @Column(name = "id_equipo")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Schema(description = "Identificador único del equipo", example = "1")
    private Integer idEquipo; // Sugerencia: Cambiado a Integer

    /**
     * Nombre del equipo (máximo 50 caracteres).
     */
    @Column(name = "nombre_equipo", length = 50, nullable = false)
    @Schema(description = "Nombre del equipo", example = "Equipo Alfa", required = true)
    @NotBlank(message = "El nombre del equipo es obligatorio") // Sugerencia: Validación
    @Size(max = 50, message = "El nombre no puede exceder los 50 caracteres")
    private String nombre;

    /**
     * Líder del equipo.
     * Relación uno-a-uno con la entidad Usuario.
     */
    @OneToOne
    @JoinColumn(name = "lider_id", referencedColumnName = "id_usuario", nullable = true)
    @Schema(description = "Líder del equipo", example = "Usuario líder del equipo")
    private Usuario lider;

    /**
     * Compañía a la que pertenece el equipo.
     */
    @ManyToOne
    @JoinColumn(name = "compania_id", referencedColumnName = "id_compania", nullable = false) // Asegura que la columna DB sea NOT NULL
    @NotNull(message = "La compañía es obligatoria") // Sugerencia: Validación
    @Schema(description = "Compañía a la que pertenece el equipo")
    private Compania compania;

    /**
     * Tipo de equipo (especialización).
     */
    @ManyToOne
    @JoinColumn(name = "tipo_equipo_id", referencedColumnName = "id_tipo_equipo", nullable = false) // Asegura que la columna DB sea NOT NULL
    @NotNull(message = "El tipo de equipo es obligatorio") // Sugerencia: Validación
    @Schema(description = "Tipo de equipo asignado")
    private TipoEquipo tipoEquipo;

    /**
     * Estado equipo.
     */
    @ManyToOne
    @JoinColumn(name = "estado_id", referencedColumnName = "id_estado", nullable = false) // Asumiendo que el estado es obligatorio
    @NotNull(message = "El estado es obligatorio") // Sugerencia: Validación
    @Schema(description = "Estado del equipo")
    private EstadoDTO estadoDTO;
}