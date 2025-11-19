package com.SAFE_Rescue.API_Perfiles.repository;

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

    /**
     * Usa JPQL explícito para buscar el usuario por su campo 'nombre' (como nombre de usuario)
     * O por su campo 'correo' (email), ya que el campo 'nombreUsuario' no existe.
     * La firma del método se mantiene para compatibilidad con el AuthService.
     */
    @Query("SELECT u FROM Usuario u WHERE u.nombre = :nombreUsuario OR u.correo = :email")
    Optional<Usuario> findByNombreUsuarioOrEmail(
            @Param("nombreUsuario") String nombreUsuario,
            @Param("email") String email
    );

    /**
     * Método corregido: busca por la propiedad 'run' de la entidad (no 'rut').
     * Spring Data infiere la consulta EXISTS al coincidir con el campo 'run'.
     */
    boolean existsByRun(String run);

}