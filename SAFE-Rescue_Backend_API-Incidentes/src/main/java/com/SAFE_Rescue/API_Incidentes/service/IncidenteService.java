package com.SAFE_Rescue.API_Incidentes.service;

import com.SAFE_Rescue.API_Incidentes.dto.DireccionDTO;
import com.SAFE_Rescue.API_Incidentes.modelo.*;
import com.SAFE_Rescue.API_Incidentes.repository.*;
import com.SAFE_Rescue.API_Incidentes.config.*;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.NoSuchElementException;

/**
 * @class IncidenteService
 * @brief Servicio para la gestión integral de Incidentes.
 * Maneja operaciones CRUD y validación de datos para Incidentes.
 * Se utiliza el patrón de Claves Foráneas Lógicas (Integer IDs) para referenciar
 * entidades externas (Estado, Usuario, Dirección, Usuario Asignado).
 */
@Service
@Transactional
public class IncidenteService {

    // REPOSITORIOS LOCALES (JPA) INYECTADOS
    @Autowired private IncidenteRepository incidenteRepository;
    @Autowired private TipoIncidenteRepository tipoIncidenteRepository;

    // CLIENTES/SERVICIOS INYECTADOS (APIs externas)
    // Se asume que estos clientes retornan un DTO de la entidad externa o null si no existe.
    @Autowired private EstadoClient estadoClient;
    @Autowired private GeolocalizacionClient geolocalizacionClient;
    @Autowired private UsuarioClient usuarioClient;
    // @Autowired private EquipoClient equipoClient; // Eliminado: Reemplazado por UsuarioClient/Usuario

    // SERVICIO LOCAL
    @Autowired private TipoIncidenteService tipoIncidenteService;

    // MÉTODOS CRUD PRINCIPALES

    /**
     * Obtiene todos los Incidentes registrados en el sistema.
     * @return Lista completa de Incidentes
     */
    public List<Incidente> findAll() {
        return incidenteRepository.findAll();
    }

