package com.SAFE_Rescue.API_Comunicacion;

import com.SAFE_Rescue.API_Comunicacion.modelo.Conversacion;
import com.SAFE_Rescue.API_Comunicacion.modelo.Mensaje;
import com.SAFE_Rescue.API_Comunicacion.modelo.ParticipanteConversacion; // Necesario para los participantes
import com.SAFE_Rescue.API_Comunicacion.repository.ConversacionRepository;
import com.SAFE_Rescue.API_Comunicacion.repository.MensajeRepository;
import com.SAFE_Rescue.API_Comunicacion.repository.ParticipanteConversacionRepository; // Necesario para guardar participantes
import net.datafaker.Faker;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional; // Necesario si la entidad Conversacion usa FetchType.LAZY

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;
import java.util.concurrent.TimeUnit;

@Profile("dev") // Este DataLoader solo se ejecutará cuando el perfil activo sea "dev"
@Component
public class DataLoader implements CommandLineRunner {

    // Asumimos que los IDs de usuario van de 1 a 20 para simulación.
    private static final int MIN_USER_ID = 1;
    private static final int MAX_USER_ID = 10;

    @Autowired
    private ConversacionRepository conversacionRepository;

    @Autowired
    private MensajeRepository mensajeRepository;

    @Autowired
    private ParticipanteConversacionRepository participanteConvRepository; // 👈 Repositorio de Participantes inyectado

