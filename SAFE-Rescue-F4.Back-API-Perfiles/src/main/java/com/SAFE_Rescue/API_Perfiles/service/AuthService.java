package com.SAFE_Rescue.API_Perfiles.service;

import com.SAFE_Rescue.API_Perfiles.config.GeolocalizacionClient;
import com.SAFE_Rescue.API_Perfiles.dto.*;
import com.SAFE_Rescue.API_Perfiles.exception.InvalidCredentialsException;
import com.SAFE_Rescue.API_Perfiles.exception.UserAlreadyExistsException;
import com.SAFE_Rescue.API_Perfiles.exception.UserNotFoundException;
import com.SAFE_Rescue.API_Perfiles.modelo.Bombero;
import com.SAFE_Rescue.API_Perfiles.modelo.Ciudadano;
import com.SAFE_Rescue.API_Perfiles.modelo.TipoUsuario;
import com.SAFE_Rescue.API_Perfiles.modelo.Usuario;
import com.SAFE_Rescue.API_Perfiles.repository.CiudadanoRepository;
import com.SAFE_Rescue.API_Perfiles.repository.UsuarioRepository;
import com.SAFE_Rescue.API_Perfiles.util.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
public class AuthService {

    private static final Integer ID_ESTADO_ACTIVO = 1;

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private TipoUsuarioService tipoUsuarioService;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private GeolocalizacionClient geolocalizacionClient;

    @Autowired
    private CiudadanoRepository ciudadanoRepository;

    /**
     * Registra un nuevo ciudadano completo (usuario + dirección)
     */
    public Ciudadano registerNewCiudadano(RegistroRequestDTO request) {
        System.out.println(" ========== INICIO REGISTRO COMPLETO ==========");
        System.out.println(" Registrando RUN: " + request.getRun());

        // 1. Verificar RUN duplicado
        if (usuarioRepository.existsByRun(request.getRun())) {
            throw new UserAlreadyExistsException("El RUN ya se encuentra registrado.");
        }

        // 2. Obtener TipoUsuario para Ciudadano (id = 5)
        TipoUsuario tipoUsuario = tipoUsuarioService.findById(5);
        System.out.println("  TipoUsuario obtenido: " + tipoUsuario.getNombre());

        // 3. Crear dirección en API de Geolocalización PRIMERO
        DireccionDTO direccionCreada = crearDireccionEnGeolocalizacion(request.getDireccion());
        System.out.println("  Dirección creada en Geolocalización - ID: " + direccionCreada.getIdDireccion());

        // 4. Crear y guardar Ciudadano
        Ciudadano ciudadano = crearCiudadano(request, tipoUsuario, direccionCreada.getIdDireccion());
        Ciudadano ciudadanoGuardado = ciudadanoRepository.save(ciudadano);

        System.out.println("  Ciudadano creado - ID: " + ciudadanoGuardado.getIdUsuario());
        System.out.println(" ========== REGISTRO COMPLETO EXITOSO ==========");

        return ciudadanoGuardado;
    }

    /**
     * Método privado para crear entidad Ciudadano
     */
    private Ciudadano crearCiudadano(RegistroRequestDTO request, TipoUsuario tipoUsuario, Integer idDireccion) {
        Ciudadano ciudadano = new Ciudadano();

        // Usar el método setUsuarioData con todos los parámetros necesarios
        // NOTA: Si Usuario.java no tiene setUsuarioData actualizado con nombreUsuario,
        // lo asignamos manualmente justo después.
        ciudadano.setUsuarioData(
                request.getRun(),
                request.getDv(),
                request.getNombre(),
                request.getAPaterno(),
                request.getAMaterno(),
                request.getTelefono(),
                request.getCorreo(),
                passwordEncoder.encode(request.getContrasenia()),
                tipoUsuario,
                LocalDateTime.now(),
                ID_ESTADO_ACTIVO
        );

        // --- NUEVO CAMPO: Asignar Nombre de Usuario ---
        ciudadano.setNombreUsuario(request.getNombreUsuario());
        // ----------------------------------------------

        // Campo específico de Ciudadano
        ciudadano.setIdDireccion(idDireccion);

        return ciudadano;
    }

    /**
     * Método privado para crear dirección en Geolocalización
     */
    private DireccionDTO crearDireccionEnGeolocalizacion(DireccionRequestDTO direccionRequest) {
        try {
            System.out.println(" 📍 Creando dirección en microservicio de Geolocalización...");

            DireccionDTO direccionDTO = new DireccionDTO();

            // Campos básicos de la dirección
            direccionDTO.setCalle(direccionRequest.getCalle());
            direccionDTO.setNumero(direccionRequest.getNumero());
            direccionDTO.setVilla(direccionRequest.getVilla());
            direccionDTO.setComplemento(direccionRequest.getComplemento());

            // Comuna básica con solo ID
            ComunaDTO comuna = new ComunaDTO();
            comuna.setIdComuna(direccionRequest.getIdComuna());
            direccionDTO.setComuna(comuna);

            // Configurar coordenadas (opcionales - con valores por defecto)
            CoordenadasDTO coordenadas = new CoordenadasDTO();
            if (direccionRequest.getCoordenadas() != null &&
                    direccionRequest.getCoordenadas().getLatitud() != null &&
                    direccionRequest.getCoordenadas().getLongitud() != null) {

                coordenadas.setLatitud(direccionRequest.getCoordenadas().getLatitud());
                coordenadas.setLongitud(direccionRequest.getCoordenadas().getLongitud());
                System.out.println(" 📍 Usando coordenadas proporcionadas por el usuario");
            } else {
                // Coordenadas por defecto (Santiago centro)
                coordenadas.setLatitud(-33.45694);
                coordenadas.setLongitud(-70.64827);
                System.out.println(" 📍 Usando coordenadas por defecto");
            }
            direccionDTO.setCoordenadas(coordenadas);

            // Usar cliente para guardar la dirección
            DireccionDTO direccionCreada = geolocalizacionClient.guardarDireccion(direccionDTO);
            System.out.println(" 📍 Dirección creada exitosamente - ID: " + direccionCreada.getIdDireccion());

            return direccionCreada;

        } catch (Exception e) {
            System.err.println(" ❌ Error al crear dirección en Geolocalización: " + e.getMessage());
            throw new RuntimeException("No se pudo crear la dirección en el servicio de geolocalización: " + e.getMessage());
        }
    }


