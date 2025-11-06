package com.SAFE_Rescue.API_Geolocalizacion.service;

import com.SAFE_Rescue.API_Geolocalizacion.modelo.Pais;
import com.SAFE_Rescue.API_Geolocalizacion.modelo.Region;
import com.SAFE_Rescue.API_Geolocalizacion.repositoy.RegionRepository;
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
public class RegionServiceTest {

    @Mock
    private RegionRepository regionRepository;

    @InjectMocks
    private RegionService regionService;

    private Region region;
    private Pais pais;
    private Faker faker;
    private Integer id;

    @BeforeEach
    public void setUp() {
        faker = new Faker();
        id = faker.number().numberBetween(1, 100);

        // 1. Crear el objeto Pais (dependencia)
        pais = new Pais();
        pais.setIdPais(1);
        pais.setNombre("Chile");
        pais.setCodigoIso("CHL");

        // 2. Crear el objeto Region
        region = new Region();
        region.setIdRegion(id);
        region.setNombre("Región Metropolitana");
        region.setIdentificacion("RM");
        region.setPais(pais);
    }

    // --- Pruebas de operaciones exitosas (Happy Path) ---

    @Test
    public void findAll_shouldReturnAllRegions() {
        // Arrange
        when(regionRepository.findAll()).thenReturn(List.of(region));

        // Act
        List<Region> regiones = regionService.findAll();

        // Assert
        assertNotNull(regiones);
        assertFalse(regiones.isEmpty());
        assertEquals(1, regiones.size());
        assertEquals(region.getNombre(), regiones.get(0).getNombre());
        verify(regionRepository, times(1)).findAll();
    }

    @Test
    public void findById_shouldReturnRegion_whenFound() {
        // Arrange
        when(regionRepository.findById(id)).thenReturn(Optional.of(region));

        // Act
        Region encontrada = regionService.findById(id);

        // Assert
        assertNotNull(encontrada);
        assertEquals(region.getNombre(), encontrada.getNombre());
        verify(regionRepository, times(1)).findById(id);
    }

    @Test
    public void save_shouldReturnSavedRegion_whenValid() {
        // Arrange
        when(regionRepository.save(any(Region.class))).thenReturn(region);

        // Act
        Region guardada = regionService.save(region);

        // Assert
        assertNotNull(guardada);
        assertEquals(region.getNombre(), guardada.getNombre());
        verify(regionRepository, times(1)).save(region);
    }

    @Test
    public void update_shouldReturnUpdatedRegion_whenValid() {
        // Arrange
        String nuevoNombre = "Región de Prueba";
        String nuevaIdentificacion = "RDP";

        Region regionConNuevosDatos = new Region();
        regionConNuevosDatos.setNombre(nuevoNombre);
        regionConNuevosDatos.setIdentificacion(nuevaIdentificacion);
        regionConNuevosDatos.setPais(pais); // Debe tener un país válido

        when(regionRepository.findById(id)).thenReturn(Optional.of(region));

        Region regionModificada = new Region();
        regionModificada.setIdRegion(id);
        regionModificada.setNombre(nuevoNombre);
        regionModificada.setIdentificacion(nuevaIdentificacion);
        regionModificada.setPais(pais);

        when(regionRepository.save(any(Region.class))).thenReturn(regionModificada);

        // Act
        Region actualizado = regionService.update(regionConNuevosDatos, id);

        // Assert
        assertNotNull(actualizado);
        assertEquals(nuevoNombre, actualizado.getNombre());
        assertEquals(nuevaIdentificacion, actualizado.getIdentificacion());
        verify(regionRepository, times(1)).findById(id);
        verify(regionRepository, times(1)).save(region); // Verifica que se guarda el objeto original modificado
    }

    @Test
    public void delete_shouldDeleteRegion_whenExists() {
        // Arrange
        when(regionRepository.existsById(id)).thenReturn(true);
        doNothing().when(regionRepository).deleteById(id);

        // Act & Assert
        assertDoesNotThrow(() -> regionService.delete(id));
        verify(regionRepository, times(1)).existsById(id);
        verify(regionRepository, times(1)).deleteById(id);
    }

