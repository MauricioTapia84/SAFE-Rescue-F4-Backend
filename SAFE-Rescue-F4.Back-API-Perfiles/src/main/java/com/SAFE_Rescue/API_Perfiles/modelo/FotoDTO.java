package com.SAFE_Rescue.API_Perfiles.modelo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * DTO (Data Transfer Object) para representar la entidad Foto
 * que reside en el API de Fotos. Usado para deserializar la respuesta del WebClient.
 */
@NoArgsConstructor
@AllArgsConstructor
@Data
public class FotoDTO implements IHasId { // Implementa IHasId

    @Schema(description = "Identificador único de la foto", example = "1")
    private Integer idFoto;

    @Schema(description = "URL de la foto del usuario", example = "http://api-fotos.com/fotos/user123.jpg")
    private String url;

    @Schema(description = "Fecha y hora en que se subió la Foto", example = "2025-09-09T10:30:00")
    private LocalDateTime fechaSubida;

    @Schema(description = "Descripción de la foto", example = "Fotografía de incidente.")
    private String descripcion;

    @Override
    public Integer getId() {
        return idFoto; // Usa idFoto como el identificador
    }
}