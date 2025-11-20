package com.SAFE_Rescue.API_Perfiles.service;

import com.SAFE_Rescue.API_Perfiles.dto.AuthResponseDTO;
import com.SAFE_Rescue.API_Perfiles.exception.InvalidCredentialsException;
import com.SAFE_Rescue.API_Perfiles.exception.UserAlreadyExistsException;
import com.SAFE_Rescue.API_Perfiles.exception.UserNotFoundException;
import com.SAFE_Rescue.API_Perfiles.modelo.TipoUsuario; // Necesitas el modelo de TipoUsuario
import com.SAFE_Rescue.API_Perfiles.modelo.Usuario;
import com.SAFE_Rescue.API_Perfiles.repository.UsuarioRepository;
import com.SAFE_Rescue.API_Perfiles.util.JwtUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

// Permite usar anotaciones Mockito y JUnit 5
@ExtendWith(MockitoExtension.class)
public class AuthServiceTest {

    // Inyecta las dependencias simuladas (Mocks) en la instancia real de AuthService
    @InjectMocks
    private AuthService authService;

    // Dependencias a simular
    @Mock
    private UsuarioRepository usuarioRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private JwtUtil jwtUtil;

    // Datos de prueba comunes
    private Usuario usuarioPrueba;
    private final String USUARIO_EMAIL = "test@safe.cl";
    private final String CONTRASENA_RAW = "password123";
    private final String CONTRASENA_HASH = "$2a$10$hashedpassword";
    private final String TIPO_PERFIL = "BOMBERO";
    private final String MOCK_JWT = "mock.jwt.token";

    @BeforeEach
    void setUp() {
        // Inicializa el objeto Usuario para usar en múltiples pruebas
        usuarioPrueba = new Usuario();
        usuarioPrueba.setIdUsuario(1);
        usuarioPrueba.setNombre("testuser");
        usuarioPrueba.setCorreo(USUARIO_EMAIL);
        usuarioPrueba.setContrasenia(CONTRASENA_HASH);

        TipoUsuario tipo = new TipoUsuario();
        tipo.setNombre(TIPO_PERFIL);
        usuarioPrueba.setTipoUsuario(tipo);
    }

    // =================================================================
    // PRUEBAS PARA authenticateAndGenerateToken (Login)
    // =================================================================

    @Test
    void authenticateAndGenerateToken_Success() {
        // Configuración de Mocks para un login exitoso
        when(usuarioRepository.findByCorreo(USUARIO_EMAIL))
                .thenReturn(Optional.of(usuarioPrueba));
        when(passwordEncoder.matches(CONTRASENA_RAW, CONTRASENA_HASH))
                .thenReturn(true);
        when(jwtUtil.generateToken(usuarioPrueba.getIdUsuario(), TIPO_PERFIL))
                .thenReturn(MOCK_JWT);

        // Ejecución
        AuthResponseDTO response = authService.authenticateAndGenerateToken(USUARIO_EMAIL, CONTRASENA_RAW);

        // Verificación
        assertNotNull(response);
        assertEquals(MOCK_JWT, response.getToken(), "El token generado debe coincidir.");
        assertEquals(usuarioPrueba.getIdUsuario(), response.getUserData(), "Los datos del usuario deben ser correctos.");

        // Verificar que las llamadas críticas se realizaron
        verify(usuarioRepository, times(1)).findByCorreo(anyString());
        verify(passwordEncoder, times(1)).matches(anyString(), anyString());
        verify(jwtUtil, times(1)).generateToken(anyInt(), anyString());
    }

    @Test
    void authenticateAndGenerateToken_UserNotFound() {
        // Configuración de Mocks: el repositorio no encuentra al usuario
        when(usuarioRepository.findByCorreo(USUARIO_EMAIL))
                .thenReturn(Optional.empty());

        // Verificación de la excepción
        assertThrows(UserNotFoundException.class, () -> {
            authService.authenticateAndGenerateToken(USUARIO_EMAIL, CONTRASENA_RAW);
        }, "Debe lanzar UserNotFoundException si el usuario no existe.");

        // Verificar que NO se intentó comparar la contraseña ni generar el token
        verify(passwordEncoder, never()).matches(anyString(), anyString());
        verify(jwtUtil, never()).generateToken(anyInt(), anyString());
    }

