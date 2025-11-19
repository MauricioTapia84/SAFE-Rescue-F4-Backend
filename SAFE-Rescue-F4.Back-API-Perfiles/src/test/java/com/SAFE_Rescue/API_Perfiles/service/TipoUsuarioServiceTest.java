package com.SAFE_Rescue.API_Perfiles.service;

import com.SAFE_Rescue.API_Perfiles.modelo.TipoUsuario;
import com.SAFE_Rescue.API_Perfiles.repository.TipoUsuarioRepository;
import net.datafaker.Faker;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DataIntegrityViolationException;

import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class TipoUsuarioServiceTest {

    @Mock
    private TipoUsuarioRepository tipoUsuarioRepository;

    @InjectMocks
    private TipoUsuarioService tipoUsuarioService;

    private TipoUsuario tipoUsuario;
    private Faker faker;
    private Integer id;

    @BeforeEach
    public void setUp() {
        faker = new Faker();
        id = faker.number().numberBetween(1, 100);

        tipoUsuario = new TipoUsuario();
        tipoUsuario.setIdTipoUsuario(id);
        // Aseguramos que el nombre sea válido para evitar fallos de validación en el Arrange
        String position = faker.job().position();
        tipoUsuario.setNombre(position.substring(0, Math.min(50, position.length())));
    }

    // --- Pruebas de operaciones exitosas (Happy Path) ---

    @Test
    public void findAll_shouldReturnAllUserTypes() {
        // Arrange
        when(tipoUsuarioRepository.findAll()).thenReturn(List.of(tipoUsuario));

        // Act
        List<TipoUsuario> tipos = tipoUsuarioService.findAll();

        // Assert
        assertNotNull(tipos);
        assertFalse(tipos.isEmpty());
        assertEquals(1, tipos.size());
        assertEquals(tipoUsuario.getNombre(), tipos.get(0).getNombre());
        verify(tipoUsuarioRepository, times(1)).findAll();
    }

    @Test
    public void findById_shouldReturnUserType_whenFound() {
        // Arrange
        when(tipoUsuarioRepository.findById(id)).thenReturn(Optional.of(tipoUsuario));

        // Act
        TipoUsuario encontrado = tipoUsuarioService.findById(id);

        // Assert
        assertNotNull(encontrado);
        assertEquals(tipoUsuario.getNombre(), encontrado.getNombre());
        verify(tipoUsuarioRepository, times(1)).findById(id);
    }

    @Test
    public void save_shouldReturnSavedUserType_whenValid() {
        // Arrange
        when(tipoUsuarioRepository.save(any(TipoUsuario.class))).thenReturn(tipoUsuario);

        // Act
        TipoUsuario guardado = tipoUsuarioService.save(tipoUsuario);

        // Assert
        assertNotNull(guardado);
        assertEquals(tipoUsuario.getNombre(), guardado.getNombre());
        verify(tipoUsuarioRepository, times(1)).save(tipoUsuario);
    }

    @Test
    public void update_shouldReturnUpdatedUserType_whenValid() {
        // Arrange
        TipoUsuario tipoUsuarioPayload = new TipoUsuario();
        tipoUsuarioPayload.setNombre("Nombre Actualizado");

        // 1. Simular la existencia
        when(tipoUsuarioRepository.findById(id)).thenReturn(Optional.of(tipoUsuario));

        // 2. Simular el resultado del guardado (que debe reflejar el nombre del payload)
        TipoUsuario resultadoGuardado = new TipoUsuario();
        resultadoGuardado.setIdTipoUsuario(id);
        resultadoGuardado.setNombre("Nombre Actualizado");
        when(tipoUsuarioRepository.save(any(TipoUsuario.class))).thenReturn(resultadoGuardado);

        // Act
        TipoUsuario actualizado = tipoUsuarioService.update(tipoUsuarioPayload, id);

        // Assert
        assertNotNull(actualizado);
        assertEquals("Nombre Actualizado", actualizado.getNombre());
        verify(tipoUsuarioRepository, times(1)).findById(id);
        // Verificamos que se llamó a save con CUALQUIER instancia de TipoUsuario
        // (ya que el objeto original 'tipoUsuario' fue modificado por el servicio antes de guardarse)
        verify(tipoUsuarioRepository, times(1)).save(any(TipoUsuario.class));
    }

    @Test
    public void delete_shouldDeleteUserType_whenExists() {
        // ARREGLO CLAVE: Mockear findById para simular la existencia (según el flujo del servicio)
        when(tipoUsuarioRepository.findById(id)).thenReturn(Optional.of(tipoUsuario));
        // NOTA: No necesitamos mockear delete, ya que es void. Solo lo verificamos.

        // Act & Assert
        assertDoesNotThrow(() -> tipoUsuarioService.delete(id));

        // Verificamos que se llamó a findById para confirmar la existencia
        verify(tipoUsuarioRepository, times(1)).findById(id);

        // CORRECCIÓN: Verificamos que se llamó a delete con la entidad, NO con deleteById
        verify(tipoUsuarioRepository, times(1)).delete(tipoUsuario);

        // Aseguramos que existsById no fue llamado (si el servicio usa findById)
        verify(tipoUsuarioRepository, never()).existsById(any());
    }

    // --- Pruebas de escenarios de error ---

    @Test
    public void findById_shouldThrowException_whenNotFound() {
        // Arrange
        when(tipoUsuarioRepository.findById(id)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(NoSuchElementException.class, () -> tipoUsuarioService.findById(id));
        verify(tipoUsuarioRepository, times(1)).findById(id);
    }

    @Test
    public void save_shouldThrowException_whenNameIsNull() {
        // Arrange
        TipoUsuario tipoUsuarioInvalido = new TipoUsuario();
        tipoUsuarioInvalido.setNombre(null);

        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> tipoUsuarioService.save(tipoUsuarioInvalido));
        verify(tipoUsuarioRepository, never()).save(any());
    }

    @Test
    public void save_shouldThrowException_whenNameIsTooLong() {
        // Arrange
        TipoUsuario tipoUsuarioInvalido = new TipoUsuario();
        tipoUsuarioInvalido.setNombre(faker.lorem().characters(51));

        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> tipoUsuarioService.save(tipoUsuarioInvalido));
        verify(tipoUsuarioRepository, never()).save(any());
    }

    @Test
    public void save_shouldThrowException_whenDataIntegrityViolation() {
        // Arrange
        when(tipoUsuarioRepository.save(any(TipoUsuario.class))).thenThrow(DataIntegrityViolationException.class);

        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> tipoUsuarioService.save(tipoUsuario));
        verify(tipoUsuarioRepository, times(1)).save(tipoUsuario);
    }

    @Test
    public void update_shouldThrowException_whenUserTypeNotFound() {
        // Arrange
        when(tipoUsuarioRepository.findById(id)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(NoSuchElementException.class, () -> tipoUsuarioService.update(tipoUsuario, id));
        verify(tipoUsuarioRepository, times(1)).findById(id);
        verify(tipoUsuarioRepository, never()).save(any());
    }

    @Test
    public void update_shouldThrowException_whenNameIsNull() {
        // Arrange
        TipoUsuario tipoUsuarioInvalido = new TipoUsuario();
        tipoUsuarioInvalido.setNombre(null);

        // Act & Assert
        // La validación inicial debe fallar antes de buscar en el repositorio
        assertThrows(IllegalArgumentException.class, () -> tipoUsuarioService.update(tipoUsuarioInvalido, id));
        verify(tipoUsuarioRepository, never()).findById(any());
        verify(tipoUsuarioRepository, never()).save(any());
    }

    @Test
    public void update_shouldThrowException_whenDataIntegrityViolation() {
        // Arrange
        when(tipoUsuarioRepository.findById(id)).thenReturn(Optional.of(tipoUsuario));
        when(tipoUsuarioRepository.save(any(TipoUsuario.class))).thenThrow(DataIntegrityViolationException.class);

        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> tipoUsuarioService.update(tipoUsuario, id));
        verify(tipoUsuarioRepository, times(1)).findById(id);
        verify(tipoUsuarioRepository, times(1)).save(tipoUsuario);
    }

    @Test
    public void delete_shouldThrowException_whenNotFound() {
        // ARREGLO CLAVE: Mockear findById para que devuelva vacío (según el flujo del servicio)
        when(tipoUsuarioRepository.findById(id)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(NoSuchElementException.class, () -> tipoUsuarioService.delete(id));

        // Verificamos que se llamó a findById (que causó la excepción)
        verify(tipoUsuarioRepository, times(1)).findById(id);
        // Verificamos que delete(TipoUsuario) y deleteById(id) nunca fueron llamados
        verify(tipoUsuarioRepository, never()).delete(any());
        verify(tipoUsuarioRepository, never()).deleteById(any());
    }
}