package com.SAFE_Rescue.API_Incidentes.dto;

import lombok.Data;

@Data
public class ActualizarFotoRequest {
    private Long idFoto;
    private String imagenUrl;

}