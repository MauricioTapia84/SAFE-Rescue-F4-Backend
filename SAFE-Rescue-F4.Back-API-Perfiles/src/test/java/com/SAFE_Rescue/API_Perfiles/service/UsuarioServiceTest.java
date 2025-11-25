package com.SAFE_Rescue.API_Perfiles.service;

import com.SAFE_Rescue.API_Perfiles.config.EstadoClient;
import com.SAFE_Rescue.API_Perfiles.config.FotoClient;
import com.SAFE_Rescue.API_Perfiles.dto.EstadoDTO;
import com.SAFE_Rescue.API_Perfiles.modelo.TipoUsuario;
import com.SAFE_Rescue.API_Perfiles.modelo.Usuario;
import com.SAFE_Rescue.API_Perfiles.repository.UsuarioRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.reactive.function.client.WebClientResponseException;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.NoSuchElementException;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;

/**
 * Pruebas unitarias para la capa de servicio de Usuario.
 */
@ExtendWith(MockitoExtension.class)
public class UsuarioServiceTest {

    @InjectMocks
    private UsuarioService usuarioService;

    @Mock
    private UsuarioRepository usuarioRepository;

    @Mock
    private TipoUsuarioService tipoUsuarioService; // Usado en validarExistencia

    @Mock
    private EstadoClient estadoClient; // Cliente de API Estado

    @Mock
    private FotoClient fotoClient; // Cliente de API Foto

    private Usuario usuarioValido;
    private final Integer ID_USUARIO = 1;
    private final Integer ID_FOTO_GUARDADA = 100;
    private final Integer ID_ESTADO_ACTIVO = 1;

    @BeforeEach
    void setUp() {
        // Inicialización de un Usuario válido para las pruebas
        TipoUsuario tipoUsuario = new TipoUsuario();
        tipoUsuario.setIdTipoUsuario(1);
        tipoUsuario.setNombre("CIUDADANO");

        usuarioValido = new Usuario();
        usuarioValido.setIdUsuario(ID_USUARIO);
        usuarioValido.setRun("11111111");
        usuarioValido.setDv("1");
        usuarioValido.setNombre("Test");
        usuarioValido.setAPaterno("User");
        usuarioValido.setAMaterno("Mock");
        usuarioValido.setFechaRegistro(LocalDateTime.now());
        usuarioValido.setTelefono("987654321");
        usuarioValido.setCorreo("test@safe.cl");
        usuarioValido.setContrasenia("hashed_pass");
        usuarioValido.setTipoUsuario(tipoUsuario);
        usuarioValido.setIdEstado(ID_ESTADO_ACTIVO);
    }

    // =================================================================
    // PRUEBAS PARA findByNombreUsuario
    // =================================================================

    @Test
    void findByNombreUsuario_ShouldReturnUsuario_WhenFound() {
        // Arrange
        String nombreUsuario = "testnick";
        usuarioValido.setNombreUsuario(nombreUsuario);
        when(usuarioRepository.findByNombreUsuario(nombreUsuario)).thenReturn(Optional.of(usuarioValido));

        // Act
        Usuario found = usuarioService.findByNombreUsuario(nombreUsuario);

        // Assert
        assertNotNull(found);
        assertEquals(nombreUsuario, found.getNombreUsuario());
        verify(usuarioRepository, times(1)).findByNombreUsuario(nombreUsuario);
    }

    @Test
    void findByNombreUsuario_ShouldThrowException_WhenNotFound() {
        // Arrange
        String nombreUsuario = "nonexistentnick";
        when(usuarioRepository.findByNombreUsuario(nombreUsuario)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(NoSuchElementException.class, () -> {
            usuarioService.findByNombreUsuario(nombreUsuario);
        }, "Debe lanzar NoSuchElementException si el usuario no existe.");
    }

    // =================================================================
    // PRUEBAS PARA save
    // =================================================================

    @Test
    void save_ShouldThrowException_OnDataIntegrityViolation() {
        // Arrange
        when(tipoUsuarioService.findById(anyInt())).thenReturn(new TipoUsuario());

        when(estadoClient.getEstadoById(anyInt())).thenReturn(new EstadoDTO());

        when(usuarioRepository.save(any(Usuario.class))).thenThrow(DataIntegrityViolationException.class);

        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> {
            usuarioService.save(usuarioValido);
        }, "Debe lanzar IllegalArgumentException al fallar la integridad de datos.");

        verify(usuarioRepository, times(1)).save(usuarioValido);
    }


    // =================================================================
    // PRUEBAS PARA subirYActualizarFotoUsuario (Funcionalidad clave)
    // =================================================================

