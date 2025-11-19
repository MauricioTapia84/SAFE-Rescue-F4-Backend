package com.SAFE_Rescue.API_Perfiles.service;

import com.SAFE_Rescue.API_Perfiles.config.GeolocalizacionClient;
import com.SAFE_Rescue.API_Perfiles.dto.DireccionDTO;
import com.SAFE_Rescue.API_Perfiles.modelo.Ciudadano;
import com.SAFE_Rescue.API_Perfiles.repository.CiudadanoRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.NoSuchElementException;

/**
 * Servicio para la gestión de Ciudadanos.
 * Hereda propiedades de Usuario y maneja lógica específica de Ciudadanos,
 * incluyendo la persistencia de su dirección a través del MS-Geolocalización.
 */
@Service
public class CiudadanoService {

    @Autowired
    private CiudadanoRepository ciudadanoRepository;

    @Autowired
    private UsuarioService usuarioService;

    // Cliente Feign/WebClient para interactuar con la API de Geolocalización
    @Autowired
    private GeolocalizacionClient geolocalizacionClient;

    /**
     * Obtiene todos los Ciudadanos registrados en el sistema.
     *
     * @return Lista de todos los usuarios.
     */
    public List<Ciudadano> findAll() {
        return ciudadanoRepository.findAll();
    }

    /**
     * Busca un Ciudadano por su ID único.
     *
     * @param id El ID del Ciudadano.
     * @return El Ciudadano encontrado.
     * @throws NoSuchElementException Si el Ciudadano no es encontrado.
     */
    public Ciudadano findById(Integer id) {
        return ciudadanoRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("Ciudadano no encontrado con ID: " + id));
    }

    /**
     * Guarda un nuevo Ciudadano.
     */
    public Ciudadano save(Ciudadano ciudadano) {
        if (ciudadano == null) {
            throw new IllegalArgumentException("El objeto Ciudadano no puede ser nulo.");
        }

        // 1. Validar las relaciones básicas de Usuario
        usuarioService.validarExistencia(ciudadano);

        // 2. Persistir la Dirección y obtener su ID
        validarYGuardarDireccion(ciudadano);

        try {
            // 3. Guardar el registro de Ciudadano (con el idDireccion ya seteado)
            return ciudadanoRepository.save(ciudadano);
        } catch (DataIntegrityViolationException e) {
            // Captura errores de unicidad del padre (RUN, correo, teléfono)
            throw new IllegalArgumentException("Error de integridad de datos. El RUN, correo o teléfono ya existen.", e);
        }
    }

    /**
     * Actualiza un Ciudadano existente.
     */
    public Ciudadano update(Ciudadano ciudadano, Integer id) {
        if (ciudadano == null) {
            throw new IllegalArgumentException("El objeto Ciudadano a actualizar no puede ser nulo.");
        }

        // 1. Validar las relaciones básicas de Usuario
        usuarioService.validarExistencia(ciudadano);

        // 2. Persistir la Dirección y obtener su ID
        validarYGuardarDireccion(ciudadano); // Reutilizamos el método para guardar/actualizar la dirección

        Ciudadano ciudadanoExistente = findById(id);

        // 3. Actualizar los campos heredados de Usuario
        ciudadanoExistente.setRun(ciudadano.getRun());
        ciudadanoExistente.setDv(ciudadano.getDv());
        ciudadanoExistente.setNombre(ciudadano.getNombre());
        ciudadanoExistente.setAPaterno(ciudadano.getAPaterno());
        ciudadanoExistente.setAMaterno(ciudadano.getAMaterno());
        ciudadanoExistente.setFechaRegistro(ciudadano.getFechaRegistro());
        ciudadanoExistente.setTelefono(ciudadano.getTelefono());
        ciudadanoExistente.setCorreo(ciudadano.getCorreo());
        ciudadanoExistente.setContrasenia(ciudadano.getContrasenia());
        ciudadanoExistente.setIntentosFallidos(ciudadano.getIntentosFallidos());
        ciudadanoExistente.setRazonBaneo(ciudadano.getRazonBaneo());
        ciudadanoExistente.setDiasBaneo(ciudadano.getDiasBaneo());
        ciudadanoExistente.setIdEstado(ciudadano.getIdEstado());
        ciudadanoExistente.setIdFoto(ciudadano.getIdFoto());
        ciudadanoExistente.setTipoUsuario(ciudadano.getTipoUsuario());

        // 4. Actualizar el campo específico de Ciudadano (idDireccion)
        // El idDireccion ya fue seteado en el paso 2 al llamar a validarYGuardarDireccion
        ciudadanoExistente.setIdDireccion(ciudadano.getIdDireccion());

        try {
            return ciudadanoRepository.save(ciudadanoExistente);
        } catch (DataIntegrityViolationException e) {
            throw new IllegalArgumentException("Error de integridad de datos. El RUN, correo o teléfono ya existen.", e);
        }
    }

    /**
     * Elimina un Ciudadano por su ID.
     */
    public void delete(Integer id) {
        Ciudadano ciudadano = findById(id); // Reusamos findById para consistencia y NotFound

        try {
            // NOTA: Idealmente, solo se debería "desactivar" el usuario o realizar un Soft-Delete
            // La eliminación en cascada del registro padre (Usuario) es manejada por JPA.
            ciudadanoRepository.delete(ciudadano);
        } catch (DataIntegrityViolationException e) {
            // Este servicio no debe manejar referencias externas de otros microservicios (como Incidentes).
            // Si hay un error, lo reportamos.
            throw new IllegalStateException("No se puede eliminar el ciudadano. Verifique que no tenga incidentes activos o referencias externas.", e);
        }
    }

    /**
     * Valida la existencia de la Dirección DTO y la guarda/actualiza en el MS-Cordenadas.
     * Luego, asigna el ID retornado a la entidad Ciudadano.
     *
     * @param ciudadano El ciudadano con el DireccionDTO a persistir.
     */
    private void validarYGuardarDireccion(Ciudadano ciudadano) {
        DireccionDTO direccionDTO = geolocalizacionClient.getDireccionById(ciudadano.getIdDireccion());

        if (direccionDTO == null) {
            throw new IllegalArgumentException("La información de dirección es obligatoria para un Ciudadano.");
        }

        try {
            // Llama a la API de Geolocalización para guardar la dirección.
            // Se espera que este método retorne el DTO con el ID ya asignado
            // (o un ID simple si la implementación del cliente es diferente).
            DireccionDTO direccionGuardada = geolocalizacionClient.guardarDireccion(direccionDTO);

            if (direccionGuardada != null && direccionGuardada.getIdDireccion() != null) {
                // Asignamos el ID retornado por el MS-Geolocalización al campo idDireccion del Ciudadano.
                ciudadano.setIdDireccion(direccionGuardada.getIdDireccion());
            } else {
                throw new IllegalStateException("El MS-Cordenadas no devolvió un ID de dirección válido.");
            }
        } catch (Exception e) {
            // Captura errores de comunicación con el microservicio externo.
            throw new IllegalStateException("Error al comunicarse con el servicio de Geolocalización para guardar la dirección.", e);
        }
    }
}