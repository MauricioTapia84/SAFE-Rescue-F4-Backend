package com.SAFE_Rescue.API_Incidentes.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @class EstadoDTO
 * @brief DTO que simula la respuesta de la API externa de Estados.
 * Representa el estado actual de un incidente (e.g., "Reportado", "En Camino", "Resuelto").
 */
@Schema(description = "DTO que representa el estado actual de un Incidente, obtenido desde la API de Estados.")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class EstadoDTO {

    /**
     * Identificador único del estado.
     */
    @Schema(description = "Identificador único del estado", example = "1")
    private Integer idEstado;

    /**
     * Nombre descriptivo del estado.
     */
    @Schema(description = "Nombre descriptivo del estado (e.g., Reportado, En Camino)", example = "Reportado")
    private String nombre;

    /**
     * Descripción detallada del estado.
     */
    @Schema(description = "Descripción detallada del estado", example = "El incidente acaba de ser registrado.")
    private String descripcion;

}