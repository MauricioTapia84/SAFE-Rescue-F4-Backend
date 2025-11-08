package com.SAFE_Rescue.API_Perfiles.service;

import com.SAFE_Rescue.API_Perfiles.modelo.*;
import com.SAFE_Rescue.API_Perfiles.repositoy.EquipoRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy; // Importación necesaria
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.NoSuchElementException;

@Service
public class EquipoService {

    private final EquipoRepository equipoRepository;
    private final TipoEquipoService tipoEquipoService;
    private final CompaniaService companiaService;

    // CORRECCIÓN CLAVE: Usamos @Lazy para diferir la inicialización de BomberoService
    // y romper el ciclo de dependencia.
    private final BomberoService bomberoService;

    // Usando inyección por constructor para todas las dependencias (mejor práctica)
    @Autowired
    public EquipoService(
            EquipoRepository equipoRepository,
            TipoEquipoService tipoEquipoService,
            CompaniaService companiaService,
            @Lazy BomberoService bomberoService) { // @Lazy aquí es crucial

        this.equipoRepository = equipoRepository;
        this.tipoEquipoService = tipoEquipoService;
        this.companiaService = companiaService;
        this.bomberoService = bomberoService;
    }


    // --- MÉTODOS CRUD ---

    public List<Equipo> findAll() {
        return equipoRepository.findAll();
    }

    public Equipo findById(Integer id) {
        return equipoRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("Equipo not found with ID: " + id));
    }

    public Equipo save(Equipo equipo) {
        if (equipo == null) {
            throw new IllegalArgumentException("The team cannot be null.");
        }

        // 1. Validate all dependencies before saving
        validarTipoEquipo(equipo);
        validarCompania(equipo);
        validarLider(equipo);

        try {
            return equipoRepository.save(equipo);
        } catch (DataIntegrityViolationException e) {
            throw new IllegalArgumentException("Data integrity error. The team name already exists or does not meet restrictions.", e);
        }
    }

    public Equipo update(Equipo equipo, Integer id) {
        if (equipo == null) {
            throw new IllegalArgumentException("The team to update cannot be null.");
        }

        // 1. Check existence
        Equipo equipoExistente = findById(id);

        // 2. Validate dependencies of the new object (equipo)
        validarTipoEquipo(equipo);
        validarCompania(equipo);
        validarLider(equipo);

        // 3. Apply changes
        equipoExistente.setNombre(equipo.getNombre());

        // Use setters if the object is not null (although validation already ensures it exists)
        if (equipo.getCompania() != null) equipoExistente.setCompania(equipo.getCompania());
        if (equipo.getTipoEquipo() != null) equipoExistente.setTipoEquipo(equipo.getTipoEquipo());
        if (equipo.getLider() != null) equipoExistente.setLider(equipo.getLider());

        try {
            return equipoRepository.save(equipoExistente);
        } catch (DataIntegrityViolationException e) {
            // Handle case where the new name is duplicated with another ID
            throw new IllegalArgumentException("Data integrity error. Cannot update the team, the name might already exist.", e);
        }
    }

    /**
     * Deletes a team from the system, handling foreign key errors.
     * @param id Identifier of the team to delete
     * @throws NoSuchElementException If the team is not found
     * @throws IllegalStateException If the team has associated firefighters (foreign key in use)
     */
    public void delete(Integer id) {
        // Reuse findById to check existence
        Equipo equipo = findById(id);

        try {
            equipoRepository.delete(equipo);
        } catch (DataIntegrityViolationException e) {
            // Handle error if foreign keys exist (e.g., Firefighters associated with this team)
            throw new IllegalStateException("Cannot delete the team because it has associated firefighters. Remove the firefighters from the team first.", e);
        }
    }

    // --- VALIDATION AND UTILITY METHODS ---

    private void validarTipoEquipo(Equipo equipo) {
        if (equipo.getTipoEquipo() == null || equipo.getTipoEquipo().getIdTipoEquipo() == null) {
            throw new IllegalArgumentException("The team type is mandatory.");
        }

        try {
            // Check existence of the TeamType (local entity)
            TipoEquipo tipo = tipoEquipoService.findById(equipo.getTipoEquipo().getIdTipoEquipo());
            // Ensure the object is the managed instance
            equipo.setTipoEquipo(tipo);
        } catch (NoSuchElementException e) {
            throw new IllegalArgumentException("The associated team type does not exist.", e);
        }
    }

    /**
     * Validates the existence of the company in the local CompaniaService.
     */
    private void validarCompania(Equipo equipo) {
        if (equipo.getCompania() == null || equipo.getCompania().getIdCompania() == null) {
            throw new IllegalArgumentException("The company is mandatory.");
        }

        try {
            // Validate existence using the local CompaniaService
            Compania compania = companiaService.findById(equipo.getCompania().getIdCompania());
            // Ensure the object is the managed instance
            equipo.setCompania(compania);
        } catch (NoSuchElementException e) {
            throw new IllegalArgumentException("The company associated with the team does not exist.", e);
        }
    }

    /**
     * Validates that the leader ID (User) exists in the local UsuarioService.
     */
    private void validarLider(Equipo equipo) {
        // The leader can be null, but if provided, must exist.
        if (equipo.getLider() != null && equipo.getLider().getIdUsuario() != null) {
            try {
                // Validate existence using the local UsuarioService
                Bombero lider = bomberoService.findById(equipo.getLider().getIdUsuario());
                // Ensure the object is the managed instance
                equipo.setLider(lider);
            } catch (NoSuchElementException e) {
                throw new IllegalArgumentException("The associated leader (User ID: " + equipo.getLider().getIdUsuario() + ") does not exist.", e);
            }
        }
    }
}