    // --- Pruebas de escenarios de error ---

    @Test
    public void findById_shouldThrowException_whenNotFound() {
        // Arrange
        when(regionRepository.findById(id)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(NoSuchElementException.class, () -> regionService.findById(id));
        verify(regionRepository, times(1)).findById(id);
    }

    // --- Pruebas de Validación en SAVE ---

    @Test
    public void save_shouldThrowException_whenNameIsNull() {
        // Arrange
        region.setNombre(null);

        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> regionService.save(region));
        verify(regionRepository, never()).save(any());
    }

    @Test
    public void save_shouldThrowException_whenIdentificacionIsEmpty() {
        // Arrange
        region.setIdentificacion("   ");

        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> regionService.save(region));
        verify(regionRepository, never()).save(any());
    }

    @Test
    public void save_shouldThrowException_whenPaisIsNull() {
        // Arrange
        region.setPais(null);

        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> regionService.save(region));
        verify(regionRepository, never()).save(any());
    }

    @Test
    public void save_shouldThrowException_whenNameIsTooLong() {
        // Arrange
        region.setNombre(faker.lorem().characters(101)); // Más de 100 caracteres

        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> regionService.save(region));
        verify(regionRepository, never()).save(any());
    }

    @Test
    public void save_shouldThrowException_whenIdentificacionIsTooLong() {
        // Arrange
        region.setIdentificacion("123456"); // Más de 5 caracteres

        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> regionService.save(region));
        verify(regionRepository, never()).save(any());
    }

    @Test
    public void save_shouldThrowException_whenDataIntegrityViolation_duplicateOrInvalidFK() {
        // Arrange
        when(regionRepository.save(any(Region.class))).thenThrow(DataIntegrityViolationException.class);

        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> regionService.save(region));
        verify(regionRepository, times(1)).save(region);
    }

    // --- Pruebas de Validación y Errores en UPDATE ---

    @Test
    public void update_shouldThrowException_whenRegionIsNull() {
        // Arrange
        Region nullRegion = null;

        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> regionService.update(nullRegion, id));
        verify(regionRepository, never()).findById(any());
    }

    @Test
    public void update_shouldThrowException_whenRegionNotFound() {
        // Arrange
        when(regionRepository.findById(id)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(NoSuchElementException.class, () -> regionService.update(region, id));
        verify(regionRepository, times(1)).findById(id);
        verify(regionRepository, never()).save(any());
    }

    @Test
    public void update_shouldThrowException_whenNewPaisIsNull() {
        // Arrange
        Region regionConError = new Region();
        regionConError.setNombre("Nueva");
        regionConError.setIdentificacion("NVA");
        regionConError.setPais(null); // Violación de validación

        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> regionService.update(regionConError, id));
        verify(regionRepository, never()).findById(any());
    }

    @Test
    public void update_shouldThrowException_whenDataIntegrityViolation() {
        // Arrange
        when(regionRepository.findById(id)).thenReturn(Optional.of(region));
        when(regionRepository.save(any(Region.class))).thenThrow(DataIntegrityViolationException.class);

        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> regionService.update(region, id));
        verify(regionRepository, times(1)).findById(id);
        verify(regionRepository, times(1)).save(region);
    }

    // --- Pruebas de Eliminación con Restricciones ---

    @Test
    public void delete_shouldThrowException_whenRegionHasComunasAssociated() {
        // Arrange
        when(regionRepository.existsById(id)).thenReturn(true);
        // Simula la restricción de clave externa (Foreign Key Constraint)
        doThrow(DataIntegrityViolationException.class).when(regionRepository).deleteById(id);

        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> regionService.delete(id));
        verify(regionRepository, times(1)).existsById(id);
        verify(regionRepository, times(1)).deleteById(id);
    }
}