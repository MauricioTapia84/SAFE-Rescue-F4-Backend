package com.SAFE_Rescue.API_Comunicacion.repository;

import com.SAFE_Rescue.API_Comunicacion.modelo.Notificacion;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

/**
 * Repositorio JPA para la entidad Notificacion.
 * Constantes de estado actualizadas: 8=Recibido (Pendiente), 9=Visto (Leída).
 */
@Repository
public interface NotificacionRepository extends JpaRepository<Notificacion, Integer> {

    // Constantes de estado actualizadas según la tabla 'estado':
    // 8: Recibido (Se considera "Pendiente" o "No Leída")
    // 9: Visto (Se considera "Leída")
    Integer ESTADO_PENDIENTE = 8;
    Integer ESTADO_LEIDA = 9;

    /**
     * Busca las notificaciones no leídas (idEstado = 8) para un usuario.
     * Ordena por fecha de creación descendente y usa paginación.
     *
     * @param idUsuarioReceptor ID del usuario receptor (String).
     * @param idEstado Debe ser '8' (ESTADO_PENDIENTE).
     * @param pageable Configuración de paginación.
     * @return Página de notificaciones pendientes.
     */
    Page<Notificacion> findByIdUsuarioReceptorAndIdEstadoEqualsOrderByFechaCreacionDesc(
            String idUsuarioReceptor,
            Integer idEstado,
            Pageable pageable
    );

    /**
     * Consulta optimizada para marcar todas las notificaciones pendientes de un usuario como leídas (idEstado = 9).
     *
     * @param idUsuarioReceptor ID del usuario.
     * @param nuevoEstado ID del nuevo estado (9 - Visto).
     * @param estadoAnterior ID del estado a actualizar (8 - Recibido).
     * @return El número de filas actualizadas.
     */
    @Modifying
    @Query("UPDATE Notificacion n SET n.idEstado = :nuevoEstado WHERE n.idUsuarioReceptor = :idUsuarioReceptor AND n.idEstado = :estadoAnterior")
    int actualizarEstadoPorUsuario(
            @Param("idUsuarioReceptor") String idUsuarioReceptor,
            @Param("nuevoEstado") Integer nuevoEstado,
            @Param("estadoAnterior") Integer estadoAnterior
    );
}