package com.SAFE_Rescue.API_Incidentes;

import com.SAFE_Rescue.API_Incidentes.config.*;
import com.SAFE_Rescue.API_Incidentes.modelo.*;
import com.SAFE_Rescue.API_Incidentes.repository.*;
import net.datafaker.Faker;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClientResponseException;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.Random;

/**
 * Componente de carga de datos de prueba para el entorno "dev" de la API de Incidentes.
 * Consume datos de DTOs existentes de APIs externas a través de sus clientes
 * y carga entidades locales (TipoIncidente, Incidente) utilizando solo los IDs (Claves Foráneas Lógicas).
 */
@Profile("dev")
@Component
public class DataLoader implements CommandLineRunner {

    // Repositorios locales inyectados
    @Autowired private TipoIncidenteRepository tipoIncidenteRepository;
    @Autowired private IncidenteRepository incidenteRepository;

    // CLIENTES DEDICADOS PARA APIS EXTERNAS
    // ------------------------------------
    @Autowired private EstadoClient estadoClient;
    @Autowired private GeolocalizacionClient geolocalizacionClient;
    @Autowired private UsuarioClient usuarioClient;
    // El EquipoClient ha sido eliminado.
    // ------------------------------------

    private final Faker faker = new Faker(new Locale("es"));
    private final Random random = new Random();

    // Listas para almacenar los DTOs obtenidos de las APIs externas (solo necesitamos sus IDs)
    private List<UsuarioDTO> usuariosExistentes;
    private List<EstadoDTO> estadosExistentes;
    private List<DireccionDTO> direccionesExistentes;

