package com.SAFE_Rescue.API_Geolocalizacion.repositoy;

import com.SAFE_Rescue.API_Geolocalizacion.modelo.Coordenadas;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * Repositorio para la gestión de Coordenadas
 * Maneja operaciones CRUD desde la base de datos usando Jakarta
 */
@Repository
public interface CoordenadasRepository extends JpaRepository<Coordenadas, Integer> {

}
