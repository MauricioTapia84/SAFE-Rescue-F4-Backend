package com.SAFE_Rescue.API_Perfiles.modelo;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

/**
 * DTO (Data Transfer Object) para representar la entidad Dirección
 * que reside en el API_Geolocalizacion. Usado para deserializar la respuesta del WebClient.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class DireccionDTO {

    private Integer idDireccion;
    private String calle;
    private String numero;
    private String villa;
    private String complemento;

    // Estos campos representan las FKs lógicas que la API de Geolocalización maneja internamente.
    private Integer idComuna;
    private Integer idGeolocalizacion;

    // Nota: Esta clase NO lleva anotaciones @Entity, @Table, ni @Id porque no es gestionada por JPA en este servicio.
}