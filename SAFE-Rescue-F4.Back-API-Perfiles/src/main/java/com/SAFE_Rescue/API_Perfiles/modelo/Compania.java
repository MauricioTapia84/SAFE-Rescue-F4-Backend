package com.SAFE_Rescue.API_Perfiles.modelo;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDate;

/**
 * Entidad que representa una Compañía de bomberos, nativa del microservicio.
 * Referencia la Dirección solo por ID (clave foránea lógica a la API externa de Geolocalización).
 */
@NoArgsConstructor
@AllArgsConstructor
@Data
@Entity
@Table(name = "compania")
public class Compania {

    /**
     * Identificador único de la compañía.
     */
    @Id
    @Column(name = "id_compania")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Schema(description = "Identificador único de la compañía", example = "1")
    private Integer idCompania;

    /**
     * Nombre de la compañía (debe ser único).
     */
    @Column(unique = true, length = 50, nullable = false)
    @Schema(description = "Nombre de la compañía", example = "Compañía 13", required = true, maxLength = 50)
    @NotBlank(message = "El nombre de la compañía es obligatorio")
    @Size(max = 50, message = "El nombre no puede exceder los 50 caracteres")
    private String nombre;

    /**
     * Código de la compañía (identificador interno o distintivo).
     */
    @Column(length = 20, nullable = true)
    @Schema(description = "Código identificador de la compañía", example = "C-13")
    private String codigo;

    /**
     * Fecha de fundación de la compañía.
     */
    @Column(name = "fecha_fundacion", nullable = true)
    @Schema(description = "Fecha de fundación", example = "1980-05-15")
    private LocalDate fechaFundacion;

    // --- CLAVE FORÁNEA LÓGICA (Microservicio Dirección) ---

    /**
     * ID de la Dirección, referenciando la entidad Direccion en API_Geolocalizacion.
     * Es una clave foránea lógica.
     */
    @Column(name = "id_direccion", nullable = false)
    @NotNull(message = "El ID de la dirección es obligatorio")
    @Schema(description = "ID de la Dirección (clave foránea a la API externa de Geolocalización)", required = true, example = "100")
    private Integer idDireccion;
}