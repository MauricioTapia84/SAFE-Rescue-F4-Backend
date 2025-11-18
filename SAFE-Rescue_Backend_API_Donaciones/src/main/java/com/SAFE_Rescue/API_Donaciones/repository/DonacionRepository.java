package com.SAFE_Rescue.API_Donaciones.repository;

import com.SAFE_Rescue.API_Donaciones.modelo.Donacion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Repositorio JPA para la entidad Donacion.
 * Proporciona métodos CRUD básicos y consultas personalizadas.
 */
@Repository
public interface DonacionRepository extends JpaRepository<Donacion, Integer> {

    /**
     * Busca todas las donaciones realizadas por un donante específico (ID lógico externo).
     * El nombre del método se basa en la propiedad 'idDonante' de la entidad Donacion.
     * * @param idDonante ID del donante, consumido desde la API externa de Usuarios.
     * @return Lista de donaciones asociadas a ese donante.
     */
    List<Donacion> findByIdDonante(Integer idDonante);
}