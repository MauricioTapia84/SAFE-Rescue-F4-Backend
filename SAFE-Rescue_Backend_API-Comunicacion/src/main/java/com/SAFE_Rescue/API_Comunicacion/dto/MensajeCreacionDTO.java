package com.SAFE_Rescue.API_Comunicacion.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

/**
 * DTO (Data Transfer Object) utilizado para recibir los datos necesarios
 * al crear un nuevo mensaje a través de la API. Solo incluye los campos
 * que el cliente debe proporcionar (detalle, emisor, estado).
 */
public class MensajeCreacionDTO {

    @NotBlank(message = "El detalle (contenido) del mensaje no puede estar vacío.")
    @Size(max = 2000, message = "El detalle del mensaje no puede exceder los 2000 caracteres.")
    private String detalle;

    @NotNull(message = "El ID del usuario emisor es obligatorio.")
    @Min(value = 1, message = "El ID del usuario emisor debe ser un número positivo.")
    private Integer idUsuarioEmisor;

    @NotNull(message = "El ID del estado del mensaje (ej. Enviado) es obligatorio.")
    @Min(value = 1, message = "El ID del estado debe ser un número positivo.")
    private Integer idEstado;

    // --- Getters y Setters ---

    public String getDetalle() {
        return detalle;
    }

    public void setDetalle(String detalle) {
        this.detalle = detalle;
    }

    public Integer getIdUsuarioEmisor() {
        return idUsuarioEmisor;
    }

    public void setIdUsuarioEmisor(Integer idUsuarioEmisor) {
        this.idUsuarioEmisor = idUsuarioEmisor;
    }

    public Integer getIdEstado() {
        return idEstado;
    }

    public void setIdEstado(Integer idEstado) {
        this.idEstado = idEstado;
    }
}