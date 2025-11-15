package com.SAFE_Rescue.API_Comunicacion.repository;

import com.SAFE_Rescue.API_Comunicacion.modelo.HistorialMensaje;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Repositorio para la entidad {@link HistorialMensaje}.
 * Proporciona operaciones CRUD y algunas consultas personalizadas.
 */
@Repository
public interface HistorialMensajeRepository extends JpaRepository<HistorialMensaje, Integer> {

    /**
     * Busca todos los registros de historial asociados a un ID de mensaje específico.
     * Se espera que la búsqueda se ordene por fecha_historial descendente por defecto.
     *
     * @param idMensaje ID del mensaje.
     * @return Lista de registros de historial.
     */
    List<HistorialMensaje> findByMensajeIdMensaje(Integer idMensaje);
}