package com.SAFE_Rescue.API_Perfiles.service;

import com.SAFE_Rescue.API_Perfiles.modelo.Compania;
import com.SAFE_Rescue.API_Perfiles.modelo.Equipo;
import com.SAFE_Rescue.API_Perfiles.modelo.TipoEquipo;
import com.SAFE_Rescue.API_Perfiles.repositoy.EquipoRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
// Se eliminan importaciones de WebClient y Mono, ya que Compania es local
import com.SAFE_Rescue.API_Perfiles.modelo.Usuario;

import java.util.List;
import java.util.NoSuchElementException;

@Service
public class EquipoService {

    @Autowired
    private EquipoRepository equipoRepository;

    @Autowired
    private TipoEquipoService tipoEquipoService;

    // Inyección de servicios locales para validar relaciones
    @Autowired
    private CompaniaService companiaService;

    @Autowired
    private UsuarioService usuarioService; // Necesario para validar la existencia del líder

    // --- MÉTODOS CRUD ---

    public List<Equipo> findAll() {
        return equipoRepository.findAll();
    }

    public Equipo findById(Integer id) {
        return equipoRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("Equipo no encontrado con ID: " + id));
    }

    public Equipo save(Equipo equipo) {
        if (equipo == null) {
            throw new IllegalArgumentException("El equipo no puede ser nulo.");
        }

        // 1. Validar todas las dependencias antes de guardar
        validarTipoEquipo(equipo);
        validarCompania(equipo); // Validación local
        validarLider(equipo);     // Validación de la existencia del líder (Usuario)

        try {
            return equipoRepository.save(equipo);
        } catch (DataIntegrityViolationException e) {
            throw new IllegalArgumentException("Error de integridad de datos. El nombre del equipo ya existe o no cumple con las restricciones.", e);
        }
    }

    public Equipo update(Equipo equipo, Integer id) {
        if (equipo == null) {
            throw new IllegalArgumentException("El equipo a actualizar no puede ser nulo.");
        }

        // 1. Verificar existencia
        Equipo equipoExistente = findById(id);

        // 2. Validar dependencias del nuevo objeto (equipo)
        validarTipoEquipo(equipo);
        validarCompania(equipo);
        validarLider(equipo);

        // 3. Aplicar cambios
        equipoExistente.setNombre(equipo.getNombre());

        // Se usan los setters si el objeto no es nulo (aunque la validación ya asegura que si viene, existe)
        if (equipo.getCompania() != null) equipoExistente.setCompania(equipo.getCompania());
        if (equipo.getTipoEquipo() != null) equipoExistente.setTipoEquipo(equipo.getTipoEquipo());
        if (equipo.getLider() != null) equipoExistente.setLider(equipo.getLider());

        try {
            return equipoRepository.save(equipoExistente);
        } catch (DataIntegrityViolationException e) {
            // Manejamos el caso de que el nuevo nombre esté duplicado con otro ID
            throw new IllegalArgumentException("Error de integridad de datos. No se puede actualizar el equipo, el nombre podría ya existir.", e);
        }
    }

    /**
     * Elimina un equipo del sistema, manejando errores de clave foránea.
     * @param id Identificador del equipo a eliminar
     * @throws NoSuchElementException Si el equipo no se encuentra
     * @throws IllegalStateException Si el equipo tiene bomberos asociados (clave foránea en uso)
     */
    public void delete(Integer id) {
        // Reusamos findById para verificar existencia
        Equipo equipo = findById(id);

        try {
            equipoRepository.delete(equipo);
        } catch (DataIntegrityViolationException e) {
            // Manejamos el error si existen claves foráneas (ej: Bomberos asociados a este equipo)
            throw new IllegalStateException("No se puede eliminar el equipo porque tiene bomberos asociados. Remueva los bomberos del equipo primero.", e);
        }
    }

    // --- MÉTODOS DE VALIDACIÓN Y UTILIDADES ---

    private void validarTipoEquipo(Equipo equipo) {
        if (equipo.getTipoEquipo() == null || equipo.getTipoEquipo().getIdTipoEquipo() == null) {
            throw new IllegalArgumentException("El tipo de equipo es obligatorio.");
        }

        try {
            // Verifica la existencia del TipoEquipo (entidad local)
            TipoEquipo tipo = tipoEquipoService.findById(equipo.getTipoEquipo().getIdTipoEquipo());
            // Asegura que el objeto sea la instancia gestionada
            equipo.setTipoEquipo(tipo);
        } catch (NoSuchElementException e) {
            throw new IllegalArgumentException("El tipo de equipo asociado no existe.", e);
        }
    }

    /**
     * Valida la existencia de la compañía en el servicio local CompaniaService.
     */
    private void validarCompania(Equipo equipo) {
        if (equipo.getCompania() == null || equipo.getCompania().getIdCompania() == null) {
            throw new IllegalArgumentException("La compañía es obligatoria.");
        }

        try {
            // Validar la existencia usando el servicio local CompaniaService
            Compania compania = companiaService.findById(equipo.getCompania().getIdCompania());
            // Asegura que el objeto sea la instancia gestionada
            equipo.setCompania(compania);
        } catch (NoSuchElementException e) {
            throw new IllegalArgumentException("La compañía asociada al equipo no existe.", e);
        }
    }

    /**
     * Valida que el ID del líder (Usuario) exista en el servicio local UsuarioService.
     */
    private void validarLider(Equipo equipo) {
        // El líder puede ser nulo, pero si se proporciona, debe existir.
        if (equipo.getLider() != null && equipo.getLider().getIdUsuario() != null) {
            try {
                // Validar la existencia usando el servicio local UsuarioService
                Usuario lider = usuarioService.findById(equipo.getLider().getIdUsuario());
                // Asegura que el objeto sea la instancia gestionada
                equipo.setLider(lider);
            } catch (NoSuchElementException e) {
                throw new IllegalArgumentException("El líder asociado (Usuario ID: " + equipo.getLider().getIdUsuario() + ") no existe.", e);
            }
        }
    }
}