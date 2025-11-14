package com.SAFE_Rescue.API_Comunicacion.repository;

import com.SAFE_Rescue.API_Comunicacion.modelo.ParticipanteConversacion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ParticipanteConversacionRepository extends JpaRepository<ParticipanteConversacion, Integer> {

    /**
     * Busca todas las asignaciones de participantes para una conversación específica.
     */
    List<ParticipanteConversacion> findByConversacion_IdConversacion(Integer idConversacion);

    /**
     * Busca todas las asignaciones donde un usuario específico sea participante.
     */
    List<ParticipanteConversacion> findByIdUsuarioOrderByFechaUnionDesc(Integer idUsuario);

    /**
     * Busca una asignación específica por ID de usuario e ID de conversación.
     * Útil para verificar si un usuario ya es miembro.
     */
    Optional<ParticipanteConversacion> findByIdUsuarioAndConversacion_IdConversacion(Integer idUsuario, Integer idConversacion);

    /**
     * Elimina una asignación específica por ID de usuario e ID de conversación.
     */
    @SuppressWarnings("UnusedReturnValue")
    void deleteByIdUsuarioAndConversacion_IdConversacion(Integer idUsuario, Integer idConversacion);
}