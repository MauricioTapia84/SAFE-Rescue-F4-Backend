package com.SAFE_Rescue.API_Incidentes.service;

import com.SAFE_Rescue.API_Incidentes.dto.DireccionDTO;
import com.SAFE_Rescue.API_Incidentes.dto.EstadoDTO;
import com.SAFE_Rescue.API_Incidentes.dto.UsuarioDTO;
import com.SAFE_Rescue.API_Incidentes.modelo.*;
import com.SAFE_Rescue.API_Incidentes.repository.*;
import com.SAFE_Rescue.API_Incidentes.config.*;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.NoSuchElementException;

@Service
@Transactional
public class IncidenteService {

    @Autowired private IncidenteRepository incidenteRepository;
    @Autowired private TipoIncidenteRepository tipoIncidenteRepository;
    @Autowired private EstadoClient estadoClient;
    @Autowired private GeolocalizacionClient geolocalizacionClient;
    @Autowired private UsuarioClient usuarioClient;
    @Autowired private TipoIncidenteService tipoIncidenteService;

    @Autowired @Lazy private HistorialIncidenteService historialIncidenteService;

    public List<Incidente> findAll() {
        return incidenteRepository.findAll();
    }

    public Incidente findById(Integer id) {
        return incidenteRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("No se encontró Incidente con ID: " + id));
    }

    public Incidente save(Incidente incidente) {
        try {
            validarIncidente(incidente);
            validarExistenciaReferencias(incidente);
            incidente.setFechaUltimaActualizacion(LocalDateTime.now());
            return incidenteRepository.save(incidente);
        } catch (NoSuchElementException e) {
            throw new IllegalArgumentException("Error de asignación: " + e.getMessage(), e);
        } catch (Exception e) {
            throw new RuntimeException("Error al guardar el incidente: " + e.getMessage(), e);
        }
    }

    public Incidente update(Incidente incidenteDatosNuevos, Integer id) {
        if (incidenteDatosNuevos == null) throw new IllegalArgumentException("El incidente no puede ser nulo");

        Incidente incidenteExistente = findById(id);
        Integer estadoActualId = incidenteExistente.getIdEstadoIncidente();

        try {
            validarActualizacion(incidenteDatosNuevos);

            // DETECCIÓN DE CAMBIOS
            Integer nuevoEstadoId = incidenteDatosNuevos.getIdEstadoIncidente();
            boolean cambioEstado = nuevoEstadoId != null && !nuevoEstadoId.equals(estadoActualId);

            Integer nuevoAsignadoId = incidenteDatosNuevos.getIdUsuarioAsignado();
            boolean cambioAsignado = nuevoAsignadoId != null && !nuevoAsignadoId.equals(incidenteExistente.getIdUsuarioAsignado());

            String nuevoTitulo = incidenteDatosNuevos.getTitulo();
            boolean cambioTitulo = nuevoTitulo != null && !nuevoTitulo.equals(incidenteExistente.getTitulo());

            String nuevoDetalle = incidenteDatosNuevos.getDetalle();
            boolean cambioDetalle = nuevoDetalle != null && !nuevoDetalle.equals(incidenteExistente.getDetalle());

            // APLICAR CAMBIOS
            if (cambioTitulo) incidenteExistente.setTitulo(nuevoTitulo);
            if (cambioDetalle) incidenteExistente.setDetalle(nuevoDetalle);

            if (incidenteDatosNuevos.getIdDireccion() != null) {
                validarExistenciaReferencia(geolocalizacionClient, incidenteDatosNuevos.getIdDireccion(), "Direccion");
                incidenteExistente.setIdDireccion(incidenteDatosNuevos.getIdDireccion());
            }
            if (incidenteDatosNuevos.getIdCiudadano() != null) {
                validarExistenciaReferencia(usuarioClient, incidenteDatosNuevos.getIdCiudadano(), "Ciudadano");
                incidenteExistente.setIdCiudadano(incidenteDatosNuevos.getIdCiudadano());
            }

            String nombreNuevoEstado = null;
            if (cambioEstado) {
                EstadoDTO estadoDto = (EstadoDTO) validarExistenciaReferencia(estadoClient, nuevoEstadoId, "Estado");
                nombreNuevoEstado = estadoDto.getNombre();
                incidenteExistente.setIdEstadoIncidente(nuevoEstadoId);
            }

            String nombreNuevoUsuario = null;
            if (cambioAsignado) {
                UsuarioDTO usuarioDto = (UsuarioDTO) validarExistenciaReferencia(usuarioClient, nuevoAsignadoId, "Usuario Asignado");
                nombreNuevoUsuario = (usuarioDto.getNombre() != null) ? usuarioDto.getNombre() : "Usuario " + nuevoAsignadoId;
                incidenteExistente.setIdUsuarioAsignado(nuevoAsignadoId);
            }

            if (incidenteDatosNuevos.getTipoIncidente() != null && incidenteDatosNuevos.getTipoIncidente().getIdTipoIncidente() != null) {
                TipoIncidente tipoEncontrado = tipoIncidenteRepository.findById(incidenteDatosNuevos.getTipoIncidente().getIdTipoIncidente())
                        .orElseThrow(() -> new NoSuchElementException("Tipo Incidente no encontrado"));
                incidenteExistente.setTipoIncidente(tipoEncontrado);
            }

            incidenteExistente.setFechaUltimaActualizacion(LocalDateTime.now());
            Incidente incidenteGuardado = incidenteRepository.save(incidenteExistente);

            // HISTORIALES
            if (cambioEstado) {
                String msg = (nombreNuevoEstado != null) ? "Se cambió el estado a: " + nombreNuevoEstado : "Se cambió el estado a ID: " + nuevoEstadoId;
                historialIncidenteService.registrarCambioEstado(incidenteGuardado, estadoActualId, nuevoEstadoId, msg);
                estadoActualId = nuevoEstadoId;
            }
            if (cambioAsignado) {
                String msg = (nombreNuevoUsuario != null) ? "Incidente asignado a: " + nombreNuevoUsuario : "Incidente asignado a usuario ID: " + nuevoAsignadoId;
                historialIncidenteService.registrarCambioEstado(incidenteGuardado, estadoActualId, estadoActualId, msg);
            }
            if (cambioTitulo) {
                historialIncidenteService.registrarCambioEstado(incidenteGuardado, estadoActualId, estadoActualId, "Se cambió el título a: " + nuevoTitulo);
            }
            if (cambioDetalle) {
                historialIncidenteService.registrarCambioEstado(incidenteGuardado, estadoActualId, estadoActualId, "Se actualizó la descripción del incidente.");
            }

            return incidenteGuardado;
        } catch (Exception e) {
            throw new RuntimeException("Error al actualizar incidente: " + e.getMessage(), e);
        }
    }

    public void delete(Integer id) {
        incidenteRepository.delete(findById(id));
    }

    public Incidente agregarUbicacionAIncidente(Integer incidenteId, String ubicacionJson) {
        DireccionDTO direccionCreada = geolocalizacionClient.subirUbicacion(ubicacionJson);
        Incidente incidente = findById(incidenteId);
        incidente.setIdDireccion(direccionCreada.getIdDireccion());
        incidente.setFechaUltimaActualizacion(LocalDateTime.now());
        return incidenteRepository.save(incidente);
    }

    public void asignarDireccion(Integer incidenteId, Integer idDireccion) {
        Incidente incidente = findById(incidenteId);
        validarExistenciaReferencia(geolocalizacionClient, idDireccion, "Direccion");
        incidente.setIdDireccion(idDireccion);
        incidente.setFechaUltimaActualizacion(LocalDateTime.now());
        incidenteRepository.save(incidente);
    }

    private Object validarExistenciaReferencia(Object client, Integer id, String entityName) {
        Object found = null;
        if (client instanceof EstadoClient) found = ((EstadoClient) client).findById(id);
        else if (client instanceof GeolocalizacionClient) found = ((GeolocalizacionClient) client).findById(id);
        else if (client instanceof UsuarioClient) found = ((UsuarioClient) client).findById(id);

        if (found == null) throw new NoSuchElementException(entityName + " no encontrado con ID: " + id);
        return found;
    }

    private void validarExistenciaReferencias(Incidente incidente) {
        if (incidente.getTipoIncidente() == null || incidente.getTipoIncidente().getIdTipoIncidente() == null) throw new IllegalArgumentException("Tipo Incidente obligatorio");
        tipoIncidenteService.findById(incidente.getTipoIncidente().getIdTipoIncidente());

        if (incidente.getIdEstadoIncidente() == null) throw new IllegalArgumentException("ID Estado obligatorio");
        validarExistenciaReferencia(estadoClient, incidente.getIdEstadoIncidente(), "Estado");

        if (incidente.getIdCiudadano() == null) throw new IllegalArgumentException("ID Ciudadano obligatorio");
        validarExistenciaReferencia(usuarioClient, incidente.getIdCiudadano(), "Ciudadano");

        if (incidente.getIdDireccion() == null) throw new IllegalArgumentException("ID Dirección obligatorio");
        validarExistenciaReferencia(geolocalizacionClient, incidente.getIdDireccion(), "Direccion");

        if (incidente.getIdUsuarioAsignado() != null) {
            validarExistenciaReferencia(usuarioClient, incidente.getIdUsuarioAsignado(), "Usuario Asignado");
        }
    }

    private void validarIncidente(Incidente incidente) {
        if (incidente.getTitulo() == null || incidente.getTitulo().isBlank()) throw new IllegalArgumentException("Título vacío");
        if (incidente.getDetalle() == null || incidente.getDetalle().isBlank()) throw new IllegalArgumentException("Detalle vacío");
        if (incidente.getFechaRegistro() == null) throw new IllegalArgumentException("Fecha registro obligatoria");
    }

    private void validarActualizacion(Incidente incidente) {
        if (incidente.getTitulo() != null && incidente.getTitulo().length() > 50) throw new IllegalArgumentException("Título muy largo");
        if (incidente.getDetalle() != null && incidente.getDetalle().length() > 400) throw new IllegalArgumentException("Detalle muy largo");
    }

    public void asignarCiudadano(Integer i, Integer c) { asignarGenerico(i, c, usuarioClient, "Ciudadano", (inc, id) -> inc.setIdCiudadano(id)); }
    public void asignarTipoIncidente(Integer i, Integer t) {
        Incidente inc = findById(i);
        inc.setTipoIncidente(tipoIncidenteRepository.findById(t).orElseThrow());
        inc.setFechaUltimaActualizacion(LocalDateTime.now());
        incidenteRepository.save(inc);
    }
    public void asignarEstadoIncidente(Integer i, Integer e) { asignarGenerico(i, e, estadoClient, "Estado", (inc, id) -> inc.setIdEstadoIncidente(id)); }
    public void asignarUsuarioAsignado(Integer i, Integer u) { asignarGenerico(i, u, usuarioClient, "Usuario Asignado", (inc, id) -> inc.setIdUsuarioAsignado(id)); }

    private interface Setter { void set(Incidente i, Integer id); }
    private void asignarGenerico(Integer iId, Integer refId, Object client, String name, Setter setter) {
        Incidente inc = findById(iId);
        validarExistenciaReferencia(client, refId, name);
        setter.set(inc, refId);
        inc.setFechaUltimaActualizacion(LocalDateTime.now());
        incidenteRepository.save(inc);
    }
}