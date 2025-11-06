package com.SAFE_Rescue.API_Perfiles.modelo;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "tipo_usuario")
@NoArgsConstructor
@AllArgsConstructor
@Data
@Schema(description = "Entidad que representa un tipo de usuario")
public class TipoUsuario {

    @Id
    @Column(name = "id_tipo_usuario")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Schema(description = "Identificador único del tipo de usuario", example = "1")
    private Integer idTipoUsuario; // Sugerencia: Usar Integer en lugar de int

    /**
     * Nombre del Tipo usuario
     */
    @Schema(description = "Nombre del tipo de usuario", example = "Admin")
    @Column(length = 50, nullable = false)
    @NotBlank(message = "El nombre del tipo de usuario es obligatorio") // Sugerencia: Validación
    @Size(max = 50, message = "El nombre no puede exceder los 50 caracteres") // Sugerencia: Validación
    private String nombre;
}