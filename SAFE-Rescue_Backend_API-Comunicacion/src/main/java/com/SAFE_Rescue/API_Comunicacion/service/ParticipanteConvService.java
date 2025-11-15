package com.SAFE_Rescue.API_Comunicacion.service;

import com.SAFE_Rescue.API_Comunicacion.config.UsuarioClient; // 👈 Importamos el cliente de Usuarios
import com.SAFE_Rescue.API_Comunicacion.modelo.Conversacion;
import com.SAFE_Rescue.API_Comunicacion.modelo.ParticipanteConversacion;
import com.SAFE_Rescue.API_Comunicacion.repository.ParticipanteConversacionRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.NoSuchElementException;

/**
 * Servicio que gestiona la asignación y desasignación de participantes a las conversaciones.
 * Utiliza el patrón de Many-to-Many a través de la entidad ParticipanteConversacion.
 * Ahora utiliza UsuarioClient para validar la existencia del usuario antes de la asignación.
 */
@Service
public class ParticipanteConvService {

    private final ParticipanteConversacionRepository participanteConvRepository;
    private final ConversacionService conversacionService;
    private final UsuarioClient usuarioClient; // 👈 Declaramos el cliente

    @Autowired
    public ParticipanteConvService(
            ParticipanteConversacionRepository participanteConvRepository,
            ConversacionService conversacionService,
            UsuarioClient usuarioClient) { // 👈 Inyectamos el cliente
        this.participanteConvRepository = participanteConvRepository;
        this.conversacionService = conversacionService;
        this.usuarioClient = usuarioClient; // 👈 Asignamos el cliente
    }

    /**
     * Asigna un usuario/entidad a una conversación existente.
     *
     * @param idConversacion ID de la conversación a la que unir.
     * @param idUsuario ID del participante a unir.
     * @return La nueva asignación de ParticipanteConversacion.
     * @throws NoSuchElementException Si la conversación no existe.
     * @throws IllegalStateException Si el usuario ya está unido a la conversación.
     * @throws IllegalArgumentException Si el ID de usuario no existe en el microservicio de Usuarios.
     */
    @Transactional
    public ParticipanteConversacion unirParticipanteAConversacion(Integer idConversacion, Integer idUsuario) {
        // 1. Validar que la conversación exista
        Conversacion conversacion = conversacionService.findById(idConversacion);

        // 2. Validar que el usuario exista en el microservicio de Usuarios
        if (usuarioClient.findById(idUsuario).isEmpty()) {
            throw new IllegalArgumentException("Usuario con ID " + idUsuario + " no encontrado en el microservicio de Usuarios. No se puede unir a la conversación.");
        }

        // 3. Verificar si el usuario ya es miembro
        if (participanteConvRepository.findByIdUsuarioAndConversacion_IdConversacion(idUsuario, idConversacion).isPresent()) {
            throw new IllegalStateException("El usuario con ID " + idUsuario + " ya es un participante en la conversación " + idConversacion);
        }

        // 4. Crear la nueva asignación
        ParticipanteConversacion nuevoParticipante = new ParticipanteConversacion();
        nuevoParticipante.setConversacion(conversacion);
        nuevoParticipante.setIdUsuario(idUsuario);

        return participanteConvRepository.save(nuevoParticipante);
    }

    /**
     * Desasigna (elimina) un participante de una conversación específica.
     *
     * @param idConversacion ID de la conversación.
     * @param idUsuario ID del participante a desasignar.
     * @throws NoSuchElementException Si la asignación participante-conversación no existe.
     */
    @Transactional
    public void eliminarParticipanteDeConversacion(Integer idConversacion, Integer idUsuario) {
        // En este caso, no es estrictamente necesario validar el idUsuario con el cliente,
        // ya que la excepción se lanzará si la asignación local (participanteConvRepository) no existe.

        // 1. Verificar si la asignación existe
        participanteConvRepository.findByIdUsuarioAndConversacion_IdConversacion(idUsuario, idConversacion)
                .orElseThrow(() -> new NoSuchElementException("El participante con ID " + idUsuario + " no está asociado a la conversación " + idConversacion));

        // 2. Eliminar la asignación
        participanteConvRepository.deleteByIdUsuarioAndConversacion_IdConversacion(idUsuario, idConversacion);
    }

    /**
     * Obtiene todos los participantes de una conversación dada.
     *
     * @param idConversacion ID de la conversación.
     * @return Lista de objetos ParticipanteConversacion (que incluyen el idUsuario).
     * @throws NoSuchElementException Si la conversación no existe.
     */
    public List<ParticipanteConversacion> findParticipantesByConversacion(Integer idConversacion) {
        // Opcionalmente, validar que la conversación exista (a través del servicio)
        conversacionService.findById(idConversacion);

        return participanteConvRepository.findByConversacion_IdConversacion(idConversacion);
    }

    /**
     * Obtiene todas las conversaciones en las que participa un usuario/entidad dado.
     *
     * @param idUsuario ID del participante.
     * @return Lista de asignaciones ParticipanteConversacion (ordenadas por fecha de unión).
     */
    public List<ParticipanteConversacion> findConversacionesByParticipante(Integer idUsuario) {
        return participanteConvRepository.findByIdUsuarioOrderByFechaUnionDesc(idUsuario);
    }
}