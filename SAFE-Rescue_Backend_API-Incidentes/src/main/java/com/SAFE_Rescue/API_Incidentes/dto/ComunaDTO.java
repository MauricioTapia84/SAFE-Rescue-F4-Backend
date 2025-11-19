package com.SAFE_Rescue.API_Incidentes.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @class ComunaDTO
 * @brief DTO utilizado para transferir datos de una Comuna/Municipio.
 * Es comúnmente anidado dentro de otros DTOs, como DireccionDTO,
 * para proporcionar información geográfica completa.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ComunaDTO {

    /**
     * Identificador único de la Comuna.
     */
    @Schema(description = "Identificador único de la Comuna", example = "32")
    private Integer idComuna;

    /**
     * Nombre descriptivo de la Comuna.
     */
    @Schema(description = "Nombre de la Comuna", example = "Las Condes")
    private String nombre;

    /**
     * Código postal de la Comuna.
     */
    @Schema(description = "Código postal de la Comuna", example = "7550000")
    private String codigoPostal;

}