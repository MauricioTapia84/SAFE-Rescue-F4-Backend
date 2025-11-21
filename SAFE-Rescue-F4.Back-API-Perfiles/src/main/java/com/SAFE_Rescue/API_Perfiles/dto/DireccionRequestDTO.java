package com.SAFE_Rescue.API_Perfiles.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

@Data
public class DireccionRequestDTO {

    @NotBlank(message = "La calle es obligatoria")
    private String calle;

    @NotBlank(message = "El número es obligatorio")
    @Pattern(regexp = "\\d+", message = "El número debe contener solo dígitos")
    private String numero;

    private String villa;

    private String complemento;

    @NotNull(message = "La comuna es obligatoria")
    private Integer idComuna;

    private CoordenadasDTO coordenadas;
}