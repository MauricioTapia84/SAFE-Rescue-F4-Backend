package com.SAFE_Rescue.API_Registros.service;

import com.SAFE_Rescue.API_Registros.modelo.Categoria;
import com.SAFE_Rescue.API_Registros.modelo.Estado;
import com.SAFE_Rescue.API_Registros.modelo.Historial;
import com.SAFE_Rescue.API_Registros.repository.HistorialRepository;
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
 * Pruebas unitarias para la capa de servicio de Historial.
 * Se utiliza Mockito para simular el repositorio y servicios dependientes (EstadoService).
 */
@ExtendWith(MockitoExtension.class)
public class HistorialServiceTest {

    @Mock
    private HistorialRepository historialRepository;

    @Mock
    private EstadoService estadoService;

    @InjectMocks
    private HistorialService historialService;

    private Faker faker;
    private Historial historialValido;
    private Estado estadoValido;
    private Categoria categoriaValida;
    private Integer idHistorial;
    private Integer idEstado;

    @BeforeEach
    void setUp() {
        faker = new Faker();
        idHistorial = faker.number().numberBetween(1, 100);
        idEstado = faker.number().numberBetween(1, 10);

        // Configuración de objetos relacionados
        estadoValido = new Estado();
        estadoValido.setIdEstado(idEstado);
        estadoValido.setNombre("ACTIVO");

        categoriaValida = new Categoria();
        categoriaValida.setIdCategoria(faker.number().numberBetween(1, 5));
        categoriaValida.setNombre("INCENDIO");

        // Objeto Historial base válido
        historialValido = new Historial();
        historialValido.setIdHistorial(idHistorial);
        historialValido.setDetalle(faker.lorem().sentence());
        historialValido.setFechaHistorial(LocalDateTime.now());
        historialValido.setEstado(estadoValido);
        historialValido.setCategoria(categoriaValida);
    }

    // -------------------------------------------------------------------------
    // TEST DE OPERACIONES CRUD
    // -------------------------------------------------------------------------

    @Test
    void findAll_ShouldReturnAllHistorials() {
        // Arrange
        List<Historial> listaEsperada = List.of(historialValido, new Historial());
        when(historialRepository.findAll()).thenReturn(listaEsperada);

        // Act
        List<Historial> resultado = historialService.findAll();

        // Assert
        assertNotNull(resultado);
        assertEquals(2, resultado.size());
        verify(historialRepository, times(1)).findAll();
    }

    @Test
    void findById_ShouldReturnHistorial_WhenFound() {
        // Arrange
        when(historialRepository.findById(idHistorial)).thenReturn(Optional.of(historialValido));

        // Act
        Historial resultado = historialService.findById(idHistorial);

        // Assert
        assertNotNull(resultado);
        assertEquals(idHistorial, resultado.getIdHistorial());
        verify(historialRepository, times(1)).findById(idHistorial);
    }

