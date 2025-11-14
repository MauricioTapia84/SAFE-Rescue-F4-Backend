package com.SAFE_Rescue.API_Comunicacion.repository;

import com.SAFE_Rescue.API_Comunicacion.modelo.HistorialMensaje;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * Repositorio para la entidad {@link HistorialMensaje}.
 * Proporciona operaciones CRUD y algunas consultas personalizadas.
 */
@Repository
public interface HistorialMensajeRepository extends JpaRepository<HistorialMensaje, Integer> {


}