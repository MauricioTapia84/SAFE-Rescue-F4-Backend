package com.SAFE_Rescue.API_Perfiles.service;

import com.SAFE_Rescue.API_Perfiles.dto.AuthResponseDTO;
import com.SAFE_Rescue.API_Perfiles.exception.InvalidCredentialsException;
import com.SAFE_Rescue.API_Perfiles.exception.UserAlreadyExistsException;
import com.SAFE_Rescue.API_Perfiles.exception.UserNotFoundException;
import com.SAFE_Rescue.API_Perfiles.modelo.Bombero;
import com.SAFE_Rescue.API_Perfiles.modelo.Ciudadano;
import com.SAFE_Rescue.API_Perfiles.modelo.Usuario;
import com.SAFE_Rescue.API_Perfiles.repository.UsuarioRepository;
import com.SAFE_Rescue.API_Perfiles.util.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class AuthService {

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private JwtUtil jwtUtil;

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
        String token = jwtUtil.generateToken(usuario.getIdUsuario(), tipoPerfil);
        System.out.println(" TOKEN GENERADO: " + (token != null ? "***" : "null"));

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

    public Usuario registerNewUser(Usuario nuevoUsuario) {
        System.out.println(" ========== INICIO REGISTRO ==========");
        System.out.println(" Registrando usuario: " + nuevoUsuario.getNombre());
        System.out.println(" Correo: " + nuevoUsuario.getCorreo());
        System.out.println(" RUN: " + nuevoUsuario.getRun());

        // 1. Verificar RUN duplicado
        System.out.println(" Verificando RUN duplicado: " + nuevoUsuario.getRun());
        if (usuarioRepository.existsByRun(nuevoUsuario.getRun())) {
            System.out.println(" RUN YA REGISTRADO: " + nuevoUsuario.getRun());
            throw new UserAlreadyExistsException("El RUN ya se encuentra registrado.");
        }
        System.out.println(" RUN DISPONIBLE");

        // 2. Cifrar contraseña
        System.out.println(" CIFRANDO CONTRASEÑA...");
        String encodedPassword = passwordEncoder.encode(nuevoUsuario.getContrasenia());
        nuevoUsuario.setContrasenia(encodedPassword);
        System.out.println(" CONTRASEÑA CIFRADA");

        // 3. Guardar en BD
        System.out.println(" GUARDANDO USUARIO EN BD...");
        Usuario usuarioGuardado = usuarioRepository.save(nuevoUsuario);
        System.out.println(" ========== REGISTRO EXITOSO ==========");
        System.out.println("   Usuario ID: " + usuarioGuardado.getIdUsuario());
        System.out.println("   Nombre: " + usuarioGuardado.getNombre());

        return usuarioGuardado;
    }
}