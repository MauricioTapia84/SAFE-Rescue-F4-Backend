package com.SAFE_Rescue.API_Incidentes.service;

import com.SAFE_Rescue.API_Incidentes.config.EstadoClient;
import com.SAFE_Rescue.API_Incidentes.config.GeolocalizacionClient;
import com.SAFE_Rescue.API_Incidentes.config.EquipoClient;
import com.SAFE_Rescue.API_Incidentes.modelo.*;
import com.SAFE_Rescue.API_Incidentes.config.UsuarioClient;
import com.SAFE_Rescue.API_Incidentes.repository.IncidenteRepository;
import com.SAFE_Rescue.API_Incidentes.repository.TipoIncidenteRepository;

import net.datafaker.Faker;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;


import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class IncidenteServiceTest {

    @Mock
    private IncidenteRepository incidenteRepository;

    @Mock
    private TipoIncidenteRepository tipoIncidenteRepository;

    @Mock
    private TipoIncidenteService tipoIncidenteService;

    @Mock
    private UsuarioClient usuarioClient;

    @Mock
    private EstadoClient estadoClient;

    @Mock
    private GeolocalizacionClient geolocalizacionClient;

    @Mock
    private EquipoClient equipoClient;

    @InjectMocks
    private IncidenteService incidenteService;

    private Incidente incidente;
    private Faker faker;
    private Integer id;
    private EstadoDTO estadoDTO = new EstadoDTO();
    private TipoIncidente tipoIncidente;
    private UsuarioDTO usuarioReporta;
    private Integer idDireccionExistente;

    @BeforeEach
    public void setUp() {
        faker = new Faker();
        id = faker.number().numberBetween(1, 100);
        idDireccionExistente = faker.number().numberBetween(1, 50);

        // Inicialización de DTOs y dependencias
        estadoDTO.setIdEstado(1);
        estadoDTO.setNombre("Activo");

        tipoIncidente = new TipoIncidente(2, "Incendio");
        usuarioReporta = new UsuarioDTO(1,"20000000","2","Jose","Nogales","Benites",LocalDate.now(),"111119999","correo@correo.cl",estadoDTO.getIdEstado(),1,1);

        // Crear objeto Incidente con datos simulados
        incidente = new Incidente();
        incidente.setIdIncidente(id);

        // CORRECCIÓN: Título corto para pasar la validación de 50 caracteres
        incidente.setTitulo("Título corto para prueba unitaria");

        incidente.setDetalle(faker.lorem().paragraph());
        incidente.setFechaRegistro(LocalDateTime.now());
        incidente.setTipoIncidente(tipoIncidente);
        incidente.setIdCiudadano(usuarioReporta.getId());
        incidente.setIdEstadoIncidente(estadoDTO.getIdEstado());
        incidente.setIdDireccion(idDireccionExistente);
        incidente.setIdEquipo(null);
    }

    // --- Pruebas de operaciones CRUD BÁSICAS (Ajustadas) ---

    @Test
    public void findById_shouldReturnIncident_whenIncidentExists() {
        when(incidenteRepository.findById(id)).thenReturn(Optional.of(incidente));
        Incidente encontrado = incidenteService.findById(id);
        assertNotNull(encontrado);
    }

    @Test
    public void save_shouldReturnSavedIncident_whenValid() {
        // Arrange: Mocks de validación de todas las referencias
        when(tipoIncidenteService.findById(anyInt())).thenReturn(tipoIncidente);
        when(usuarioClient.findById(anyInt())).thenReturn(usuarioReporta);
        when(estadoClient.findById(anyInt())).thenReturn(estadoDTO);

        // El cliente devuelve un objeto (DireccionDTO simple) para validar su existencia
        when(geolocalizacionClient.findById(anyInt())).thenReturn(new DireccionDTO());

        when(incidenteRepository.save(any(Incidente.class))).thenReturn(incidente);

        // Act
        Incidente guardado = incidenteService.save(incidente);

        // Assert
        assertNotNull(guardado);
        verify(incidenteRepository, times(1)).save(incidente);
        verify(geolocalizacionClient, times(1)).findById(incidente.getIdDireccion());
    }

    @Test
    public void save_shouldThrowException_whenDireccionNotFound() {
        // Arrange: Mocks de dependencias exitosos, excepto Dirección
        when(tipoIncidenteService.findById(anyInt())).thenReturn(tipoIncidente);
        when(usuarioClient.findById(anyInt())).thenReturn(usuarioReporta);
        when(estadoClient.findById(anyInt())).thenReturn(estadoDTO);

        // Simular que GeolocalizacionClient falla (devuelve null)
        when(geolocalizacionClient.findById(anyInt())).thenReturn(null);

        // Act & Assert
        // Se espera un IllegalArgumentException (el tipo de excepción correcto)
        assertThrows(IllegalArgumentException.class, () -> incidenteService.save(incidente));

        // Verificaciones
        verify(geolocalizacionClient, times(1)).findById(incidente.getIdDireccion());
        verify(incidenteRepository, never()).save(any());
    }


    @Test
    public void update_shouldReturnUpdatedIncident_whenIncidentExists() {
        // Arrange
        Incidente incidenteExistente = new Incidente();
        incidenteExistente.setIdIncidente(id);
        incidenteExistente.setTitulo("Título Antiguo");
        incidenteExistente.setTipoIncidente(tipoIncidente);
        incidenteExistente.setIdCiudadano(usuarioReporta.getId());
        incidenteExistente.setIdEstadoIncidente(estadoDTO.getIdEstado());
        incidenteExistente.setIdDireccion(idDireccionExistente);

        when(incidenteRepository.findById(id)).thenReturn(Optional.of(incidenteExistente));
        when(incidenteRepository.save(any(Incidente.class))).thenReturn(incidente);

        // Mocks para la validación interna y externa
        when(tipoIncidenteRepository.findById(anyInt())).thenReturn(Optional.of(tipoIncidente));
        when(usuarioClient.findById(anyInt())).thenReturn(usuarioReporta);
        when(estadoClient.findById(anyInt())).thenReturn(estadoDTO);
        // El cliente devuelve un objeto (DireccionDTO simple) para validar su existencia
        when(geolocalizacionClient.findById(anyInt())).thenReturn(new DireccionDTO());

        // Act
        Incidente actualizado = incidenteService.update(incidente, id);

        // Assert
        assertNotNull(actualizado);
        verify(incidenteRepository, times(1)).findById(id);
        verify(incidenteRepository, times(1)).save(incidenteExistente);
        verify(geolocalizacionClient, times(1)).findById(incidente.getIdDireccion());
    }

    // --- Pruebas de métodos de GESTIÓN DE DIRECCIONES (Ubicación) ---

    @Test
    public void agregarUbicacionAIncidente_shouldUpdateIncidentWithDireccionId() {
        // Arrange
        Integer expectedDireccionId = 98765;
        String mockUbicacionJson = "{\"calle\":\"Nueva\",\"numero\":\"456\"}";

        // 1. Simular el DTO que devuelve el cliente de geolocalización (usando el constructor completo)
        DireccionDTO direccionResponse = new DireccionDTO(expectedDireccionId,"Avenida Pajaritos","1234","Los saltamontes","Depto 12",new ComunaDTO(),new GeolocalizacionDTO());

        when(geolocalizacionClient.subirUbicacion(mockUbicacionJson))
                .thenReturn(direccionResponse);

        // 2. Simular la obtención y guardado del incidente
        when(incidenteRepository.findById(id)).thenReturn(Optional.of(incidente));
        when(incidenteRepository.save(any(Incidente.class))).thenReturn(incidente);

        // Act
        Incidente returnedIncident = incidenteService.agregarUbicacionAIncidente(id, mockUbicacionJson);

        // Assert
        assertNotNull(returnedIncident);
        assertEquals(expectedDireccionId, returnedIncident.getIdDireccion());

        verify(geolocalizacionClient, times(1)).subirUbicacion(mockUbicacionJson);
        verify(incidenteRepository, times(1)).findById(id);
        verify(incidenteRepository, times(1)).save(incidente);
    }

    @Test
    public void agregarUbicacionAIncidente_shouldThrowException_whenDireccionUploadFails() {
        // Arrange
        String mockUbicacionJson = "{\"calle\":\"Nueva\",\"numero\":\"456\"}";

        // Simular fallo de comunicación (RuntimeException) del cliente al subir
        when(geolocalizacionClient.subirUbicacion(mockUbicacionJson))
                .thenThrow(new RuntimeException("Error de conexión con servicio de direcciones"));

        // Act & Assert
        assertThrows(RuntimeException.class, () -> incidenteService.agregarUbicacionAIncidente(id, mockUbicacionJson));

        // Verificaciones
        verify(geolocalizacionClient, times(1)).subirUbicacion(mockUbicacionJson);
        verify(incidenteRepository, never()).findById(anyInt());
    }

    @Test
    public void asignarDireccion_shouldUpdateIncidentWithExistingDireccionId() {
        // Arrange
        Integer newDireccionId = 12345;
        Incidente incidenteExistente = new Incidente();
        incidenteExistente.setIdIncidente(id);
        incidenteExistente.setIdDireccion(idDireccionExistente);

        when(incidenteRepository.findById(id)).thenReturn(Optional.of(incidenteExistente));
        // Simular que el ID de dirección SÍ existe
        when(geolocalizacionClient.findById(newDireccionId)).thenReturn(new DireccionDTO());
        when(incidenteRepository.save(any(Incidente.class))).thenReturn(incidenteExistente);

        // Act
        incidenteService.asignarDireccion(id, newDireccionId);

        // Assert
        assertEquals(newDireccionId, incidenteExistente.getIdDireccion());
        verify(incidenteRepository, times(1)).findById(id);
        verify(incidenteRepository, times(1)).save(incidenteExistente);
        verify(geolocalizacionClient, times(1)).findById(newDireccionId);
    }
}