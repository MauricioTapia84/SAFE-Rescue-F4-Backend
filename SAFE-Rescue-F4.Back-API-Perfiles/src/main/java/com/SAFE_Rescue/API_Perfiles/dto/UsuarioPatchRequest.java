package com.SAFE_Rescue.API_Perfiles.dto;

import lombok.Data;

@Data
public class UsuarioPatchRequest {
    private Long idFoto;
    private String nombre;
    private String aPaterno;
    private String aMaterno;
    private String telefono;
    private String correo;

}