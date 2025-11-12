package com.SAFE_Rescue.API_Perfiles.modelo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @class GeolocalizacionDTO
 * @brief DTO que representa las coordenadas geográficas (Latitud y Longitud) de una ubicación.
 */
@Schema(description = "DTO que contiene las coordenadas geográficas de un punto.")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class GeolocalizacionDTO {

    /**
     * @property idGeolocalizacion
     * @brief Identificador opcional de la geolocalización.
     * @details Podría ser usado para referenciar un registro si esta información
     * fuera persistente, aunque su uso en un DTO anidado suele ser para transporte
     * de datos de coordenadas.
     */
    private Integer idGeolocalizacion;

    /**
     * Latitud de la ubicación.
     */
    @Schema(description = "Latitud de la ubicación geográfica del incidente", example = "-33.4378")
    private Double latitud;

    /**
     * Longitud de la ubicación.
     */
    @Schema(description = "Longitud de la ubicación geográfica del incidente", example = "-70.6504")
    private Double longitud;
}