package com.SAFE_Rescue.API_Perfiles;

import com.SAFE_Rescue.API_Perfiles.modelo.*;
import com.SAFE_Rescue.API_Perfiles.repositoy.*;
import net.datafaker.Faker;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.*;
import java.util.concurrent.ExecutionException;

/**
 * Componente de carga de datos de prueba para el entorno "dev".
 * Simula la persistencia de entidades nativas y la referencia a DTOs de microservicios externos
 * mediante IDs (Claves Foráneas Lógicas).
 * * Corrección aplicada: Se valida que idDireccion no sea nulo al crear Compañías.
 */
@Profile("dev")
@Component
public class DataLoader implements CommandLineRunner {

    // Repositorios locales inyectados
    @Autowired private UsuarioRepository usuarioRepository;
    @Autowired private BomberoRepository bomberoRepository;
    @Autowired private TipoUsuarioRepository tipoUsuarioRepository;
    @Autowired private TipoEquipoRepository tipoEquipoRepository;
    @Autowired private EquipoRepository equipoRepository;
    @Autowired private CompaniaRepository companiaRepository;

    // WebClients para APIs externas inyectados (asumidos desde la configuración)
    @Autowired private WebClient estadoWebClient;

    @Autowired
    @Qualifier("geolocalizacionWebClient")
    private WebClient direccionWebClient;

    // @Autowired private WebClient fotoWebClient;

    private final Faker faker = new Faker(new Locale("es"));
    private final Set<String> uniqueRuns = new HashSet<>();
    private final Set<String> uniqueTelefonos = new HashSet<>();
    private final Set<String> uniqueCorreos = new HashSet<>();

    private static final String BOMBERO_TIPO = "Bombero en Terreno";
    private static final String OPERADOR_TIPO = "Operador de Sala";

    @Override
    public void run(String... args) {
        System.out.println("Cargando datos de prueba...");

        try {
            // 1. Cargar catálogos locales
            List<TipoUsuario> tiposUsuario = crearTiposUsuario();
            List<TipoEquipo> tiposEquipo = crearTiposEquipo();

            // 2. Obtener DTOs de APIs externas
            List<EstadoDTO> estadoDTOS = obtenerEntidadesExternas(estadoWebClient, "", EstadoDTO.class);
            List<DireccionDTO> direccionDTOS = obtenerEntidadesExternas(direccionWebClient, "", DireccionDTO.class);

            // Verificación mínima de pre-requisitos
            if (tiposUsuario.isEmpty() || tiposEquipo.isEmpty() || estadoDTOS.isEmpty()) {
                System.err.println("Error: No se pudieron obtener entidades de catálogo o estados. Deteniendo la carga.");
                return;
            }

            if (direccionDTOS.isEmpty()) {
                System.err.println("Advertencia: No se pudieron obtener Direcciones de la API externa. Se usarán IDs simulados para idDireccion.");
            }

            // 3. Crear Entidades NATIVAS: Compania (usa idDireccion externo o simulado)
            List<Compania> companias = crearCompanias(direccionDTOS);

            if (companias.isEmpty()) {
                System.err.println("Error: Fallo al crear entidades Compania. Deteniendo la carga.");
                return;
            }

            // 4. Generar Equipos (usa idEstado externo y Compania nativa)
            List<Equipo> equipos = crearEquipos(tiposEquipo, companias, estadoDTOS);

            // 5. Generar Usuarios y Bomberos (usan idEstado y idFoto externos, y Equipo nativo)
            crearUsuarios(tiposUsuario, estadoDTOS, equipos);

            System.out.println("Carga de datos finalizada.");
        } catch (Exception e) {
            System.err.println("Un error inesperado ocurrió durante la carga de datos: " + e.getMessage());
            e.printStackTrace();
        }
    }

    // --- Métodos para crear entidades locales ---

    private List<TipoUsuario> crearTiposUsuario() {
        if (tipoUsuarioRepository.count() > 0) return tipoUsuarioRepository.findAll();
        List<String> nombres = Arrays.asList("Jefe de Compañía", BOMBERO_TIPO, OPERADOR_TIPO, "Administrador", "Ciudadano");
        List<TipoUsuario> tiposUsuario = new ArrayList<>();
        for (String nombre : nombres) {
            TipoUsuario tipo = new TipoUsuario();
            tipo.setNombre(nombre);
            tiposUsuario.add(tipoUsuarioRepository.save(tipo));
        }
        return tiposUsuario;
    }

