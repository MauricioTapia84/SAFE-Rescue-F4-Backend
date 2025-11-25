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
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClientResponseException;

import java.time.ZoneId;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

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

    // PasswordEncoder para hashear contraseñas
    @Autowired private PasswordEncoder passwordEncoder;

    // CLIENTES DEDICADOS PARA APIS EXTERNAS
    // ------------------------------------
    @Autowired private EstadoClient estadoClient;
    @Autowired private GeolocalizacionClient geolocalizacionClient;
    @Autowired private FotoClient fotoClient;
    // ------------------------------------

    // Contraseña fija para usuarios de prueba (RESTAURADO)
    private static final String PRUEBA_PASSWORD_FIJA = "TestPassword123!";

    private final Faker faker = new Faker(new Locale("es"));
    private final Set<String> uniqueRuns = new HashSet<>();
    private final Set<String> uniqueTelefonos = new HashSet<>();
    private final Set<String> uniqueCorreos = new HashSet<>();
    // Set para evitar duplicados de nombre de usuario
    private final Set<String> uniqueNombresUsuario = new HashSet<>();

    private static final String BOMBERO_TIPO = "Bombero en Terreno";
    private static final String OPERADOR_TIPO = "Operador de Sala";

    @Override
    public void run(String... args) {
        System.out.println("Cargando datos de prueba...");

        try {
            // Verifica si ya hay usuarios persistidos para evitar duplicados en reinicios (RESTAURADO)
            if (usuarioRepository.count() > 0) {
                System.out.println("La base de datos ya contiene usuarios. Omitiendo la carga de DataLoader.");
                return;
            }

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

            // 5. Generar Usuarios Fijos (Para pruebas funcionales) (RESTAURADO Y CORREGIDO)
            crearUsuariosFijos(tiposUsuario, estadoDTOS, equipos, direccionDTOS);

            // 6. Generar Usuarios Aleatorios (Para volumen de datos) (RESTAURADO)
            crearUsuariosAleatorios(tiposUsuario, estadoDTOS, equipos, direccionDTOS);

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
        // Fallback por si el servicio de Estado está caído (RESTAURADO)
        try {
            return estadoClient.getAllEstados();
        } catch (WebClientResponseException e) {
            System.err.println("FALLBACK: Error al conectar con API Estado. Usando estado simulado ID=1.");
            EstadoDTO fallback = new EstadoDTO();
            fallback.setIdEstado(1);
            fallback.setNombre("Activo");
            return Collections.singletonList(fallback);
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
            // CORREGIDO: Usar tipoEquipoRepository para TipoEquipo
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
                direccionId = direccionDTO.getIdDireccion();

                if (direccionId == null) {
                    direccionId = 100 + i;
                    System.err.println("WARN: DireccionDTO del microservicio retornó ID nulo. Usando ID de fallback: " + direccionId);
                }
            } else {
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
                equipo.setIdEstado(estadoDTO.getIdEstado() != null ? estadoDTO.getIdEstado() : 1);
            } else {
                equipo.setIdEstado(1);
            }

            equipos.add(equipoRepository.save(equipo));
        }
        return equipos;
    }


    // --- MÉTODO: Crear usuarios fijos para pruebas ---

    private void crearUsuariosFijos(List<TipoUsuario> tiposUsuario, List<EstadoDTO> estadoDTOS, List<Equipo> equipos, List<DireccionDTO> direccionDTOS) {
        System.out.println("--- Creando Usuarios Fijos de Prueba ---");

        Map<String, TipoUsuario> tiposMap = tiposUsuario.stream()
                .collect(Collectors.toMap(TipoUsuario::getNombre, t -> t));

        // Obtener el primer ID de Estado y Dirección para consistencia
        Integer estadoIdFijo = estadoDTOS != null && !estadoDTOS.isEmpty() && estadoDTOS.get(0).getIdEstado() != null ? estadoDTOS.get(0).getIdEstado() : 1;
        Integer direccionIdFijo = direccionDTOS != null && !direccionDTOS.isEmpty() && direccionDTOS.get(0).getIdDireccion() != null ? direccionDTOS.get(0).getIdDireccion() : 200;

        String hashedPassword = passwordEncoder.encode(PRUEBA_PASSWORD_FIJA);

        // Datos fijos CORREGIDOS: Nombres de usuario alfanuméricos para evitar ConstraintViolationException
        List<String[]> usuariosData = Arrays.asList(
                new String[]{"Jefe de Compañía", "Jefe", "Compania", "jefe@test.com", "jefetest", "12345678"},
                new String[]{BOMBERO_TIPO, "Bombero", "Terreno", "bombero@test.com", "bomberotest", "23456789"},
                new String[]{OPERADOR_TIPO, "Operador", "Sala", "operador@test.com", "operadortest", "34567890"},
                new String[]{"Administrador", "Admin", "Sistema", "admin@test.com", "admintest", "45678901"},
                new String[]{"Ciudadano", "Ciudadano", "Comun", "ciudadano@test.com", "ciudadanotest", "56789012"}
        );

        for (String[] data : usuariosData) {
            String tipoNombre = data[0];
            TipoUsuario tipo = tiposMap.get(tipoNombre);
            if (tipo == null) continue;

            Usuario usuario;
            if (tipoNombre.equalsIgnoreCase("Ciudadano")) {
                Ciudadano ciudadano = new Ciudadano();
                ciudadano.setIdDireccion(direccionIdFijo);
                usuario = ciudadano;
            } else if (tipoNombre.equalsIgnoreCase(BOMBERO_TIPO) || tipoNombre.equalsIgnoreCase(OPERADOR_TIPO)) {
                Bombero bombero = new Bombero();
                if (!equipos.isEmpty()) {
                    bombero.setEquipo(equipos.get(0)); // Asignar al primer equipo
                }
                usuario = bombero;
            } else {
                usuario = new Usuario();
            }

            usuario.setRun(data[5]);
            usuario.setDv(calcularDv(usuario.getRun()));
            usuario.setNombre(data[1]);
            usuario.setAPaterno(data[2]);
            usuario.setAMaterno("Fijo");
            usuario.setNombreUsuario(data[4]); // Nombre de usuario fijo (alfanumérico)
            usuario.setFechaRegistro(java.time.LocalDate.now().atStartOfDay());
            usuario.setTelefono("9" + data[5]); // Teléfono fijo
            usuario.setCorreo(data[3]); // Correo fijo

            usuario.setContrasenia(hashedPassword);
            System.out.println("✅ Usuario Fijo " + usuario.getNombreUsuario() + " creado. Contraseña: " + PRUEBA_PASSWORD_FIJA);

            usuario.setIntentosFallidos(0);
            usuario.setRazonBaneo(null);
            usuario.setDiasBaneo(null);
            usuario.setTipoUsuario(tipo);
            usuario.setIdEstado(estadoIdFijo);
            usuario.setIdFoto(fotoClient.getRandomExistingFotoId());

            // Persistir
            if (usuario instanceof Bombero) {
                bomberoRepository.save((Bombero) usuario);
            } else {
                usuarioRepository.save(usuario);
            }

            // Agregar a los sets para evitar colisiones con los aleatorios
            uniqueNombresUsuario.add(usuario.getNombreUsuario());
            uniqueCorreos.add(usuario.getCorreo());
            uniqueRuns.add(usuario.getRun());
            uniqueTelefonos.add(usuario.getTelefono());
        }
    }


    /**
     * Método para crear usuarios aleatorios, con contraseña mejorada y asignando IDs de Estado y Foto.
     */
    private void crearUsuariosAleatorios(List<TipoUsuario> tiposUsuario, List<EstadoDTO> estadoDTOS, List<Equipo> equipos, List<DireccionDTO> direccionDTOS) {

        System.out.println("--- Creando Usuarios Aleatorios para volumen ---");
        int estadoDTOSize = (estadoDTOS != null) ? estadoDTOS.size() : 0;
        int direccionDTOSize = (direccionDTOS != null) ? direccionDTOS.size() : 0;

        for (TipoUsuario tipo : tiposUsuario) {
            int cantidad = 2; // Cantidad de usuarios aleatorios por tipo

            for (int i = 0; i < cantidad; i++) {
                Usuario usuario;

                if (tipo.getNombre().equalsIgnoreCase("Ciudadano")) {
                    Ciudadano ciudadano = new Ciudadano();
                    usuario = ciudadano;

                    if (direccionDTOSize > 0) {
                        DireccionDTO direccionDTO = direccionDTOS.get(faker.random().nextInt(direccionDTOSize));
                        Integer direccionId = direccionDTO.getIdDireccion();
                        if (direccionId == null) {
                            direccionId = 200 + i;
                        }
                        ciudadano.setIdDireccion(direccionId);
                    } else {
                        ciudadano.setIdDireccion(200 + i);
                    }

                } else if (tipo.getNombre().equalsIgnoreCase(BOMBERO_TIPO) || tipo.getNombre().equalsIgnoreCase(OPERADOR_TIPO)) {
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

                usuario.setNombreUsuario(crearNombreUsuarioUnico(usuario.getNombre()));

                Date pastDate = Date.from(faker.timeAndDate().past(5, TimeUnit.DAYS));
                usuario.setFechaRegistro(pastDate.toInstant().atZone(ZoneId.systemDefault()).toLocalDate().atStartOfDay());

                usuario.setTelefono(crearTelefonoUnico());
                usuario.setCorreo(crearCorreoUnico());

                // --- MODIFICACIÓN: CONTRASEÑA ALEATORIA MEJORADA (RESTAURADO) ---
                String rawPassword = generarContraseniaSeguraAleatoria(usuario.getNombre());
                String hashedPassword = passwordEncoder.encode(rawPassword);
                usuario.setContrasenia(hashedPassword);
                // ---------------------------------------------------

                usuario.setIntentosFallidos(0);
                usuario.setRazonBaneo(null);
                usuario.setDiasBaneo(null);
                usuario.setTipoUsuario(tipo);

                // CLAVE FORÁNEA LÓGICA: ID del Estado
                if (estadoDTOSize > 0) {
                    EstadoDTO estadoDTO = estadoDTOS.get(faker.random().nextInt(estadoDTOSize));
                    usuario.setIdEstado(estadoDTO.getIdEstado() != null ? estadoDTO.getIdEstado() : 1);
                } else {
                    usuario.setIdEstado(1);
                }

                Integer fotoId = fotoClient.getRandomExistingFotoId();
                usuario.setIdFoto(fotoId);

                // Persistir según el tipo
                if (usuario instanceof Bombero) {
                    bomberoRepository.save((Bombero) usuario);
                } else {
                    usuarioRepository.save(usuario);
                }
            }
        }
    }


    // --- Métodos de utilidad ---

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

    // --- MÉTODO PARA NOMBRE DE USUARIO ÚNICO ---
    private String crearNombreUsuarioUnico(String baseName) {
        String nombreUsuario;
        String safeBase = baseName.replaceAll("[^a-zA-Z0-9]", ""); // Solo letras y números
        if (safeBase.length() < 3) safeBase = "User"; // Evitar nombres muy cortos

        do {
            // Genera algo como "Juan482" o "Maria12"
            nombreUsuario = safeBase + faker.number().digits(3);

            // Validar longitud (entre 5 y 20)
            if (nombreUsuario.length() > 20) {
                nombreUsuario = nombreUsuario.substring(0, 20);
            } else if (nombreUsuario.length() < 5) {
                nombreUsuario = nombreUsuario + faker.number().digits(5 - nombreUsuario.length());
            }

        } while (!uniqueNombresUsuario.add(nombreUsuario));

        return nombreUsuario;
    }

    // --- MÉTODO: Generar contraseña segura aleatoria (Cumple Mayús, Minús, Números, Signos) (RESTAURADO) ---
    private String generarContraseniaSeguraAleatoria(String baseName) {
        String base = baseName.length() >= 5 ? baseName.substring(0, 5) : baseName;
        // Asegura Mayúscula, Minúscula, Número y Símbolo
        String upper = base.substring(0, 1).toUpperCase();
        String lower = base.length() > 1 ? base.substring(1).toLowerCase() : "user";
        String numbers = faker.number().digits(3);
        String symbol = "!@#$%^&*".charAt(faker.random().nextInt(8)) + "";
        return upper + lower + numbers + symbol;
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