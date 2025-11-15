package com.SAFE_Rescue.API_Comunicacion.modelo;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * DTO utilizado para recibir los datos necesarios al crear una nueva notificación.
 * El idConversacion es opcional para notificaciones de sistema.
 */
public class NotificacionCreacionDTO {

    @NotBlank(message = "El ID del usuario receptor es obligatorio.")
    private String idUsuarioReceptor;

    @NotBlank(message = "El detalle (contenido) de la notificación no puede estar vacío.")
    @Size(max = 2000, message = "El detalle no puede exceder los 2000 caracteres.")
    private String detalle;

    // CAMBIO: idConversacion es opcional.
    private Integer idConversacion;

    // --- Getters y Setters ---

    public String getIdUsuarioReceptor() {
        return idUsuarioReceptor;
    }

    public void setIdUsuarioReceptor(String idUsuarioReceptor) {
        this.idUsuarioReceptor = idUsuarioReceptor;
    }

    public String getDetalle() {
        return detalle;
    }

    public void setDetalle(String detalle) {
        this.detalle = detalle;
    }

    public Integer getIdConversacion() {
        return idConversacion;
    }

    public void setIdConversacion(Integer idConversacion) {
        this.idConversacion = idConversacion;
    }
}