package com.SAFE_Rescue.API_Geolocalizacion.service;

import com.SAFE_Rescue.API_Geolocalizacion.modelo.Coordenadas;
import com.SAFE_Rescue.API_Geolocalizacion.repositoy.CoordenadasRepository;
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
public class CoordenadasServiceTest { // Cambiado de CordenadasServiceTest a CoordenadasServiceTest

    @Mock
    private CoordenadasRepository coordenadasRepository;

    @InjectMocks
    private CoordenadasService coordenadasService; // Cambiado de CordenadasService a CoordenadasService

    private Coordenadas coordenadas; // Cambiado de Cordenadas a Coordenadas
    private Faker faker;
    private Integer id;

    @BeforeEach
    public void setUp() {
        faker = new Faker();
        id = faker.number().numberBetween(1, 100);

        coordenadas = new Coordenadas(); // Cambiado de Cordenadas a Coordenadas
        coordenadas.setIdCoordenadas(id); // Cambiado de setIdGeolocalizacion a setIdCoordenadas
        // Generamos latitud y longitud aleatorias (como Float)
        coordenadas.setLatitud((float) faker.number().randomDouble(6,  -90, 90));
        coordenadas.setLongitud((float) faker.number().randomDouble(6, -180,  180));
    }

    // --- Pruebas de operaciones exitosas (Happy Path) ---

    @Test
    public void findAll_shouldReturnAllCoordenadas() { // Nombre del método actualizado
        // Arrange
        when(coordenadasRepository.findAll()).thenReturn(List.of(coordenadas));

        // Act
        List<Coordenadas> coordenadasList = coordenadasService.findAll(); // Variable actualizada

        // Assert
        assertNotNull(coordenadasList);
        assertFalse(coordenadasList.isEmpty());
        assertEquals(1, coordenadasList.size());
        assertEquals(coordenadas.getLatitud(), coordenadasList.get(0).getLatitud());
        verify(coordenadasRepository, times(1)).findAll();
    }

    @Test
    public void findById_shouldReturnCoordenadas_whenFound() { // Nombre del método actualizado
        // Arrange
        when(coordenadasRepository.findById(id)).thenReturn(Optional.of(coordenadas));

        // Act
        Coordenadas encontrado = coordenadasService.findById(id); // Variable actualizada

        // Assert
        assertNotNull(encontrado);
        assertEquals(coordenadas.getLatitud(), encontrado.getLatitud());
        verify(coordenadasRepository, times(1)).findById(id);
    }

    @Test
    public void save_shouldReturnSavedCoordenadas_whenValid() { // Nombre del método actualizado
        // Arrange
        when(coordenadasRepository.save(any(Coordenadas.class))).thenReturn(coordenadas); // Cambiado Cordenadas.class a Coordenadas.class

        // Act
        Coordenadas guardada = coordenadasService.save(coordenadas); // Variable actualizada

        // Assert
        assertNotNull(guardada);
        assertEquals(coordenadas.getLatitud(), guardada.getLatitud());
        verify(coordenadasRepository, times(1)).save(coordenadas);
    }

    @Test
    public void update_shouldReturnUpdatedCoordenadas_whenValid() { // Nombre del método actualizado
        // Arrange
        float nuevaLatitud = (float) faker.number().randomDouble(6, 10, 20);
        float nuevaLongitud = (float) faker.number().randomDouble(6, -50, -40);

        Coordenadas coordenadasActualizada = new Coordenadas(); // Variable actualizada
        coordenadasActualizada.setLatitud(nuevaLatitud);
        coordenadasActualizada.setLongitud(nuevaLongitud);

        when(coordenadasRepository.findById(id)).thenReturn(Optional.of(coordenadas));
        when(coordenadasRepository.save(any(Coordenadas.class))).thenReturn(coordenadasActualizada); // Cambiado Cordenadas.class a Coordenadas.class

        // Act
        Coordenadas actualizado = coordenadasService.update(coordenadasActualizada, id); // Variable actualizada

        // Assert
        assertNotNull(actualizado);
        assertEquals(nuevaLatitud, actualizado.getLatitud());
        assertEquals(nuevaLongitud, actualizado.getLongitud());

        // Verifica que se buscó por ID y se guardó la entidad modificada
        verify(coordenadasRepository, times(1)).findById(id);
        verify(coordenadasRepository, times(1)).save(coordenadas);
    }

