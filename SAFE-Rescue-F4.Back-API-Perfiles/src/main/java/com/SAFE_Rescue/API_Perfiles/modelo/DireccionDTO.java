package com.SAFE_Rescue.API_Perfiles.modelo;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

/**
 * DTO que simula la respuesta de la API externa de Geolocalización para Direcciones.
 */
@Data
@NoArgsConstructor // Lombok para constructor sin argumentos
@AllArgsConstructor // Lombok para constructor con todos los argumentos
public class DireccionDTO implements IHasId {
    @JsonProperty("direccionId")
    private Integer id;
    private String calle;
    private String numero;
    private String comuna;

    @Override
    public Integer getId() {
        return id;
    }
}