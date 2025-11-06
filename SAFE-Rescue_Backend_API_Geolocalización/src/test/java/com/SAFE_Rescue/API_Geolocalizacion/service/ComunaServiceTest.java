package com.SAFE_Rescue.API_Geolocalizacion.service;

import com.SAFE_Rescue.API_Geolocalizacion.modelo.Comuna;
import com.SAFE_Rescue.API_Geolocalizacion.modelo.Region;
import com.SAFE_Rescue.API_Geolocalizacion.repositoy.ComunaRepository;
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
public class ComunaServiceTest {

    @Mock
    private ComunaRepository comunaRepository;

    @InjectMocks
    private ComunaService comunaService;

    private Comuna comuna;
    private Region region;
    private Faker faker;
    private Integer id;

    @BeforeEach
    public void setUp() {
        faker = new Faker();
        id = faker.number().numberBetween(1, 100);

        // 1. Crear el objeto Region (dependencia)
        region = new Region();
        region.setIdRegion(1);
        region.setNombre("Región Metropolitana");

        // 2. Crear el objeto Comuna
        comuna = new Comuna();
        comuna.setIdComuna(id);
        comuna.setNombre("Las Condes");
        comuna.setCodigoPostal("7550000");
        comuna.setRegion(region);
    }

    // --- Pruebas de operaciones exitosas (Happy Path) ---

    @Test
    public void findAll_shouldReturnAllComunas() {
        // Arrange
        when(comunaRepository.findAll()).thenReturn(List.of(comuna));

        // Act
        List<Comuna> comunas = comunaService.findAll();

        // Assert
        assertNotNull(comunas);
        assertFalse(comunas.isEmpty());
        assertEquals(1, comunas.size());
        assertEquals(comuna.getNombre(), comunas.get(0).getNombre());
        verify(comunaRepository, times(1)).findAll();
    }

    @Test
    public void findById_shouldReturnComuna_whenFound() {
        // Arrange
        when(comunaRepository.findById(id)).thenReturn(Optional.of(comuna));

        // Act
        Comuna encontrada = comunaService.findById(id);

        // Assert
        assertNotNull(encontrada);
        assertEquals(comuna.getNombre(), encontrada.getNombre());
        verify(comunaRepository, times(1)).findById(id);
    }

    @Test
    public void save_shouldReturnSavedComuna_whenValid() {
        // Arrange
        when(comunaRepository.save(any(Comuna.class))).thenReturn(comuna);

        // Act
        Comuna guardada = comunaService.save(comuna);

        // Assert
        assertNotNull(guardada);
        assertEquals(comuna.getNombre(), guardada.getNombre());
        verify(comunaRepository, times(1)).save(comuna);
    }

    @Test
    public void update_shouldReturnUpdatedComuna_whenValid() {
        // Arrange
        String nuevoNombre = "Providencia";
        String nuevoCodigoPostal = "7500000";

        // Creamos una nueva región para simular un cambio de región
        Region nuevaRegion = new Region();
        nuevaRegion.setIdRegion(2);
        nuevaRegion.setNombre("Región de Valparaíso");

        Comuna comunaConNuevosDatos = new Comuna();
        comunaConNuevosDatos.setNombre(nuevoNombre);
        comunaConNuevosDatos.setCodigoPostal(nuevoCodigoPostal);
        comunaConNuevosDatos.setRegion(nuevaRegion);

        when(comunaRepository.findById(id)).thenReturn(Optional.of(comuna));

        Comuna comunaModificada = new Comuna();
        comunaModificada.setIdComuna(id);
        comunaModificada.setNombre(nuevoNombre);
        comunaModificada.setCodigoPostal(nuevoCodigoPostal);
        comunaModificada.setRegion(nuevaRegion);

        when(comunaRepository.save(any(Comuna.class))).thenReturn(comunaModificada);

        // Act
        Comuna actualizado = comunaService.update(comunaConNuevosDatos, id);

        // Assert
        assertNotNull(actualizado);
        assertEquals(nuevoNombre, actualizado.getNombre());
        assertEquals(nuevoCodigoPostal, actualizado.getCodigoPostal());
        assertEquals(nuevaRegion.getNombre(), actualizado.getRegion().getNombre());
        verify(comunaRepository, times(1)).findById(id);
        verify(comunaRepository, times(1)).save(comuna);
    }

