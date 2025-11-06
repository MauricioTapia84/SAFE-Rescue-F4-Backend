package com.SAFE_Rescue.API_Geolocalizacion.service;

import com.SAFE_Rescue.API_Geolocalizacion.modelo.Pais;
import com.SAFE_Rescue.API_Geolocalizacion.repositoy.PaisRepository;
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
public class PaisServiceTest {

    @Mock
    private PaisRepository paisRepository; // Mock del Repositorio

    @InjectMocks
    private PaisService paisService; // Inyección del Servicio

    private Pais pais;
    private Faker faker;
    private Integer id;

    @BeforeEach
    public void setUp() {
        faker = new Faker();
        id = faker.number().numberBetween(1, 100);

        pais = new Pais();
        pais.setIdPais(id);
        pais.setNombre("Chile");
        pais.setCodigoIso("CHL");
    }

    // --- Pruebas de operaciones exitosas (Happy Path) ---

    @Test
    public void findAll_shouldReturnAllPaises() {
        // Arrange
        when(paisRepository.findAll()).thenReturn(List.of(pais));

        // Act
        List<Pais> paises = paisService.findAll();

        // Assert
        assertNotNull(paises);
        assertFalse(paises.isEmpty());
        assertEquals(1, paises.size());
        assertEquals(pais.getNombre(), paises.get(0).getNombre());
        verify(paisRepository, times(1)).findAll();
    }

    @Test
    public void findById_shouldReturnPais_whenFound() {
        // Arrange
        when(paisRepository.findById(id)).thenReturn(Optional.of(pais));

        // Act
        Pais encontrado = paisService.findById(id);

        // Assert
        assertNotNull(encontrado);
        assertEquals(pais.getNombre(), encontrado.getNombre());
        verify(paisRepository, times(1)).findById(id);
    }

    @Test
    public void save_shouldReturnSavedPais_whenValid() {
        // Arrange
        when(paisRepository.save(any(Pais.class))).thenReturn(pais);

        // Act
        Pais guardado = paisService.save(pais);

        // Assert
        assertNotNull(guardado);
        assertEquals(pais.getNombre(), guardado.getNombre());
        verify(paisRepository, times(1)).save(pais);
    }

    @Test
    public void update_shouldReturnUpdatedPais_whenValid() {
        // Arrange
        String nuevoNombre = "Perú Actualizado";
        String nuevoCodigo = "PER";

        Pais paisConNuevosDatos = new Pais();
        paisConNuevosDatos.setNombre(nuevoNombre);
        paisConNuevosDatos.setCodigoIso(nuevoCodigo);

        // Simula la entidad antigua que se encuentra
        when(paisRepository.findById(id)).thenReturn(Optional.of(pais));

        // Simula la entidad guardada después de la actualización (el objeto 'pais' modificado)
        Pais paisModificado = new Pais();
        paisModificado.setIdPais(id);
        paisModificado.setNombre(nuevoNombre);
        paisModificado.setCodigoIso(nuevoCodigo);
        when(paisRepository.save(any(Pais.class))).thenReturn(paisModificado);

        // Act
        Pais actualizado = paisService.update(paisConNuevosDatos, id);

        // Assert
        assertNotNull(actualizado);
        assertEquals(nuevoNombre, actualizado.getNombre());
        assertEquals(nuevoCodigo, actualizado.getCodigoIso());
        verify(paisRepository, times(1)).findById(id);
        verify(paisRepository, times(1)).save(pais); // Verifica que se guarda el objeto original modificado
    }

    @Test
    public void delete_shouldDeletePais_whenExists() {
        // Arrange
        when(paisRepository.existsById(id)).thenReturn(true);
        doNothing().when(paisRepository).deleteById(id);

        // Act & Assert
        assertDoesNotThrow(() -> paisService.delete(id));
        verify(paisRepository, times(1)).existsById(id);
        verify(paisRepository, times(1)).deleteById(id);
    }

    // --- Pruebas de escenarios de error (Incluyendo validaciones) ---

    @Test
    public void findById_shouldThrowException_whenNotFound() {
        // Arrange
        when(paisRepository.findById(id)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(NoSuchElementException.class, () -> paisService.findById(id));
        verify(paisRepository, times(1)).findById(id);
    }

    // --- Pruebas de Validación en SAVE ---

    @Test
    public void save_shouldThrowException_whenNameIsNull() {
        // Arrange
        pais.setNombre(null);

        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> paisService.save(pais));
        verify(paisRepository, never()).save(any());
    }

    @Test
    public void save_shouldThrowException_whenCodigoIsoIsEmpty() {
        // Arrange
        pais.setCodigoIso("   ");

        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> paisService.save(pais));
        verify(paisRepository, never()).save(any());
    }

    @Test
    public void save_shouldThrowException_whenCodigoIsoIsWrongLength() {
        // Arrange
        pais.setCodigoIso("CL"); // Longitud incorrecta (solo 2)

        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> paisService.save(pais));
        verify(paisRepository, never()).save(any());
    }

    @Test
    public void save_shouldThrowException_whenDataIntegrityViolation() {
        // Arrange
        when(paisRepository.save(any(Pais.class))).thenThrow(DataIntegrityViolationException.class);

        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> paisService.save(pais));
        verify(paisRepository, times(1)).save(pais);
    }

    // --- Pruebas de Validación y Errores en UPDATE ---

    @Test
    public void update_shouldThrowException_whenPaisIsNull() {
        // Arrange
        Pais nullPais = null;

        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> paisService.update(nullPais, id));
        verify(paisRepository, never()).findById(any());
    }

    @Test
    public void update_shouldThrowException_whenPaisNotFound() {
        // Arrange
        when(paisRepository.findById(id)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(NoSuchElementException.class, () -> paisService.update(pais, id));
        verify(paisRepository, times(1)).findById(id);
        verify(paisRepository, never()).save(any());
    }

    @Test
    public void update_shouldThrowException_whenNewCodigoIsoIsWrongLength() {
        // Arrange
        Pais paisConError = new Pais();
        paisConError.setNombre("España");
        paisConError.setCodigoIso("ESPZ"); // Longitud incorrecta (4)

        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> paisService.update(paisConError, id));
        verify(paisRepository, never()).findById(any());
        verify(paisRepository, never()).save(any());
    }


    @Test
    public void update_shouldThrowException_whenDataIntegrityViolation() {
        // Arrange
        when(paisRepository.findById(id)).thenReturn(Optional.of(pais));
        when(paisRepository.save(any(Pais.class))).thenThrow(DataIntegrityViolationException.class);

        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> paisService.update(pais, id));
        verify(paisRepository, times(1)).findById(id);
        verify(paisRepository, times(1)).save(pais);
    }

    // --- Pruebas de Eliminación ---

    @Test
    public void delete_shouldThrowException_whenNotFound() {
        // Arrange
        when(paisRepository.existsById(id)).thenReturn(false);

        // Act & Assert
        assertThrows(NoSuchElementException.class, () -> paisService.delete(id));
        verify(paisRepository, times(1)).existsById(id);
        verify(paisRepository, never()).deleteById(any());
    }

    @Test
    public void delete_shouldThrowException_whenHasRegionsAssociated() {
        // Arrange
        when(paisRepository.existsById(id)).thenReturn(true);
        // Simula la restricción de clave externa (Foreign Key Constraint)
        doThrow(DataIntegrityViolationException.class).when(paisRepository).deleteById(id);

        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> paisService.delete(id));
        verify(paisRepository, times(1)).existsById(id);
        verify(paisRepository, times(1)).deleteById(id);
    }
}