    /**
     * Busca un Incidente por su ID único.
     * @param id Identificador del Incidente (Integer, según la entidad)
     * @return Incidente encontrado
     * @throws NoSuchElementException Si no se encuentra el incidente
     */
    public Incidente findById(Integer id) { // Cambiado a Integer para coincidir con la entidad
        return incidenteRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("No se encontró Incidente con ID: " + id));
    }

    /**
     * Guarda un nuevo incidente en el sistema.
     * Realiza validaciones y verifica la existencia de todas las referencias por ID.
     * @param incidente Datos del incidente a guardar
     * @return Incidente guardado con ID generado
     * @throws IllegalArgumentException Si una entidad relacionada no existe o si faltan datos.
     * @throws RuntimeException Si ocurre algún error durante el proceso.
     */
    public Incidente save(Incidente incidente) {
        try {
            // 1. Validar campos obligatorios del incidente (título, detalle, etc.)
            validarIncidente(incidente);

            // 2. Validar existencia de todas las referencias por ID (Locales y Externas).
            // Esto lanzará IllegalArgumentException si alguna relación no existe.
            validarExistenciaReferencias(incidente);

            // 3. Ya que el incidente solo almacena IDs lógicos, no necesitamos reasignar DTOs completos.
            // La entidad Incidente ya viene con los IDs correctos para guardar.

            // 4. Guardar el Incidente en la base de datos local
            return incidenteRepository.save(incidente);
        } catch (NoSuchElementException e) {
            // Captura si una de las validaciones de existencia falló
            throw new IllegalArgumentException("Error de asignación: " + e.getMessage(), e);
        } catch (Exception e) {
            // Captura errores genéricos o de validación de atributos
            throw new RuntimeException("Error al guardar el incidente: " + e.getMessage(), e);
        }
    }

    /**
     * Actualiza los datos de un incidente existente.
     *
     * @param incidente Datos actualizados del incidente (contiene los IDs de las referencias)
     * @param id Identificador del incidente a actualizar
     * @return Incidente actualizado
     * @throws IllegalArgumentException Si el incidente proporcionado es nulo, o si una referencia ID no existe.
     * @throws NoSuchElementException Si no se encuentra el incidente a actualizar
     * @throws RuntimeException Si ocurre algún error durante la actualización
     */
    public Incidente update(Incidente incidente, Integer id) { // Cambiado a Integer
        if (incidente == null) {
            throw new IllegalArgumentException("El incidente no puede ser nulo");
        }

        Incidente incidenteExistente = findById(id);

        try {
            // 1. Validar campos de actualización
            validarActualizacion(incidente);

            // 2. Actualizar campos locales y IDs de referencia si son proporcionados.
            if (incidente.getTitulo() != null) {
                incidenteExistente.setTitulo(incidente.getTitulo());
            }
            if (incidente.getDetalle() != null) {
                incidenteExistente.setDetalle(incidente.getDetalle());
            }
            // Actualización de IDs de referencia (solo si vienen en la fuente)
            if (incidente.getIdDireccion() != null) {
                validarExistenciaReferencia(geolocalizacionClient, incidente.getIdDireccion(), "Direccion");
                incidenteExistente.setIdDireccion(incidente.getIdDireccion());
            }
            if (incidente.getIdCiudadano() != null) {
                validarExistenciaReferencia(usuarioClient, incidente.getIdCiudadano(), "Ciudadano");
                incidenteExistente.setIdCiudadano(incidente.getIdCiudadano());
            }
            if (incidente.getIdEstadoIncidente() != null) {
                validarExistenciaReferencia(estadoClient, incidente.getIdEstadoIncidente(), "Estado");
                incidenteExistente.setIdEstadoIncidente(incidente.getIdEstadoIncidente());
            }
            // MODIFICADO: Ahora se asigna un usuario en lugar de un equipo.
            if (incidente.getIdUsuarioAsignado() != null) {
                validarExistenciaReferencia(usuarioClient, incidente.getIdUsuarioAsignado(), "Usuario Asignado");
                incidenteExistente.setIdUsuarioAsignado(incidente.getIdUsuarioAsignado());
            }
            // TipoIncidente (local - se debe buscar la entidad completa para la relación JPA)
            if (incidente.getTipoIncidente() != null && incidente.getTipoIncidente().getIdTipoIncidente() != null) {
                TipoIncidente tipoEncontrado = tipoIncidenteRepository.findById(incidente.getTipoIncidente().getIdTipoIncidente())
                        .orElseThrow(() -> new NoSuchElementException("Tipo Incidente no encontrado con ID: " + incidente.getTipoIncidente().getIdTipoIncidente()));
                incidenteExistente.setTipoIncidente(tipoEncontrado);
            }

            // 3. Guardar el incidente actualizado
            return incidenteRepository.save(incidenteExistente);
        } catch (NoSuchElementException e) {
            // Captura si una de las validaciones de existencia falló
            throw new IllegalArgumentException("Error de asignación en la actualización: " + e.getMessage(), e);
        } catch (Exception e) {
            throw new RuntimeException("Error al actualizar incidente con ID " + id + ": " + e.getMessage(), e);
        }
    }

    /**
     * Elimina un incidente del sistema.
     * @param id Identificador del incidente a eliminar
     * @throws NoSuchElementException Si no se encuentra el incidente
     */
    public void delete(Integer id) { // Cambiado a Integer
        Incidente incidente = findById(id);
        incidenteRepository.delete(incidente);
    }

    // --- MÉTODOS PARA GESTIÓN DE DIRECCIONES ---

    /**
     * Crea una nueva dirección en el microservicio de Geolocalización
     * y la asigna al incidente especificado.
     *
     * @param incidenteId ID del incidente a actualizar.
     * @param ubicacionJson Payload JSON con los datos de la dirección.
     * @return El Incidente actualizado con el nuevo ID de Dirección.
     * @throws NoSuchElementException Si no se encuentra el incidente.
     * @throws RuntimeException Si el cliente de geolocalización falla.
     */
    public Incidente agregarUbicacionAIncidente(Integer incidenteId, String ubicacionJson) {
        // 1. Llamar al microservicio para crear la dirección
        DireccionDTO direccionCreada = geolocalizacionClient.subirUbicacion(ubicacionJson);

        // 2. Obtener el incidente
        Incidente incidente = findById(incidenteId);

        // 3. Asignar el ID de la dirección al incidente
        incidente.setIdDireccion(direccionCreada.getIdDireccion());

        // 4. Guardar y retornar
        return incidenteRepository.save(incidente);
    }

    /**
     * Asigna una Dirección (Ubicación) a un incidente existente mediante IDs.
     * @param incidenteId ID del incidente
     * @param idDireccion ID de la Dirección a asignar
     */
    public void asignarDireccion(
            Integer incidenteId,
            Integer idDireccion) {
        Incidente incidente = findById(incidenteId);
        validarExistenciaReferencia(geolocalizacionClient, idDireccion, "Direccion");
        incidente.setIdDireccion(idDireccion);
        incidenteRepository.save(incidente);
    }

    // --- MÉTODOS PRIVADOS DE VALIDACIÓN ---

    /**
     * Método genérico para validar la existencia de una referencia externa (DTO/Entidad) por ID.
     * Se espera que el client.findById() retorne la entidad o DTO, o null si no existe.
     * @param client El cliente Feign para la entidad externa.
     * @param id El ID a buscar.
     * @param entityName Nombre de la entidad para el mensaje de error.
     * @throws NoSuchElementException Si el objeto no es encontrado (retorna null).
     */
    private void validarExistenciaReferencia(Object client, Integer id, String entityName) {
        Object found = null;
        if (client instanceof EstadoClient) {
            found = ((EstadoClient) client).findById(id);
        } else if (client instanceof GeolocalizacionClient) {
            found = ((GeolocalizacionClient) client).findById(id);
        } else if (client instanceof UsuarioClient) {
            // El UsuarioClient se usa tanto para Ciudadano como para Usuario Asignado
            found = ((UsuarioClient) client).findById(id);
        }
        // Eliminado: else if (client instanceof EquipoClient) { ... }

        if (found == null) {
            throw new NoSuchElementException(entityName + " no encontrado con ID: " + id);
        }
    }

    /**
     * Valida la existencia de todos los IDs de referencia requeridos
     * para la creación de un nuevo incidente.
     * @param incidente El incidente a validar.
     * @throws IllegalArgumentException Si falta un ID obligatorio.
     * @throws NoSuchElementException Si una entidad relacionada no existe.
     */
    private void validarExistenciaReferencias(Incidente incidente) {
        // Validación local: TipoIncidente (requiere la Entidad completa)
        if (incidente.getTipoIncidente() == null || incidente.getTipoIncidente().getIdTipoIncidente() == null) {
            throw new IllegalArgumentException("El Tipo de Incidente es obligatorio.");
        }
        tipoIncidenteService.findById(incidente.getTipoIncidente().getIdTipoIncidente()); // Lanza NoSuchElementException si no existe

        // Validaciones externas (uso de IDs lógicos):
        if (incidente.getIdEstadoIncidente() == null) {
            throw new IllegalArgumentException("El ID del Estado es obligatorio.");
        }
        validarExistenciaReferencia(estadoClient, incidente.getIdEstadoIncidente(), "Estado");

        if (incidente.getIdCiudadano() == null) {
            throw new IllegalArgumentException("El ID del Ciudadano es obligatorio.");
        }
        validarExistenciaReferencia(usuarioClient, incidente.getIdCiudadano(), "Ciudadano");

        if (incidente.getIdDireccion() == null) {
            throw new IllegalArgumentException("El ID de la Dirección es obligatorio.");
        }
        validarExistenciaReferencia(geolocalizacionClient, incidente.getIdDireccion(), "Direccion");

        // MODIFICADO: UsuarioAsignadoID es opcional al crear
        if (incidente.getIdUsuarioAsignado() != null) {
            validarExistenciaReferencia(usuarioClient, incidente.getIdUsuarioAsignado(), "Usuario Asignado");
        }
    }

    /**
     * Realiza la validación de un Incidente antes de ser guardado por primera vez.
     * @param incidente El incidente a validar.
     */
    private void validarIncidente(Incidente incidente) {
        if (incidente.getTitulo() == null || incidente.getTitulo().isBlank()) {
            throw new IllegalArgumentException("El Título no puede estar vacío.");
        }
        if (incidente.getDetalle() == null || incidente.getDetalle().isBlank()) {
            throw new IllegalArgumentException("El Detalle no puede estar vacío.");
        }
        // Validar Longitudes
        if (incidente.getTitulo().length() > 50) {
            throw new IllegalArgumentException("El Título no puede exceder 50 caracteres.");
        }
        if (incidente.getDetalle().length() > 400) {
            throw new IllegalArgumentException("El Detalle no puede exceder 400 caracteres.");
        }
        // Validar fecha de registro (aunque se espera que se setee en la capa de controlador o DTO)
        if (incidente.getFechaRegistro() == null) {
            throw new IllegalArgumentException("La fecha de registro es obligatoria.");
        }
    }

    /**
     * Realiza la validación de un Incidente durante una actualización.
     * Solo valida si los campos no son nulos.
     * @param incidente El incidente con datos de actualización.
     */
    private void validarActualizacion(Incidente incidente) {
        if (incidente.getTitulo() != null && incidente.getTitulo().length() > 50) {
            throw new IllegalArgumentException("El Título no puede exceder 50 caracteres.");
        }
        if (incidente.getDetalle() != null && incidente.getDetalle().length() > 400) {
            throw new IllegalArgumentException("El Detalle no puede exceder 400 caracteres.");
        }
    }


    // MÉTODOS DE ASIGNACIÓN DE RELACIONES POR ID (Para endpoints dedicados)

    /**
     * Asigna un Ciudadano a un incidente existente mediante IDs.
     * @param incidenteId ID del incidente
     * @param idCiudadano ID del Ciudadano a asignar
     */
    public void asignarCiudadano(
            Integer incidenteId,
            Integer idCiudadano) {
        Incidente incidente = findById(incidenteId);
        validarExistenciaReferencia(usuarioClient, idCiudadano, "Ciudadano");
        incidente.setIdCiudadano(idCiudadano);
        incidenteRepository.save(incidente);
    }

    /**
     * Asigna un Tipo de Incidente a un incidente existente mediante IDs.
     * @param incidenteId ID del incidente
     * @param tipoIncidenteId ID del tipo de incidente a asignar
     */
    public void asignarTipoIncidente(
            Integer incidenteId,
            Integer tipoIncidenteId) {
        Incidente incidente = findById(incidenteId);
        // Repositorio JPA, usa Optional y orElseThrow
        TipoIncidente tipoIncidente = tipoIncidenteRepository.findById(tipoIncidenteId)
                .orElseThrow(() -> new NoSuchElementException("Tipo Incidente no encontrado con ID: " + tipoIncidenteId));
        incidente.setTipoIncidente(tipoIncidente);
        incidenteRepository.save(incidente);
    }

    /**
     * Asigna un Estado de incidente a un incidente existente mediante IDs.
     * @param incidenteId ID del incidente
     * @param idEstadoIncidente ID del Estado a asignar
     */
    public void asignarEstadoIncidente(
            Integer incidenteId,
            Integer idEstadoIncidente) {
        Incidente incidente = findById(incidenteId);
        validarExistenciaReferencia(estadoClient, idEstadoIncidente, "Estado");
        incidente.setIdEstadoIncidente(idEstadoIncidente);
        incidenteRepository.save(incidente);
    }

    /**
     * Asigna un Usuario responsable/asignado a un incidente existente mediante IDs.
     * @param incidenteId ID del incidente
     * @param idUsuarioAsignado ID del Usuario a asignar
     */
    public void asignarUsuarioAsignado(
            Integer incidenteId,
            Integer idUsuarioAsignado) {
        Incidente incidente = findById(incidenteId);
        validarExistenciaReferencia(usuarioClient, idUsuarioAsignado, "Usuario Asignado");
        incidente.setIdUsuarioAsignado(idUsuarioAsignado);
        incidenteRepository.save(incidente);
    }
}