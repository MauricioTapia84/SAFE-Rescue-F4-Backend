package com.SAFE_Rescue.API_Geolocalizacion.modelo;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.persistence.*;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Entidad que representa una comuna en Chile.
 * Depende directamente de la entidad Region.
 */
@NoArgsConstructor
@AllArgsConstructor
@Data
@Entity
@Table(name = "comuna")
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
public class Comuna {

    @Id
    @Column(name = "id_comuna")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Schema(description = "Identificador único de la comuna", example = "3")
    private Integer idComuna;

    @Column(length = 100, nullable = false)
    @Schema(description = "Nombre oficial de la comuna", example = "Providencia", required = true, maxLength = 100)
    @Size(max = 100)
    private String nombre;

    @Column(length = 10)
    @Schema(description = "Código postal asociado a la comuna", example = "7500000", maxLength = 10)
    @Size(max = 10)
    private String codigoPostal;

    // --- Relación a Región (ManyToOne: Muchas Comunas a Una Región) ---

    // MODIFICADO: Se eliminó @JsonBackReference para que el JSON incluya la región
    // Se cambió FetchType a EAGER para asegurar la carga de los datos
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "id_region", nullable = false) // Clave foránea a la entidad Region
    @Schema(description = "Región a la que pertenece la comuna", required = true)
    private Region region;
}