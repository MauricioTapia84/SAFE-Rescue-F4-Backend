package com.SAFE_Rescue.API_Geolocalizacion.modelo;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.persistence.*;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Entidad que representa un país. Es la raíz de la jerarquía geográfica.
 */
@NoArgsConstructor
@AllArgsConstructor
@Data
@Entity
@Table(name = "pais")
public class Pais {

    @Id
    @Column(name = "id_pais")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Schema(description = "Identificador único del país", example = "1")
    private int idPais;

    @Column(length = 50, nullable = false)
    @Schema(description = "Nombre oficial del país", example = "Chile", required = true, maxLength = 50)
    @Size(max = 50)
    private String nombre;

    @Column(length = 3, nullable = false, unique = true)
    @Schema(description = "Código ISO 3166-1 alpha-3 del país", example = "CHL", required = true, maxLength = 3)
    @Size(max = 3)
    private String codigoIso;
}