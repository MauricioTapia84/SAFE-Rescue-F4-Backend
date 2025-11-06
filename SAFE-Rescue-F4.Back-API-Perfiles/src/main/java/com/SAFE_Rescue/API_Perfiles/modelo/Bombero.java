package com.SAFE_Rescue.API_Perfiles.modelo;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull; // Importación necesaria
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@NoArgsConstructor
@AllArgsConstructor
@Data // Usamos @PrimaryKeyJoinColumn y @MapsId para indicar que el ID de Bombero es heredado del ID de Usuario
@PrimaryKeyJoinColumn(name = "id_usuario") // Esta anotación ya estaba en tu código y es correcta.
public class Bombero extends Usuario {

    // Al usar InheritanceType.JOINED, la clave primaria de la subclase es también una clave foránea
    // que referencia al padre (Usuario). Usamos @MapsId para hacer esto explícito y correcto.
    @Id
    @MapsId
    @Column(name = "id_usuario")
    private Integer idUsuario; // Debe coincidir el tipo Integer con el padre Usuario (sugerencia de revisión anterior)


    /**
     * Equipo.
     * Relación Muchos-a-uno con la entidad Equipo.
     */
    @ManyToOne
    @JoinColumn(name = "equipo_id", referencedColumnName = "id_equipo", nullable = false) // Asegura que la columna DB sea NOT NULL
    @NotNull(message = "El equipo es obligatorio para un bombero") // Sugerencia: Validación
    @Schema(description = "Equipo asociado al usuario")
    private Equipo equipo;


}