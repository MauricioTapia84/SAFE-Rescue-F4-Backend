package com.SAFE_Rescue.API_Incidentes.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class FotoDTO {

    @JsonProperty("idFoto")
    private Integer idFoto;

    @JsonProperty("url")
    private String url;

    @JsonProperty("datos")
    private byte[] datos;

    @JsonProperty("tipo")
    private String tipo;

    @JsonProperty("tamanio")
    private Integer tamanio;

    @JsonProperty("fechaSubida")
    private LocalDateTime fechaSubida;

    @JsonProperty("descripcion")
    private String descripcion;
}