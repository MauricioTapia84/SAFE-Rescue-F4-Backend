package com.SAFE_Rescue.API_Incidentes;

import com.SAFE_Rescue.API_Incidentes.config.*;
import com.SAFE_Rescue.API_Incidentes.dto.DireccionDTO;
import com.SAFE_Rescue.API_Incidentes.dto.EstadoDTO;
import com.SAFE_Rescue.API_Incidentes.dto.UsuarioDTO;
import com.SAFE_Rescue.API_Incidentes.modelo.*;
import com.SAFE_Rescue.API_Incidentes.repository.*;
import com.SAFE_Rescue.API_Incidentes.service.HistorialIncidenteService; // IMPORTADO
import net.datafaker.Faker;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClientResponseException;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.Random;

@Profile("dev")
@Component
public class DataLoader implements CommandLineRunner {

    @Autowired private TipoIncidenteRepository tipoIncidenteRepository;
    @Autowired private IncidenteRepository incidenteRepository;

    @Autowired private EstadoClient estadoClient;
    @Autowired private GeolocalizacionClient geolocalizacionClient;
    @Autowired private UsuarioClient usuarioClient;

    @Autowired private HistorialIncidenteService historialService; // INYECTADO

    private final Faker faker = new Faker(new Locale("es"));
    private final Random random = new Random();

    private List<UsuarioDTO> usuariosExistentes;
    private List<EstadoDTO> estadosExistentes;
    private List<DireccionDTO> direccionesExistentes;

    @Override
    public void run(String... args) {
        System.out.println("Cargando datos de prueba para la API de Incidentes...");

        try {
            obtenerDTOsExternos();

            if (!validarDependencias()) {
                System.err.println("Error: Falta información crucial de alguna API externa.");
                return;
            }

            List<TipoIncidente> tiposIncidentes = crearTiposIncidente();

            if (tiposIncidentes.isEmpty()) {
                System.err.println("Error: No se pudieron crear los Tipos de Incidente.");
                return;
            }

            crearIncidentes(tiposIncidentes);

            System.out.println("Carga de datos finalizada. Incidentes: " + incidenteRepository.count());

        } catch (WebClientResponseException e) {
            System.err.println("Error API externa: " + e.getStatusCode());
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void obtenerDTOsExternos() {
        System.out.println("-> Consumiendo APIs externas...");
        estadosExistentes = estadoClient.getAllEstados();
        direccionesExistentes = geolocalizacionClient.getAllDirecciones();
        usuariosExistentes = usuarioClient.findAll();
    }

    private boolean validarDependencias() {
        return estadosExistentes != null && !estadosExistentes.isEmpty() &&
                direccionesExistentes != null && !direccionesExistentes.isEmpty() &&
                usuariosExistentes != null && !usuariosExistentes.isEmpty();
    }

    private List<TipoIncidente> crearTiposIncidente() {
        if (tipoIncidenteRepository.count() > 0) return tipoIncidenteRepository.findAll();

        List<String[]> tiposData = Arrays.asList(
                new String[]{"Incendio Estructural", "Fuego en edificación."},
                new String[]{"Emergencia Médica", "Requiere soporte vital."},
                new String[]{"Accidente Vehicular", "Colisión de vehículos."},
                new String[]{"Rescate Animal", "Animal atrapado."},
                new String[]{"Escape de Gas", "Fuga peligrosa."}
        );

        List<TipoIncidente> tiposIncidentes = new ArrayList<>();
        for (String[] data : tiposData) {
            TipoIncidente tipo = new TipoIncidente();
            tipo.setNombre(data[0]);
            tiposIncidentes.add(tipoIncidenteRepository.save(tipo));
        }
        return tiposIncidentes;
    }

    private void crearIncidentes(List<TipoIncidente> tiposIncidentes) {
        if (incidenteRepository.count() > 0) return;

        int numUsuarios = usuariosExistentes.size();
        int numDirecciones = direccionesExistentes.size();
        int numEstados = estadosExistentes.size();

        // Crear 10 incidentes
        for (int i = 0; i < 10; i++) {
            Incidente inc = new Incidente();
            inc.setTitulo(faker.company().buzzword());
            inc.setDetalle(faker.lorem().paragraph());
            inc.setFechaRegistro(LocalDateTime.from(LocalDateTime.now().minusHours(random.nextInt(48)))); // Hasta 48h atrás

            inc.setTipoIncidente(tiposIncidentes.get(random.nextInt(tiposIncidentes.size())));
            inc.setIdCiudadano(usuariosExistentes.get(random.nextInt(numUsuarios)).getIdUsuario());
            inc.setIdDireccion(direccionesExistentes.get(random.nextInt(numDirecciones)).getIdDireccion());
            inc.setIdEstadoIncidente(estadosExistentes.get(random.nextInt(numEstados)).getIdEstado());

            if (random.nextBoolean()) {
                inc.setIdUsuarioAsignado(usuariosExistentes.get(random.nextInt(numUsuarios)).getIdUsuario());
            }

            Incidente guardado = incidenteRepository.save(inc);

            // SIMULACIÓN DE HISTORIAL para el último incidente creado (i==9)
            // Hacemos que este incidente tenga una historia de evolución
            if (i == 9) {
                // 1. Simulamos que nació en Estado 1 (Activo)
                // 2. Pasó a Estado 5 (Localizado) hace 2 horas
                historialService.registrarCambioEstado(guardado, 1, 5, "Incidente localizado por central.");

                // 3. Pasó a Estado 4 (En Proceso) hace 1 hora (Actualizamos el incidente real también)
                guardado.setIdEstadoIncidente(4);
                guardado.setIdUsuarioAsignado(usuariosExistentes.get(0).getIdUsuario()); // Asignamos al primer usuario
                incidenteRepository.save(guardado); // Guardamos el estado actual

                historialService.registrarCambioEstado(guardado, 5, 4, "Unidad despachada al lugar.");
            }
        }
        System.out.println("-> 10 Incidentes generados (con historial de prueba en el último).");
    }
}