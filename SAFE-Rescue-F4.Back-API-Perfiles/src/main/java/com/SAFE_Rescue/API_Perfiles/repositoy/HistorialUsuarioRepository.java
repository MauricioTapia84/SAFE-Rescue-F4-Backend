package com.SAFE_Rescue.API_Perfiles.repositoy;

import com.SAFE_Rescue.API_Perfiles.modelo.HistorialUsuario;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Repositorio para la entidad HistorialUsuario, permitiendo
 * operaciones CRUD y consultas sobre los registros de auditoría
 * de cambios de estado de los perfiles (Usuario y Equipo).
 */
@Repository
public interface HistorialUsuarioRepository extends JpaRepository<HistorialUsuario, Integer> {

    /**
     * Busca todos los registros de historial para un usuario específico,
     * utilizando el ID del usuario. Los resultados se ordenan por la fecha
     * del evento de forma descendente (el más reciente primero).
     * * NOTA: Se usa 'findByUsuario_Id' para buscar por el ID de la entidad
     * relacionada 'Usuario', evitando errores de comparación de tipos.
     *
     * @param idUsuario ID del usuario (clave foránea en HistorialUsuario).
     * @return Lista de registros de HistorialUsuario.
     */
    List<HistorialUsuario> findByUsuarioIdUsuario(Integer idUsuario);

    /**
     * Busca todos los registros de historial para un equipo específico,
     * utilizando el ID del equipo. Los resultados se ordenan por la fecha
     * del evento de forma descendente (el más reciente primero).
     * * NOTA: Se usa 'findByEquipo_Id' para buscar por el ID de la entidad
     * relacionada 'Equipo', evitando errores de comparación de tipos.
     *
     * @param idEquipo ID del equipo (clave foránea en HistorialUsuario).
     * @return Lista de registros de HistorialUsuario.
     */
    List<HistorialUsuario> findByEquipoIdEquipo(Integer idEquipo);
}