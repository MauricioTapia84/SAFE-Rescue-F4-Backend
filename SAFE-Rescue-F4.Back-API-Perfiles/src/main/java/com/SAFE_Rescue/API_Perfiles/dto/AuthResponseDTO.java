package com.SAFE_Rescue.API_Perfiles.dto;

import com.SAFE_Rescue.API_Perfiles.modelo.Usuario;
import lombok.Data;

@Data
public class AuthResponseDTO {
    private String token;
    private Usuario userData;
}