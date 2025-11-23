package com.SAFE_Rescue.API_Perfiles.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * Data Transfer Object (DTO) utilizado para recibir los datos de
 * un nuevo usuario durante el proceso de registro.
 * Excluye campos generados por el sistema (ID, intentos, etc.).
 */
@Data
public class RegistroRequestDTO {

    // --- NUEVO CAMPO: NOMBRE DE USUARIO ---
    @NotBlank(message = "El nombre de usuario es obligatorio")
    @Size(min = 5, max = 20, message = "El nombre de usuario debe tener entre 5 y 20 caracteres")
    @Pattern(regexp = "^[a-zA-Z0-9]+$", message = "El nombre de usuario solo debe contener letras y números")
    private String nombreUsuario;
    // --------------------------------------

    // Datos del usuario
    @NotBlank(message = "El Run es obligatorio")
    @Size(min = 7, max = 8, message = "El Run debe tener entre 7 y 8 dígitos")
    private String run;

    @NotBlank(message = "El Dígito verificador es obligatorio")
    @Size(min = 1, max = 1, message = "El Dígito verificador debe ser 1 carácter")
    private String dv;

    @NotBlank(message = "El nombre es obligatorio")
    @Size(min = 2, max = 50, message = "El nombre debe tener entre 2 y 50 caracteres")
    private String nombre;

    @NotBlank(message = "El apellido paterno es obligatorio")
    @Size(max = 50, message = "El apellido paterno no puede exceder los 50 caracteres")
    @JsonProperty("apaterno")
    private String aPaterno;

    @NotBlank(message = "El apellido materno es obligatorio")
    @Size(max = 50, message = "El apellido materno no puede exceder los 50 caracteres")
    @JsonProperty("amaterno")
    private String aMaterno;

    @NotBlank(message = "El teléfono es obligatorio")
    @Size(max = 9, message = "El teléfono debe tener 9 dígitos")
    private String telefono;

    @NotBlank(message = "El correo es obligatorio")
    @Email(message = "El correo debe tener un formato válido.")
    @Size(max = 80, message = "El correo no puede exceder los 80 caracteres")
    private String correo;

    @NotBlank(message = "La contraseña es obligatoria")
    private String contrasenia;

    // Datos de dirección para el ciudadano
    @Valid
    @NotNull(message = "La dirección es obligatoria")
    private DireccionRequestDTO direccion;
}