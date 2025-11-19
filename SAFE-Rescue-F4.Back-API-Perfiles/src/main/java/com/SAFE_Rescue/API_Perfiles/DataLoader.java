package com.SAFE_Rescue.API_Perfiles;

import com.SAFE_Rescue.API_Perfiles.config.GeolocalizacionClient;
import com.SAFE_Rescue.API_Perfiles.config.FotoClient;
import com.SAFE_Rescue.API_Perfiles.config.EstadoClient;
import com.SAFE_Rescue.API_Perfiles.dto.DireccionDTO;
import com.SAFE_Rescue.API_Perfiles.dto.EstadoDTO;
import com.SAFE_Rescue.API_Perfiles.modelo.*;
import com.SAFE_Rescue.API_Perfiles.repository.*;
import net.datafaker.Faker;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClientResponseException;

import java.time.ZoneId;
import java.util.*;
import java.util.concurrent.TimeUnit;

/**
 * Componente de carga de datos de prueba para el entorno "dev".
 * Utiliza clientes dedicados para APIs externas (Geolocalización, Fotos, Estado)
 * con fallbacks robustos para entornos de desarrollo.
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

    // CLIENTES DEDICADOS PARA APIS EXTERNAS
    // ------------------------------------
    @Autowired private EstadoClient estadoClient;
    @Autowired private GeolocalizacionClient geolocalizacionClient;
    @Autowired private FotoClient fotoClient;
    // ------------------------------------


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
            List<EstadoDTO> estadoDTOS = obtenerEstados();
            List<DireccionDTO> direccionDTOS = geolocalizacionClient.getAllDirecciones();

            if (tiposUsuario.isEmpty() || tiposEquipo.isEmpty() || estadoDTOS == null || estadoDTOS.isEmpty()) {
                System.err.println("Error: No se pudieron obtener entidades de catálogo o estados. Deteniendo la carga.");
                return;
            }

            if (direccionDTOS == null || direccionDTOS.isEmpty()) {
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

        } catch (WebClientResponseException e) {
            System.err.println("Error de respuesta de API durante la carga: " + e.getStatusCode() + " - " + e.getResponseBodyAsString());
            e.printStackTrace();
        } catch (Exception e) {
            System.err.println("Un error inesperado ocurrió durante la carga de datos: " + e.getMessage());
            e.printStackTrace();
        }
    }

    // --- Métodos de utilidad de APIs Externas ---

    private List<EstadoDTO> obtenerEstados() {
        return estadoClient.getAllEstados();
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
     */
    private List<Compania> crearCompanias(List<DireccionDTO> direccionDTOS) {
        if (companiaRepository.count() > 0) return companiaRepository.findAll();

        List<Compania> companias = new ArrayList<>();
        int numCompanias = 10;
        int direccionDTOSize = (direccionDTOS != null) ? direccionDTOS.size() : 0;

        for (int i = 0; i < numCompanias; i++) {
            Compania compania = new Compania();

            compania.setNombre(faker.company().name());
            compania.setCodigo(faker.code().asin());

            java.util.Date pastDate = Date.from(faker.timeAndDate().past(20, java.util.concurrent.TimeUnit.DAYS));
            compania.setFechaFundacion(pastDate.toInstant().atZone(ZoneId.systemDefault()).toLocalDate());

            // ASIGNACIÓN DE CLAVE FORÁNEA LÓGICA (ID de Dirección)
            Integer direccionId;

            if (direccionDTOSize > 0) {
                DireccionDTO direccionDTO = direccionDTOS.get(i % direccionDTOSize);

                // AJUSTE: Usar getId() para acceder al ID mapeado (idDireccion)
                direccionId = direccionDTO.getIdDireccion();

                // Si el ID del DTO es nulo (después de la corrección de DTO, esto no debería pasar)
                if (direccionId == null) {
                    direccionId = 100 + i;
                    System.err.println("WARN: DireccionDTO del microservicio retornó ID nulo. Usando ID de fallback: " + direccionId);
                }
            } else {
                // Si la lista de DTOs está vacía (API down), usar IDs simulados.
                direccionId = 100 + i;
            }

            compania.setIdDireccion(direccionId);

            companias.add(companiaRepository.save(compania));
        }
        return companias;
    }

    private List<Equipo> crearEquipos(List<TipoEquipo> tiposEquipo, List<Compania> companias, List<EstadoDTO> estadoDTOS) {
        if (equipoRepository.count() > 0) return equipoRepository.findAll();

        List<Equipo> equipos = new ArrayList<>();
        int estadoDTOSize = (estadoDTOS != null) ? estadoDTOS.size() : 0;

        for (int i = 0; i < 5; i++) {
            Equipo equipo = new Equipo();
            equipo.setNombre(faker.team().name());
            equipo.setTipoEquipo(tiposEquipo.get(faker.random().nextInt(tiposEquipo.size())));

            equipo.setCompania(companias.get(faker.random().nextInt(companias.size())));

            if (estadoDTOSize > 0) {
                EstadoDTO estadoDTO = estadoDTOS.get(faker.random().nextInt(estadoDTOSize));
                // Usamos getId() que es el método de IHasId implementado por EstadoDTO.
                equipo.setIdEstado(estadoDTO.getIdEstado() != null ? estadoDTO.getIdEstado() : 1);
            } else {
                equipo.setIdEstado(1);
            }

            equipos.add(equipoRepository.save(equipo));
        }
        return equipos;
    }

    /**
     * Método para crear usuarios y bomberos, asignando IDs de Estado y Foto (usando FotoClient).
     */
    private void crearUsuarios(List<TipoUsuario> tiposUsuario, List<EstadoDTO> estadoDTOS, List<Equipo> equipos) {
        if (usuarioRepository.count() > 0) return;

        int estadoDTOSize = (estadoDTOS != null) ? estadoDTOS.size() : 0;

        for (TipoUsuario tipo : tiposUsuario) {
            int cantidad = 2;
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

                Date pastDate = Date.from(faker.timeAndDate().past(5, TimeUnit.DAYS));
                usuario.setFechaRegistro(pastDate.toInstant().atZone(ZoneId.systemDefault()).toLocalDate().atStartOfDay());

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
                    // Usamos getId() que es el método de IHasId implementado por EstadoDTO.
                    usuario.setIdEstado(estadoDTO.getIdEstado() != null ? estadoDTO.getIdEstado() : 1);
                } else {
                    usuario.setIdEstado(1);
                }

                String fotoUrl = faker.internet().url();
                Integer fotoId = fotoClient.getRandomExistingFotoId();

                usuario.setIdFoto(fotoId);

                // Persistir con el repositorio correcto
                if (usuario instanceof Bombero) {
                    bomberoRepository.save((Bombero) usuario);
                } else {
                    usuarioRepository.save(usuario);
                }
            }
        }
    }


    // --- Métodos de utilidad (Mantenidos) ---

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