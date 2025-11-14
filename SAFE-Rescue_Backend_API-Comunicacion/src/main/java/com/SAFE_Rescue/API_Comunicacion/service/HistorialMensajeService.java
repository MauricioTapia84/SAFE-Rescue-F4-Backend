package com.SAFE_Rescue.API_Comunicacion.service;

import com.SAFE_Rescue.API_Comunicacion.modelo.Conversacion;
import com.SAFE_Rescue.API_Comunicacion.modelo.Mensaje;
import com.SAFE_Rescue.API_Comunicacion.repository.MensajeRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.NoSuchElementException;

/**
 * Servicio que gestiona el envío, recuperación (historial) y eliminación de mensajes.
 * Implementa lógica de validación de participantes y utiliza paginación para el historial.
 */
@Service
public class HistorialMensajeService {

    private final MensajeRepository mensajeRepository;
    private final ConversacionService conversacionService;
    private final ParticipanteConvService participanteConvService; // Para verificar si el remitente es miembro

    @Autowired
    public HistorialMensajeService(
            MensajeRepository mensajeRepository,
            ConversacionService conversacionService,
            ParticipanteConvService participanteConvService) {
        this.mensajeRepository = mensajeRepository;
        this.conversacionService = conversacionService;
        this.participanteConvService = participanteConvService;
    }

    /**
     * Envía y persiste un nuevo mensaje en una conversación.
     *
     * @param idConversacion ID de la conversación de destino.
     * @param idUsuarioEmisor ID del participante que envía el mensaje.
     * @param detalle Contenido textual del mensaje.
     * @param idEstado ID del estado inicial del mensaje (ej: 1 para Enviado).
     * @return El mensaje persistido.
     * @throws NoSuchElementException Si la conversación no existe.
     * @throws IllegalStateException Si el remitente no es un participante activo de la conversación.
     */
    @Transactional
    public Mensaje enviarMensaje(Integer idConversacion, Integer idUsuarioEmisor, String detalle, Integer idEstado) {
        // 1. Validar que la conversación exista
        Conversacion conversacion = conversacionService.findById(idConversacion);

        // 2. Validar que el remitente sea un participante activo de la conversación
        boolean esParticipante = participanteConvService.findParticipantesByConversacion(idConversacion).stream()
                .anyMatch(p -> p.getIdUsuario().equals(idUsuarioEmisor));

        if (!esParticipante) {
            throw new IllegalStateException("El remitente con ID " + idUsuarioEmisor + " no es un participante activo de la conversación " + idConversacion);
        }

        // 3. Crear el objeto Mensaje
        Mensaje nuevoMensaje = new Mensaje();
        nuevoMensaje.setConversacion(conversacion);
        nuevoMensaje.setIdUsuarioEmisor(idUsuarioEmisor);
        nuevoMensaje.setDetalle(detalle);
        nuevoMensaje.setIdEstado(idEstado);
        // La fecha de creación se asigna automáticamente mediante @PrePersist en la entidad.

        return mensajeRepository.save(nuevoMensaje);
    }

    /**
     * Actualiza el estado de un mensaje (ej: de Enviado a Leído).
     *
     * @param idMensaje ID del mensaje a actualizar.
     * @param nuevoIdEstado El nuevo ID del estado.
     * @return El mensaje actualizado.
     * @throws NoSuchElementException Si el mensaje no existe.
     */
    @Transactional
    public Mensaje actualizarEstadoMensaje(Integer idMensaje, Integer nuevoIdEstado) {
        Mensaje mensaje = mensajeRepository.findById(idMensaje)
                .orElseThrow(() -> new NoSuchElementException("Mensaje no encontrado con ID: " + idMensaje));

        mensaje.setIdEstado(nuevoIdEstado);
        return mensajeRepository.save(mensaje);
    }


    /**
     * Recupera el historial de mensajes de una conversación con paginación.
     * Los mensajes se ordenan por fecha de creación ascendente (los más antiguos primero).
     *
     * @param idConversacion ID de la conversación.
     * @param page Número de página (0-based).
     * @param size Número de mensajes por página.
     * @return Una página de objetos Mensaje.
     */
    public Page<Mensaje> cargarHistorial(Integer idConversacion, int page, int size) {
        // 1. Opcionalmente, validar la existencia de la conversación
        conversacionService.findById(idConversacion);

        // 2. Crear la configuración de paginación
        Pageable pageable = PageRequest.of(page, size);

        // 3. Buscar mensajes (ordenados por fecha de creación)
        return mensajeRepository.findByConversacion_IdConversacion(idConversacion, pageable);
    }

    /**
     * Elimina un mensaje por su ID.
     *
     * @param idMensaje ID del mensaje a eliminar.
     * @throws NoSuchElementException Si el mensaje no existe.
     */
    @Transactional
    public void eliminarMensaje(Integer idMensaje) {
        if (!mensajeRepository.existsById(idMensaje)) {
            throw new NoSuchElementException("Mensaje no encontrado con ID: " + idMensaje);
        }
        mensajeRepository.deleteById(idMensaje);
    }

    /**
     * Busca un mensaje específico por su ID.
     * @param idMensaje ID del mensaje.
     * @return El objeto Mensaje.
     * @throws NoSuchElementException Si el mensaje no existe.
     */
    public Mensaje findById(Integer idMensaje) {
        return mensajeRepository.findById(idMensaje)
                .orElseThrow(() -> new NoSuchElementException("Mensaje no encontrado con ID: " + idMensaje));
    }
}