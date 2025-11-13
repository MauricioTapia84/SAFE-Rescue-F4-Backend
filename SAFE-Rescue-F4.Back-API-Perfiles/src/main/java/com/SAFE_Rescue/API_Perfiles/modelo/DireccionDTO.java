package com.SAFE_Rescue.API_Perfiles.modelo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @class DireccionDTO
 * @brief DTO que representa la dirección física completa de un Incidente,
 * incluyendo información geográfica anidada y de comuna.
 */
@Schema(description = "DTO que representa la dirección física completa de un Incidente.")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class DireccionDTO  {

    /**
     * Identificador único de la dirección.
     */
    @Schema(description = "Identificador único de la Dirección", example = "1")
    private Integer idDireccion;

    /**
     * Nombre de la calle.
     */
    @Schema(description = "Nombre de la calle o avenida", example = "Avenida Las Camelias")
    private String calle;

    /**
     * Número de la propiedad.
     */
    @Schema(description = "Número de la dirección (suele ser numérico, pero se guarda como String)", example = "1234")
    private String numero;

    /**
     * Nombre de la villa o conjunto residencial (opcional).
     */
    @Schema(description = "Nombre de la Villa o conjunto habitacional (opcional)", example = "Villa Los Aromos")
    private String villa;

    /**
     * Complemento de la dirección (e.g., Departamento, Torre, Piso, Oficina).
     */
    @Schema(description = "Complemento de la dirección (Departamento, Oficina, Torre, etc.)", example = "Dpto. 402, Torre A")
    private String complemento;

    // 2. Objetos anidados:

    /**
     * Información de la comuna a la que pertenece la dirección.
     */
    @Schema(description = "Información detallada de la Comuna/Municipio.")
    private ComunaDTO comuna;

    /**
     * Coordenadas geográficas de la dirección.
     */
    @Schema(description = "Coordenadas de Latitud y Longitud de la dirección.")
    private GeolocalizacionDTO geolocalizacion;

}