    private List<TipoEquipo> crearTiposEquipo() {
        if (tipoEquipoRepository.count() > 0) return tipoEquipoRepository.findAll();
        List<String> nombres = Arrays.asList(
                "Médico", "Administrativo", "Forestales", "Rescate Urbano",
                "Materiales Peligrosos", "Alturas", "Subacuático", "Logístico"
        );
        List<TipoEquipo> tiposEquipo = new ArrayList<>();
        for (String nombre : nombres) {
            TipoEquipo tipo = new TipoEquipo();
            tipo.setNombre(nombre);
            tiposEquipo.add(tipoEquipoRepository.save(tipo));
        }
        return tiposEquipo;
    }

    /**
     * Crea entidades nativas Compania, asociándoles el ID de un DireccionDTO.
     * ** CORRECCIÓN APLICADA AQUÍ **
     */
    private List<Compania> crearCompanias(List<DireccionDTO> direccionDTOS) {
        if (companiaRepository.count() > 0) return companiaRepository.findAll();

        List<Compania> companias = new ArrayList<>();
        int numCompanias = 10;
        int direccionDTOSize = direccionDTOS.size();

        for (int i = 0; i < numCompanias; i++) {
            Compania compania = new Compania();

            compania.setNombre(faker.company().name());
            compania.setCodigo(faker.code().asin());

            // Conversión de java.util.Date a java.time.LocalDate
            java.util.Date pastDate = Date.from(faker.timeAndDate().past(20, java.util.concurrent.TimeUnit.DAYS));
            compania.setFechaFundacion(pastDate.toInstant().atZone(ZoneId.systemDefault()).toLocalDate());

            // ASIGNACIÓN DE CLAVE FORÁNEA LÓGICA
            Integer direccionId;

            if (direccionDTOSize > 0) {
                // 1. Intentar obtener el ID del DTO externo
                DireccionDTO direccionDTO = direccionDTOS.get(i % direccionDTOSize);
                direccionId = direccionDTO.getId();

                // 2. CORRECCIÓN: Si el ID del DTO es nulo, usar un ID de fallback.
                if (direccionId == null) {
                    direccionId = 0 + i; // Fallback: usar un ID simulado alto.
                    System.err.println("WARN: DireccionDTO del microservicio retornó ID nulo. Usando ID de fallback: " + direccionId);
                }
            } else {
                // 3. Si la lista de DTOs está vacía, usar IDs simulados para cumplir la restricción NOT NULL.
                direccionId = 1000 + i;
            }

            // Asignar el ID (garantizado que no es nulo)
            compania.setIdDireccion(direccionId);

            companias.add(companiaRepository.save(compania));
        }
        return companias;
    }

    /**
     * Crea entidades Equipo, utilizando entidades nativas Compania y el ID de Estado.
     */
    private List<Equipo> crearEquipos(List<TipoEquipo> tiposEquipo, List<Compania> companias, List<EstadoDTO> estadoDTOS) {
        if (equipoRepository.count() > 0) return equipoRepository.findAll();

        List<Equipo> equipos = new ArrayList<>();
        int estadoDTOSize = estadoDTOS.size();

        for (int i = 0; i < 5; i++) {
            Equipo equipo = new Equipo();
            equipo.setNombre(faker.team().name());
            equipo.setTipoEquipo(tiposEquipo.get(faker.random().nextInt(tiposEquipo.size())));

            // RELACIÓN INTERNA: Usa la entidad Compania
            equipo.setCompania(companias.get(faker.random().nextInt(companias.size())));

            // CLAVE FORÁNEA LÓGICA: ID del Estado
            if (estadoDTOSize > 0) {
                EstadoDTO estadoDTO = estadoDTOS.get(faker.random().nextInt(estadoDTOSize));
                equipo.setIdEstado(estadoDTO.getId() != null ? estadoDTO.getId() : 1); // Fallback si el ID de estado es nulo
            } else {
                equipo.setIdEstado(1); // Fallback: ID de estado por defecto
            }

            equipos.add(equipoRepository.save(equipo));
        }
        return equipos;
    }

