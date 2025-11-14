package com.SAFE_Rescue.API_Incidentes.repository;

import com.SAFE_Rescue.API_Incidentes.modelo.HistorialIncidente;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Repositorio para la entidad HistorialIncidente, permitiendo
 * operaciones CRUD y consultas sobre los registros de auditoría
 * de cambios de estado de los Incidentes.
 */
@Repository
public interface HistorialIncidenteRepository extends JpaRepository<HistorialIncidente, Integer> {

    List<HistorialIncidente> findByIncidenteIdIncidente(Integer idIncidente);
}