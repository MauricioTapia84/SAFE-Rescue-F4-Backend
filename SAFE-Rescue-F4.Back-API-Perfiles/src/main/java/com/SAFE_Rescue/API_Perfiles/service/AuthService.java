package com.SAFE_Rescue.API_Perfiles.service;

import com.SAFE_Rescue.API_Perfiles.dto.AuthResponseDTO;
import com.SAFE_Rescue.API_Perfiles.exception.InvalidCredentialsException;
import com.SAFE_Rescue.API_Perfiles.exception.UserAlreadyExistsException;
import com.SAFE_Rescue.API_Perfiles.exception.UserNotFoundException;
import com.SAFE_Rescue.API_Perfiles.modelo.Usuario;
import com.SAFE_Rescue.API_Perfiles.repository.UsuarioRepository;
import com.SAFE_Rescue.API_Perfiles.util.JwtUtil; // ⭐ NECESITAS ESTA CLASE UTILITARIA
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder; // ⭐ NECESITAS UN ENCODER
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class AuthService {

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private PasswordEncoder passwordEncoder; // Para comparar contraseñas hash

    @Autowired
    private JwtUtil jwtUtil; // Para generar el token

    /**
     * Autentica un usuario, verifica la contraseña y genera un Token JWT.
     * @param nombreUsuario Nombre de usuario o email para la autenticación.
     * @param contrasena Contraseña sin cifrar proporcionada por el usuario.
     * @return AuthResponseDTO conteniendo el Token JWT y los datos del usuario.
     * @throws InvalidCredentialsException Si el usuario no existe o la contraseña es incorrecta.
     */
    public AuthResponseDTO authenticateAndGenerateToken(String nombreUsuario, String contrasena) {

        // 1. Buscar el usuario por nombreUsuario o email
        Optional<Usuario> usuarioOpt = usuarioRepository.findByNombreUsuarioOrEmail(nombreUsuario, nombreUsuario);

        if (usuarioOpt.isEmpty()) {
            throw new UserNotFoundException("Usuario no encontrado.");
        }

        Usuario usuario = usuarioOpt.get();

        // 2. Verificar la contraseña
        if (!passwordEncoder.matches(contrasena, usuario.getContrasenia())) {
            throw new InvalidCredentialsException("Credenciales inválidas.");
        }

        // 3. Verificar estado (opcional, ej: si la cuenta está activa)
        // if (!usuario.isActivo()) {
        //     throw new UserNotActiveException("La cuenta del usuario no está activa.");
        // }

        // 4. Generar el Token JWT
        // Los claims deben incluir información clave para autorización, como el tipo de perfil.
        String token = jwtUtil.generateToken(usuario.getIdUsuario(), usuario.getTipoUsuario().getNombreUpperCased());

        // 5. Construir la respuesta
        AuthResponseDTO response = new AuthResponseDTO();
        response.setToken(token);
        // Retornamos el objeto Usuario, confiando en que Jackson lo serializará
        // correctamente (incluyendo los campos de Bombero/Ciudadano por herencia).
        response.setUserData(usuario);

        return response;
    }

    /**
     * Método de servicio para la lógica de registro.
     * @param nuevoUsuario La entidad Usuario a registrar.
     * @return El usuario guardado.
     */
    public Usuario registerNewUser(Usuario nuevoUsuario) {
        // 1. Lógica de verificación (ej: RUN/Email no duplicados)
        if (usuarioRepository.existsByRun(nuevoUsuario.getRun())) {
            throw new UserAlreadyExistsException("El RUN ya se encuentra registrado.");
        }

        // 2. Cifrar la contraseña antes de guardar
        String encodedPassword = passwordEncoder.encode(nuevoUsuario.getContrasenia());
        nuevoUsuario.setContrasenia(encodedPassword);

        // 3. Guardar en la DB
        return usuarioRepository.save(nuevoUsuario);
    }
}