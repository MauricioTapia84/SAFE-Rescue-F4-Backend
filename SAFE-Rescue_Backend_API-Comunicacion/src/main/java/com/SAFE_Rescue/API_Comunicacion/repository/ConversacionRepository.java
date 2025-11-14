package com.SAFE_Rescue.API_Comunicacion.repository;

import com.SAFE_Rescue.API_Comunicacion.modelo.Conversacion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Repositorio para la entidad {@link Conversacion}.
 * Proporciona operaciones CRUD y algunas consultas personalizadas.
 */
@Repository
public interface ConversacionRepository extends JpaRepository<Conversacion, Integer> {

    List<Conversacion> findByTipo(String tipo);
}