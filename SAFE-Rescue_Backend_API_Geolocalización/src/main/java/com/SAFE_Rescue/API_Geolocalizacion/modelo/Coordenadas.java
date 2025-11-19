package com.SAFE_Rescue.API_Geolocalizacion.modelo;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.persistence.*;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Entidad que almacena las coordenadas de latitud y longitud.
 * Tiene una relación OneToOne con la entidad Direccion.
 */
@NoArgsConstructor
@AllArgsConstructor
@Data
@Entity
@Table(name = "coordenadas") // Cambiado para coincidir con el nombre de la entidad
public class Coordenadas { // Corregido el nombre (tenía "Cordenadas")

    @Id
    @Column(name = "id_coordenadas") // Cambiado para ser consistente
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Schema(description = "Identificador único de las coordenadas", example = "1") // Actualizada descripción
    private Integer idCoordenadas; // Cambiado para ser consistente

    // Latitud: Rango de -90 a 90
    @Column(nullable = false)
    @Schema(description = "Coordenada latitud (eje Y)", example = "-33.4489", required = true)
    @DecimalMin("-90.0")
    @DecimalMax("90.0")
    private Float latitud;

    // Longitud: Rango de -180 a 180
    @Column(nullable = false)
    @Schema(description = "Coordenada longitud (eje X)", example = "-70.6693", required = true)
    @DecimalMin("-180.0")
    @DecimalMax("180.0")
    private Float longitud;
}