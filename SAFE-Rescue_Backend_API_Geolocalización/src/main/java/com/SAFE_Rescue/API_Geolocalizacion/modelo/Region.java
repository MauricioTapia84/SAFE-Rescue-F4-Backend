package com.SAFE_Rescue.API_Geolocalizacion.modelo;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.persistence.*;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Entidad que representa una región en Chile.
 * Depende directamente de la entidad Pais.
 */
@NoArgsConstructor
@AllArgsConstructor
@Data
@Entity
@Table(name = "region")
public class Region {

    @Id
    @Column(name = "id_region")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Schema(description = "Identificador único de la región", example = "13")
    private int idRegion;

    @Column(length = 100, nullable = false)
    @Schema(description = "Nombre oficial de la región", example = "Metropolitana de Santiago", required = true, maxLength = 100)
    @Size(max = 100)
    private String nombre;

    @Column(length = 5, nullable = false, unique = true)
    @Schema(description = "Identificación de la región (Número Romano o abreviatura)", example = "RM", required = true, maxLength = 5)
    @Size(max = 5)
    private String identificacion; // Campo para el número o abreviatura (ej: "RM", "XIII")

    // --- Relación a Pais (ManyToOne: Muchas Regiones a Un País) ---

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_pais", nullable = false) // Clave foránea a la entidad Pais
    @Schema(description = "País al que pertenece la región", required = true)
    private Pais pais;
}