package com.SAFE_Rescue.API_Incidentes.service;

import com.SAFE_Rescue.API_Incidentes.modelo.TipoIncidente;
import com.SAFE_Rescue.API_Incidentes.repository.TipoIncidenteRepository;
import net.datafaker.Faker;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class TipoIncidenteServiceTest {

    @Mock
    private TipoIncidenteRepository tipoIncidenteRepository;

    @InjectMocks
    private TipoIncidenteService tipoIncidenteService;

    private TipoIncidente tipoIncidenteValido;
    private Faker faker;
    private Integer idExistente;
    private Integer idNoExistente;

    @BeforeEach
    public void setUp() {
        faker = new Faker();
        idExistente = faker.number().numberBetween(1, 100);
        idNoExistente = 999;

        // Tipo de Incidente Válido y listo para usar en las pruebas
        tipoIncidenteValido = new TipoIncidente(idExistente, "Incendio Estructural");
    }

    // --- PRUEBAS DE FINDALL ---

    @Test
    public void findAll_shouldReturnListOfIncidents() {
        // Arrange
        TipoIncidente tipo2 = new TipoIncidente(2, "Accidente de Tráfico");
        List<TipoIncidente> listaEsperada = Arrays.asList(tipoIncidenteValido, tipo2);

        when(tipoIncidenteRepository.findAll()).thenReturn(listaEsperada);

        // Act
        List<TipoIncidente> resultado = tipoIncidenteService.findAll();

        // Assert
        assertNotNull(resultado);
        assertEquals(2, resultado.size());
        verify(tipoIncidenteRepository, times(1)).findAll();
    }

    // --- PRUEBAS DE FIND_BY_ID ---

    @Test
    public void findById_shouldReturnIncident_whenIdExists() {
        // Arrange
        when(tipoIncidenteRepository.findById(idExistente)).thenReturn(Optional.of(tipoIncidenteValido));

        // Act
        TipoIncidente encontrado = tipoIncidenteService.findById(idExistente);

        // Assert
        assertNotNull(encontrado);
        assertEquals(idExistente, encontrado.getIdTipoIncidente());
        verify(tipoIncidenteRepository, times(1)).findById(idExistente);
    }

    @Test
    public void findById_shouldThrowException_whenIdDoesNotExist() {
        // Arrange
        when(tipoIncidenteRepository.findById(idNoExistente)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(NoSuchElementException.class, () -> tipoIncidenteService.findById(idNoExistente));
        verify(tipoIncidenteRepository, times(1)).findById(idNoExistente);
    }

    // --- PRUEBAS DE SAVE (Con validación) ---

    @Test
    public void save_shouldReturnSavedIncident_whenValid() {
        // Arrange
        TipoIncidente nuevoTipo = new TipoIncidente(1,"Inundación leve");
        when(tipoIncidenteRepository.save(any(TipoIncidente.class))).thenReturn(tipoIncidenteValido);

        // Act
        TipoIncidente guardado = tipoIncidenteService.save(nuevoTipo);

        // Assert
        assertNotNull(guardado);
        verify(tipoIncidenteRepository, times(1)).save(nuevoTipo);
    }

    @Test
    public void save_shouldThrowRuntimeException_whenNameIsNull() {
        // Arrange
        TipoIncidente tipoInvalido = new TipoIncidente(1,null);

        // Act & Assert
        // El servicio envuelve la IllegalArgumentException en una RuntimeException
        assertThrows(RuntimeException.class, () -> tipoIncidenteService.save(tipoInvalido));
        verify(tipoIncidenteRepository, never()).save(any());
    }

    @Test
    public void save_shouldThrowRuntimeException_whenNameIsTooLong() {
        // Arrange
        // Generar una cadena de más de 50 caracteres (por ejemplo, 60)
        String nombreLargo = faker.lorem().characters(60);
        TipoIncidente tipoInvalido = new TipoIncidente(1,nombreLargo);

        // Act & Assert
        assertThrows(RuntimeException.class, () -> tipoIncidenteService.save(tipoInvalido));
        verify(tipoIncidenteRepository, never()).save(any());
    }

    // --- PRUEBAS DE UPDATE ---

    @Test
    public void update_shouldReturnUpdatedIncident_whenIncidentExists() {
        // Arrange
        TipoIncidente tipoExistente = new TipoIncidente(idExistente, "Nombre Antiguo");
        TipoIncidente datosActualizados = new TipoIncidente(1,"Nombre Nuevo Válido");

        when(tipoIncidenteRepository.findById(idExistente)).thenReturn(Optional.of(tipoExistente));
        when(tipoIncidenteRepository.save(any(TipoIncidente.class))).thenReturn(tipoExistente); // Devuelve el objeto modificado

        // Act
        TipoIncidente resultado = tipoIncidenteService.update(datosActualizados, idExistente);

        // Assert
        assertNotNull(resultado);
        assertEquals("Nombre Nuevo Válido", resultado.getNombre());
        verify(tipoIncidenteRepository, times(1)).findById(idExistente);
        verify(tipoIncidenteRepository, times(1)).save(tipoExistente);
    }

    @Test
    public void update_shouldThrowException_whenIdDoesNotExist() {
        // Arrange
        TipoIncidente datosActualizados = new TipoIncidente(1,"Nombre Nuevo");
        when(tipoIncidenteRepository.findById(idNoExistente)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(NoSuchElementException.class, () -> tipoIncidenteService.update(datosActualizados, idNoExistente));
        verify(tipoIncidenteRepository, times(1)).findById(idNoExistente);
        verify(tipoIncidenteRepository, never()).save(any());
    }

    @Test
    public void update_shouldThrowRuntimeException_whenNewNameIsTooLong() {
        // Arrange
        TipoIncidente tipoExistente = new TipoIncidente(idExistente, "Nombre Antiguo");
        // Generar una cadena de más de 50 caracteres
        TipoIncidente datosActualizados = new TipoIncidente(1,faker.lorem().characters(51));

        when(tipoIncidenteRepository.findById(idExistente)).thenReturn(Optional.of(tipoExistente));

        // Act & Assert
        assertThrows(RuntimeException.class, () -> tipoIncidenteService.update(datosActualizados, idExistente));
        verify(tipoIncidenteRepository, times(1)).findById(idExistente);
        verify(tipoIncidenteRepository, never()).save(any());
    }

    // --- PRUEBAS DE DELETE ---

    @Test
    public void delete_shouldSucceed_whenIdExists() {
        // Arrange
        when(tipoIncidenteRepository.existsById(idExistente)).thenReturn(true);
        // No simulamos el deleteById porque es un método void

        // Act
        tipoIncidenteService.delete(idExistente);

        // Assert
        verify(tipoIncidenteRepository, times(1)).existsById(idExistente);
        verify(tipoIncidenteRepository, times(1)).deleteById(idExistente);
    }

    @Test
    public void delete_shouldThrowException_whenIdDoesNotExist() {
        // Arrange
        when(tipoIncidenteRepository.existsById(idNoExistente)).thenReturn(false);

        // Act & Assert
        assertThrows(NoSuchElementException.class, () -> tipoIncidenteService.delete(idNoExistente));
        verify(tipoIncidenteRepository, times(1)).existsById(idNoExistente);
        verify(tipoIncidenteRepository, never()).deleteById(anyInt());
    }
}