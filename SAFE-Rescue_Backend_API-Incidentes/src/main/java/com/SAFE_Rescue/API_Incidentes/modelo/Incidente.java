package com.SAFE_Rescue.API_Incidentes.modelo;

import com.SAFE_Rescue.API_Incidentes.dto.FotoDTO;
import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "incidente")
@NoArgsConstructor
@AllArgsConstructor
@Data
public class Incidente {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer idIncidente;

    @Column(length = 50, nullable = false)
    @NotBlank(message = "El título del incidente es obligatorio.")
    @Size(max = 50, message = "El título no puede exceder los 50 caracteres.")
    private String titulo;

    @Column(length = 400, nullable = true)
    @Size(max = 400, message = "El detalle no puede exceder los 400 caracteres.")
    private String detalle;

    @Column(name = "fecha_incidente", nullable = false)
    @NotNull(message = "La fecha de registro es obligatoria.")
    @Schema(description = "Fecha y hora en que se registró el incidente", example = "2025-09-09T10:30:00")
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime fechaRegistro;

    // --- NUEVO CAMPO (Soluciona el error setFechaUltimaActualizacion) ---
    @Column(name = "fecha_ultima_actualizacion")
    @Schema(description = "Fecha de la última modificación del incidente", example = "2025-09-09T12:00:00")
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime fechaUltimaActualizacion;
    // --------------------------------------------------------------------

    // Campos de texto para persistencia visual
    @Column(length = 100, nullable = true)
    private String region;

    @Column(length = 100, nullable = true)
    private String comuna;

    @Column(length = 200, nullable = true)
    private String direccion;

    // Relaciones y FKs
    @ManyToOne
    @JoinColumn(name = "tipo_incidente_id", referencedColumnName = "idTipoIncidente", nullable = false)
    @NotNull(message = "El tipo de incidente es obligatorio.")
    private TipoIncidente tipoIncidente;

    @Column(name = "ubicacion_id", nullable = false)
    @NotNull(message = "La ID de la ubicación del incidente es obligatoria.")
    private Integer idDireccion;

    @Column(name = "ciudadano_id", nullable = false)
    @NotNull(message = "La ID del ciudadano que reporta es obligatoria.")
    private Integer idCiudadano;

    @Column(name = "estado_incidente_id", nullable = false)
    @NotNull(message = "La ID del estado del incidente es obligatoria.")
    private Integer idEstadoIncidente;

    @Column(name = "usuario_asignado_id", nullable = true)
    private Integer idUsuarioAsignado;

    private Integer IdFoto;

    @PreUpdate
    protected void onUpdate() {
        fechaUltimaActualizacion = LocalDateTime.now();
    }
}