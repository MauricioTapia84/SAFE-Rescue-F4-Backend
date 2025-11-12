package com.SAFE_Rescue.API_Incidentes.modelo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * @class FotoDTO
 * @brief DTO (Data Transfer Object) para representar la entidad Foto,
 * que reside en el API de Fotos. Usado para deserializar la respuesta del WebClient.
 */
@Schema(description = "DTO que contiene la metadata de una fotografía de incidente.")
@NoArgsConstructor
@AllArgsConstructor
@Data
public class FotoDTO implements IHasId {

    /**
     * Identificador único de la foto.
     */
    @Schema(description = "Identificador único de la foto", example = "1")
    private Integer idFoto;

    /**
     * URL de acceso a la foto.
     */
    @Schema(description = "URL de la foto del usuario", example = "http://api-fotos.com/fotos/user123.jpg")
    private String url;

    /**
     * Fecha y hora en que se subió la Foto.
     */
    @Schema(description = "Fecha y hora en que se subió la Foto", example = "2025-09-09T10:30:00")
    private LocalDateTime fechaSubida;

    /**
     * Descripción proporcionada para la foto.
     */
    @Schema(description = "Descripción de la foto", example = "Fotografía de incidente.")
    private String descripcion;

    /**
     * @brief Implementación del método getId() de la interfaz IHasId.
     * @return El idFoto.
     */
    @Override
    public Integer getId() {
        return idFoto;
    }
}