    @Test
    void findById_ShouldThrowNoSuchElementException_WhenNotFound() {
        // Arrange
        when(historialRepository.findById(idHistorial)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(NoSuchElementException.class, () -> historialService.findById(idHistorial));
        verify(historialRepository, times(1)).findById(idHistorial);
    }

    @Test
    void findByEstadoId_ShouldReturnFilteredHistorials() {
        // Arrange
        List<Historial> listaFiltrada = List.of(historialValido);
        // 1. Simular la búsqueda del Estado
        when(estadoService.findById(idEstado)).thenReturn(estadoValido);
        // 2. Simular la búsqueda en el repositorio usando el objeto Estado
        when(historialRepository.findByEstado(estadoValido)).thenReturn(listaFiltrada);

        // Act
        List<Historial> resultado = historialService.findByEstadoId(idEstado);

        // Assert
        assertNotNull(resultado);
        assertEquals(1, resultado.size());
        assertEquals(idEstado, resultado.get(0).getEstado().getIdEstado());
        verify(estadoService, times(1)).findById(idEstado);
        verify(historialRepository, times(1)).findByEstado(estadoValido);
    }

    @Test
    void save_ShouldReturnSavedHistorial_WhenValid() {
        // Arrange
        when(historialRepository.save(any(Historial.class))).thenReturn(historialValido);

        // Act
        Historial resultado = historialService.save(historialValido);

        // Assert
        assertNotNull(resultado);
        assertEquals(historialValido.getDetalle(), resultado.getDetalle());
        verify(historialRepository, times(1)).save(historialValido);
    }

    @Test
    void save_ShouldThrowIllegalArgumentException_OnDataIntegrityViolation() {
        // Arrange
        doThrow(DataIntegrityViolationException.class).when(historialRepository).save(any(Historial.class));

        // Act & Assert
        IllegalArgumentException thrown = assertThrows(IllegalArgumentException.class, () -> historialService.save(historialValido));
        assertTrue(thrown.getMessage().contains("Error de integridad de datos"));
        verify(historialRepository, times(1)).save(historialValido);
    }

    @Test
    void delete_ShouldBeSuccessful_WhenFound() {
        // Arrange
        when(historialRepository.findById(idHistorial)).thenReturn(Optional.of(historialValido));
        doNothing().when(historialRepository).delete(historialValido);

        // Act
        historialService.delete(idHistorial);

        // Assert
        verify(historialRepository, times(1)).findById(idHistorial);
        verify(historialRepository, times(1)).delete(historialValido);
    }

    @Test
    void delete_ShouldThrowNoSuchElementException_WhenNotFound() {
        // Arrange
        when(historialRepository.findById(idHistorial)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(NoSuchElementException.class, () -> historialService.delete(idHistorial));
        verify(historialRepository, times(1)).findById(idHistorial);
        verify(historialRepository, never()).delete(any(Historial.class));
    }

    @Test
    void delete_ShouldThrowIllegalArgumentException_OnDataIntegrityViolation() {
        // Arrange
        when(historialRepository.findById(idHistorial)).thenReturn(Optional.of(historialValido));
        // Simular un error de restricción de clave foránea al intentar eliminar
        doThrow(DataIntegrityViolationException.class).when(historialRepository).delete(historialValido);

        // Act & Assert
        IllegalArgumentException thrown = assertThrows(IllegalArgumentException.class, () -> historialService.delete(idHistorial));
        assertTrue(thrown.getMessage().contains("No se puede eliminar Historial."));
        verify(historialRepository, times(1)).findById(idHistorial);
        verify(historialRepository, times(1)).delete(historialValido);
    }

    // -------------------------------------------------------------------------
    // TEST DE VALIDACIÓN DE ATRIBUTOS (validarAtributosHistorial)
    // -------------------------------------------------------------------------

    @Test
    void validarAtributosHistorial_ShouldPass_WhenHistorialIsValid() {
        // Act & Assert: No debería lanzar excepción
        assertDoesNotThrow(() -> historialService.validarAtributosHistorial(historialValido));
    }

    @Test
    void validarAtributosHistorial_ShouldThrowException_WhenHistorialIsNull() {
        // Act & Assert
        IllegalArgumentException thrown = assertThrows(IllegalArgumentException.class,
                () -> historialService.validarAtributosHistorial(null));
        assertEquals("El historial no puede ser nulo.", thrown.getMessage());
    }

    @Test
    void validarAtributosHistorial_ShouldThrowException_WhenDetalleIsNull() {
        // Arrange
        historialValido.setDetalle(null);

        // Act & Assert
        IllegalArgumentException thrown = assertThrows(IllegalArgumentException.class,
                () -> historialService.validarAtributosHistorial(historialValido));
        assertEquals("El detalle del historial es un campo obligatorio.", thrown.getMessage());
    }

    @Test
    void validarAtributosHistorial_ShouldThrowException_WhenDetalleIsEmpty() {
        // Arrange
        historialValido.setDetalle("  "); // Espacios en blanco

        // Act & Assert
        IllegalArgumentException thrown = assertThrows(IllegalArgumentException.class,
                () -> historialService.validarAtributosHistorial(historialValido));
        assertEquals("El detalle del historial es un campo obligatorio.", thrown.getMessage());
    }

    @Test
    void validarAtributosHistorial_ShouldThrowException_WhenDetalleIsTooLong() {
        // Arrange: Crear una cadena de más de 250 caracteres
        String detalleLargo = faker.lorem().characters(251);
        historialValido.setDetalle(detalleLargo);

        // Act & Assert
        IllegalArgumentException thrown = assertThrows(IllegalArgumentException.class,
                () -> historialService.validarAtributosHistorial(historialValido));
        assertEquals("El detalle del historial no puede exceder los 250 caracteres.", thrown.getMessage());
    }

    @Test
    void validarAtributosHistorial_ShouldThrowException_WhenFechaHistorialIsNull() {
        // Arrange
        historialValido.setFechaHistorial(null);

        // Act & Assert
        IllegalArgumentException thrown = assertThrows(IllegalArgumentException.class,
                () -> historialService.validarAtributosHistorial(historialValido));
        assertEquals("La fecha del historial es un campo obligatorio.", thrown.getMessage());
    }

    @Test
    void validarAtributosHistorial_ShouldThrowException_WhenEstadoIsNull() {
        // Arrange
        historialValido.setEstado(null);

        // Act & Assert
        IllegalArgumentException thrown = assertThrows(IllegalArgumentException.class,
                () -> historialService.validarAtributosHistorial(historialValido));
        assertEquals("El Estado del historial es un campo obligatorio.", thrown.getMessage());
    }

    @Test
    void validarAtributosHistorial_ShouldThrowException_WhenCategoriaIsNull() {
        // Arrange
        historialValido.setCategoria(null);

        // Act & Assert
        IllegalArgumentException thrown = assertThrows(IllegalArgumentException.class,
                () -> historialService.validarAtributosHistorial(historialValido));
        assertEquals("La Categoría del historial es un campo obligatorio.", thrown.getMessage());
    }
}