    @Test
    void subirYActualizarFotoUsuario_Success() throws Exception {
        // Arrange
        MockMultipartFile mockFile = new MockMultipartFile(
                "foto", "perfil.jpg", "image/jpeg", "datos-binarios".getBytes());

        // Mocks para el flujo exitoso
        when(usuarioRepository.findById(ID_USUARIO)).thenReturn(Optional.of(usuarioValido));
        when(fotoClient.uploadFoto(any(byte[].class), anyString())).thenReturn(ID_FOTO_GUARDADA);

        Usuario usuarioDespuesSave = new Usuario();
        usuarioDespuesSave.setIdUsuario(usuarioValido.getIdUsuario());
        usuarioDespuesSave.setNombre(usuarioValido.getNombre());
        usuarioDespuesSave.setIdFoto(ID_FOTO_GUARDADA);

        // La simulación de save devuelve el usuario con el ID de foto actualizado
        when(usuarioRepository.save(any(Usuario.class))).thenReturn(usuarioDespuesSave);

        // Act
        Usuario result = usuarioService.subirYActualizarFotoUsuario(ID_USUARIO, mockFile);

        // Assert
        assertNotNull(result);
        assertEquals(ID_FOTO_GUARDADA, result.getIdFoto(), "El usuario debe tener el ID de foto actualizado.");

        // Verificaciones de llamadas
        verify(usuarioRepository, times(1)).findById(ID_USUARIO);
        verify(fotoClient, times(1)).uploadFoto(mockFile.getBytes(), mockFile.getOriginalFilename());
        verify(usuarioRepository, times(1)).save(usuarioValido); // Verificamos que se guardó el objeto modificado
    }

    @Test
    void subirYActualizarFotoUsuario_ShouldThrowRuntimeException_WhenUsuarioNotFound() {
        // Arrange
        MockMultipartFile mockFile = new MockMultipartFile(
                "foto", "perfil.jpg", "image/jpeg", "datos-binarios".getBytes());

        // Simular que el usuario no existe
        when(usuarioRepository.findById(ID_USUARIO)).thenReturn(Optional.empty());

        // Act & Assert
        RuntimeException thrown = assertThrows(RuntimeException.class, () -> {
            usuarioService.subirYActualizarFotoUsuario(ID_USUARIO, mockFile);
        });

        assertEquals("Usuario no encontrado con ID: " + ID_USUARIO, thrown.getMessage());

        // Verificar que no se llamó a las APIs externas ni se intentó guardar
        verify(fotoClient, never()).uploadFoto(any(), anyString());
        verify(usuarioRepository, never()).save(any(Usuario.class));
    }

    @Test
    void subirYActualizarFotoUsuario_ShouldThrowRuntimeException_WhenFotoClientFails() {
        // Arrange
        MockMultipartFile mockFile = new MockMultipartFile(
                "foto", "perfil.jpg", "image/jpeg", "datos-binarios".getBytes());

        when(usuarioRepository.findById(ID_USUARIO)).thenReturn(Optional.of(usuarioValido));

        // Simular que la API externa falla con un error de cliente (4xx)
        WebClientResponseException clientException = WebClientResponseException.create(
                400, "Archivo inválido", null, null, null);

        // Simular la falla de la API externa
        doThrow(clientException).when(fotoClient).uploadFoto(any(), anyString());

        // Act & Assert
        RuntimeException thrown = assertThrows(RuntimeException.class, () -> {
            usuarioService.subirYActualizarFotoUsuario(ID_USUARIO, mockFile);
        });

        // Debe propagar la excepción del cliente (o la que la envuelve, dependiendo de cómo se maneje en el cliente)
        assertTrue(thrown instanceof WebClientResponseException || thrown.getCause() instanceof WebClientResponseException,
                "Debe lanzar una excepción relacionada con el fallo del cliente web.");

        // Verificar que NO se intentó guardar el usuario
        verify(usuarioRepository, never()).save(any(Usuario.class));
    }

    @Test
    void subirYActualizarFotoUsuario_ShouldThrowRuntimeException_WhenIOFails() throws IOException {
        // Arrange
        MockMultipartFile mockFile = mock(MockMultipartFile.class); // Mockeamos para simular fallo de I/O

        when(usuarioRepository.findById(ID_USUARIO)).thenReturn(Optional.of(usuarioValido));

        // Simular fallo al leer los bytes del archivo (I/O Exception)
        when(mockFile.getBytes()).thenThrow(new IOException("Error de lectura de archivo"));

        // Act & Assert
        assertThrows(RuntimeException.class, () -> {
            usuarioService.subirYActualizarFotoUsuario(ID_USUARIO, mockFile);
        });

        // Verificar que no se llamó a las APIs externas ni se intentó guardar
        verify(fotoClient, never()).uploadFoto(any(), anyString());
        verify(usuarioRepository, never()).save(any(Usuario.class));
    }
}