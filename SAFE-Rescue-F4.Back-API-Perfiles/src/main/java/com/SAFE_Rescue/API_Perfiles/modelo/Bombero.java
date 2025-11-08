package com.SAFE_Rescue.API_Perfiles.modelo;

import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * Entidad Bombero que hereda de Usuario.
 */
@EqualsAndHashCode(callSuper = true)
@Data
@Entity
@PrimaryKeyJoinColumn(name = "id_bombero")
public class Bombero extends Usuario {

    // Relación ManyToOne con la entidad nativa Equipo
    @ManyToOne
    @JoinColumn(name = "equipo_id", referencedColumnName = "id_equipo")
    private Equipo equipo;
}