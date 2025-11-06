package com.SAFE_Rescue.API_Geolocalizacion.repositoy;

import com.SAFE_Rescue.API_Geolocalizacion.modelo.Direccion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * Repositorio para la gestión de Direcciones
 * Maneja operaciones CRUD desde la base de datos usando Jakarta
 */
@Repository
public interface DireccionRepository extends JpaRepository<Direccion, Integer> {

}
