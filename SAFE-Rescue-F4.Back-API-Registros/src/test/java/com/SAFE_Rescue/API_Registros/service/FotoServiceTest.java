package com.SAFE_Rescue.API_Registros.service;

import com.SAFE_Rescue.API_Registros.modelo.Foto;
import com.SAFE_Rescue.API_Registros.repository.FotoRepository;
import net.datafaker.Faker;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DataIntegrityViolationException;

import java.time.LocalDateTime;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Pruebas unitarias para la capa de servicio de Foto.
 * Se utiliza Mockito para simular el FotoRepository.
 */
@ExtendWith(MockitoExtension.class)
public class FotoServiceTest {

    @Mock
    private FotoRepository fotoRepository;

    @InjectMocks
    private FotoService fotoService;

    private Faker faker;
    private Foto fotoValida;
    private Integer idFoto;

    @BeforeEach
    void setUp() {
        faker = new Faker();
        idFoto = faker.number().numberBetween(1, 100);

        // Objeto Foto base válido
        fotoValida = new Foto();
        fotoValida.setIdFoto(idFoto);
        fotoValida.setUrl(faker.internet().url());
        fotoValida.setFechaSubida(LocalDateTime.now());
        fotoValida.setDescripcion(faker.lorem().characters(10));
    }

    // -------------------------------------------------------------------------
    // TEST DE OPERACIONES CRUD BÁSICAS
    // -------------------------------------------------------------------------

    @Test
    void findAll_ShouldReturnAllFotos() {
        // Arrange
        List<Foto> listaEsperada = List.of(fotoValida, new Foto());
        when(fotoRepository.findAll()).thenReturn(listaEsperada);

        // Act
        List<Foto> resultado = fotoService.findAll();

        // Assert
        assertNotNull(resultado);
        assertEquals(2, resultado.size());
        verify(fotoRepository, times(1)).findAll();
    }

    @Test
    void findById_ShouldReturnFoto_WhenFound() {
        // Arrange
        when(fotoRepository.findById(idFoto)).thenReturn(Optional.of(fotoValida));

        // Act
        Foto resultado = fotoService.findById(idFoto);

        // Assert
        assertNotNull(resultado);
        assertEquals(idFoto, resultado.getIdFoto());
        verify(fotoRepository, times(1)).findById(idFoto);
    }

