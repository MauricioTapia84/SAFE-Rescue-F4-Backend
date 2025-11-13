package com.SAFE_Rescue.API_Perfiles.modelo;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * Entidad Ciudadano que hereda de Usuario.
 */
@EqualsAndHashCode(callSuper = true)
@Data
@Entity
@PrimaryKeyJoinColumn(name = "id_ciudadano")
public class Ciudadano extends Usuario {

    /**
     * ID de la Dirección, referenciando la entidad Direccion en API_Geolocalizacion.
     * Es una clave foránea lógica.
     */
    @Column(name = "id_direccion", nullable = false)
    @NotNull(message = "El ID de la dirección es obligatorio")
    @Schema(description = "ID de la Dirección (clave foránea a la API externa de Geolocalización)", required = true, example = "100")
    private Integer idDireccion;
}