    @Test
    void authenticateAndGenerateToken_InvalidPassword() {
        // Configuración de Mocks: usuario encontrado, pero contraseña incorrecta
        when(usuarioRepository.findByCorreo(USUARIO_EMAIL))
                .thenReturn(Optional.of(usuarioPrueba));
        when(passwordEncoder.matches(CONTRASENA_RAW, CONTRASENA_HASH))
                .thenReturn(false); // Falla la verificación de contraseña

        // Verificación de la excepción
        assertThrows(InvalidCredentialsException.class, () -> {
            authService.authenticateAndGenerateToken(USUARIO_EMAIL, CONTRASENA_RAW);
        }, "Debe lanzar InvalidCredentialsException si la contraseña es incorrecta.");

        // Verificar que NO se intentó generar el token
        verify(jwtUtil, never()).generateToken(anyInt(), anyString());
    }

    // Si implementas UserNotActiveException, añadirías esta prueba:
    /*
    @Test
    void authenticateAndGenerateToken_UserNotActive() {
        // Asumiendo que has añadido la lógica de estado al usuarioPrueba y al service
        usuarioPrueba.setActivo(false);
        when(usuarioRepository.findByNombreUsuarioOrEmail(USUARIO_EMAIL, USUARIO_EMAIL))
                .thenReturn(Optional.of(usuarioPrueba));
        when(passwordEncoder.matches(CONTRASENA_RAW, CONTRASENA_HASH))
                .thenReturn(true);

        assertThrows(UserNotActiveException.class, () -> {
            authService.authenticateAndGenerateToken(USUARIO_EMAIL, CONTRASENA_RAW);
        });
    }
    */

    // =================================================================
    // PRUEBAS PARA registerNewUser (Registro)
    // =================================================================

    @Test
    void registerNewUser_Success() {
        Usuario nuevoUsuario = new Usuario();
        nuevoUsuario.setRun("11111111-1");
        nuevoUsuario.setContrasenia(CONTRASENA_RAW);

        // Configuración de Mocks: el RUN no existe, se cifra y se guarda
        when(usuarioRepository.existsByRun("11111111-1")).thenReturn(false);
        when(passwordEncoder.encode(CONTRASENA_RAW)).thenReturn(CONTRASENA_HASH);
        when(usuarioRepository.save(nuevoUsuario)).thenReturn(nuevoUsuario);

        // Ejecución
        Usuario result = authService.registerNewUser(nuevoUsuario);

        // Verificación
        assertNotNull(result);
        // Verificar que la contraseña fue cifrada antes de guardar
        assertEquals(CONTRASENA_HASH, result.getContrasenia(), "La contraseña debe ser cifrada antes de guardar.");

        // Verificar que el save se realizó
        verify(usuarioRepository, times(1)).save(nuevoUsuario);
    }

    @Test
    void registerNewUser_UserAlreadyExists() {
        Usuario nuevoUsuario = new Usuario();
        nuevoUsuario.setRun("11111111-1");

        // Configuración de Mocks: el RUN ya existe
        when(usuarioRepository.existsByRun("11111111-1")).thenReturn(true);

        // Verificación de la excepción
        assertThrows(UserAlreadyExistsException.class, () -> {
            authService.registerNewUser(nuevoUsuario);
        }, "Debe lanzar UserAlreadyExistsException si el RUN ya existe.");

        // Verificar que NO se cifró la contraseña ni se intentó guardar
        verify(passwordEncoder, never()).encode(anyString());
        verify(usuarioRepository, never()).save(any(Usuario.class));
    }
}