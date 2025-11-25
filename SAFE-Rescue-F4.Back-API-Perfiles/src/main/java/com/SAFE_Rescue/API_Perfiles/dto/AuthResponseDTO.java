package com.SAFE_Rescue.API_Perfiles.dto;

import lombok.Data;

@Data
public class AuthResponseDTO {
    private String token;
    private String tipoPerfil;
    private Object userData;
}