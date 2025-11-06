package com.SAFE_Rescue.API_Geolocalizacion.service;

import com.SAFE_Rescue.API_Geolocalizacion.modelo.Geolocalizacion;
import com.SAFE_Rescue.API_Geolocalizacion.repositoy.GeolocalizacionRepository;
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
public class GeolocalizacionServiceTest {

    @Mock
    private GeolocalizacionRepository geolocalizacionRepository; // Mock del Repositorio

    @InjectMocks
    private GeolocalizacionService geolocalizacionService; // Inyección del Servicio

    private Geolocalizacion geolocalizacion;
    private Faker faker;
    private Integer id;

    @BeforeEach
    public void setUp() {
        faker = new Faker();
        id = faker.number().numberBetween(1, 100);

        geolocalizacion = new Geolocalizacion();
        geolocalizacion.setIdGeolocalizacion(id);
        // Generamos latitud y longitud aleatorias (como Float)
        geolocalizacion.setLatitud((float) faker.number().randomDouble(6,  -90, 90));
        geolocalizacion.setLongitud((float) faker.number().randomDouble(6, -180,  180));
    }

    // --- Pruebas de operaciones exitosas (Happy Path) ---

    @Test
    public void findAll_shouldReturnAllGeolocalizaciones() {
        // Arrange
        when(geolocalizacionRepository.findAll()).thenReturn(List.of(geolocalizacion));

        // Act
        List<Geolocalizacion> geos = geolocalizacionService.findAll();

        // Assert
        assertNotNull(geos);
        assertFalse(geos.isEmpty());
        assertEquals(1, geos.size());
        assertEquals(geolocalizacion.getLatitud(), geos.get(0).getLatitud());
        verify(geolocalizacionRepository, times(1)).findAll();
    }

    @Test
    public void findById_shouldReturnGeolocalizacion_whenFound() {
        // Arrange
        when(geolocalizacionRepository.findById(id)).thenReturn(Optional.of(geolocalizacion));

        // Act
        Geolocalizacion encontrado = geolocalizacionService.findById(id);

        // Assert
        assertNotNull(encontrado);
        assertEquals(geolocalizacion.getLatitud(), encontrado.getLatitud());
        verify(geolocalizacionRepository, times(1)).findById(id);
    }

    @Test
    public void save_shouldReturnSavedGeolocalizacion_whenValid() {
        // Arrange
        when(geolocalizacionRepository.save(any(Geolocalizacion.class))).thenReturn(geolocalizacion);

        // Act
        Geolocalizacion guardada = geolocalizacionService.save(geolocalizacion);

        // Assert
        assertNotNull(guardada);
        assertEquals(geolocalizacion.getLatitud(), guardada.getLatitud());
        verify(geolocalizacionRepository, times(1)).save(geolocalizacion);
    }

    @Test
    public void update_shouldReturnUpdatedGeolocalizacion_whenValid() {
        // Arrange
        float nuevaLatitud = (float) faker.number().randomDouble(6, 10, 20);
        float nuevaLongitud = (float) faker.number().randomDouble(6, -50, -40);

        Geolocalizacion geolocalizacionActualizada = new Geolocalizacion();
        geolocalizacionActualizada.setLatitud(nuevaLatitud);
        geolocalizacionActualizada.setLongitud(nuevaLongitud);

        when(geolocalizacionRepository.findById(id)).thenReturn(Optional.of(geolocalizacion));
        when(geolocalizacionRepository.save(any(Geolocalizacion.class))).thenReturn(geolocalizacionActualizada);

        // Act
        Geolocalizacion actualizado = geolocalizacionService.update(geolocalizacionActualizada, id);

        // Assert
        assertNotNull(actualizado);
        assertEquals(nuevaLatitud, actualizado.getLatitud());
        assertEquals(nuevaLongitud, actualizado.getLongitud());

        // Verifica que se buscó por ID y se guardó la entidad modificada
        verify(geolocalizacionRepository, times(1)).findById(id);
        verify(geolocalizacionRepository, times(1)).save(geolocalizacion);
    }

    @Test
    public void delete_shouldDeleteGeolocalizacion_whenExists() {
        // Arrange
        when(geolocalizacionRepository.existsById(id)).thenReturn(true);
        doNothing().when(geolocalizacionRepository).deleteById(id);

        // Act & Assert
        assertDoesNotThrow(() -> geolocalizacionService.delete(id));
        verify(geolocalizacionRepository, times(1)).existsById(id);
        verify(geolocalizacionRepository, times(1)).deleteById(id);
    }

    // --- Pruebas de escenarios de error ---

    @Test
    public void findById_shouldThrowException_whenNotFound() {
        // Arrange
        when(geolocalizacionRepository.findById(id)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(NoSuchElementException.class, () -> geolocalizacionService.findById(id));
        verify(geolocalizacionRepository, times(1)).findById(id);
    }

    @Test
    public void save_shouldThrowException_whenGeolocalizacionIsNull() {
        // Arrange
        Geolocalizacion nullGeo = null;

        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> geolocalizacionService.save(nullGeo));
        verify(geolocalizacionRepository, never()).save(any());
    }

    // (NOTA: No necesitamos testear latitud/longitud nulas aquí,
    // ya que eso debe ser manejado por validadores de la entidad/BD o un DTO antes de llegar al Service)

    @Test
    public void save_shouldThrowException_whenDataIntegrityViolation() {
        // Arrange
        when(geolocalizacionRepository.save(any(Geolocalizacion.class))).thenThrow(DataIntegrityViolationException.class);

        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> geolocalizacionService.save(geolocalizacion));
        verify(geolocalizacionRepository, times(1)).save(geolocalizacion);
    }

    @Test
    public void update_shouldThrowException_whenGeolocalizacionIsNull() {
        // Arrange
        Geolocalizacion nullGeo = null;

        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> geolocalizacionService.update(nullGeo, id));
        verify(geolocalizacionRepository, never()).findById(any());
        verify(geolocalizacionRepository, never()).save(any());
    }

    @Test
    public void update_shouldThrowException_whenGeolocalizacionNotFound() {
        // Arrange
        when(geolocalizacionRepository.findById(id)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(NoSuchElementException.class, () -> geolocalizacionService.update(geolocalizacion, id));
        verify(geolocalizacionRepository, times(1)).findById(id);
        verify(geolocalizacionRepository, never()).save(any());
    }

    @Test
    public void update_shouldThrowException_whenDataIntegrityViolation() {
        // Arrange
        when(geolocalizacionRepository.findById(id)).thenReturn(Optional.of(geolocalizacion));
        // Simula la violación de integridad al intentar guardar la actualización (ej. coordenadas inválidas)
        when(geolocalizacionRepository.save(any(Geolocalizacion.class))).thenThrow(DataIntegrityViolationException.class);

        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> geolocalizacionService.update(geolocalizacion, id));
        verify(geolocalizacionRepository, times(1)).findById(id);
        verify(geolocalizacionRepository, times(1)).save(geolocalizacion);
    }

    @Test
    public void delete_shouldThrowException_whenNotFound() {
        // Arrange
        when(geolocalizacionRepository.existsById(id)).thenReturn(false);

        // Act & Assert
        assertThrows(NoSuchElementException.class, () -> geolocalizacionService.delete(id));
        verify(geolocalizacionRepository, times(1)).existsById(id);
        verify(geolocalizacionRepository, never()).deleteById(any());
    }
}