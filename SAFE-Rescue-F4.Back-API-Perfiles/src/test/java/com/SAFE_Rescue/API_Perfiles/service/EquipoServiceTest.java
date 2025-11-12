package com.SAFE_Rescue.API_Perfiles.service;

import com.SAFE_Rescue.API_Perfiles.modelo.Compania;
import com.SAFE_Rescue.API_Perfiles.modelo.Equipo;
import com.SAFE_Rescue.API_Perfiles.modelo.TipoEquipo;
import com.SAFE_Rescue.API_Perfiles.repositoy.EquipoRepository;
import net.datafaker.Faker;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DataIntegrityViolationException;

import java.time.LocalDate;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.*;

// Usa MockitoExtension para inicializar los mocks
@ExtendWith(MockitoExtension.class)
public class EquipoServiceTest {

    @Mock
    private EquipoRepository equipoRepository;

    // Dependencias mockeadas
    @Mock
    private CompaniaService companiaService;

    @Mock
    private TipoEquipoService tipoEquipoService;

    // El servicio a probar, con los mocks inyectados
    @InjectMocks
    private EquipoService equipoService;

    private Equipo equipo;
    private Faker faker;
    private Integer id;
    private TipoEquipo tipoEquipo;
    private Compania compania;

    @BeforeEach
    public void setUp() {
        faker = new Faker();
        id = faker.number().numberBetween(1, 100);

        // Crear objetos de dependencia
        tipoEquipo = new TipoEquipo(1, "Rescate Urbano");
        compania = new Compania(1, "Primera Compañía","Compañia suprema", LocalDate.now(), 1);

        // Crear objeto Equipo base
        equipo = new Equipo();
        equipo.setIdEquipo(id);
        equipo.setNombre(faker.team().name());
        equipo.setTipoEquipo(tipoEquipo);
        equipo.setCompania(compania);
        equipo.setLider(null);
    }

    // --- Pruebas de operaciones exitosas (Happy Path) ---

    @Test
    public void findAll_shouldReturnAllTeams() {
        // Arrange
        when(equipoRepository.findAll()).thenReturn(List.of(equipo));

        // Act
        List<Equipo> equipos = equipoService.findAll();

        // Assert
        assertNotNull(equipos);
        assertEquals(1, equipos.size());
        verify(equipoRepository, times(1)).findAll();
    }

    @Test
    public void findById_shouldReturnTeam_whenTeamExists() {
        // Arrange
        when(equipoRepository.findById(id)).thenReturn(Optional.of(equipo));

        // Act
        Equipo encontrado = equipoService.findById(id);

        // Assert
        assertNotNull(encontrado);
        assertEquals(equipo.getNombre(), encontrado.getNombre());
        verify(equipoRepository, times(1)).findById(id);
    }

    @Test
    public void save_shouldReturnSavedTeam_whenValid() {
        // Arrange
        when(tipoEquipoService.findById(equipo.getTipoEquipo().getIdTipoEquipo())).thenReturn(equipo.getTipoEquipo());
        when(companiaService.findById(anyInt())).thenReturn(compania);
        when(equipoRepository.save(any(Equipo.class))).thenReturn(equipo);

        // Act
        Equipo guardado = equipoService.save(equipo);

        // Assert
        assertNotNull(guardado);
        verify(equipoRepository, times(1)).save(equipo);
        verify(tipoEquipoService, times(1)).findById(equipo.getTipoEquipo().getIdTipoEquipo());
        verify(companiaService, times(1)).findById(compania.getIdCompania());
    }

    @Test
    public void update_shouldReturnUpdatedTeam_whenTeamExists() {
        // Arrange
        Equipo equipoExistente = new Equipo();
        equipoExistente.setIdEquipo(id);
        equipoExistente.setNombre("Nombre Antiguo");
        equipoExistente.setTipoEquipo(tipoEquipo);
        equipoExistente.setCompania(compania);

        // Datos de actualización
        Equipo equipoNuevo = new Equipo();
        equipoNuevo.setNombre("Nuevo Nombre");
        equipoNuevo.setTipoEquipo(new TipoEquipo(2, "Busqueda"));
        equipoNuevo.setCompania(new Compania(2, "Nueva Cia", "Desc", LocalDate.now(), 1));

        // Mocks
        when(equipoRepository.findById(id)).thenReturn(Optional.of(equipoExistente));
        when(tipoEquipoService.findById(equipoNuevo.getTipoEquipo().getIdTipoEquipo())).thenReturn(equipoNuevo.getTipoEquipo());
        when(companiaService.findById(anyInt())).thenReturn(equipoNuevo.getCompania());
        // El repositorio devuelve la instancia existente (modificada)
        when(equipoRepository.save(any(Equipo.class))).thenReturn(equipoExistente);

        // Act
        Equipo actualizado = equipoService.update(equipoNuevo, id);

        // Assert
        assertNotNull(actualizado);
        assertEquals(equipoNuevo.getNombre(), actualizado.getNombre());
        // Verificamos que los datos se copiaron al objeto existente antes de guardar
        assertEquals(equipoNuevo.getNombre(), equipoExistente.getNombre());

        verify(equipoRepository, times(1)).findById(id);
        verify(equipoRepository, times(1)).save(equipoExistente);
        verify(tipoEquipoService, times(1)).findById(equipoNuevo.getTipoEquipo().getIdTipoEquipo());
        verify(companiaService, times(1)).findById(equipoNuevo.getCompania().getIdCompania());
    }

