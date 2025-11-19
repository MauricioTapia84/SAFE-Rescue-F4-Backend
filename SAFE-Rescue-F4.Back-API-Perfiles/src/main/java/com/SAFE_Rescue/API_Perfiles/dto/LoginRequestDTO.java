package com.SAFE_Rescue.API_Perfiles.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
@Schema(description = "DTO para solicitud de login")
public class LoginRequestDTO {

    @NotBlank(message = "El correo es obligatorio")
    @Email(message = "El correo debe tener un formato válido")
    @Schema(description = "Correo electrónico del usuario", example = "usuario@ejemplo.com", required = true)
    private String correo;

    @NotBlank(message = "La contraseña es obligatoria")
    @Schema(description = "Contraseña del usuario", example = "miContraseñaSegura", required = true)
    private String contrasena;
}