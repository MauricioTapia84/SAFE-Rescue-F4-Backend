package com.SAFE_Rescue.API_Perfiles.repository;

import com.SAFE_Rescue.API_Perfiles.modelo.Bombero;
import com.SAFE_Rescue.API_Perfiles.modelo.Ciudadano;
import com.SAFE_Rescue.API_Perfiles.modelo.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * Repositorio para la gestión de Usuarios
 */
@Repository
public interface UsuarioRepository extends JpaRepository<Usuario, Integer> {


    Optional<Usuario> findByCorreo(String correo);

    // NUEVO: Cargar usuario con subclase específica
    @Query("SELECT u FROM Usuario u LEFT JOIN FETCH u.tipoUsuario WHERE u.idUsuario = :id")
    Optional<Usuario> findByIdWithTipoUsuario(@Param("id") Integer id);

    // NUEVO: Buscar Ciudadano específico
    @Query("SELECT c FROM Ciudadano c WHERE c.idUsuario = :id")
    Optional<Ciudadano> findCiudadanoById(@Param("id") Integer id);

    // NUEVO: Buscar Bombero específico
    @Query("SELECT b FROM Bombero b WHERE b.idUsuario = :id")
    Optional<Bombero> findBomberoById(@Param("id") Integer id);

    boolean existsByRun(String run);

}