    @Test
    public void delete_shouldDeleteTeam_whenTeamExists() {
        // Arrange
        when(equipoRepository.findById(id)).thenReturn(Optional.of(equipo));
        doNothing().when(equipoRepository).delete(any(Equipo.class));

        // Act
        assertDoesNotThrow(() -> equipoService.delete(id));

        // Assert
        verify(equipoRepository, times(1)).findById(id);
        verify(equipoRepository, times(1)).delete(equipo);
    }

    // --- Pruebas de escenarios de error ---

    @Test
    public void findById_shouldThrowException_whenTeamNotFound() {
        // Arrange
        when(equipoRepository.findById(id)).thenReturn(Optional.empty());

        // Assert
        assertThrows(NoSuchElementException.class, () -> equipoService.findById(id));
        verify(equipoRepository, times(1)).findById(id);
    }

    @Test
    public void save_shouldThrowException_whenTeamIsNull() {
        // Assert
        assertThrows(IllegalArgumentException.class, () -> equipoService.save(null));
        verify(equipoRepository, never()).save(any());
    }

    @Test
    public void save_shouldThrowException_whenInvalidAttributes() {
        // Arrange
        // 1. Simular que las dependencias OBLIGATORIAS existen para que la lógica
        // pueda intentar validar el atributo interno (el nombre).
        when(tipoEquipoService.findById(equipo.getTipoEquipo().getIdTipoEquipo())).thenReturn(tipoEquipo);
        when(companiaService.findById(compania.getIdCompania())).thenReturn(compania);

        // 2. Establecer el atributo inválido que debe causar la excepción
        equipo.setNombre("");

        // Act & Assert
        // Se debe lanzar IllegalArgumentException porque el nombre es inválido
        assertThrows(IllegalArgumentException.class, () -> equipoService.save(equipo));

        // Verify: Aunque falle por el nombre, las validaciones de dependencias
        // se ejecutaron y, lo más importante, NO se llamó a equipoRepository.save().
        verify(tipoEquipoService, times(1)).findById(anyInt());
        verify(companiaService, times(1)).findById(anyInt());
        verify(equipoRepository, never()).save(any());
    }

    @Test
    public void save_shouldThrowException_whenTipoEquipoNotFound() {
        // Arrange
        // Simular que TipoEquipoService no encuentra el tipo
        when(tipoEquipoService.findById(equipo.getTipoEquipo().getIdTipoEquipo())).thenThrow(new NoSuchElementException());

        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> equipoService.save(equipo));
        verify(tipoEquipoService, times(1)).findById(equipo.getTipoEquipo().getIdTipoEquipo());
        verify(equipoRepository, never()).save(any());
        verify(companiaService, never()).findById(anyInt()); // No debe llamarse a CompaniaService
    }

    @Test
    public void save_shouldThrowException_whenExternalCompanyNotFound() {
        // Arrange
        when(tipoEquipoService.findById(equipo.getTipoEquipo().getIdTipoEquipo())).thenReturn(equipo.getTipoEquipo());

        // Simular que CompaniaService no encuentra la compañía (devuelve null)
        when(companiaService.findById(anyInt())).thenReturn(null);

        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> equipoService.save(equipo));

        // Verify
        verify(tipoEquipoService, times(1)).findById(equipo.getTipoEquipo().getIdTipoEquipo());
        verify(companiaService, times(1)).findById(compania.getIdCompania());
        verify(equipoRepository, never()).save(any());
    }

    @Test
    public void save_shouldThrowException_whenDataIntegrityViolation() {
        // Arrange
        when(tipoEquipoService.findById(any())).thenReturn(equipo.getTipoEquipo());
        when(companiaService.findById(anyInt())).thenReturn(compania);

        // Simular el error de base de datos
        when(equipoRepository.save(any(Equipo.class))).thenThrow(DataIntegrityViolationException.class);

        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> equipoService.save(equipo));

        // Verify
        verify(equipoRepository, times(1)).save(equipo);
        verify(companiaService, times(1)).findById(compania.getIdCompania());
    }


    @Test
    public void update_shouldThrowException_whenTeamNotFound() {
        // Arrange
        when(equipoRepository.findById(id)).thenReturn(Optional.empty());

        // Assert
        assertThrows(NoSuchElementException.class, () -> equipoService.update(equipo, id));
        verify(equipoRepository, times(1)).findById(id);
        verify(equipoRepository, never()).save(any());
    }

    @Test
    public void update_shouldThrowException_whenTipoEquipoNotFound() {
        // Arrange
        Equipo equipoExistente = new Equipo();
        equipoExistente.setIdEquipo(id);

        when(equipoRepository.findById(id)).thenReturn(Optional.of(equipoExistente));
        when(tipoEquipoService.findById(equipo.getTipoEquipo().getIdTipoEquipo())).thenThrow(new NoSuchElementException());

        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> equipoService.update(equipo, id));
        verify(tipoEquipoService, times(1)).findById(equipo.getTipoEquipo().getIdTipoEquipo());
        verify(equipoRepository, never()).save(any());
    }

    @Test
    public void delete_shouldThrowException_whenTeamNotFound() {
        // Arrange
        when(equipoRepository.findById(id)).thenReturn(Optional.empty());

        // Assert
        assertThrows(NoSuchElementException.class, () -> equipoService.delete(id));
        verify(equipoRepository, times(1)).findById(id);
        verify(equipoRepository, never()).delete(any());
    }
}