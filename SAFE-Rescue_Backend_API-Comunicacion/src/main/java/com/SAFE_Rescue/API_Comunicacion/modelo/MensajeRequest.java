package com.SAFE_Rescue.API_Comunicacion.modelo;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

/**
 * DTO utilizado para recibir los datos de un nuevo mensaje.
 */
public class MensajeRequest {

    @NotNull(message = "El ID del usuario emisor es obligatorio")
    private Integer idUsuarioEmisor;

    @NotBlank(message = "El detalle del mensaje no puede estar vacío")
    private String detalle;

    @NotNull(message = "El ID del estado inicial es obligatorio")
    private Integer idEstado;

    // --- Getters y Setters ---

    public Integer getIdUsuarioEmisor() { return idUsuarioEmisor; }
    public void setIdUsuarioEmisor(Integer idUsuarioEmisor) { this.idUsuarioEmisor = idUsuarioEmisor; }
    public String getDetalle() { return detalle; }
    public void setDetalle(String detalle) { this.detalle = detalle; }
    public Integer getIdEstado() { return idEstado; }
    public void setIdEstado(Integer idEstado) { this.idEstado = idEstado; }
}