    @Test
    public void delete_shouldDeleteComuna_whenExists() {
        // Arrange
        when(comunaRepository.existsById(id)).thenReturn(true);
        doNothing().when(comunaRepository).deleteById(id);

        // Act & Assert
        assertDoesNotThrow(() -> comunaService.delete(id));
        verify(comunaRepository, times(1)).existsById(id);
        verify(comunaRepository, times(1)).deleteById(id);
    }

    // --- Pruebas de escenarios de error ---

    @Test
    public void findById_shouldThrowException_whenNotFound() {
        // Arrange
        when(comunaRepository.findById(id)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(NoSuchElementException.class, () -> comunaService.findById(id));
        verify(comunaRepository, times(1)).findById(id);
    }

    // --- Pruebas de Validación en SAVE ---

    @Test
    public void save_shouldThrowException_whenNameIsNull() {
        // Arrange
        comuna.setNombre(null);

        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> comunaService.save(comuna));
        verify(comunaRepository, never()).save(any());
    }

    @Test
    public void save_shouldThrowException_whenRegionIsNull() {
        // Arrange
        comuna.setRegion(null);

        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> comunaService.save(comuna));
        verify(comunaRepository, never()).save(any());
    }

    @Test
    public void save_shouldThrowException_whenNameIsTooLong() {
        // Arrange
        comuna.setNombre(faker.lorem().characters(101)); // Más de 100 caracteres

        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> comunaService.save(comuna));
        verify(comunaRepository, never()).save(any());
    }

    @Test
    public void save_shouldThrowException_whenDataIntegrityViolation_invalidRegion() {
        // Arrange
        when(comunaRepository.save(any(Comuna.class))).thenThrow(DataIntegrityViolationException.class);

        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> comunaService.save(comuna));
        verify(comunaRepository, times(1)).save(comuna);
    }

    // --- Pruebas de Validación y Errores en UPDATE ---

    @Test
    public void update_shouldThrowException_whenComunaIsNull() {
        // Arrange
        Comuna nullComuna = null;

        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> comunaService.update(nullComuna, id));
        verify(comunaRepository, never()).findById(any());
    }

    @Test
    public void update_shouldThrowException_whenComunaNotFound() {
        // Arrange
        when(comunaRepository.findById(id)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(NoSuchElementException.class, () -> comunaService.update(comuna, id));
        verify(comunaRepository, times(1)).findById(id);
        verify(comunaRepository, never()).save(any());
    }

    @Test
    public void update_shouldThrowException_whenNewRegionIsNull() {
        // Arrange
        Comuna comunaConError = new Comuna();
        comunaConError.setNombre("Vitacura");
        comunaConError.setCodigoPostal("7630000");
        comunaConError.setRegion(null); // Violación de validación

        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> comunaService.update(comunaConError, id));
        verify(comunaRepository, never()).findById(any());
    }

    @Test
    public void update_shouldThrowException_whenDataIntegrityViolation() {
        // Arrange
        when(comunaRepository.findById(id)).thenReturn(Optional.of(comuna));
        when(comunaRepository.save(any(Comuna.class))).thenThrow(DataIntegrityViolationException.class);

        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> comunaService.update(comuna, id));
        verify(comunaRepository, times(1)).findById(id);
        verify(comunaRepository, times(1)).save(comuna);
    }

    // --- Pruebas de Eliminación con Restricciones ---

    @Test
    public void delete_shouldThrowException_whenComunaHasDireccionesAssociated() {
        // Arrange
        when(comunaRepository.existsById(id)).thenReturn(true);
        // Simula la restricción de clave externa (Foreign Key Constraint)
        doThrow(DataIntegrityViolationException.class).when(comunaRepository).deleteById(id);

        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> comunaService.delete(id));
        verify(comunaRepository, times(1)).existsById(id);
        verify(comunaRepository, times(1)).deleteById(id);
    }
}