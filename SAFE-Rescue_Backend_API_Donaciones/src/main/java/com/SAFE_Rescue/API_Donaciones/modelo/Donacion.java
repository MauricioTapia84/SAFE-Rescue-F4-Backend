package com.SAFE_Rescue.API_Donaciones.modelo;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.persistence.*;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Entidad que representa una Donación financiera o en especie registrada en el sistema.
 * Contiene información sobre el donante, el monto, la fecha, el método de pago y el posible homenaje.
 */
@Entity
@Table(name = "donacion") // Nombre de la tabla según el diagrama
@NoArgsConstructor // Genera constructor sin argumentos
@AllArgsConstructor // Genera constructor con todos los argumentos
@Data // Genera getters, setters, toString, equals y hashCode
public class Donacion {

    /**
     * Identificador único de la donación (PK).
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY) // Auto-incremental
    @Column(name = "id_donacion")
    @Schema(description = "Identificador único de la donación", example = "1")
    private Integer idDonacion;

    /**
     * ID del Donante (Clave foránea lógica) - Requerido (*).
     */
    @Column(name = "id_donante", nullable = false)
    @NotNull(message = "El ID del donante es obligatorio.")
    @Schema(description = "ID del Usuario que realiza la donación", required = true, example = "42")
    private Integer idDonante;

    /**
     * Monto de la donación - Requerido (*).
     * Se usa Double, pero BigDecimal es más recomendable para datos financieros.
     */
    @Column(nullable = false)
    @NotNull(message = "El monto de la donación es obligatorio.")
    @DecimalMin(value = "0.01", message = "El monto debe ser positivo.")
    @Schema(description = "Monto de la donación", required = true, example = "50.00")
    private Integer monto; // Mapea a monto

    /**
     * Fecha y hora exacta de la donación - Requerido (*).
     */
    @Column(name = "fecha_donacion", nullable = false)
    @NotNull(message = "La fecha y hora de la donación son obligatorias.")
    @Schema(description = "Fecha y hora de la donación", example = "2025-11-17T15:00:00")
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime fechaDonacion; // Mapea a fecha_donacion

    /**
     * Método de pago utilizado - Requerido (*).
     */
    @Column(name = "metodo_pago", length = 50, nullable = false)
    @NotBlank(message = "El método de pago es obligatorio.")
    @Size(max = 50, message = "El método de pago no puede exceder los 50 caracteres.")
    @Schema(description = "Método de pago (ej: Tarjeta, PayPal, Efectivo)", required = true, example = "Tarjeta de Crédito")
    private String metodoPago; // Mapea a metodo_pago

    /**
     * Tipo de homenaje (opcional 'o').
     */
    @Column(name = "tipo_homenaje", length = 50, nullable = true)
    @Size(max = 50, message = "El tipo de homenaje no puede exceder los 50 caracteres.")
    @Schema(description = "Tipo de homenaje asociado a la donación (opcional)", required = false, example = "Memoria de un ser querido")
    private String tipoHomenaje; // Mapea a tipo_homenaje

    /**
     * Detalle del homenaje (opcional 'o').
     */
    @Column(name = "detalle_homenaje", length = 400, nullable = true)
    @Size(max = 400, message = "El detalle del homenaje no puede exceder los 400 caracteres.")
    @Schema(description = "Detalle o mensaje del homenaje (opcional)", required = false, example = "En recuerdo de mi abuela.")
    private String detalleHomenaje; // Mapea a detalle_homenaje
}