    @Override
    @Transactional // Agregamos Transaccional si Conversacion o Mensaje tienen inicialización perezosa (Lazy)
    public void run(String... args) throws Exception {
        System.out.println("DataLoader para API_Comunicacion está en ejecución...");

        Faker faker = new Faker();
        Random random = new Random();

        List<Conversacion> conversacionesPersistidas = new ArrayList<>();

        // --- 1. Carga de Hilos de Conversacion ---
        if (conversacionRepository.count() == 0) {
            System.out.println("Cargando datos iniciales para Conversaciones (hilos)...");

            // Tipos de conversación a simular
            String[] tiposConversacion = {"Privada", "Grupo", "Emergencia"};

            for (int i = 0; i < 8; i++) {
                Conversacion conversacion = new Conversacion();
                String tipo = tiposConversacion[random.nextInt(tiposConversacion.length)];

                conversacion.setTipo(tipo);

                // Si es grupo o emergencia, le damos un nombre visible
                if (!"Privada".equals(tipo)) {
                    conversacion.setNombre(faker.company().name() + " Chat");
                } else {
                    // Para conversaciones privadas, el nombre es nulo
                    conversacion.setNombre(null);
                }

                conversacion.setFechaCreacion(LocalDateTime.now().minusDays(random.nextInt(30)));

                try {
                    conversacionesPersistidas.add(conversacionRepository.save(conversacion));
                } catch (Exception e) {
                    System.err.println("Error al guardar Conversacion: " + e.getMessage());
                }
            }
            System.out.println("Carga de " + conversacionesPersistidas.size() + " hilos de Conversacion completada.");
        } else {
            System.out.println("La base de datos ya contiene Conversaciones. No se cargarán datos iniciales.");
            // Si ya hay conversaciones, las cargamos para poder añadir mensajes.
            conversacionesPersistidas = conversacionRepository.findAll();
        }

        // -----------------------------------------------------
        // --- 2. Carga de Participantes de la Conversación ---
        // -----------------------------------------------------
        if (!conversacionesPersistidas.isEmpty() && participanteConvRepository.count() == 0) {
            System.out.println("Cargando Participantes de Conversación...");
            int participantesTotales = 0;

            for (Conversacion conversacion : conversacionesPersistidas) {
                // Usamos un Set para asegurar IDs de usuario únicos por conversación
                Set<Integer> idsParticipantesUnicos = new HashSet<>();

                // Definir cuántos participantes tendrá: 2 para privadas, 3 o 4 para grupo/emergencia
                int minParticipantes = conversacion.getTipo().equals("Privada") ? 2 : 3;
                int maxParticipantes = conversacion.getTipo().equals("Privada") ? 2 : 4;
                int numParticipantes = faker.number().numberBetween(minParticipantes, maxParticipantes + 1);

                while (idsParticipantesUnicos.size() < numParticipantes) {
                    idsParticipantesUnicos.add(faker.number().numberBetween(MIN_USER_ID, MAX_USER_ID + 1));
                }

                for (Integer idUsuario : idsParticipantesUnicos) {
                    ParticipanteConversacion participante = new ParticipanteConversacion();
                    participante.setConversacion(conversacion);
                    participante.setIdUsuario(idUsuario);
                    // Asignamos una fecha de unión cercana a la fecha de creación de la conversación
                    participante.setFechaUnion(conversacion.getFechaCreacion().plusHours(faker.number().numberBetween(1, 24)));

                    try {
                        participanteConvRepository.save(participante);
                        participantesTotales++;
                    } catch (Exception e) {
                        System.err.println("Error al guardar ParticipanteConv: " + e.getMessage());
                    }
                }
            }
            System.out.println("Carga de " + participantesTotales + " Participantes de Conversación completada.");
        } else if (participanteConvRepository.count() > 0) {
            System.out.println("La base de datos ya contiene ParticipantesConv. No se cargarán datos iniciales.");
        }


        // --------------------------------------------------------------------
        // --- 3. Carga de Mensajes para cada Conversacion (Lógica simplificada) ---
        // --------------------------------------------------------------------
        if (mensajeRepository.count() == 0 && !conversacionesPersistidas.isEmpty()) {
            System.out.println("Cargando datos iniciales para Mensajes y vinculándolos a hilos...");

            int mensajesTotales = 0;

            for (Conversacion conversacion : conversacionesPersistidas) {
                // Obtener los IDs de los participantes que acabamos de crear
                List<ParticipanteConversacion> participantesConv = participanteConvRepository.findByConversacion_IdConversacion(conversacion.getIdConversacion());
                if (participantesConv.isEmpty()) continue; // Salta si no tiene participantes

                List<Integer> idsParticipantes = participantesConv.stream()
                        .map(ParticipanteConversacion::getIdUsuario)
                        .toList();

                // Generar entre 3 y 8 mensajes por cada conversación
                int numMensajes = random.nextInt(6) + 3;

                for (int j = 0; j < numMensajes; j++) {
                    Mensaje mensaje = new Mensaje();

                    // --- Vínculo Clave ---
                    mensaje.setConversacion(conversacion);

                    // --- Simulación de Emisor/Receptor ---
                    // Seleccionamos un emisor de la lista de participantes válidos
                    int idEmisor = idsParticipantes.get(j % idsParticipantes.size());
                    mensaje.setIdUsuarioEmisor(idEmisor);

                    // --- Contenido y Estado ---
                    String detalleGenerado = faker.lorem().paragraph(random.nextInt(3) + 1);
                    mensaje.setDetalle(detalleGenerado.length() > 2000 ? detalleGenerado.substring(0, 2000) : detalleGenerado);

                    // Asignamos el estado: 1=Enviado, 2=Leído, 3=Error (por ejemplo)
                    mensaje.setIdEstado(faker.number().numberBetween(1, 3));

                    // Aseguramos que la fecha del mensaje sea posterior a la fecha de creación de la conversación
                    mensaje.setFechaCreacion(conversacion.getFechaCreacion().plusHours(faker.number().numberBetween(25, 100)));


                    try {
                        mensajeRepository.save(mensaje);
                        mensajesTotales++;
                    } catch (Exception e) {
                        System.err.println("Error al guardar Mensaje en la conversación " + conversacion.getIdConversacion() + ": " + e.getMessage());
                    }
                }
            }
            System.out.println("Carga de " + mensajesTotales + " Mensajes completada.");

        } else if (mensajeRepository.count() > 0) {
            System.out.println("La base de datos ya contiene Mensajes. No se cargarán datos iniciales.");
        }

        System.out.println("DataLoader para API_Comunicacion finalizado.");
    }
}