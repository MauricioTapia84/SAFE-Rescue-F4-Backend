package com.SAFE_Rescue.API_Comunicacion.service;

import com.SAFE_Rescue.API_Comunicacion.modelo.Conversacion;
import com.SAFE_Rescue.API_Comunicacion.repository.ConversacionRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.data.domain.Sort;

import java.util.List;
import java.util.NoSuchElementException;

/**
 * Servicio que implementa la lógica de negocio para la gestión de la entidad Conversacion.
 * Se encarga de la creación, consulta y eliminación de los hilos de conversación,
 * sin gestionar directamente los participantes (que se manejan en otra entidad).
 */
@Service
public class ConversacionService {

    private final ConversacionRepository conversacionRepository;

    /**
     * Constructor para inyección de dependencias.
     */
    @Autowired
    public ConversacionService(ConversacionRepository conversacionRepository) {
        this.conversacionRepository = conversacionRepository;
    }

    /**
     * Crea un nuevo hilo de conversación.
     * La fecha de creación se asigna automáticamente mediante @PrePersist en la entidad.
     *
     * @param tipo El tipo de conversación (ej: 'Privada', 'Grupo', 'Emergencia').
     * @param nombre El nombre opcional para la conversación (ej: para grupos). Puede ser null.
     * @return La conversación creada.
     */
    @Transactional
    public Conversacion iniciarNuevaConversacion(String tipo, String nombre) {
        if (tipo == null || tipo.trim().isEmpty()) {
            throw new IllegalArgumentException("El tipo de conversación es obligatorio.");
        }

        Conversacion nuevaConversacion = new Conversacion();
        nuevaConversacion.setTipo(tipo);
        nuevaConversacion.setNombre(nombre);
        // NOTA: La fecha de creación se asigna automáticamente en la entidad.

        return conversacionRepository.save(nuevaConversacion);
    }

    /**
     * Obtiene una lista de todas las conversaciones en el sistema.
     * @return Una lista de objetos Conversacion ordenados por fecha de creación descendente.
     */
    public List<Conversacion> findAll() {
        // Ordenar por la fecha de creación descendente (más reciente primero)
        return conversacionRepository.findAll(Sort.by(Sort.Direction.DESC, "fechaCreacion"));
    }

    /**
     * Busca una conversación por su identificador único.
     * @param idConversacion ID de la conversación a buscar.
     * @return El objeto Conversacion si es encontrado.
     * @throws NoSuchElementException Si la conversación no se encuentra.
     */
    public Conversacion findById(Integer idConversacion) {
        return conversacionRepository.findById(idConversacion)
                .orElseThrow(() -> new NoSuchElementException("Conversación no encontrada con ID: " + idConversacion));
    }

    /**
     * Obtiene una lista de conversaciones por su tipo.
     * @param tipo Tipo de conversación a buscar.
     * @return Lista de conversaciones del tipo especificado.
     */
    public List<Conversacion> findByTipo(String tipo) {
        // Se asume que este método existe en ConversacionRepository.
        return conversacionRepository.findByTipo(tipo);
    }

    /**
     * Elimina una conversación por su identificador único.
     * Gracias a CascadeType.ALL y orphanRemoval, esto eliminará todos los mensajes asociados.
     * ADVERTENCIA: La eliminación de la conversación también podría necesitar
     * gestionar la eliminación de los registros de participantes asociados en tu otra entidad.
     *
     * @param idConversacion ID de la conversación a eliminar.
     * @throws NoSuchElementException Si la conversación no se encuentra.
     */
    @Transactional
    public void delete(Integer idConversacion) {
        Conversacion conversacion = findById(idConversacion); // Valida la existencia
        conversacionRepository.delete(conversacion);
    }
}