    @Override
    public void run(String... args) {
        System.out.println("Cargando datos de prueba para la API de Incidentes...");

        try {
            // 1. Obtener DTOs de APIs externas
            obtenerDTOsExternos();

            if (!validarDependencias()) {
                System.err.println("Error: Falta información crucial de alguna API externa. Deteniendo la carga de incidentes.");
                return;
            }

            // 2. Cargar catálogos locales: TipoIncidente
            List<TipoIncidente> tiposIncidentes = crearTiposIncidente();

            if (tiposIncidentes.isEmpty()) {
                System.err.println("Error: No se pudieron crear los Tipos de Incidente. Deteniendo la carga.");
                return;
            }

            // 3. Generar Incidentes utilizando los DTOs obtenidos
            crearIncidentes(tiposIncidentes);

            System.out.println("Carga de datos de Incidentes finalizada. Total de incidentes: " + incidenteRepository.count());

        } catch (WebClientResponseException e) {
            System.err.println("Error de respuesta de API durante la carga: " + e.getStatusCode() + " - " + e.getResponseBodyAsString());
            e.printStackTrace();
        } catch (Exception e) {
            System.err.println("Un error inesperado ocurrió durante la carga de datos: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Llama a todos los clientes para obtener las listas de DTOs existentes.
     */
    private void obtenerDTOsExternos() {
        System.out.println("-> Consumiendo API externa: Estados...");
        estadosExistentes = estadoClient.getAllEstados();

        System.out.println("-> Consumiendo API externa: Direcciones...");
        direccionesExistentes = geolocalizacionClient.getAllDirecciones();

        System.out.println("-> Consumiendo API externa: Usuarios (Ciudadanos y Recursos)...");
        usuariosExistentes = usuarioClient.findAll();

        // Se eliminó la llamada a equipoClient
    }

    /**
     * Valida que las listas de DTOs obtenidas no estén vacías o nulas.
     */
    private boolean validarDependencias() {
        boolean valid = true;

        if (estadosExistentes == null || estadosExistentes.isEmpty()) {
            System.err.println("ADVERTENCIA: No se obtuvieron Estados de la API externa. (Fatal para Incidentes)");
            valid = false;
        }
        if (direccionesExistentes == null || direccionesExistentes.isEmpty()) {
            System.err.println("ADVERTENCIA: No se obtuvieron Direcciones de la API externa. (Fatal para Incidentes)");
            valid = false;
        }
        if (usuariosExistentes == null || usuariosExistentes.isEmpty()) {
            System.err.println("ADVERTENCIA: No se obtuvieron Usuarios (Ciudadanos/Asignados) de la API externa. (Fatal para Incidentes)");
            valid = false;
        }
        // Se eliminó la validación de equipos
        return valid;
    }

    /**
     * Crea y persiste las entidades locales TipoIncidente.
     */
    private List<TipoIncidente> crearTiposIncidente() {
        if (tipoIncidenteRepository.count() > 0) return tipoIncidenteRepository.findAll();

        List<String[]> tiposData = Arrays.asList(
                new String[]{"Incendio Estructural", "Fuego en una edificación, casa o fábrica."},
                new String[]{"Emergencia Médica", "Requiere soporte vital básico o avanzado."},
                new String[]{"Accidente Vehicular", "Colisión o volcamiento de vehículos."},
                new String[]{"Rescate Animal", "Animal atrapado o herido en altura/espacio confinado."},
                new String[]{"Escape de Gas", "Fuga de gas que representa peligro inminente."}
        );

        List<TipoIncidente> tiposIncidentes = new ArrayList<>();
        for (String[] data : tiposData) {
            TipoIncidente tipo = new TipoIncidente();
            tipo.setNombre(data[0]);
            tiposIncidentes.add(tipoIncidenteRepository.save(tipo));
        }
        System.out.println("-> " + tiposIncidentes.size() + " Tipos de Incidente cargados.");
        return tiposIncidentes;
    }

    /**
     * Crea y persiste los Incidentes, asignando relaciones locales y IDs lógicos de APIs externas.
     */
    private void crearIncidentes(List<TipoIncidente> tiposIncidentes) {
        if (incidenteRepository.count() > 0) return;

        // Utilizamos listas no nulas garantizadas por validarDependencias()
        int numUsuarios = usuariosExistentes.size();
        int numDirecciones = direccionesExistentes.size();
        int numEstados = estadosExistentes.size();

        // Generar 10 incidentes aleatorios
        for (int i = 0; i < 10; i++) {
            Incidente inc = new Incidente();
            inc.setTitulo(faker.company().buzzword());
            inc.setDetalle(faker.lorem().paragraph());
            inc.setFechaRegistro(LocalDateTime.from(LocalDateTime.now().minusHours(random.nextInt(100)))); // Fecha de registro reciente

            // --- ASIGNACIONES DE RELACIONES ---

            // 1. Relación JPA Local (TipoIncidente)
            inc.setTipoIncidente(tiposIncidentes.get(random.nextInt(tiposIncidentes.size())));

            // 2. Claves Foráneas Lógicas (IDs) de Microservicios:
            // Ciudadano/Usuario que reporta (MANDATORIO)
            inc.setIdCiudadano(usuariosExistentes.get(random.nextInt(numUsuarios)).getIdUsuario());

            // Dirección/Ubicación (MANDATORIO)
            inc.setIdDireccion(direccionesExistentes.get(random.nextInt(numDirecciones)).getIdDireccion());

            // Estado (MANDATORIO)
            inc.setIdEstadoIncidente(estadosExistentes.get(random.nextInt(numEstados)).getIdEstado());

            // Usuario Asignado / Recurso (OPCIONAL)
            // Se asigna un usuario al 50% de los incidentes
            if (random.nextBoolean()) {
                inc.setIdUsuarioAsignado(usuariosExistentes.get(random.nextInt(numUsuarios)).getIdUsuario());
            } else {
                inc.setIdUsuarioAsignado(null);
            }

            // Se elimina la asignación de idEquipo
            // inc.setIdEquipo(null); // No es necesario si ya se eliminó del modelo. Si no lo has eliminado, déjalo.

            incidenteRepository.save(inc);
        }
        System.out.println("-> 10 Incidentes de prueba cargados.");
    }
}
