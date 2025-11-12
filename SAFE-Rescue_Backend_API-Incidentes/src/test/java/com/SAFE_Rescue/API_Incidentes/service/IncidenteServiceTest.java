package com.SAFE_Rescue.API_Incidentes.service;

import com.SAFE_Rescue.API_Incidentes.config.EstadoClient;
import com.SAFE_Rescue.API_Incidentes.config.GeolocalizacionClient;
import com.SAFE_Rescue.API_Incidentes.modelo.EstadoDTO;
import com.SAFE_Rescue.API_Incidentes.modelo.Incidente;
import com.SAFE_Rescue.API_Incidentes.modelo.TipoIncidente;
import com.SAFE_Rescue.API_Incidentes.config.UsuarioClient;
import com.SAFE_Rescue.API_Incidentes.modelo.UsuarioDTO;
import com.SAFE_Rescue.API_Incidentes.repository.IncidenteRepository;

import net.datafaker.Faker;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import java.time.LocalDate;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.*;

// --- Fin Clases Mock Simplificadas ---

@ExtendWith(MockitoExtension.class)
public class IncidenteServiceTest {

    // --- DTO Placeholder para simular la respuesta del cliente de geolocalización ---
    // Asumimos que el cliente devuelve un objeto con el ID del mapa.
    private static class MapIdDTO {
        private Integer idMapa;
        public MapIdDTO(Integer idMapa) { this.idMapa = idMapa; }
        public Integer getIdMapa() { return idMapa; }
        public void setIdMapa(Integer idMapa) { this.idMapa = idMapa; }
    }
    // --- Fin DTO Placeholder ---


    @MockitoBean
    private IncidenteRepository incidenteRepository;

    @MockitoBean
    private TipoIncidenteService tipoIncidenteService; // Dependencia para validar el tipo de incidente

    @MockitoBean
    private UsuarioClient usuarioClient;

    @MockitoBean
    private EstadoClient estadoClient; // Cliente para obtener el estado

    // **Añadido/Corregido:** Mock para el cliente de Geolocalización
    @MockitoBean
    private GeolocalizacionClient geolocalizacionClient; // Cliente para interacción con mapas

    @InjectMocks
    private IncidenteService incidenteService; // El servicio bajo prueba

    private Incidente incidente;
    private Faker faker;
    private Integer id;
    private EstadoDTO estadoDTO = new EstadoDTO();
    private TipoIncidente tipoIncidente;
    private UsuarioDTO usuarioReporta;

    @BeforeEach
    public void setUp() {
        faker = new Faker();
        id = faker.number().numberBetween(1, 100);

        // Inicialización de DTOs y dependencias
        estadoDTO.setIdEstado(1);
        estadoDTO.setNombre("Activo");

        tipoIncidente = new TipoIncidente(2, "Incendio");
        usuarioReporta = new UsuarioDTO(1,"20000000","2","Jose","Nogales","Benites",LocalDate.now(),"111119999","correo@correo.cl",estadoDTO.getIdEstado(),1,1);

        // Crear objeto Incidente con datos simulados
        incidente = new Incidente();
        incidente.setIdIncidente(id);
        incidente.setTitulo(faker.lorem().sentence(3));
        incidente.setDetalle(faker.lorem().paragraph());
        incidente.setFechaRegistro(LocalDate.now());
        incidente.setTipoIncidente(tipoIncidente);
        incidente.setIdCiudadano(usuarioReporta.getId());
        incidente.setIdEstadoIncidente(estadoDTO.getIdEstado()); // Estado por defecto "Activo"
        incidente.setIdDireccion(faker.number().numberBetween(1, 50));
        // Nota: Asumo que tu clase Incidente tiene métodos setIdMapa/getIdMapa o usa setIdDireccion para el ID del mapa.
        // Para que el test pase, asumo getIdMapa() existe.
    }

    // --- Pruebas de operaciones CRUD exitosas ---

    @Test
    public void findAll_shouldReturnAllIncidents() {
        // Arrange
        when(incidenteRepository.findAll()).thenReturn(List.of(incidente));

        // Act
        List<Incidente> incidentes = incidenteService.findAll();

        // Assert
        assertNotNull(incidentes);
        assertFalse(incidentes.isEmpty());
        assertEquals(1, incidentes.size());
        assertEquals(incidente.getTitulo(), incidentes.get(0).getTitulo());
        verify(incidenteRepository, times(1)).findAll();
    }

    @Test
    public void findById_shouldReturnIncident_whenIncidentExists() {
        // Arrange
        when(incidenteRepository.findById(id)).thenReturn(Optional.of(incidente));

        // Act
        Incidente encontrado = incidenteService.findById(id);

        // Assert
        assertNotNull(encontrado);
        assertEquals(incidente.getTitulo(), encontrado.getTitulo());
        verify(incidenteRepository, times(1)).findById(id);
    }

