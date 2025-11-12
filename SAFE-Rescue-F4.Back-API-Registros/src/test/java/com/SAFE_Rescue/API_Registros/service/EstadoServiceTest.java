package com.SAFE_Rescue.API_Registros.service;

import com.SAFE_Rescue.API_Registros.modelo.Estado;
import com.SAFE_Rescue.API_Registros.repository.EstadoRepository;
import net.datafaker.Faker;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DataIntegrityViolationException;

import java.util.Collections;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Pruebas unitarias para la capa de servicio de Estado.
 * Se utiliza Mockito para simular el EstadoRepository.
 */
@ExtendWith(MockitoExtension.class)
public class EstadoServiceTest {

    @Mock
    private EstadoRepository estadoRepository;

    @InjectMocks
    private EstadoService estadoService;

    private Faker faker;
    private Estado estadoValido;
    private Integer idEstado;

    @BeforeEach
    void setUp() {
        faker = new Faker();
        idEstado = faker.number().numberBetween(1, 10);

        // Objeto Estado base válido
        estadoValido = new Estado();
        estadoValido.setIdEstado(idEstado);
        estadoValido.setNombre(faker.lorem().word().toUpperCase());
        estadoValido.setDescripcion(faker.lorem().characters(30));
    }

    // -------------------------------------------------------------------------
    // TEST DE OPERACIONES CRUD Y BÚSQUEDA
    // -------------------------------------------------------------------------

    @Test
    void findAll_ShouldReturnAllEstados() {
        // Arrange
        List<Estado> listaEsperada = List.of(estadoValido, new Estado());
        when(estadoRepository.findAll()).thenReturn(listaEsperada);

        // Act
        List<Estado> resultado = estadoService.findAll();

        // Assert
        assertNotNull(resultado);
        assertEquals(2, resultado.size());
        verify(estadoRepository, times(1)).findAll();
    }

    @Test
    void findById_ShouldReturnEstado_WhenFound() {
        // Arrange
        when(estadoRepository.findById(idEstado)).thenReturn(Optional.of(estadoValido));

        // Act
        Estado resultado = estadoService.findById(idEstado);

        // Assert
        assertNotNull(resultado);
        assertEquals(idEstado, resultado.getIdEstado());
        verify(estadoRepository, times(1)).findById(idEstado);
    }

