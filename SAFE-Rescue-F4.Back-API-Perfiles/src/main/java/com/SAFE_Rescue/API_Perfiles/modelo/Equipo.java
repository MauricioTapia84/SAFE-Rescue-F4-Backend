package com.SAFE_Rescue.API_Perfiles.modelo;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Entidad que representa un Equipo de trabajo.
 * Referencia el Estado por ID (clave foránea lógica) e incluye una relación
 * OneToOne opcional con el Bombero que actúa como líder (liderEquipo).
 */
@Entity
@Table(name = "equipo")
@NoArgsConstructor
@AllArgsConstructor
@Data
public class Equipo {

    @Id
    @Column(name = "id_equipo")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer idEquipo;

    @Column(length = 50, nullable = false)
    private String nombre;

    // Relación ManyToOne con la entidad nativa Compania
    @ManyToOne
    @JoinColumn(name = "compania_id", referencedColumnName = "id_compania", nullable = false)
    private Compania compania;

    // Relación ManyToOne con la entidad nativa TipoEquipo
    @ManyToOne
    @JoinColumn(name = "tipo_equipo_id", referencedColumnName = "id_tipo_equipo", nullable = false)
    private TipoEquipo tipoEquipo;

    // --- Relación Opcional con el Líder (Bombero) ---
    // La clave foránea 'lider_id' apunta al id_usuario/id_bombero.
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "lider_id", referencedColumnName = "id_usuario", nullable = true)
    @Schema(description = "Líder del equipo (Referencia al ID de Usuario/Bombero)", required = false)
    private Bombero lider; // <-- Este es el atributo líder

    // --- CLAVE FORÁNEA LÓGICA (Microservicio Estado) ---

    @Column(name = "estado_id", nullable = false)
    @Schema(description = "ID del Estado (clave foránea lógica a la API externa de Estados)", required = true, example = "1")
    private Integer idEstado;
}