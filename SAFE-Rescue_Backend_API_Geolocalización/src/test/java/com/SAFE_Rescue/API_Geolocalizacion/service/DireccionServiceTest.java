package com.SAFE_Rescue.API_Geolocalizacion.service;

import com.SAFE_Rescue.API_Geolocalizacion.modelo.Comuna;
import com.SAFE_Rescue.API_Geolocalizacion.modelo.Direccion;
import com.SAFE_Rescue.API_Geolocalizacion.modelo.Geolocalizacion;
import com.SAFE_Rescue.API_Geolocalizacion.repositoy.DireccionRepository;
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
public class DireccionServiceTest {

    @Mock
    private DireccionRepository direccionRepository;

    @InjectMocks
    private DireccionService direccionService;

    private Direccion direccion;
    private Comuna comuna;
    private Geolocalizacion geolocalizacion;
    private Faker faker;
    private Integer id;

    @BeforeEach
    public void setUp() {
        faker = new Faker();
        id = faker.number().numberBetween(1, 100);

        // 1. Crear dependencias
        comuna = new Comuna();
        comuna.setIdComuna(10);
        comuna.setNombre("Maipú");

        geolocalizacion = new Geolocalizacion();
        geolocalizacion.setIdGeolocalizacion(20);
        geolocalizacion.setLatitud((float) faker.number().randomDouble(6, (long) -33.5,(long) -33.4));
        geolocalizacion.setLongitud((float) faker.number().randomDouble(6,(long) -70.7,(long) -70.6));

        // 2. Crear la entidad principal
        direccion = new Direccion();
        direccion.setIdDireccion(id);
        direccion.setCalle("Avenida Pajaritos");
        direccion.setNumero("1234");
        direccion.setComplemento("Depto 501");
        direccion.setComuna(comuna);
        direccion.setGeolocalizacion(geolocalizacion);
    }

    // --- Pruebas de operaciones exitosas (Happy Path) ---

    @Test
    public void findAll_shouldReturnAllDirecciones() {
        // Arrange
        when(direccionRepository.findAll()).thenReturn(List.of(direccion));

        // Act
        List<Direccion> direcciones = direccionService.findAll();

        // Assert
        assertNotNull(direcciones);
        assertFalse(direcciones.isEmpty());
        assertEquals(1, direcciones.size());
        assertEquals(direccion.getCalle(), direcciones.get(0).getCalle());
        verify(direccionRepository, times(1)).findAll();
    }

    @Test
    public void findById_shouldReturnDireccion_whenFound() {
        // Arrange
        when(direccionRepository.findById(id)).thenReturn(Optional.of(direccion));

        // Act
        Direccion encontrada = direccionService.findById(id);

        // Assert
        assertNotNull(encontrada);
        assertEquals(direccion.getCalle(), encontrada.getCalle());
        verify(direccionRepository, times(1)).findById(id);
    }

    @Test
    public void save_shouldReturnSavedDireccion_whenValid() {
        // Arrange
        when(direccionRepository.save(any(Direccion.class))).thenReturn(direccion);

        // Act
        Direccion guardada = direccionService.save(direccion);

        // Assert
        assertNotNull(guardada);
        assertEquals(direccion.getCalle(), guardada.getCalle());
        verify(direccionRepository, times(1)).save(direccion);
    }

    @Test
    public void update_shouldReturnUpdatedDireccion_whenValid() {
        // Arrange
        String nuevaCalle = "Nueva Calle Central";
        String nuevoNumero = "567";

        Direccion direccionConNuevosDatos = new Direccion();
        direccionConNuevosDatos.setCalle(nuevaCalle);
        direccionConNuevosDatos.setNumero(nuevoNumero);
        direccionConNuevosDatos.setComuna(comuna); // Mantener dependencias válidas
        direccionConNuevosDatos.setGeolocalizacion(geolocalizacion);

        when(direccionRepository.findById(id)).thenReturn(Optional.of(direccion));

        Direccion direccionModificada = new Direccion();
        direccionModificada.setIdDireccion(id);
        direccionModificada.setCalle(nuevaCalle);
        direccionModificada.setNumero(nuevoNumero);
        direccionModificada.setComuna(comuna);
        direccionModificada.setGeolocalizacion(geolocalizacion);

        when(direccionRepository.save(any(Direccion.class))).thenReturn(direccionModificada);

        // Act
        Direccion actualizado = direccionService.update(direccionConNuevosDatos, id);

        // Assert
        assertNotNull(actualizado);
        assertEquals(nuevaCalle, actualizado.getCalle());
        assertEquals(nuevoNumero, actualizado.getNumero());
        verify(direccionRepository, times(1)).findById(id);
        verify(direccionRepository, times(1)).save(direccion);
    }