    @Test
    public void findById_shouldThrowException_whenIncidentNotFound() {
        // Arrange
        when(incidenteRepository.findById(anyInt())).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(NoSuchElementException.class, () -> incidenteService.findById(999));
        verify(incidenteRepository, times(1)).findById(999);
    }

    @Test
    public void save_shouldReturnSavedIncident_whenValid() {
        // Arrange: Se simulan las validaciones de dependencias
        when(tipoIncidenteService.findById(anyInt())).thenReturn(tipoIncidente);
        when(usuarioClient.findById(anyInt())).thenReturn(usuarioReporta);
        when(estadoClient.findById(anyInt())).thenReturn(estadoDTO);
        when(incidenteRepository.save(any(Incidente.class))).thenReturn(incidente);

        // Act
        Incidente guardado = incidenteService.save(incidente);

        // Assert
        assertNotNull(guardado);
        verify(incidenteRepository, times(1)).save(incidente);
        verify(tipoIncidenteService, times(1)).findById(incidente.getTipoIncidente().getIdTipoIncidente());
        verify(usuarioClient, times(1)).findById(incidente.getIdCiudadano());
        verify(estadoClient, times(1)).findById(incidente.getIdEstadoIncidente());
    }

    @Test
    public void update_shouldReturnUpdatedIncident_whenIncidentExists() {
        // Arrange
        Incidente incidenteExistente = new Incidente();
        incidenteExistente.setIdIncidente(id);
        incidenteExistente.setTitulo("Título Antiguo");
        // Aseguramos que el objeto existente tenga referencias válidas
        incidenteExistente.setTipoIncidente(tipoIncidente);
        incidenteExistente.setIdCiudadano(usuarioReporta.getId());
        incidenteExistente.setIdEstadoIncidente(estadoDTO.getIdEstado());

        when(incidenteRepository.findById(id)).thenReturn(Optional.of(incidenteExistente));
        when(incidenteRepository.save(any(Incidente.class))).thenReturn(incidente);
        when(tipoIncidenteService.findById(anyInt())).thenReturn(tipoIncidente);
        when(usuarioClient.findById(anyInt())).thenReturn(usuarioReporta);
        when(estadoClient.findById(anyInt())).thenReturn(estadoDTO);

        // Act
        Incidente actualizado = incidenteService.update(incidente, id);

        // Assert
        assertNotNull(actualizado);
        assertEquals(incidente.getTitulo(), actualizado.getTitulo());
        verify(incidenteRepository, times(1)).findById(id);
        // Debe guardarse el objeto existente actualizado
        verify(incidenteRepository, times(1)).save(incidenteExistente);
        verify(tipoIncidenteService, times(1)).findById(incidente.getTipoIncidente().getIdTipoIncidente());
        verify(usuarioClient, times(1)).findById(incidente.getIdCiudadano());
        verify(estadoClient, times(1)).findById(incidente.getIdEstadoIncidente());
    }

    @Test
    public void update_shouldThrowException_whenIncidentNotFound() {
        // Arrange
        // Mocks de validación para que el objeto 'incidente' sea válido
        when(tipoIncidenteService.findById(anyInt())).thenReturn(tipoIncidente);
        when(usuarioClient.findById(anyInt())).thenReturn(usuarioReporta);
        when(estadoClient.findById(anyInt())).thenReturn(estadoDTO);
        when(incidenteRepository.findById(anyInt())).thenReturn(Optional.empty());

        // Act & Assert: la excepción debe ser NoSuchElementException
        assertThrows(NoSuchElementException.class, () -> incidenteService.update(incidente, 999));

        verify(incidenteRepository, times(1)).findById(999);
        verify(tipoIncidenteService, times(1)).findById(incidente.getTipoIncidente().getIdTipoIncidente());
        verify(usuarioClient, times(1)).findById(incidente.getIdCiudadano());
        verify(estadoClient, times(1)).findById(incidente.getIdEstadoIncidente());
    }

    @Test
    public void delete_shouldDeleteIncident_whenIncidentExists() {
        // Arrange
        when(incidenteRepository.findById(id)).thenReturn(Optional.of(incidente));

        // Act & Assert
        assertDoesNotThrow(() -> incidenteService.delete(id));

        verify(incidenteRepository, times(1)).findById(id);
        verify(incidenteRepository, times(1)).delete(incidente);
    }

    @Test
    public void delete_shouldThrowException_whenIncidentNotFound() {
        // Arrange
        when(incidenteRepository.findById(anyInt())).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(NoSuchElementException.class, () -> incidenteService.delete(999));

        verify(incidenteRepository, times(1)).findById(999);
        verify(incidenteRepository, never()).delete(any());
    }

    // --- Pruebas de escenarios de error (Validación y Clientes) ---

