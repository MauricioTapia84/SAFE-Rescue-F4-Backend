package com.SAFE_Rescue.API_Comunicacion.service;

import com.SAFE_Rescue.API_Comunicacion.modelo.Conversacion;
import com.SAFE_Rescue.API_Comunicacion.modelo.Mensaje;
import com.SAFE_Rescue.API_Comunicacion.repository.ConversacionRepository;
import com.SAFE_Rescue.API_Comunicacion.repository.MensajeRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional; // Usa la anotación de Spring
import org.springframework.data.domain.Sort;

import java.time.LocalDateTime; // Usa LocalDateTime
import java.util.List;
import java.util.NoSuchElementException;

/**
 * Servicio que implementa la lógica de negocio para la gestión de Mensajes.
 * Permite crear, obtener y eliminar mensajes, y gestionar la relación con Conversaciones.
 */
@Service
public class MensajeService {

    private final MensajeRepository mensajeRepository;
    private final ConversacionRepository conversacionRepository;

    /**
     * Constructor para inyección de dependencias.
     */
    @Autowired
    public MensajeService(MensajeRepository mensajeRepository, ConversacionRepository conversacionRepository) {
        this.mensajeRepository = mensajeRepository;
        this.conversacionRepository = conversacionRepository;
    }

    /**
     * Crea un nuevo mensaje y lo asocia a una conversación existente.
     * La fecha de creación se asigna automáticamente en la entidad Mensaje (@PrePersist).
     *
     * @param conversacionId ID de la conversación a la que pertenece el mensaje.
     * @param nuevoMensaje Objeto Mensaje con el detalle, emisor y estado.
     * @return El mensaje creado y guardado.
     * @throws NoSuchElementException Si la conversación no es encontrada.
     * @throws IllegalArgumentException Si falta algún campo obligatorio.
     */
    @Transactional
    public Mensaje createMessage(Integer conversacionId, Mensaje nuevoMensaje) {
        // 1. Buscar y validar la conversación
        Conversacion conversacion = conversacionRepository.findById(conversacionId)
                .orElseThrow(() -> new NoSuchElementException("Conversación con ID " + conversacionId + " no encontrada."));

        // 2. Validaciones adicionales (se complementan con @NotNull y @NotBlank en la entidad)
        if (nuevoMensaje.getIdUsuarioEmisor() == null) {
            throw new IllegalArgumentException("El ID del usuario emisor es obligatorio.");
        }
        if (nuevoMensaje.getIdEstado() == null) {
            // Nota: Es crucial definir el estado inicial (ej. 'Enviado', ID 1)
            throw new IllegalArgumentException("El ID del estado del mensaje es obligatorio (ej: 'Enviado').");
        }

        // 3. Vincular la entidad
        nuevoMensaje.setConversacion(conversacion);

        // 4. Guardar el mensaje (la fecha de creación se asigna en @PrePersist)
        return mensajeRepository.save(nuevoMensaje);
    }

    /**
     * Obtiene una lista de todos los mensajes, ordenados por fecha de creación descendente.
     * @return Una lista de objetos Mensaje.
     */
    public List<Mensaje> findAll() {
        // Ordenar por la fecha de creación para mostrar lo más reciente primero
        return mensajeRepository.findAll(Sort.by(Sort.Direction.DESC, "fechaCreacion"));
    }

    /**
     * Obtiene todos los mensajes pertenecientes a una conversación específica.
     * @param conversacionId ID de la conversación.
     * @return Lista de mensajes ordenados cronológicamente.
     */
    public List<Mensaje> findMessagesByConversation(Integer conversacionId) {
        // Validación de existencia de conversación
        if (!conversacionRepository.existsById(conversacionId)) {
            throw new NoSuchElementException("Conversación con ID " + conversacionId + " no encontrada.");
        }
        // Buscar mensajes ordenados por fecha de creación (ascendente, cronológico)
        return mensajeRepository.findByConversacionIdConversacion(conversacionId);
    }

    /**
     * Busca un mensaje por su identificador único.
     * @param idMensaje ID del mensaje a buscar.
     * @return El objeto Mensaje si es encontrado.
     * @throws NoSuchElementException Si el mensaje no se encuentra.
     */
    public Mensaje findById(Integer idMensaje) {
        return mensajeRepository.findById(idMensaje)
                .orElseThrow(() -> new NoSuchElementException("Mensaje no encontrado con ID: " + idMensaje));
    }

    /**
     * Elimina un mensaje por su identificador único.
     * @param idMensaje ID del mensaje a eliminar.
     * @throws NoSuchElementException Si el mensaje no se encuentra.
     */
    @Transactional
    public void delete(Integer idMensaje) {
        Mensaje mensaje = mensajeRepository.findById(idMensaje)
                .orElseThrow(() -> new NoSuchElementException("Mensaje no encontrado con ID: " + idMensaje + " para eliminar."));

        // Se podría agregar lógica de auditoría aquí antes de la eliminación
        mensajeRepository.delete(mensaje);
    }
}