    @Test
    void findById_ShouldThrowNoSuchElementException_WhenNotFound() {
        // Arrange
        when(fotoRepository.findById(idFoto)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(NoSuchElementException.class, () -> fotoService.findById(idFoto));
        verify(fotoRepository, times(1)).findById(idFoto);
    }

    // -------------------------------------------------------------------------
    // TEST DE SAVE
    // -------------------------------------------------------------------------

    @Test
    void save_ShouldReturnSavedFoto_WhenValid() {
        // Arrange
        when(fotoRepository.save(any(Foto.class))).thenReturn(fotoValida);

        // Act
        Foto resultado = fotoService.save(fotoValida);

        // Assert
        assertNotNull(resultado);
        assertEquals(fotoValida.getUrl(), resultado.getUrl());
        verify(fotoRepository, times(1)).save(fotoValida);
    }

    @Test
    void save_ShouldThrowIllegalArgumentException_OnValidationFailure() {
        // Arrange: Invalidar la foto quitando la URL
        fotoValida.setUrl(null);

        // Act & Assert: La excepción es lanzada por la validación interna del servicio
        assertThrows(IllegalArgumentException.class, () -> fotoService.save(fotoValida));
        verify(fotoRepository, never()).save(any(Foto.class));
    }

    @Test
    void save_ShouldThrowIllegalArgumentException_OnRepositorySaveFailure() {
        // Arrange: Simular error de integridad de datos (unicidad de URL, por ejemplo)
        doThrow(IllegalArgumentException.class).when(fotoRepository).save(any(Foto.class));

        // Act & Assert
        IllegalArgumentException thrown = assertThrows(IllegalArgumentException.class, () -> fotoService.save(fotoValida));
        assertTrue(thrown.getMessage().contains("Error de integridad de datos."));
        verify(fotoRepository, times(1)).save(fotoValida);
    }

    // -------------------------------------------------------------------------
    // TEST DE UPDATE
    // -------------------------------------------------------------------------

    @Test
    void update_ShouldBeSuccessful_WhenValid() {
        // Arrange
        Foto fotoActualizada = new Foto();
        fotoActualizada.setUrl(faker.internet().url());
        fotoActualizada.setFechaSubida(LocalDateTime.now());
        fotoActualizada.setDescripcion("Nueva descripción");

        when(fotoRepository.findById(idFoto)).thenReturn(Optional.of(fotoValida));
        when(fotoRepository.save(any(Foto.class))).thenReturn(fotoActualizada);

        // Act
        fotoService.update(fotoActualizada, idFoto);

        // Assert
        // Capturamos el argumento pasado a save para verificar la actualización
        verify(fotoRepository, times(1)).save(any(Foto.class));
        verify(fotoRepository, times(1)).findById(idFoto);
    }

    @Test
    void update_ShouldThrowNoSuchElementException_WhenNotFound() {
        // Arrange
        when(fotoRepository.findById(idFoto)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(NoSuchElementException.class, () -> fotoService.update(fotoValida, idFoto));
        verify(fotoRepository, times(1)).findById(idFoto);
        verify(fotoRepository, never()).save(any(Foto.class));
    }

    @Test
    void update_ShouldThrowIllegalArgumentException_WhenInputFotoIsNull() {
        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> fotoService.update(null, idFoto));
        verify(fotoRepository, never()).findById(any(Integer.class));
        verify(fotoRepository, never()).save(any(Foto.class));
    }

    @Test
    void update_ShouldThrowIllegalArgumentException_OnDataIntegrityViolation() {
        // Arrange
        when(fotoRepository.findById(idFoto)).thenReturn(Optional.of(fotoValida));
        // Simular que el guardado falla por URL duplicada (ej: otro registro ya tiene esta URL)
        doThrow(DataIntegrityViolationException.class).when(fotoRepository).save(any(Foto.class));

        // Act & Assert
        IllegalArgumentException thrown = assertThrows(IllegalArgumentException.class, () -> fotoService.update(fotoValida, idFoto));
        assertTrue(thrown.getMessage().contains("La URL de la foto ya existe."));
        verify(fotoRepository, times(1)).findById(idFoto);
        verify(fotoRepository, times(1)).save(any(Foto.class));
    }

    // -------------------------------------------------------------------------
    // TEST DE DELETE
    // -------------------------------------------------------------------------

    @Test
    void delete_ShouldBeSuccessful_WhenFound() {
        // Arrange
        when(fotoRepository.findById(idFoto)).thenReturn(Optional.of(fotoValida));
        doNothing().when(fotoRepository).delete(fotoValida);

        // Act & Assert: No debería lanzar excepción
        assertDoesNotThrow(() -> fotoService.delete(idFoto));
        verify(fotoRepository, times(1)).findById(idFoto);
        verify(fotoRepository, times(1)).delete(fotoValida);
    }

    @Test
    void delete_ShouldThrowNoSuchElementException_WhenNotFound() {
        // Arrange
        when(fotoRepository.findById(idFoto)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(NoSuchElementException.class, () -> fotoService.delete(idFoto));
        verify(fotoRepository, times(1)).findById(idFoto);
        verify(fotoRepository, never()).delete(any(Foto.class));
    }

    @Test
    void delete_ShouldThrowIllegalArgumentException_OnDataIntegrityViolation() {
        // Arrange
        when(fotoRepository.findById(idFoto)).thenReturn(Optional.of(fotoValida));
        // Simular un error de restricción de clave foránea al intentar eliminar (foto asociada a un usuario)
        doThrow(DataIntegrityViolationException.class).when(fotoRepository).delete(fotoValida);

        // Act & Assert
        IllegalArgumentException thrown = assertThrows(IllegalArgumentException.class, () -> fotoService.delete(idFoto));
        assertTrue(thrown.getMessage().contains("No se puede eliminar la foto, está siendo utilizada por un usuario."));
        verify(fotoRepository, times(1)).findById(idFoto);
        verify(fotoRepository, times(1)).delete(fotoValida);
    }

    // -------------------------------------------------------------------------
    // TEST DE VALIDACIÓN DE ATRIBUTOS (validarAtributosFoto)
    // -------------------------------------------------------------------------

    @Test
    void validarAtributosFoto_ShouldPass_WhenFotoIsValid() {
        // Assert: No debería lanzar excepción
        assertDoesNotThrow(() -> fotoService.validarAtributosFoto(fotoValida));
    }

    @Test
    void validarAtributosFoto_ShouldThrowException_WhenFotoIsNull() {
        // Act & Assert
        IllegalArgumentException thrown = assertThrows(IllegalArgumentException.class,
                () -> fotoService.validarAtributosFoto(null));
        assertEquals("La foto no puede ser nula.", thrown.getMessage());
    }

    @Test
    void validarAtributosFoto_ShouldThrowException_WhenUrlIsNull() {
        // Arrange
        fotoValida.setUrl(null);

        // Act & Assert
        IllegalArgumentException thrown = assertThrows(IllegalArgumentException.class,
                () -> fotoService.validarAtributosFoto(fotoValida));
        assertEquals("La URL de la foto es un campo obligatorio.", thrown.getMessage());
    }

    @Test
    void validarAtributosFoto_ShouldThrowException_WhenUrlIsTooLong() {
        // Arrange
        String urlLarga = faker.lorem().characters(256); // Más de 255
        fotoValida.setUrl(urlLarga);

        // Act & Assert
        IllegalArgumentException thrown = assertThrows(IllegalArgumentException.class,
                () -> fotoService.validarAtributosFoto(fotoValida));
        assertEquals("La URL de la foto no puede exceder los 255 caracteres.", thrown.getMessage());
    }

    @Test
    void validarAtributosFoto_ShouldThrowException_WhenFechaSubidaIsNull() {
        // Arrange
        fotoValida.setFechaSubida(null);

        // Act & Assert
        IllegalArgumentException thrown = assertThrows(IllegalArgumentException.class,
                () -> fotoService.validarAtributosFoto(fotoValida));
        assertEquals("La Fecha Subida de la foto es un campo obligatorio.", thrown.getMessage());
    }

    @Test
    void validarAtributosFoto_ShouldThrowException_WhenDescripcionIsTooLong() {
        // Arrange
        String descripcionLarga = faker.lorem().characters(101); // Más de 100
        fotoValida.setDescripcion(descripcionLarga);

        // Act & Assert
        IllegalArgumentException thrown = assertThrows(IllegalArgumentException.class,
                () -> fotoService.validarAtributosFoto(fotoValida));
        assertEquals("La Descripción de la foto no puede exceder los 100 caracteres.", thrown.getMessage());
    }

    @Test
    void validarAtributosFoto_ShouldPass_WhenDescripcionIsNull() {
        // Arrange
        fotoValida.setDescripcion(null);

        // Act & Assert: La descripción es opcional, debe pasar la validación
        assertDoesNotThrow(() -> fotoService.validarAtributosFoto(fotoValida));
    }
}