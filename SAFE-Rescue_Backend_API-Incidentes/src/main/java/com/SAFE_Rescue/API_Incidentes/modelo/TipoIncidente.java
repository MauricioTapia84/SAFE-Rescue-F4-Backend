package com.SAFE_Rescue.API_Incidentes.modelo;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

/**
 * Entidad que representa los diferentes tipos de incidentes en el sistema.
 * <p>
 * Cada tipo de incidente define una categorización particular para los incidentes reportados,
 * permitiendo agruparlos por características o funciones específicas.
 * </p>
 *
 * @see Incidente
 */
@Entity
@Table(name = "tipo_incidente")
@NoArgsConstructor
@AllArgsConstructor
@Data
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class TipoIncidente {

    /**
     * Identificador único autoincremental del tipo de Incidente.
     * <p>
     * Se genera automáticamente mediante la estrategia IDENTITY de la base de datos.
     * </p>
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @EqualsAndHashCode.Include
    private Integer idTipoIncidente;

    /**
     * Nombre descriptivo del tipo de incidente.
     * Añadida la restricción 'unique = true' para asegurar que no se repitan los nombres.
     */
    @Column(name = "nombre_tipo", length = 50, nullable = false, unique = true)
    @NotBlank(message = "El nombre del tipo de incidente es obligatorio.")
    @Size(max = 50, message = "El nombre no puede exceder los 50 caracteres.")
    private String nombre;

}