    public AuthResponseDTO authenticateAndGenerateToken(String correo, String contrasena) {
        System.out.println(" ========== INICIO AUTENTICACIÓN ==========");
        System.out.println(" Correo recibido: " + correo);
        System.out.println(" Contraseña recibida: " + (contrasena != null ? "***" : "null"));

        // 1. Buscar usuario por correo
        System.out.println(" Buscando usuario con correo: '" + correo + "'");
        Optional<Usuario> usuarioOpt = usuarioRepository.findByCorreo(correo);

        if (usuarioOpt.isEmpty()) {
            System.out.println(" USUARIO NO ENCONTRADO con correo: '" + correo + "'");

            // Debug: mostrar todos los correos existentes en la BD
            System.out.println(" LISTA DE CORREOS EXISTENTES EN BD:");
            try {
                List<Usuario> todosUsuarios = usuarioRepository.findAll();
                if (todosUsuarios.isEmpty()) {
                    System.out.println("   ️ No hay usuarios en la base de datos");
                } else {
                    todosUsuarios.forEach(u ->
                            System.out.println("   - '" + u.getCorreo() + "' (ID: " + u.getIdUsuario() + ")")
                    );
                }
            } catch (Exception e) {
                System.out.println("    Error al obtener lista de usuarios: " + e.getMessage());
            }

            throw new UserNotFoundException("Usuario no encontrado.");
        }

        Usuario usuario = usuarioOpt.get();
        System.out.println(" USUARIO ENCONTRADO:");
        System.out.println("   ID: " + usuario.getIdUsuario());
        System.out.println("   Nombre: " + usuario.getNombre());
        System.out.println("   Correo: " + usuario.getCorreo());
        System.out.println("   RUN: " + usuario.getRun());

        // 2. Verificar la contraseña
        System.out.println("   VERIFICANDO CONTRASEÑA...");
        System.out.println("   Contraseña proporcionada: " + (contrasena != null ? "***" : "null"));
        System.out.println("   Contraseña en BD (hash): " + usuario.getContrasenia());

        boolean passwordMatches = passwordEncoder.matches(contrasena, usuario.getContrasenia());
        System.out.println("   ¿Contraseña coincide?: " + passwordMatches);

        if (!passwordMatches) {
            System.out.println(" CONTRASEÑA INCORRECTA para usuario: " + usuario.getCorreo());
            throw new InvalidCredentialsException("Credenciales inválidas.");
        }
        System.out.println(" CONTRASEÑA VÁLIDA");

        // 3. Determinar tipo de perfil
        System.out.println("👤 DETERMINANDO TIPO DE PERFIL...");
        String tipoPerfil;
        Object userData;

        Optional<Ciudadano> ciudadanoOpt = usuarioRepository.findCiudadanoById(usuario.getIdUsuario());
        Optional<Bombero> bomberoOpt = usuarioRepository.findBomberoById(usuario.getIdUsuario());

        System.out.println("   ¿Es ciudadano?: " + ciudadanoOpt.isPresent());
        System.out.println("   ¿Es bombero?: " + bomberoOpt.isPresent());

        if (ciudadanoOpt.isPresent()) {
            tipoPerfil = "CIUDADANO";
            userData = ciudadanoOpt.get();
            System.out.println(" TIPO: CIUDADANO");
        } else if (bomberoOpt.isPresent()) {
            tipoPerfil = "BOMBERO";
            userData = bomberoOpt.get();
            System.out.println(" TIPO: BOMBERO");
        } else {
            System.out.println(" TIPO DE PERFIL NO DETERMINADO");
            throw new RuntimeException("Tipo de perfil no determinado para el usuario");
        }

        // 4. Generar token
        System.out.println(" GENERANDO TOKEN JWT...");
        System.out.println("   userId: " + usuario.getIdUsuario());
        System.out.println("   tipoPerfil: " + tipoPerfil);

        String token = jwtUtil.generateToken(usuario.getIdUsuario(), tipoPerfil);

        if (token == null || token.isEmpty()) {
            System.err.println(" ERROR: Token es nulo o vacío");
            throw new RuntimeException("Error al generar el token JWT");
        }

        System.out.println(" ✅ TOKEN GENERADO");
        System.out.println("   Longitud: " + token.length() + " caracteres");
        System.out.println("   Primeros 50 chars: " + token.substring(0, Math.min(50, token.length())) + "...");

        // 5. Construir respuesta
        AuthResponseDTO response = new AuthResponseDTO();
        response.setToken(token);
        response.setTipoPerfil(tipoPerfil);
        response.setUserData(userData);

        System.out.println(" ========== AUTENTICACIÓN EXITOSA ==========");
        System.out.println("   Usuario: " + usuario.getNombre());
        System.out.println("   Tipo: " + tipoPerfil);
        System.out.println("   Token generado: " + (token != null ? "SÍ" : "NO"));

        return response;
    }
}