package com.SAFE_Rescue.API_Perfiles.service;

import com.SAFE_Rescue.API_Perfiles.config.EstadoClient;
import com.SAFE_Rescue.API_Perfiles.config.FotoClient;
import com.SAFE_Rescue.API_Perfiles.modelo.EstadoDTO;
import com.SAFE_Rescue.API_Perfiles.modelo.TipoUsuario;
import com.SAFE_Rescue.API_Perfiles.modelo.Usuario;
import com.SAFE_Rescue.API_Perfiles.repositoy.UsuarioRepository;
import net.datafaker.Faker;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDate;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class UsuarioServiceTest {

    @Mock
    private UsuarioRepository usuarioRepository;

    @Mock
    private TipoUsuarioService tipoUsuarioService;

    @Mock
    private EstadoClient estadoClient;

    @Mock
    private FotoClient fotoClient;

    @InjectMocks
    private UsuarioService usuarioService;

    private Usuario usuario;
    private Faker faker;
    private Integer id;

    private EstadoDTO estadoDTO = new EstadoDTO();

    @BeforeEach
    public void setUp() {
        faker = new Faker();
        id = faker.number().numberBetween(1, 100);

        // Inicialización de estadoDTO con constructor sin argumentos y setters
        estadoDTO.setIdEstado(1);
        estadoDTO.setNombre("Activo");

        // Crear objetos de dependencia
        TipoUsuario tipoUsuario = new TipoUsuario(1, "Bombero");

        // Crear objeto Usuario con datos simulados
        usuario = new Usuario();
        usuario.setIdUsuario(id);
        usuario.setRun(faker.idNumber().valid());
        usuario.setDv("1");
        usuario.setNombre(faker.name().firstName());
        usuario.setAPaterno(faker.name().lastName());
        usuario.setAMaterno(faker.name().lastName());
        usuario.setFechaRegistro(LocalDate.now());
        usuario.setTelefono(faker.phoneNumber().cellPhone());
        usuario.setCorreo(faker.internet().emailAddress());
        usuario.setContrasenia(faker.internet().password());
        usuario.setIntentosFallidos(0);
        usuario.setRazonBaneo(null);
        usuario.setDiasBaneo(0);
        usuario.setTipoUsuario(tipoUsuario);
        usuario.setIdEstado(estadoDTO.getId());
        usuario.setIdFoto(faker.number().numberBetween(1, 50));
    }

    // --- Pruebas de operaciones CRUD exitosas (sin cambios) ---

    @Test
    public void findAll_shouldReturnAllUsers() {
        when(usuarioRepository.findAll()).thenReturn(List.of(usuario));
        List<Usuario> usuarios = usuarioService.findAll();
        assertNotNull(usuarios);
        assertFalse(usuarios.isEmpty());
        assertEquals(1, usuarios.size());
        assertEquals(usuario.getNombre(), usuarios.get(0).getNombre());
        verify(usuarioRepository, times(1)).findAll();
    }

    @Test
    public void findById_shouldReturnUser_whenUserExists() {
        when(usuarioRepository.findById(id)).thenReturn(Optional.of(usuario));
        Usuario encontrado = usuarioService.findById(id);
        assertNotNull(encontrado);
        assertEquals(usuario.getNombre(), encontrado.getNombre());
        verify(usuarioRepository, times(1)).findById(id);
    }

    @Test
    public void findById_shouldThrowException_whenUserNotFound() {
        when(usuarioRepository.findById(anyInt())).thenReturn(Optional.empty());
        assertThrows(NoSuchElementException.class, () -> usuarioService.findById(999));
        verify(usuarioRepository, times(1)).findById(999);
    }

    @Test
    public void save_shouldReturnSavedUser_whenValid() {
        // Arrange
        when(tipoUsuarioService.findById(any())).thenReturn(usuario.getTipoUsuario());
        when(estadoClient.getEstadoById(anyInt())).thenReturn(estadoDTO);
        when(usuarioRepository.save(any(Usuario.class))).thenReturn(usuario);

        // Act
        Usuario guardado = usuarioService.save(usuario);

        // Assert
        assertNotNull(guardado);
        verify(usuarioRepository, times(1)).save(usuario);
        verify(tipoUsuarioService, times(1)).findById(usuario.getTipoUsuario().getIdTipoUsuario());
        verify(estadoClient, times(1)).getEstadoById(usuario.getIdEstado());
    }

    @Test
    public void update_shouldReturnUpdatedUser_whenUserExists() {
        // Arrange
        Usuario usuarioExistente = new Usuario();
        usuarioExistente.setIdUsuario(id);
        usuarioExistente.setNombre("Nombre Antiguo");

        // Aseguramos que el objeto existente tenga campos válidos
        usuarioExistente.setRun("12345678");
        usuarioExistente.setDv("9");
        usuarioExistente.setAPaterno("Paterno");
        usuarioExistente.setAMaterno("Materno");
        usuarioExistente.setFechaRegistro(LocalDate.now());
        usuarioExistente.setTelefono("987654321");
        usuarioExistente.setCorreo("old@email.com");
        usuarioExistente.setContrasenia("pass");
        usuarioExistente.setTipoUsuario(usuario.getTipoUsuario());
        usuarioExistente.setIdFoto(1);
        usuarioExistente.setIdEstado(estadoDTO.getId()); // ID DE ESTADO VÁLIDO

        when(usuarioRepository.findById(id)).thenReturn(Optional.of(usuarioExistente));
        when(usuarioRepository.save(any(Usuario.class))).thenReturn(usuario);
        when(tipoUsuarioService.findById(any())).thenReturn(usuario.getTipoUsuario());
        when(estadoClient.getEstadoById(anyInt())).thenReturn(estadoDTO);

        // Act
        Usuario actualizado = usuarioService.update(usuario, id);

        // Assert
        assertNotNull(actualizado);
        assertEquals(usuario.getNombre(), actualizado.getNombre());
        verify(usuarioRepository, times(1)).findById(id);
        verify(usuarioRepository, times(1)).save(usuarioExistente);
        verify(tipoUsuarioService, times(1)).findById(usuario.getTipoUsuario().getIdTipoUsuario());
        verify(estadoClient, times(1)).getEstadoById(usuario.getIdEstado());
    }

    @Test
    public void update_shouldThrowException_whenUserNotFound() {
        // Arrange
        // Mocks de validación para que el objeto 'usuario' sea válido antes de la búsqueda.
        when(tipoUsuarioService.findById(any())).thenReturn(usuario.getTipoUsuario());
        when(estadoClient.getEstadoById(anyInt())).thenReturn(estadoDTO);
        when(usuarioRepository.findById(anyInt())).thenReturn(Optional.empty());

        // El objeto 'usuario' es válido, por lo tanto, la excepción debe ser NoSuchElementException
        assertThrows(NoSuchElementException.class, () -> usuarioService.update(usuario, 999));

        verify(usuarioRepository, times(1)).findById(999);
        verify(tipoUsuarioService, times(1)).findById(usuario.getTipoUsuario().getIdTipoUsuario());
        verify(estadoClient, times(1)).getEstadoById(usuario.getIdEstado());
    }

    @Test
    public void delete_shouldDeleteUser_whenUserExists() {
        when(usuarioRepository.findById(id)).thenReturn(Optional.of(usuario));

        // Act & Assert
        assertDoesNotThrow(() -> usuarioService.delete(id));

        verify(usuarioRepository, times(1)).findById(id);
        verify(usuarioRepository, times(1)).delete(usuario);
    }

    // --- Pruebas de escenarios de error (sin cambios) ---

    @Test
    public void save_shouldThrowException_whenDataIntegrityViolation() {
        // Arrange
        when(tipoUsuarioService.findById(any())).thenReturn(usuario.getTipoUsuario());
        when(estadoClient.getEstadoById(anyInt())).thenReturn(estadoDTO);

        // Simular la violación al intentar guardar
        when(usuarioRepository.save(any(Usuario.class))).thenThrow(new DataIntegrityViolationException("RUN o correo duplicado"));

        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> usuarioService.save(usuario));

        // Verificaciones
        verify(tipoUsuarioService, times(1)).findById(usuario.getTipoUsuario().getIdTipoUsuario());
        verify(estadoClient, times(1)).getEstadoById(usuario.getIdEstado());
        verify(usuarioRepository, times(1)).save(usuario);
    }

    @Test
    public void save_shouldThrowException_whenEstadoNotFound() {
        when(tipoUsuarioService.findById(any())).thenReturn(usuario.getTipoUsuario());

        // Simular que el EstadoClient falla (lanza RuntimeException)
        when(estadoClient.getEstadoById(anyInt())).thenThrow(new RuntimeException("El ID no existe."));

        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> usuarioService.save(usuario));

        // Verificaciones
        verify(tipoUsuarioService, times(1)).findById(usuario.getTipoUsuario().getIdTipoUsuario());
        verify(estadoClient, times(1)).getEstadoById(usuario.getIdEstado());
        verify(usuarioRepository, never()).save(any());
    }

    // --- Pruebas del método de subir foto (Ajuste de Verificación) ---

    @Test
    public void subirYActualizarFotoUsuario_shouldUpdateUserWithPhotoId() {
        // Arrange
        String mockPhotoIdString = "12345";
        Integer mockPhotoId = Integer.parseInt(mockPhotoIdString);
        MultipartFile mockFile = mock(MultipartFile.class);

        when(fotoClient.uploadFoto(mockFile)).thenReturn(mockPhotoIdString);
        when(usuarioRepository.findById(id)).thenReturn(Optional.of(usuario));
        when(usuarioRepository.save(any(Usuario.class))).thenReturn(usuario);

        // Act
        Usuario returnedUser = usuarioService.subirYActualizarFotoUsuario(id, mockFile);

        // Assert
        assertNotNull(returnedUser);
        assertEquals(mockPhotoId, returnedUser.getIdFoto());
        verify(fotoClient, times(1)).uploadFoto(mockFile);
        verify(usuarioRepository, times(1)).findById(id);
        verify(usuarioRepository, times(1)).save(usuario);
    }

    @Test
    public void subirYActualizarFotoUsuario_shouldThrowException_whenPhotoUploadFails() {
        // Arrange
        MultipartFile mockFile = mock(MultipartFile.class);

        // Simular fallo de comunicación ANTES de buscar el usuario
        when(fotoClient.uploadFoto(mockFile)).thenThrow(new RuntimeException("Error de conexión"));

        // Act & Assert
        assertThrows(RuntimeException.class, () -> usuarioService.subirYActualizarFotoUsuario(id, mockFile));

        // Assert: Solo verificamos lo que realmente ocurre (la llamada a fotoClient falla)
        // No se debe llamar a findById ni a save.
        verify(fotoClient, times(1)).uploadFoto(mockFile);
        verify(usuarioRepository, never()).findById(anyInt()); // NO SE BUSCA EN BD
        verify(usuarioRepository, never()).save(any());
    }

    @Test
    public void subirYActualizarFotoUsuario_shouldThrowException_whenPhotoIdIsNotNumeric() {
        // Arrange
        MultipartFile mockFile = mock(MultipartFile.class);

        // Simular que la API devuelve una cadena no numérica ANTES de buscar el usuario
        when(fotoClient.uploadFoto(mockFile)).thenReturn("NO_ES_UN_ID");

        // Act & Assert
        assertThrows(RuntimeException.class, () -> usuarioService.subirYActualizarFotoUsuario(id, mockFile));

        // Assert: Solo verificamos lo que realmente ocurre (la llamada a fotoClient tiene éxito)
        // La excepción debe ocurrir al intentar parsear el resultado o al validar en el servicio.
        // Asumiendo que el servicio intenta parsear justo después de la llamada a fotoClient.
        verify(fotoClient, times(1)).uploadFoto(mockFile);
        verify(usuarioRepository, never()).findById(anyInt()); // NO SE BUSCA EN BD
        verify(usuarioRepository, never()).save(any());
    }
}