    @Test
    public void delete_shouldDeleteCoordenadas_whenExists() { // Nombre del método actualizado
        // Arrange
        when(coordenadasRepository.existsById(id)).thenReturn(true);
        doNothing().when(coordenadasRepository).deleteById(id);

        // Act & Assert
        assertDoesNotThrow(() -> coordenadasService.delete(id));
        verify(coordenadasRepository, times(1)).existsById(id);
        verify(coordenadasRepository, times(1)).deleteById(id);
    }

    // --- Pruebas de escenarios de error ---

    @Test
    public void findById_shouldThrowException_whenNotFound() {
        // Arrange
        when(coordenadasRepository.findById(id)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(NoSuchElementException.class, () -> coordenadasService.findById(id));
        verify(coordenadasRepository, times(1)).findById(id);
    }

    @Test
    public void save_shouldThrowException_whenCoordenadasIsNull() { // Nombre del método actualizado
        // Arrange
        Coordenadas nullCoordenadas = null; // Variable actualizada

        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> coordenadasService.save(nullCoordenadas));
        verify(coordenadasRepository, never()).save(any());
    }

    // (NOTA: No necesitamos testear latitud/longitud nulas aquí,
    // ya que eso debe ser manejado por validadores de la entidad/BD o un DTO antes de llegar al Service)

    @Test
    public void save_shouldThrowException_whenDataIntegrityViolation() {
        // Arrange
        when(coordenadasRepository.save(any(Coordenadas.class))).thenThrow(DataIntegrityViolationException.class); // Cambiado Cordenadas.class a Coordenadas.class

        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> coordenadasService.save(coordenadas));
        verify(coordenadasRepository, times(1)).save(coordenadas);
    }

    @Test
    public void update_shouldThrowException_whenCoordenadasIsNull() { // Nombre del método actualizado
        // Arrange
        Coordenadas nullCoordenadas = null; // Variable actualizada

        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> coordenadasService.update(nullCoordenadas, id));
        verify(coordenadasRepository, never()).findById(any());
        verify(coordenadasRepository, never()).save(any());
    }

    @Test
    public void update_shouldThrowException_whenCoordenadasNotFound() { // Nombre del método actualizado
        // Arrange
        when(coordenadasRepository.findById(id)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(NoSuchElementException.class, () -> coordenadasService.update(coordenadas, id));
        verify(coordenadasRepository, times(1)).findById(id);
        verify(coordenadasRepository, never()).save(any());
    }

    @Test
    public void update_shouldThrowException_whenDataIntegrityViolation() {
        // Arrange
        when(coordenadasRepository.findById(id)).thenReturn(Optional.of(coordenadas));
        // Simula la violación de integridad al intentar guardar la actualización (ej. coordenadas inválidas)
        when(coordenadasRepository.save(any(Coordenadas.class))).thenThrow(DataIntegrityViolationException.class); // Cambiado Cordenadas.class a Coordenadas.class

        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> coordenadasService.update(coordenadas, id));
        verify(coordenadasRepository, times(1)).findById(id);
        verify(coordenadasRepository, times(1)).save(coordenadas);
    }

    @Test
    public void delete_shouldThrowException_whenNotFound() {
        // Arrange
        when(coordenadasRepository.existsById(id)).thenReturn(false);

        // Act & Assert
        assertThrows(NoSuchElementException.class, () -> coordenadasService.delete(id));
        verify(coordenadasRepository, times(1)).existsById(id);
        verify(coordenadasRepository, never()).deleteById(any());
    }
}