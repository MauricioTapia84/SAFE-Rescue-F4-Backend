package com.SAFE_Rescue.API_Perfiles.service;

import com.SAFE_Rescue.API_Perfiles.modelo.Equipo;
import com.SAFE_Rescue.API_Perfiles.modelo.HistorialUsuario;
import com.SAFE_Rescue.API_Perfiles.modelo.Usuario;
import com.SAFE_Rescue.API_Perfiles.repositoy.HistorialUsuarioRepository;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Servicio encargado de gestionar y registrar los cambios de estado de los usuarios
 * y equipos en la tabla de historial (auditoría), así como de proveer la consulta
 * de dichos registros.
 */
@Service
public class HistorialUsuarioService {

    private final HistorialUsuarioRepository historialUsuarioRepository;
    // Necesario para validar si el perfil (usuario o equipo) existe
    private final UsuarioService usuarioService;
    private final EquipoService equipoService; // NUEVO: Necesario para validar si el equipo existe

    @Autowired
    public HistorialUsuarioService(
            HistorialUsuarioRepository historialUsuarioRepository,
            UsuarioService usuarioService,
            EquipoService equipoService) { // Inyección del nuevo servicio
        this.historialUsuarioRepository = historialUsuarioRepository;
        this.usuarioService = usuarioService;
        this.equipoService = equipoService;
    }

    public List<HistorialUsuario> findAll() {
        return historialUsuarioRepository.findAll();
    }

    // =========================================================================
    // REGISTRO DE CAMBIOS DE ESTADO
    // =========================================================================

    /**
     * Crea y persiste un nuevo registro de historial de cambio de estado para un USUARIO.
     * Este método es para uso INTERNO (llamado desde UsuarioService).
     */
    public HistorialUsuario registrarCambioEstado(
            Usuario usuario,
            Integer idEstadoAnterior,
            Integer idEstadoNuevo,
            String detalle) {

        if (usuario == null || idEstadoAnterior == null || idEstadoNuevo == null || detalle == null || detalle.trim().isEmpty()) {
            throw new IllegalArgumentException("No se puede registrar el historial para Usuario: Faltan datos obligatorios.");
        }

        HistorialUsuario registro = new HistorialUsuario();
        registro.setUsuario(usuario); // Setea el usuario
        registro.setEquipo(null);    // Asegura que el equipo sea nulo
        registro.setIdEstadoAnterior(idEstadoAnterior);
        registro.setIdEstadoNuevo(idEstadoNuevo);
        registro.setDetalle(detalle);
        registro.setFechaHistorial(LocalDateTime.now());

        return historialUsuarioRepository.save(registro);
    }

    /**
     * Crea y persiste un nuevo registro de historial de cambio de estado para un EQUIPO.
     * Este método es para uso INTERNO (llamado desde EquipoService).
     */
    public HistorialUsuario registrarCambioEstado(
            Equipo equipo, // Sobrecarga para Equipo
            Integer idEstadoAnterior,
            Integer idEstadoNuevo,
            String detalle) {

        if (equipo == null || idEstadoAnterior == null || idEstadoNuevo == null || detalle == null || detalle.trim().isEmpty()) {
            throw new IllegalArgumentException("No se puede registrar el historial para Equipo: Faltan datos obligatorios.");
        }

        HistorialUsuario registro = new HistorialUsuario();
        registro.setEquipo(equipo);  // Setea el equipo
        registro.setUsuario(null);   // Asegura que el usuario sea nulo
        registro.setIdEstadoAnterior(idEstadoAnterior);
        registro.setIdEstadoNuevo(idEstadoNuevo);
        registro.setDetalle(detalle);
        registro.setFechaHistorial(LocalDateTime.now());

        return historialUsuarioRepository.save(registro);
    }


    // =========================================================================
    // CONSULTA DEL HISTORIAL
    // =========================================================================

    /**
     * Obtiene todo el historial de cambios de estado para un USUARIO específico.
     *
     * @param idUsuario El ID del usuario cuyo historial se desea consultar.
     * @return Lista de HistorialUsuario ordenados por fecha descendente.
     * @throws EntityNotFoundException Si el usuario no existe.
     */
    public List<HistorialUsuario> obtenerHistorialPorUsuario(Integer idUsuario) {
        // Asumiendo que findById devuelve un Optional o null si no existe
        if (usuarioService.findById(idUsuario) == null) {
            throw new EntityNotFoundException("Usuario con ID " + idUsuario + " no encontrado.");
        }

        return historialUsuarioRepository.findByUsuarioIdUsuario(idUsuario);
    }

    /**
     * Obtiene todo el historial de cambios de estado para un EQUIPO específico.
     *
     * @param idEquipo El ID del equipo cuyo historial se desea consultar.
     * @return Lista de HistorialUsuario ordenados por fecha descendente.
     * @throws EntityNotFoundException Si el equipo no existe.
     */
    public List<HistorialUsuario> obtenerHistorialPorEquipo(Integer idEquipo) {
        // Asumiendo que findById devuelve un Optional o null si no existe
        if (equipoService.findById(idEquipo) == null) {
            throw new EntityNotFoundException("Equipo con ID " + idEquipo + " no encontrado.");
        }

        return historialUsuarioRepository.findByEquipoIdEquipo(idEquipo);
    }
}