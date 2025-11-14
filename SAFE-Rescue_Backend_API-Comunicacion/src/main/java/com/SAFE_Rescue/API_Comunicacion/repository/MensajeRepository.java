package com.SAFE_Rescue.API_Comunicacion.repository;

import com.SAFE_Rescue.API_Comunicacion.modelo.Mensaje;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Repositorio para la entidad {@link Mensaje}.
 * Proporciona operaciones CRUD básicas.
 */
@Repository
public interface MensajeRepository extends JpaRepository<Mensaje, Integer> {

    List<Mensaje> findByConversacionIdConversacion(Integer idConversacion);

    /**
     * Busca los mensajes de una conversación, ordenados por fecha de envío.
     * Implementa paginación para cargar el historial de manera eficiente.
     *
     * @param idConversacion ID de la conversación.
     * @param pageable Configuración de paginación y ordenamiento (por lo general, ascendente por fecha de envío).
     * @return Una página de mensajes.
     */
    Page<Mensaje> findByConversacion_IdConversacion(Integer idConversacion, Pageable pageable);

}