    @Test
    public void save_shouldThrowException_whenDataIntegrityViolation() {
        // Arrange: Mocks de validación exitosos
        when(tipoIncidenteService.findById(anyInt())).thenReturn(tipoIncidente);
        when(usuarioClient.findById(anyInt())).thenReturn(usuarioReporta);
        when(estadoClient.findById(anyInt())).thenReturn(estadoDTO);

        // Simular la violación al intentar guardar (ej: RUN duplicado si Incidente tuviera un campo único)
        when(incidenteRepository.save(any(Incidente.class))).thenThrow(new DataIntegrityViolationException("Título duplicado o violación de FK"));

        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> incidenteService.save(incidente));

        // Verificaciones
        verify(incidenteRepository, times(1)).save(incidente);
    }

    @Test
    public void save_shouldThrowException_whenEstadoNotFound() {
        // Arrange: Mocks de dependencias exitosos
        when(tipoIncidenteService.findById(anyInt())).thenReturn(tipoIncidente);
        when(usuarioClient.findById(anyInt())).thenReturn(usuarioReporta);

        // Simular que el EstadoClient falla
        when(estadoClient.findById(anyInt())).thenThrow(new RuntimeException("El ID de estado no existe."));

        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> incidenteService.save(incidente));

        // Verificaciones
        verify(estadoClient, times(1)).findById(incidente.getIdEstadoIncidente());
        verify(incidenteRepository, never()).save(any());
    }

    @Test
    public void save_shouldThrowException_whenTipoIncidenteNotFound() {
        // Arrange: Simular que TipoIncidenteService falla
        when(tipoIncidenteService.findById(anyInt())).thenThrow(new NoSuchElementException("Tipo de incidente no encontrado."));

        // Act & Assert
        assertThrows(NoSuchElementException.class, () -> incidenteService.save(incidente));

        // Verificaciones
        verify(tipoIncidenteService, times(1)).findById(incidente.getTipoIncidente().getIdTipoIncidente());
        verify(usuarioClient, never()).findById(anyInt());
        verify(incidenteRepository, never()).save(any());
    }

    // --- Pruebas del método de gestión de ubicación/mapa ---

    @Test
    public void agregarUbicacionAIncidente_shouldUpdateIncidentWithMapId() {
        // Arrange
        String mockMapIdString = "98765";
        Integer mockMapId = Integer.parseInt(mockMapIdString);
        // Simular un objeto de ubicación/datos que se enviaría al Cliente
        String mockUbicacionJson = "{\"lat\":-33.4,\"lon\":-70.6}";

        // **CORRECCIÓN 1: Mockear la llamada al cliente para devolver el DTO**
        MapIdDTO mapResponse = new MapIdDTO(mockMapId);
        when(geolocalizacionClient.subirUbicacion(mockUbicacionJson))
                .thenReturn(mapResponse);

        when(incidenteRepository.findById(id)).thenReturn(Optional.of(incidente));
        when(incidenteRepository.save(any(Incidente.class))).thenReturn(incidente);

        // Act
        Incidente returnedIncident = incidenteService.agregarUbicacionAIncidente(id, mockUbicacionJson);

        // Assert
        assertNotNull(returnedIncident);
        // **CORRECCIÓN 2: El valor que se espera en el Incidente debe ser el del DTO**
        // Asumo que tu servicio usa el DTO para establecer el ID del mapa en el Incidente (setIdMapa)
        assertEquals(mockMapId, returnedIncident.getIdMapa());
        // **CORRECCIÓN 3: Usar geolocalizacionClient en las verificaciones**
        verify(geolocalizacionClient, times(1)).subirUbicacion(mockUbicacionJson);
        verify(incidenteRepository, times(1)).findById(id);
        verify(incidenteRepository, times(1)).save(incidente);
    }

    @Test
    public void agregarUbicacionAIncidente_shouldThrowException_whenMapUploadFails() {
        // Arrange
        String mockUbicacionJson = "{\"lat\":-33.4,\"lon\":-70.6}";

        // Simular fallo de comunicación ANTES de buscar el incidente
        // **CORRECCIÓN 4: Usar geolocalizacionClient en el mock**
        when(geolocalizacionClient.subirUbicacion(mockUbicacionJson)).thenThrow(new RuntimeException("Error de conexión con servicio de mapas"));

        // Act & Assert
        assertThrows(RuntimeException.class, () -> incidenteService.agregarUbicacionAIncidente(id, mockUbicacionJson));

        // Assert: Solo se verifica la llamada a geolocalizacionClient
        // **CORRECCIÓN 5: Usar geolocalizacionClient en la verificación**
        verify(geolocalizacionClient, times(1)).subirUbicacion(mockUbicacionJson);
        verify(incidenteRepository, never()).findById(anyInt());
        verify(incidenteRepository, never()).save(any());
    }
}