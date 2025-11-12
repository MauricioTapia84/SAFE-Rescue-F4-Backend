package com.SAFE_Rescue.API_Geolocalizacion.modelo;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.persistence.*;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Entidad que representa una dirección en Chile.
 * Contiene información detallada de la ubicación y sus referencias geográficas.
 */
@NoArgsConstructor
@AllArgsConstructor
@Data
@Entity
@Table(name = "direccion")
public class Direccion {

    @Id
    @Column(name = "id_direccion")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Schema(description = "Identificador único de la dirección", example = "1")
    private Integer idDireccion;

    @Column(length = 150, nullable = false)
    @Schema(description = "Nombre de la calle, avenida o pasaje", example = "Avenida Apoquindo", required = true, maxLength = 150)
    @Size(max = 150)
    private String calle;

    @Column(length = 10, nullable = false)
    @Schema(description = "Numeración del domicilio", example = "4500", required = true, maxLength = 20)
    @Size(max = 10)
    private String numero;

    @Column(length = 100)
    @Schema(description = "Nombre de la villa, población o barrio", example = "Villa Los Jardines", maxLength = 100)
    @Size(max = 100)
    private String villa;


    @Column(length = 50)
    @Schema(description = "Información adicional (Depto, Oficina, Lote)", example = "Depto 502", maxLength = 50)
    @Size(max = 50)
    private String complemento;


    // --- Relación a Comuna (ManyToOne: Muchas Direcciones a Una Comuna) ---

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_comuna", nullable = false) // Clave foránea a la entidad Comuna
    @Schema(description = "Comuna a la que pertenece la dirección", required = true)
    private Comuna comuna;


    @OneToOne(cascade = CascadeType.PERSIST, optional = false)
    @JoinColumn(name = "id_geolocalizacion", referencedColumnName = "id_geolocalizacion")
    private Geolocalizacion geolocalizacion;
}