    @Test
    void findById_ShouldThrowNoSuchElementException_WhenNotFound() {
        // Arrange
        when(estadoRepository.findById(idEstado)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(NoSuchElementException.class, () -> estadoService.findById(idEstado));
        verify(estadoRepository, times(1)).findById(idEstado);
    }

    @Test
    void findByNombre_ShouldReturnMatchingEstados() {
        // Arrange
        String nombreBuscado = estadoValido.getNombre();
        List<Estado> listaEsperada = List.of(estadoValido);
        when(estadoRepository.findByNombre(nombreBuscado)).thenReturn(listaEsperada);

        // Act
        List<Estado> resultado = estadoService.findByNombre(nombreBuscado);

        // Assert
        assertNotNull(resultado);
        assertFalse(resultado.isEmpty());
        assertEquals(nombreBuscado, resultado.get(0).getNombre());
        verify(estadoRepository, times(1)).findByNombre(nombreBuscado);
    }

    // -------------------------------------------------------------------------
    // TEST DE SAVE
    // -------------------------------------------------------------------------

    @Test
    void save_ShouldReturnSavedEstado_WhenValid() {
        // Arrange
        when(estadoRepository.save(any(Estado.class))).thenReturn(estadoValido);

        // Act
        Estado resultado = estadoService.save(estadoValido);

        // Assert
        assertNotNull(resultado);
        assertEquals(estadoValido.getNombre(), resultado.getNombre());
        verify(estadoRepository, times(1)).save(estadoValido);
    }

    @Test
    void save_ShouldThrowIllegalArgumentException_OnDataIntegrityViolation() {
        // Arrange: Simular error de unicidad al guardar
        doThrow(DataIntegrityViolationException.class).when(estadoRepository).save(any(Estado.class));

        // Act & Assert
        IllegalArgumentException thrown = assertThrows(IllegalArgumentException.class, () -> estadoService.save(estadoValido));
        assertTrue(thrown.getMessage().contains("El nombre del estado ya existe o los datos son inválidos."));
        verify(estadoRepository, times(1)).save(estadoValido);
    }

    // -------------------------------------------------------------------------
    // TEST DE UPDATE
    // -------------------------------------------------------------------------

    @Test
    void update_ShouldBeSuccessful_WhenValid() {
        // Arrange
        String nuevoNombre = "INACTIVO";
        String nuevaDescripcion = "Estado ya no utilizado";
        Estado estadoActualizado = new Estado();
        estadoActualizado.setNombre(nuevoNombre);
        estadoActualizado.setDescripcion(nuevaDescripcion);

        when(estadoRepository.findById(idEstado)).thenReturn(Optional.of(estadoValido));
        when(estadoRepository.save(any(Estado.class))).thenReturn(estadoValido);

        // Act
        estadoService.update(estadoActualizado, idEstado);

        // Assert: Verificar que se llamó a save con el estado existente y los nuevos valores
        verify(estadoRepository, times(1)).save(argThat(estado ->
                estado.getIdEstado()==idEstado &&
                        estado.getNombre().equals(nuevoNombre) &&
                        estado.getDescripcion().equals(nuevaDescripcion)
        ));
    }

    @Test
    void update_ShouldThrowNoSuchElementException_WhenNotFound() {
        // Arrange
        when(estadoRepository.findById(idEstado)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(NoSuchElementException.class, () -> estadoService.update(estadoValido, idEstado));
        verify(estadoRepository, times(1)).findById(idEstado);
        verify(estadoRepository, never()).save(any(Estado.class));
    }

    @Test
    void update_ShouldThrowIllegalArgumentException_OnDataIntegrityViolation() {
        // Arrange
        when(estadoRepository.findById(idEstado)).thenReturn(Optional.of(estadoValido));
        // Simular que el guardado falla por nombre duplicado (DataIntegrityViolationException)
        doThrow(DataIntegrityViolationException.class).when(estadoRepository).save(any(Estado.class));

        // Act & Assert
        IllegalArgumentException thrown = assertThrows(IllegalArgumentException.class, () -> estadoService.update(estadoValido, idEstado));
        assertTrue(thrown.getMessage().contains("El nombre del estado ya existe."));
        verify(estadoRepository, times(1)).save(any(Estado.class));
    }

    // -------------------------------------------------------------------------
    // TEST DE DELETE
    // -------------------------------------------------------------------------

    @Test
    void delete_ShouldBeSuccessful_WhenFound() {
        // Arrange
        when(estadoRepository.findById(idEstado)).thenReturn(Optional.of(estadoValido));
        doNothing().when(estadoRepository).delete(estadoValido);

        // Act & Assert: No debería lanzar excepción
        assertDoesNotThrow(() -> estadoService.delete(idEstado));
        verify(estadoRepository, times(1)).findById(idEstado);
        verify(estadoRepository, times(1)).delete(estadoValido);
    }

    @Test
    void delete_ShouldThrowNoSuchElementException_WhenNotFound() {
        // Arrange
        when(estadoRepository.findById(idEstado)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(NoSuchElementException.class, () -> estadoService.delete(idEstado));
        verify(estadoRepository, times(1)).findById(idEstado);
        verify(estadoRepository, never()).delete(any(Estado.class));
    }

    @Test
    void delete_ShouldThrowIllegalArgumentException_OnDataIntegrityViolation() {
        // Arrange
        when(estadoRepository.findById(idEstado)).thenReturn(Optional.of(estadoValido));
        // Simular un error de restricción de clave foránea al intentar eliminar (estado en uso)
        doThrow(DataIntegrityViolationException.class).when(estadoRepository).delete(estadoValido);

        // Act & Assert
        IllegalArgumentException thrown = assertThrows(IllegalArgumentException.class, () -> estadoService.delete(idEstado));
        assertTrue(thrown.getMessage().contains("Está siendo utilizado por otros registros."));
        verify(estadoRepository, times(1)).findById(idEstado);
        verify(estadoRepository, times(1)).delete(estadoValido);
    }

    // -------------------------------------------------------------------------
    // TEST DE VALIDACIÓN DE ATRIBUTOS (validarAtributosEstado)
    // -------------------------------------------------------------------------

    @Test
    void validarAtributosEstado_ShouldPass_WhenEstadoIsValid() {
        // Assert: No debería lanzar excepción
        assertDoesNotThrow(() -> estadoService.validarAtributosEstado(estadoValido));
    }

    @Test
    void validarAtributosEstado_ShouldThrowException_WhenEstadoIsNull() {
        // Act & Assert
        IllegalArgumentException thrown = assertThrows(IllegalArgumentException.class,
                () -> estadoService.validarAtributosEstado(null));
        assertEquals("El estado no puede ser nulo.", thrown.getMessage());
    }

    @Test
    void validarAtributosEstado_ShouldThrowException_WhenNombreIsNull() {
        // Arrange
        estadoValido.setNombre(null);

        // Act & Assert
        IllegalArgumentException thrown = assertThrows(IllegalArgumentException.class,
                () -> estadoService.validarAtributosEstado(estadoValido));
        assertEquals("El nombre del estado es un campo obligatorio.", thrown.getMessage());
    }

    @Test
    void validarAtributosEstado_ShouldThrowException_WhenNombreIsEmpty() {
        // Arrange
        estadoValido.setNombre("  "); // Espacios en blanco

        // Act & Assert
        IllegalArgumentException thrown = assertThrows(IllegalArgumentException.class,
                () -> estadoService.validarAtributosEstado(estadoValido));
        assertEquals("El nombre del estado es un campo obligatorio.", thrown.getMessage());
    }

    @Test
    void validarAtributosEstado_ShouldThrowException_WhenNombreIsTooLong() {
        // Arrange: Crear una cadena de más de 50 caracteres
        String nombreLargo = faker.lorem().characters(51);
        estadoValido.setNombre(nombreLargo);

        // Act & Assert
        IllegalArgumentException thrown = assertThrows(IllegalArgumentException.class,
                () -> estadoService.validarAtributosEstado(estadoValido));
        assertEquals("El nombre del estado no puede exceder los 50 caracteres.", thrown.getMessage());
    }

    @Test
    void validarAtributosEstado_ShouldThrowException_WhenDescripcionIsTooLong() {
        // Arrange: Crear una cadena de más de 100 caracteres
        String descripcionLarga = faker.lorem().characters(101);
        estadoValido.setDescripcion(descripcionLarga);

        // Act & Assert
        IllegalArgumentException thrown = assertThrows(IllegalArgumentException.class,
                () -> estadoService.validarAtributosEstado(estadoValido));
        assertEquals("La Descripción del estado no puede exceder los 100 caracteres.", thrown.getMessage());
    }

    @Test
    void validarAtributosEstado_ShouldPass_WhenDescripcionIsNull() {
        // Arrange
        estadoValido.setDescripcion(null);

        // Act & Assert: La descripción es opcional, debe pasar la validación
        assertDoesNotThrow(() -> estadoService.validarAtributosEstado(estadoValido));
    }
}