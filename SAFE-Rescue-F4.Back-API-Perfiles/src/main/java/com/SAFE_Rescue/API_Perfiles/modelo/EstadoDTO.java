package com.SAFE_Rescue.API_Perfiles.modelo;

import lombok.Data;

/**
 * DTO que simula la respuesta de la API externa de Estados.
 */
@Data
public class EstadoDTO implements IHasId {
    private Integer id;
    private String nombre;
    private String descripcion;

    @Override
    public Integer getId() {
        return id;
    }
}