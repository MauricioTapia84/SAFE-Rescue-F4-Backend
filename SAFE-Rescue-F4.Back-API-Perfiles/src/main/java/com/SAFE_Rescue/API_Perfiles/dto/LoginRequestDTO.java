package com.SAFE_Rescue.API_Perfiles.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "DTO para solicitud de login")
public class LoginRequestDTO {

    @Schema(description = "Correo electrónico del usuario", example = "usuario@ejemplo.com")
    private String correo;

    @Schema(description = "Contraseña del usuario", example = "miContraseñaSegura")
    private String contrasena;

    // Constructor por defecto OBLIGATORIO
    public LoginRequestDTO() {
    }

    // Constructor con parámetros
    public LoginRequestDTO(String correo, String contrasena) {
        this.correo = correo;
        this.contrasena = contrasena;
    }

    // Getters y setters OBLIGATORIOS
    public String getCorreo() {
        return correo;
    }

    public void setCorreo(String correo) {
        this.correo = correo;
    }

    public String getContrasena() {
        return contrasena;
    }

    public void setContrasena(String contrasena) {
        this.contrasena = contrasena;
    }

    // toString para debugging
    @Override
    public String toString() {
        return "LoginRequestDTO{" +
                "correo='" + correo + '\'' +
                ", contrasena='" + (contrasena != null ? "***" : "null") + '\'' +
                '}';
    }
}