    /**
     * Método para crear usuarios y bomberos, asignando IDs de Estado y Foto.
     */
    private void crearUsuarios(List<TipoUsuario> tiposUsuario, List<EstadoDTO> estadoDTOS, List<Equipo> equipos) {
        if (usuarioRepository.count() > 0) return;

        int estadoDTOSize = estadoDTOS.size();

        for (TipoUsuario tipo : tiposUsuario) {
            int cantidad = 2; // Crear 2 usuarios de cada tipo
            for (int i = 0; i < cantidad; i++) {
                Usuario usuario;

                if (tipo.getNombre().equalsIgnoreCase(BOMBERO_TIPO) || tipo.getNombre().equalsIgnoreCase(OPERADOR_TIPO)) {
                    usuario = new Bombero();
                    if (!equipos.isEmpty()) {
                        ((Bombero) usuario).setEquipo(equipos.get(faker.random().nextInt(equipos.size())));
                    }
                } else {
                    usuario = new Usuario();
                }

                // Asignar atributos base
                usuario.setRun(crearRunUnico());
                usuario.setDv(calcularDv(usuario.getRun()));
                usuario.setNombre(faker.name().firstName());
                usuario.setAPaterno(faker.name().lastName());
                usuario.setAMaterno(faker.name().lastName());

                // Conversión de java.util.Date a java.time.LocalDate
                java.util.Date pastDate = Date.from(faker.timeAndDate().past(5, java.util.concurrent.TimeUnit.DAYS));
                usuario.setFechaRegistro(pastDate.toInstant().atZone(ZoneId.systemDefault()).toLocalDate());

                usuario.setTelefono(crearTelefonoUnico());
                usuario.setCorreo(crearCorreoUnico());
                usuario.setContrasenia("password123");
                usuario.setIntentosFallidos(0);
                usuario.setRazonBaneo(null);
                usuario.setDiasBaneo(null);
                usuario.setTipoUsuario(tipo);

                // CLAVE FORÁNEA LÓGICA: ID del Estado
                if (estadoDTOSize > 0) {
                    EstadoDTO estadoDTO = estadoDTOS.get(faker.random().nextInt(estadoDTOSize));
                    usuario.setIdEstado(estadoDTO.getId() != null ? estadoDTO.getId() : 1); // Fallback si el ID es nulo
                } else {
                    usuario.setIdEstado(1); // Fallback: ID de estado por defecto
                }

                // CLAVE FORÁNEA LÓGICA: ID de la Foto (simulado con un ID aleatorio)
                if (faker.bool().bool()) {
                    usuario.setIdFoto(faker.number().numberBetween(1, 100));
                } else {
                    usuario.setIdFoto(null);
                }

                // Persistir con el repositorio correcto
                if (usuario instanceof Bombero) {
                    bomberoRepository.save((Bombero) usuario);
                } else {
                    usuarioRepository.save(usuario);
                }
            }
        }
    }


    // --- Métodos de utilidad (sin cambios) ---

    private String crearRunUnico() {
        String run;
        do {
            run = faker.number().digits(8);
        } while (!uniqueRuns.add(run));
        return run;
    }

    private String crearTelefonoUnico() {
        String telefono;
        do {
            telefono = "9" + faker.number().digits(8);
        } while (!uniqueTelefonos.add(telefono));
        return telefono;
    }

    private String crearCorreoUnico() {
        String correo;
        do {
            correo = faker.internet().emailAddress();
        } while (!uniqueCorreos.add(correo));
        return correo;
    }

    /**
     * Método genérico para obtener datos de APIs externas.
     */
    private <T extends IHasId> List<T> obtenerEntidadesExternas(WebClient client, String uri, Class<T> clazz) {
        try {
            return client.get()
                    .uri(uri)
                    .retrieve()
                    .bodyToFlux(clazz)
                    .collectList()
                    .toFuture()
                    .get();
        } catch (WebClientResponseException e) {
            System.err.println("Error en la respuesta de la API para " + uri + ": " + e.getStatusCode() + " - " + e.getResponseBodyAsString());
            return Collections.emptyList();
        } catch (InterruptedException | ExecutionException e) {
            System.err.println("Error de conexión o inesperado al obtener datos de la API: " + uri + " - " + e.getMessage());
            return Collections.emptyList();
        }
    }

    private String calcularDv(String runStr) {
        int run = Integer.parseInt(runStr);
        int suma = 0;
        int multiplicador = 2;

        while (run > 0) {
            suma += (run % 10) * multiplicador;
            run /= 10;
            multiplicador = (multiplicador == 7) ? 2 : multiplicador + 1;
        }

        int dv = 11 - (suma % 11);
        if (dv == 11) return "0";
        if (dv == 10) return "K";
        return String.valueOf(dv);
    }
}