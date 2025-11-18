package com.SAFE_Rescue.API_Geolocalizacion.service;

import com.SAFE_Rescue.API_Geolocalizacion.modelo.Cordenadas;
import com.SAFE_Rescue.API_Geolocalizacion.repositoy.CordenadasRepository;
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
public class CordenadasServiceTest {

    @Mock
    private CordenadasRepository cordenadasRepository; // Mock del Repositorio

    @InjectMocks
    private CordenadasService cordenadasService; // Inyección del Servicio

    private Cordenadas cordenadas;
    private Faker faker;
    private Integer id;

    @BeforeEach
    public void setUp() {
        faker = new Faker();
        id = faker.number().numberBetween(1, 100);

        cordenadas = new Cordenadas();
        cordenadas.setIdGeolocalizacion(id);
        // Generamos latitud y longitud aleatorias (como Float)
        cordenadas.setLatitud((float) faker.number().randomDouble(6,  -90, 90));
        cordenadas.setLongitud((float) faker.number().randomDouble(6, -180,  180));
    }

    // --- Pruebas de operaciones exitosas (Happy Path) ---

    @Test
    public void findAll_shouldReturnAllGeolocalizaciones() {
        // Arrange
        when(cordenadasRepository.findAll()).thenReturn(List.of(cordenadas));

        // Act
        List<Cordenadas> geos = cordenadasService.findAll();

        // Assert
        assertNotNull(geos);
        assertFalse(geos.isEmpty());
        assertEquals(1, geos.size());
        assertEquals(cordenadas.getLatitud(), geos.get(0).getLatitud());
        verify(cordenadasRepository, times(1)).findAll();
    }

    @Test
    public void findById_shouldReturnGeolocalizacion_whenFound() {
        // Arrange
        when(cordenadasRepository.findById(id)).thenReturn(Optional.of(cordenadas));

        // Act
        Cordenadas encontrado = cordenadasService.findById(id);

        // Assert
        assertNotNull(encontrado);
        assertEquals(cordenadas.getLatitud(), encontrado.getLatitud());
        verify(cordenadasRepository, times(1)).findById(id);
    }

    @Test
    public void save_shouldReturnSavedGeolocalizacion_whenValid() {
        // Arrange
        when(cordenadasRepository.save(any(Cordenadas.class))).thenReturn(cordenadas);

        // Act
        Cordenadas guardada = cordenadasService.save(cordenadas);

        // Assert
        assertNotNull(guardada);
        assertEquals(cordenadas.getLatitud(), guardada.getLatitud());
        verify(cordenadasRepository, times(1)).save(cordenadas);
    }

    @Test
    public void update_shouldReturnUpdatedGeolocalizacion_whenValid() {
        // Arrange
        float nuevaLatitud = (float) faker.number().randomDouble(6, 10, 20);
        float nuevaLongitud = (float) faker.number().randomDouble(6, -50, -40);

        Cordenadas cordenadasActualizada = new Cordenadas();
        cordenadasActualizada.setLatitud(nuevaLatitud);
        cordenadasActualizada.setLongitud(nuevaLongitud);

        when(cordenadasRepository.findById(id)).thenReturn(Optional.of(cordenadas));
        when(cordenadasRepository.save(any(Cordenadas.class))).thenReturn(cordenadasActualizada);

        // Act
        Cordenadas actualizado = cordenadasService.update(cordenadasActualizada, id);

        // Assert
        assertNotNull(actualizado);
        assertEquals(nuevaLatitud, actualizado.getLatitud());
        assertEquals(nuevaLongitud, actualizado.getLongitud());

        // Verifica que se buscó por ID y se guardó la entidad modificada
        verify(cordenadasRepository, times(1)).findById(id);
        verify(cordenadasRepository, times(1)).save(cordenadas);
    }

    @Test
    public void delete_shouldDeleteGeolocalizacion_whenExists() {
        // Arrange
        when(cordenadasRepository.existsById(id)).thenReturn(true);
        doNothing().when(cordenadasRepository).deleteById(id);

        // Act & Assert
        assertDoesNotThrow(() -> cordenadasService.delete(id));
        verify(cordenadasRepository, times(1)).existsById(id);
        verify(cordenadasRepository, times(1)).deleteById(id);
    }

    // --- Pruebas de escenarios de error ---

    @Test
    public void findById_shouldThrowException_whenNotFound() {
        // Arrange
        when(cordenadasRepository.findById(id)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(NoSuchElementException.class, () -> cordenadasService.findById(id));
        verify(cordenadasRepository, times(1)).findById(id);
    }

    @Test
    public void save_shouldThrowException_whenGeolocalizacionIsNull() {
        // Arrange
        Cordenadas nullGeo = null;

        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> cordenadasService.save(nullGeo));
        verify(cordenadasRepository, never()).save(any());
    }

    // (NOTA: No necesitamos testear latitud/longitud nulas aquí,
    // ya que eso debe ser manejado por validadores de la entidad/BD o un DTO antes de llegar al Service)

    @Test
    public void save_shouldThrowException_whenDataIntegrityViolation() {
        // Arrange
        when(cordenadasRepository.save(any(Cordenadas.class))).thenThrow(DataIntegrityViolationException.class);

        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> cordenadasService.save(cordenadas));
        verify(cordenadasRepository, times(1)).save(cordenadas);
    }

    @Test
    public void update_shouldThrowException_whenGeolocalizacionIsNull() {
        // Arrange
        Cordenadas nullGeo = null;

        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> cordenadasService.update(nullGeo, id));
        verify(cordenadasRepository, never()).findById(any());
        verify(cordenadasRepository, never()).save(any());
    }

    @Test
    public void update_shouldThrowException_whenGeolocalizacionNotFound() {
        // Arrange
        when(cordenadasRepository.findById(id)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(NoSuchElementException.class, () -> cordenadasService.update(cordenadas, id));
        verify(cordenadasRepository, times(1)).findById(id);
        verify(cordenadasRepository, never()).save(any());
    }

    @Test
    public void update_shouldThrowException_whenDataIntegrityViolation() {
        // Arrange
        when(cordenadasRepository.findById(id)).thenReturn(Optional.of(cordenadas));
        // Simula la violación de integridad al intentar guardar la actualización (ej. coordenadas inválidas)
        when(cordenadasRepository.save(any(Cordenadas.class))).thenThrow(DataIntegrityViolationException.class);

        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> cordenadasService.update(cordenadas, id));
        verify(cordenadasRepository, times(1)).findById(id);
        verify(cordenadasRepository, times(1)).save(cordenadas);
    }

    @Test
    public void delete_shouldThrowException_whenNotFound() {
        // Arrange
        when(cordenadasRepository.existsById(id)).thenReturn(false);

        // Act & Assert
        assertThrows(NoSuchElementException.class, () -> cordenadasService.delete(id));
        verify(cordenadasRepository, times(1)).existsById(id);
        verify(cordenadasRepository, never()).deleteById(any());
    }
}