    @Test
    public void delete_shouldDeleteDireccion_whenExists() {
        // Arrange
        when(direccionRepository.existsById(id)).thenReturn(true);
        doNothing().when(direccionRepository).deleteById(id);

        // Act & Assert
        assertDoesNotThrow(() -> direccionService.delete(id));
        verify(direccionRepository, times(1)).existsById(id);
        verify(direccionRepository, times(1)).deleteById(id);
    }

    // --- Pruebas de escenarios de error (Validaciones) ---

    @Test
    public void findById_shouldThrowException_whenNotFound() {
        // Arrange
        when(direccionRepository.findById(id)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(NoSuchElementException.class, () -> direccionService.findById(id));
        verify(direccionRepository, times(1)).findById(id);
    }

    // --- Pruebas de Validación en SAVE ---

    @Test
    public void save_shouldThrowException_whenCalleIsNull() {
        // Arrange
        direccion.setCalle(null);

        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> direccionService.save(direccion));
        verify(direccionRepository, never()).save(any());
    }

    @Test
    public void save_shouldThrowException_whenNumeroIsEmpty() {
        // Arrange
        direccion.setNumero("   ");

        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> direccionService.save(direccion));
        verify(direccionRepository, never()).save(any());
    }

    @Test
    public void save_shouldThrowException_whenComunaIsNull() {
        // Arrange
        direccion.setComuna(null);

        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> direccionService.save(direccion));
        verify(direccionRepository, never()).save(any());
    }

    @Test
    public void save_shouldThrowException_whenGeolocalizacionIsNull() {
        // Arrange
        direccion.setGeolocalizacion(null);

        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> direccionService.save(direccion));
        verify(direccionRepository, never()).save(any());
    }

    @Test
    public void save_shouldThrowException_whenCalleIsTooLong() {
        // Arrange
        // Más de 150 caracteres
        direccion.setCalle(faker.lorem().characters(151));

        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> direccionService.save(direccion));
        verify(direccionRepository, never()).save(any());
    }

    @Test
    public void save_shouldThrowException_whenDataIntegrityViolation_invalidFK() {
        // Arrange
        // Simula que la Comuna o Geolocalizacion no existen en la BD
        when(direccionRepository.save(any(Direccion.class))).thenThrow(DataIntegrityViolationException.class);

        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> direccionService.save(direccion));
        verify(direccionRepository, times(1)).save(direccion);
    }

    // --- Pruebas de Validación y Errores en UPDATE ---

    @Test
    public void update_shouldThrowException_whenDireccionIsNull() {
        // Arrange
        Direccion nullDireccion = null;

        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> direccionService.update(nullDireccion, id));
        verify(direccionRepository, never()).findById(any());
    }

    @Test
    public void update_shouldThrowException_whenDireccionNotFound() {
        // Arrange
        when(direccionRepository.findById(id)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(NoSuchElementException.class, () -> direccionService.update(direccion, id));
        verify(direccionRepository, times(1)).findById(id);
        verify(direccionRepository, never()).save(any());
    }

    @Test
    public void update_shouldThrowException_whenDataIntegrityViolation() {
        // Arrange
        when(direccionRepository.findById(id)).thenReturn(Optional.of(direccion));
        when(direccionRepository.save(any(Direccion.class))).thenThrow(DataIntegrityViolationException.class);

        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> direccionService.update(direccion, id));
        verify(direccionRepository, times(1)).findById(id);
        verify(direccionRepository, times(1)).save(direccion);
    }

    // --- Pruebas de Eliminación ---

    @Test
    public void delete_shouldThrowException_whenNotFound() {
        // Arrange
        when(direccionRepository.existsById(id)).thenReturn(false);

        // Act & Assert
        assertThrows(NoSuchElementException.class, () -> direccionService.delete(id));
        verify(direccionRepository, times(1)).existsById(id);
        verify(direccionRepository, never()).deleteById(any());
    }
}