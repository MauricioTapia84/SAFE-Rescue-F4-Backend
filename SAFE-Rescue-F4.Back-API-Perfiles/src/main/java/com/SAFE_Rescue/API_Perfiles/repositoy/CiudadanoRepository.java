package com.SAFE_Rescue.API_Perfiles.repositoy;

import com.SAFE_Rescue.API_Perfiles.modelo.Ciudadano;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * Repositorio para la gestión de Ciudadanos
 * Maneja operaciones CRUD desde la base de datos usando Jakarta
 */
@Repository
public interface CiudadanoRepository extends JpaRepository<Ciudadano, Integer> {

}
