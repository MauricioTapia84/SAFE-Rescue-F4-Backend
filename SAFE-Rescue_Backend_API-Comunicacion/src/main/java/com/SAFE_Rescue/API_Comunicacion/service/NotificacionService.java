package com.SAFE_Rescue.API_Comunicacion.service;

import com.SAFE_Rescue.API_Comunicacion.modelo.Conversacion; // Importar si se usa
import com.SAFE_Rescue.API_Comunicacion.modelo.Notificacion;
import com.SAFE_Rescue.API_Comunicacion.repository.NotificacionRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.NoSuchElementException;

/**
 * Servicio que gestiona la lógica de negocio para las Notificaciones.
 * Opera con el String idUsuarioReceptor y el Integer idEstado para la persistencia.
 * Los IDs de estado están basados en la tabla 'estado' proporcionada:
 * 8 = Recibido (Pendiente/No Leída), 9 = Visto (Leída).
 */
@Service
public class NotificacionService {

    private final NotificacionRepository notificacionRepository;

    // Se obtienen las constantes del Repositorio para asegurar la consistencia con la DB
    private static final Integer ESTADO_PENDIENTE = NotificacionRepository.ESTADO_PENDIENTE; // ID 8 (Recibido)
    private static final Integer ESTADO_LEIDA = NotificacionRepository.ESTADO_LEIDA;       // ID 9 (Visto)

    @Autowired
    public NotificacionService(NotificacionRepository notificacionRepository) {
        this.notificacionRepository = notificacionRepository;
    }

    /**
     * Crea y registra una nueva notificación estableciendo su estado inicial como Pendiente (8).
     *
     * @param idUsuarioReceptor ID del usuario receptor (String).
     * @param detalle Contenido textual de la notificación.
     * @param conversacion La Conversacion asociada a la notificación.
     * @return La notificación persistida.
     */
    @Transactional
    public Notificacion crearNotificacion(String idUsuarioReceptor, String detalle, Conversacion conversacion) {
        Notificacion nuevaNotificacion = new Notificacion();
        nuevaNotificacion.setIdUsuarioReceptor(idUsuarioReceptor);
        nuevaNotificacion.setDetalle(detalle);
        nuevaNotificacion.setConversacion(conversacion);
        nuevaNotificacion.setIdEstado(ESTADO_PENDIENTE); // Estado inicial: 8 (Recibido)

        return notificacionRepository.save(nuevaNotificacion);
    }

    /**
     * Recupera las notificaciones Pendientes (idEstado = 8) para un usuario específico, con paginación.
     *
     * @param idUsuarioReceptor ID del usuario receptor (String).
     * @param pageable Configuración de paginación y ordenamiento.
     * @return Una página de objetos Notificacion.
     */
    public Page<Notificacion> cargarNotificacionesPendientes(String idUsuarioReceptor, Pageable pageable) {
        return notificacionRepository.findByIdUsuarioReceptorAndIdEstadoEqualsOrderByFechaCreacionDesc(
                idUsuarioReceptor,
                ESTADO_PENDIENTE, // Filtra por estado 8
                pageable
        );
    }

    /**
     * Marca una notificación específica como Leída (idEstado = 9).
     *
     * @param idNotificacion ID de la notificación.
     * @return La notificación actualizada.
     * @throws NoSuchElementException Si la notificación no existe.
     */
    @Transactional
    public Notificacion marcarComoLeida(Integer idNotificacion) {
        Notificacion notificacion = notificacionRepository.findById(idNotificacion)
                .orElseThrow(() -> new NoSuchElementException("Notificación no encontrada con ID: " + idNotificacion));

        if (!notificacion.getIdEstado().equals(ESTADO_LEIDA)) {
            notificacion.setIdEstado(ESTADO_LEIDA); // Cambiar a estado 9 (Visto)
            notificacion = notificacionRepository.save(notificacion);
        }
        return notificacion;
    }

    /**
     * Marca todas las notificaciones pendientes de un usuario como Leídas (idEstado = 9).
     *
     * @param idUsuarioReceptor ID del usuario.
     * @return El número de notificaciones que fueron marcadas como leídas.
     */
    @Transactional
    public int marcarTodasComoLeidas(String idUsuarioReceptor) {
        return notificacionRepository.actualizarEstadoPorUsuario(
                idUsuarioReceptor,
                ESTADO_LEIDA,      // Nuevo estado: 9 (Visto)
                ESTADO_PENDIENTE   // Estado a actualizar: 8 (Recibido)
        );
    }
}