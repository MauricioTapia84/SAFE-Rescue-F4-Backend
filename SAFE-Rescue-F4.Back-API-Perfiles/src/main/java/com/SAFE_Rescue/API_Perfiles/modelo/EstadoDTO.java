package com.SAFE_Rescue.API_Perfiles.modelo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO (Data Transfer Object) para representar la entidad Estado
 * que reside en el API de Estados. Usado para deserializar la respuesta del WebClient.
 */
@NoArgsConstructor
@AllArgsConstructor
@Data
public class EstadoDTO {

    @Schema(description = "Identificador único del estado", example = "1")
    private Integer idEstado;

    @Schema(description = "Nombre del estado (e.g., Activo, Inactivo, Pendiente)", example = "Activo")
    private String nombre;

    @Schema(description = "Descripción del estado (e.g., Muestra cuando un usuario esta activo)", example = "Activo")
    private String descripcion;


}