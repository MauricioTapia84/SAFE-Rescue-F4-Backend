package com.SAFE_Rescue.API_Geolocalizacion.repositoy;

import com.SAFE_Rescue.API_Geolocalizacion.modelo.Pais;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * Repositorio para la gestión de país
 * Maneja operaciones CRUD desde la base de datos usando Jakarta
 */
@Repository
public interface PaisRepository extends JpaRepository<Pais, Integer> {

}
