package com.SAFE_Rescue.API_Perfiles.dto;

import lombok.Data;

@Data
public class LoginRequestDTO {
    private String nombreUsuario;
    private String contrasena;
}