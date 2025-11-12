package com.SAFE_Rescue.API_Registros.service;

import com.SAFE_Rescue.API_Registros.modelo.Categoria;
import com.SAFE_Rescue.API_Registros.repository.CategoriaRepository;
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
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.*;

/**
 * Pruebas unitarias para la capa de servicio de Categoria.
 * Se utiliza Mockito para simular el CategoriaRepository.
 */
@ExtendWith(MockitoExtension.class)
public class CategoriaServiceTest {

    @Mock
    private CategoriaRepository categoriaRepository;

    @InjectMocks
    private CategoriaService categoriaService;

    private Faker faker;
    private Categoria categoriaValida;
    private Integer idCategoria;

    @BeforeEach
    void setUp() {
        faker = new Faker();
        idCategoria = faker.number().numberBetween(1, 10);

        // Objeto Categoria base válido
        categoriaValida = new Categoria();
        categoriaValida.setIdCategoria(idCategoria);
        categoriaValida.setNombre(faker.commerce().productName());
        categoriaValida.setDescripcion(faker.lorem().characters(30));
    }

    // -------------------------------------------------------------------------
    // TEST DE OPERACIONES CRUD Y BÚSQUEDA
    // -------------------------------------------------------------------------

    @Test
    void findAll_ShouldReturnAllCategorias() {
        // Arrange
        List<Categoria> listaEsperada = List.of(categoriaValida, new Categoria());
        when(categoriaRepository.findAll()).thenReturn(listaEsperada);

        // Act
        List<Categoria> resultado = categoriaService.findAll();

        // Assert
        assertNotNull(resultado);
        assertEquals(2, resultado.size());
        verify(categoriaRepository, times(1)).findAll();
    }

    @Test
    void findById_ShouldReturnCategoria_WhenFound() {
        // Arrange
        when(categoriaRepository.findById(idCategoria)).thenReturn(Optional.of(categoriaValida));

        // Act
        Categoria resultado = categoriaService.findById(idCategoria);

        // Assert
        assertNotNull(resultado);
        assertEquals(idCategoria, resultado.getIdCategoria());
        verify(categoriaRepository, times(1)).findById(idCategoria);
    }

