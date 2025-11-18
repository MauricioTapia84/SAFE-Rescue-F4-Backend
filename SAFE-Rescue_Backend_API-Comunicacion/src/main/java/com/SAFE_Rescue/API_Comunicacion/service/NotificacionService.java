package com.SAFE_Rescue.API_Comunicacion.service;

import com.SAFE_Rescue.API_Comunicacion.config.EstadoClient;
import com.SAFE_Rescue.API_Comunicacion.config.UsuarioClient;
import com.SAFE_Rescue.API_Comunicacion.modelo.Conversacion;
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
 * Utiliza UsuarioClient para validar la existencia del usuario receptor.
 */
@Service
public class NotificacionService {

    private final NotificacionRepository notificacionRepository;
    // Clientes de microservicios inyectados
    private final UsuarioClient usuarioClient;
    private final EstadoClient estadoClient; // Se inyecta por si se necesita validar en el futuro.

    // Se obtienen las constantes del Repositorio para asegurar la consistencia con la DB
    private static final Integer ESTADO_PENDIENTE = NotificacionRepository.ESTADO_PENDIENTE; // ID 8 (Recibido)
    private static final Integer ESTADO_LEIDA = NotificacionRepository.ESTADO_LEIDA;       // ID 9 (Visto)

    @Autowired
    public NotificacionService(NotificacionRepository notificacionRepository,
                               UsuarioClient usuarioClient,
                               EstadoClient estadoClient) {
        this.notificacionRepository = notificacionRepository;
        this.usuarioClient = usuarioClient;
        this.estadoClient = estadoClient;
    }

    /**
     * Crea y registra una nueva notificación estableciendo su estado inicial como Pendiente (8).
     *
     * @param idUsuarioReceptor ID del usuario receptor (String).
     * @param detalle Contenido textual de la notificación.
     * @param conversacion La Conversacion asociada a la notificación.
     * @return La notificación persistida.
     * @throws IllegalArgumentException Si el ID del usuario receptor no existe o no es numérico.
     */
    @Transactional
    public Notificacion crearNotificacion(String idUsuarioReceptor, String detalle, Conversacion conversacion) {

        Integer idReceptorInt;
        // 1. Manejo y validación del formato del ID fuera del bloque de conexión
        try {
            idReceptorInt = Integer.parseInt(idUsuarioReceptor);
        } catch (NumberFormatException e) {
            // Lanza directamente IllegalArgumentException, ya que es un error de formato de entrada.
            throw new IllegalArgumentException("ID de Usuario Receptor no válido (debe ser numérico): " + idUsuarioReceptor);
        }

        // 2. Validar la existencia del usuario receptor en el microservicio externo
        try {
            if (usuarioClient.findById(idReceptorInt).isEmpty()) {
                // Lanza directamente IllegalArgumentException, ya que el usuario no existe.
                throw new IllegalArgumentException("Usuario receptor con ID " + idUsuarioReceptor + " no encontrado en el microservicio de Usuarios.");
            }
        } catch (IllegalArgumentException e) {
            // Propagamos la excepción de negocio (usuario no encontrado) sin envolver.
            throw e;
        } catch (RuntimeException e) {
            // Captura errores de conexión (ej. Feign client o WebClient) y los envuelve en RuntimeException.
            throw new RuntimeException("Fallo al validar el Usuario Receptor con ID " + idUsuarioReceptor + " con el microservicio: " + e.getMessage(), e);
        }


        // 3. Crear la notificación
        Notificacion nuevaNotificacion = new Notificacion();
        nuevaNotificacion.setIdUsuarioReceptor(idUsuarioReceptor);
        nuevaNotificacion.setDetalle(detalle);
        nuevaNotificacion.setIdEstado(ESTADO_PENDIENTE); // Estado inicial: 8 (Recibido)

        // 4. Persistir y retornar
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