package com.SAFE_Rescue.API_Incidentes.dto;

import com.SAFE_Rescue.API_Incidentes.modelo.TipoIncidente;
import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
@Schema(description = "DTO para actualización parcial de incidente")
public class IncidentePatchRequest {

    @Schema(description = "Título del incidente", example = "Incidente de prueba actualizado")
    private String titulo;

    @Schema(description = "Detalle del incidente", example = "Este es un detalle actualizado del incidente")
    private String detalle;

    @Schema(description = "Región del incidente", example = "Región Metropolitana")
    private String region;

    @Schema(description = "Comuna del incidente", example = "Santiago")
    private String comuna;

    @Schema(description = "Dirección del incidente", example = "Calle Principal 123")
    private String direccion;

    @Schema(description = "ID de la dirección", example = "15")
    private Integer idDireccion;

    @Schema(description = "ID del ciudadano", example = "10")
    private Integer idCiudadano;

    @Schema(description = "ID del estado del incidente", example = "2")
    private Integer idEstadoIncidente;

    @Schema(description = "ID del usuario asignado", example = "5")
    private Integer idUsuarioAsignado;

    @Schema(description = "Tipo de incidente")
    private TipoIncidente tipoIncidente;

    private Integer IdFoto;
}