    @Test
    void findById_ShouldThrowNoSuchElementException_WhenNotFound() {
        // Arrange
        when(categoriaRepository.findById(idCategoria)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(NoSuchElementException.class, () -> categoriaService.findById(idCategoria));
        verify(categoriaRepository, times(1)).findById(idCategoria);
    }

    @Test
    void findByNombre_ShouldReturnMatchingCategorias() {
        // Arrange
        String nombreBuscado = categoriaValida.getNombre();
        List<Categoria> listaEsperada = List.of(categoriaValida);
        when(categoriaRepository.findByNombre(nombreBuscado)).thenReturn(listaEsperada);

        // Act
        List<Categoria> resultado = categoriaService.findByNombre(nombreBuscado);

        // Assert
        assertNotNull(resultado);
        assertFalse(resultado.isEmpty());
        assertEquals(nombreBuscado, resultado.get(0).getNombre());
        verify(categoriaRepository, times(1)).findByNombre(nombreBuscado);
    }

    // -------------------------------------------------------------------------
    // TEST DE SAVE
    // -------------------------------------------------------------------------

    @Test
    void save_ShouldReturnSavedCategoria_WhenValid() {
        // Arrange
        when(categoriaRepository.save(any(Categoria.class))).thenReturn(categoriaValida);

        // Act
        Categoria resultado = categoriaService.save(categoriaValida);

        // Assert
        assertNotNull(resultado);
        assertEquals(categoriaValida.getNombre(), resultado.getNombre());
        verify(categoriaRepository, times(1)).save(categoriaValida);
    }

    @Test
    void save_ShouldThrowIllegalArgumentException_OnDataIntegrityViolation() {
        // Arrange: Simular error de unicidad (nombre duplicado)
        doThrow(DataIntegrityViolationException.class).when(categoriaRepository).save(any(Categoria.class));

        // Act & Assert
        IllegalArgumentException thrown = assertThrows(IllegalArgumentException.class, () -> categoriaService.save(categoriaValida));
        assertTrue(thrown.getMessage().contains("Ya existe una categoría con ese nombre."));
        verify(categoriaRepository, times(1)).save(categoriaValida);
    }

    // -------------------------------------------------------------------------
    // TEST DE UPDATE
    // -------------------------------------------------------------------------

    @Test
    void update_ShouldBeSuccessful_WhenValid() {
        // Arrange
        String nuevoNombre = "EMERGENCIAS";
        String nuevaDescripcion = "Categoría de alta prioridad";
        Categoria categoriaActualizada = new Categoria();
        categoriaActualizada.setNombre(nuevoNombre);
        categoriaActualizada.setDescripcion(nuevaDescripcion);

        when(categoriaRepository.findById(idCategoria)).thenReturn(Optional.of(categoriaValida));
        when(categoriaRepository.save(any(Categoria.class))).thenReturn(categoriaValida);

        // Act
        categoriaService.update(categoriaActualizada, idCategoria);

        // Assert: Verificar que se llamó a save con la categoría existente y los nuevos valores
        verify(categoriaRepository, times(1)).save(argThat(cat ->
                cat.getIdCategoria()==idCategoria &&
                        cat.getNombre().equals(nuevoNombre) &&
                        cat.getDescripcion().equals(nuevaDescripcion)
        ));
        verify(categoriaRepository, times(1)).findById(idCategoria);
    }

    @Test
    void update_ShouldThrowNoSuchElementException_WhenNotFound() {
        // Arrange
        when(categoriaRepository.findById(idCategoria)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(NoSuchElementException.class, () -> categoriaService.update(categoriaValida, idCategoria));
        verify(categoriaRepository, times(1)).findById(idCategoria);
        verify(categoriaRepository, never()).save(any(Categoria.class));
    }

    @Test
    void update_ShouldThrowIllegalArgumentException_WhenInputCategoriaIsNull() {
        // Act & Assert
        IllegalArgumentException thrown = assertThrows(IllegalArgumentException.class, () -> categoriaService.update(null, idCategoria));
        assertEquals("La categoria a actualizar no puede ser nulo.", thrown.getMessage());
        verify(categoriaRepository, never()).findById(any(Integer.class));
        verify(categoriaRepository, never()).save(any(Categoria.class));
    }

    @Test
    void update_ShouldThrowIllegalArgumentException_OnDataIntegrityViolation() {
        // Arrange
        when(categoriaRepository.findById(idCategoria)).thenReturn(Optional.of(categoriaValida));
        // Simular que el guardado falla por nombre duplicado
        doThrow(DataIntegrityViolationException.class).when(categoriaRepository).save(any(Categoria.class));

        // Act & Assert
        IllegalArgumentException thrown = assertThrows(IllegalArgumentException.class, () -> categoriaService.update(categoriaValida, idCategoria));
        assertTrue(thrown.getMessage().contains("Ya existe una categoría con ese nombre."));
        verify(categoriaRepository, times(1)).save(any(Categoria.class));
    }

    // -------------------------------------------------------------------------
    // TEST DE DELETE
    // -------------------------------------------------------------------------

    @Test
    void delete_ShouldBeSuccessful_WhenFound() {
        // Arrange
        when(categoriaRepository.findById(idCategoria)).thenReturn(Optional.of(categoriaValida));
        doNothing().when(categoriaRepository).delete(categoriaValida);

        // Act & Assert: No debería lanzar excepción
        assertDoesNotThrow(() -> categoriaService.delete(idCategoria));
        verify(categoriaRepository, times(1)).findById(idCategoria);
        verify(categoriaRepository, times(1)).delete(categoriaValida);
    }

    @Test
    void delete_ShouldThrowNoSuchElementException_WhenNotFound() {
        // Arrange
        when(categoriaRepository.findById(idCategoria)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(NoSuchElementException.class, () -> categoriaService.delete(idCategoria));
        verify(categoriaRepository, times(1)).findById(idCategoria);
        verify(categoriaRepository, never()).delete(any(Categoria.class));
    }

    @Test
    void delete_ShouldThrowIllegalArgumentException_OnDataIntegrityViolation() {
        // Arrange
        when(categoriaRepository.findById(idCategoria)).thenReturn(Optional.of(categoriaValida));
        // Simular un error de restricción de clave foránea al intentar eliminar
        doThrow(DataIntegrityViolationException.class).when(categoriaRepository).delete(categoriaValida);

        // Act & Assert
        IllegalArgumentException thrown = assertThrows(IllegalArgumentException.class, () -> categoriaService.delete(idCategoria));
        assertTrue(thrown.getMessage().contains("No se puede eliminar Categoria porque está siendo referenciada por otros registros."));
        verify(categoriaRepository, times(1)).findById(idCategoria);
        verify(categoriaRepository, times(1)).delete(categoriaValida);
    }

    // -------------------------------------------------------------------------
    // TEST DE VALIDACIÓN DE ATRIBUTOS (validarAtributosCategoria)
    // -------------------------------------------------------------------------

    @Test
    void validarAtributosCategoria_ShouldPass_WhenCategoriaIsValid() {
        // Assert: No debería lanzar excepción
        assertDoesNotThrow(() -> categoriaService.validarAtributosCategoria(categoriaValida));
    }

    @Test
    void validarAtributosCategoria_ShouldThrowException_WhenCategoriaIsNull() {
        // Act & Assert
        IllegalArgumentException thrown = assertThrows(IllegalArgumentException.class,
                () -> categoriaService.validarAtributosCategoria(null));
        assertEquals("La categoria no puede ser nula.", thrown.getMessage());
    }

    @Test
    void validarAtributosCategoria_ShouldThrowException_WhenNombreIsNull() {
        // Arrange
        categoriaValida.setNombre(null);

        // Act & Assert
        IllegalArgumentException thrown = assertThrows(IllegalArgumentException.class,
                () -> categoriaService.validarAtributosCategoria(categoriaValida));
        assertEquals("El nombre de la categoria es un campo obligatorio.", thrown.getMessage());
    }

    @Test
    void validarAtributosCategoria_ShouldThrowException_WhenNombreIsEmpty() {
        // Arrange
        categoriaValida.setNombre("  "); // Espacios en blanco

        // Act & Assert
        IllegalArgumentException thrown = assertThrows(IllegalArgumentException.class,
                () -> categoriaService.validarAtributosCategoria(categoriaValida));
        assertEquals("El nombre de la categoria es un campo obligatorio.", thrown.getMessage());
    }

    @Test
    void validarAtributosCategoria_ShouldThrowException_WhenNombreIsTooLong() {
        // Arrange: Crear una cadena de más de 50 caracteres
        String nombreLargo = faker.lorem().characters(51);
        categoriaValida.setNombre(nombreLargo);

        // Act & Assert
        IllegalArgumentException thrown = assertThrows(IllegalArgumentException.class,
                () -> categoriaService.validarAtributosCategoria(categoriaValida));
        assertEquals("El nombre de la categoria no puede exceder los 50 caracteres.", thrown.getMessage());
    }

    @Test
    void validarAtributosCategoria_ShouldThrowException_WhenDescripcionIsTooLong() {
        // Arrange: Crear una cadena de más de 100 caracteres
        String descripcionLarga = faker.lorem().characters(101);
        categoriaValida.setDescripcion(descripcionLarga);

        // Act & Assert
        IllegalArgumentException thrown = assertThrows(IllegalArgumentException.class,
                () -> categoriaService.validarAtributosCategoria(categoriaValida));
        assertEquals("La Descripción de la categoria no puede exceder los 100 caracteres.", thrown.getMessage());
    }

    @Test
    void validarAtributosCategoria_ShouldPass_WhenDescripcionIsNull() {
        // Arrange
        categoriaValida.setDescripcion(null);

        // Act & Assert: La descripción es opcional, debe pasar la validación
        assertDoesNotThrow(() -> categoriaService.validarAtributosCategoria(categoriaValida));
    }
}