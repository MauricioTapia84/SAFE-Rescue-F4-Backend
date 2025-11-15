package com.SAFE_Rescue.API_Comunicacion.modelo;

import jakarta.validation.constraints.NotNull;

/**
 * DTO utilizado para actualizar solo el ID del estado de un mensaje.
 */
public class EstadoUpdateRequest {

    @NotNull(message = "El nuevo ID del estado es obligatorio")
    private Integer nuevoIdEstado;

    // --- Getters y Setters ---

    public Integer getNuevoIdEstado() { return nuevoIdEstado; }
    public void setNuevoIdEstado(Integer nuevoIdEstado) { this.nuevoIdEstado = nuevoIdEstado; }
}