package com.SAFE_Rescue.API_Perfiles.service;

import com.SAFE_Rescue.API_Perfiles.modelo.Bombero;
import com.SAFE_Rescue.API_Perfiles.repository.BomberoRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.NoSuchElementException;

/**
 * Servicio para la gestión de bomberos.
 * Hereda propiedades de Usuario y maneja lógica específica de Bombero.
 */
@Service
public class BomberoService {

    @Autowired
    private BomberoRepository bomberoRepository;

    @Autowired
    private UsuarioService usuarioService;

    @Autowired
    private EquipoService equipoService;

    /**
     * Obtiene todos los bomberos registrados en el sistema.
     *
     * @return Lista de todos los usuarios.
     */
    public List<Bombero> findAll() {
        return bomberoRepository.findAll();
    }

    /**
     * Busca un bombero por su ID único.
     *
     * @param id El ID del bombero.
     * @return El bombero encontrado.
     * @throws NoSuchElementException Si el bombero no es encontrado.
     */
    public Bombero findById(Integer id) {
        return bomberoRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("Bombero no encontrado con ID: " + id));
    }

    /**
     * Guarda un nuevo bombero.
     */
    public Bombero save(Bombero bombero) {
        if (bombero == null) {
            throw new IllegalArgumentException("El objeto Bombero no puede ser nulo.");
        }

        // 1. Validar las relaciones heredadas (TipoUsuario, ID Estado)
        usuarioService.validarExistencia(bombero);

        // 2. Validar las relaciones específicas (Equipo)
        validarRelacionesBombero(bombero);

        try {
            return bomberoRepository.save(bombero);
        } catch (DataIntegrityViolationException e) {
            // Captura errores de unicidad del padre (RUN, correo, teléfono)
            throw new IllegalArgumentException("Error de integridad de datos. El RUN, correo o teléfono ya existen.", e);
        }
    }

    /**
     * Actualiza un bombero existente.
     */
    public Bombero update(Bombero bombero, Integer id) {
        if (bombero == null) {
            throw new IllegalArgumentException("El objeto Bombero a actualizar no puede ser nulo.");
        }

        // 1. Validar las relaciones heredadas (TipoUsuario, ID Estado)
        usuarioService.validarExistencia(bombero);

        // 2. Validar las relaciones específicas (Equipo)
        validarRelacionesBombero(bombero);

        Bombero bomberoExistente = findById(id); // Reusamos findById

        // Actualizar los campos heredados de Usuario
        bomberoExistente.setRun(bombero.getRun());
        bomberoExistente.setDv(bombero.getDv());
        bomberoExistente.setNombre(bombero.getNombre());
        bomberoExistente.setAPaterno(bombero.getAPaterno());
        bomberoExistente.setAMaterno(bombero.getAMaterno());
        bomberoExistente.setFechaRegistro(bombero.getFechaRegistro());
        bomberoExistente.setTelefono(bombero.getTelefono());
        bomberoExistente.setCorreo(bombero.getCorreo());
        bomberoExistente.setContrasenia(bombero.getContrasenia());
        bomberoExistente.setIntentosFallidos(bombero.getIntentosFallidos());
        bomberoExistente.setRazonBaneo(bombero.getRazonBaneo());
        bomberoExistente.setDiasBaneo(bombero.getDiasBaneo());

        // CORRECCIÓN: Usar los IDs (Claves Foráneas Lógicas) en lugar de objetos
        bomberoExistente.setIdEstado(bombero.getIdEstado());
        bomberoExistente.setIdFoto(bombero.getIdFoto());

        // Actualizar la relación local de TipoUsuario
        bomberoExistente.setTipoUsuario(bombero.getTipoUsuario());

        // Actualizar el campo específico de Bombero
        bomberoExistente.setEquipo(bombero.getEquipo());

        try {
            return bomberoRepository.save(bomberoExistente);
        } catch (DataIntegrityViolationException e) {
            throw new IllegalArgumentException("Error de integridad de datos. El RUN, correo o teléfono ya existen.", e);
        }
    }

    /**
     * Elimina un bombero por su ID.
     */
    public void delete(Integer id) {
        Bombero bombero = findById(id); // Usamos findById para consistencia y NotFound

        try {
            bomberoRepository.delete(bombero);
        } catch (DataIntegrityViolationException e) {
            // Un bombero como subclase tiene el riesgo de que el registro padre (Usuario) esté referenciado
            // (ej. si el Usuario era el líder de un Equipo).
            throw new IllegalStateException("No se puede eliminar el bombero. Verifique que el registro de Usuario asociado no tenga referencias activas (ej. líder de equipo).", e);
        }
    }

    /**
     * Valida las relaciones específicas de Bombero, asegurando que el equipo exista y no sea nulo.
     */
    private void validarRelacionesBombero(Bombero bombero) {
        if (bombero.getEquipo() == null) {
            throw new IllegalArgumentException("El equipo es obligatorio para un bombero.");
        }

        try {
            // Intenta encontrar el equipo para validar su existencia
            equipoService.findById(bombero.getEquipo().getIdEquipo());
        } catch (NoSuchElementException e) {
            throw new IllegalArgumentException("El equipo asociado no existe.", e);
        }
    }
}