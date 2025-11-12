package com.SAFE_Rescue.API_Registros.controller;

import com.SAFE_Rescue.API_Registros.modelo.Estado;
import com.SAFE_Rescue.API_Registros.modelo.Historial;
import com.SAFE_Rescue.API_Registros.service.HistorialService;
import com.fasterxml.jackson.databind.ObjectMapper;
import net.datafaker.Faker;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.List;
import java.util.NoSuchElementException;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Pruebas unitarias para HistorialController.
 * Utiliza @WebMvcTest para aislar la capa del controlador.
 */
@WebMvcTest(HistorialController.class)
public class HistorialControllerTest {

    private static final String API_BASE_URL = "/api-registros/v1/historiales";

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private HistorialService historialService;

    private Faker faker;
    private Historial historialValido;
    private Integer historialId;
    private Integer estadoId;

    @BeforeEach
    void setUp() {
        faker = new Faker();
        historialId = faker.number().numberBetween(1, 100);
        estadoId = faker.number().numberBetween(1, 10);

        // Crear un mock de Estado para evitar NPE en serialización
        Estado estadoMock = new Estado();
        estadoMock.setIdEstado(estadoId);
        estadoMock.setNombre("ACTIVO");

        historialValido = new Historial();
        historialValido.setIdHistorial(historialId);
        historialValido.setFechaHistorial(LocalDateTime.now());
        historialValido.setIdUsuarioReporte(1);
        historialValido.setEstado(estadoMock);
    }

    // -------------------------------------------------------------------------
    // GET / (findAll)
    // -------------------------------------------------------------------------

    @Test
    void getAllHistorial_ShouldReturn200AndList() throws Exception {
        // Arrange
        List<Historial> listaEsperada = List.of(historialValido);
        when(historialService.findAll()).thenReturn(listaEsperada);

        // Act & Assert
        mockMvc.perform(get(API_BASE_URL)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().json(objectMapper.writeValueAsString(listaEsperada)))
                .andExpect(jsonPath("$.size()").value(1));

        verify(historialService, times(1)).findAll();
    }

    // -------------------------------------------------------------------------
    // GET /{id} (findById)
    // -------------------------------------------------------------------------

    @Test
    void getHistorialById_ShouldReturn200AndHistorial_WhenFound() throws Exception {
        // Arrange
        when(historialService.findById(historialId)).thenReturn(historialValido);

        // Act & Assert
        mockMvc.perform(get(API_BASE_URL + "/{id}", historialId)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().json(objectMapper.writeValueAsString(historialValido)))
                .andExpect(jsonPath("$.idHistorial").value(historialId));

        verify(historialService, times(1)).findById(historialId);
    }

    @Test
    void getHistorialById_ShouldReturn404_WhenNotFound() throws Exception {
        // Arrange
        when(historialService.findById(historialId)).thenThrow(new NoSuchElementException());

        // Act & Assert
        mockMvc.perform(get(API_BASE_URL + "/{id}", historialId)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());

        verify(historialService, times(1)).findById(historialId);
    }

    // -------------------------------------------------------------------------
    // GET /buscar?estadoId={estadoId} (findByEstadoId)
    // -------------------------------------------------------------------------

    @Test
    void buscarHistorialPorEstado_ShouldReturn200AndList_WhenFound() throws Exception {
        // Arrange
        List<Historial> listaEsperada = List.of(historialValido);
        when(historialService.findByEstadoId(estadoId)).thenReturn(listaEsperada);

        // Act & Assert
        mockMvc.perform(get(API_BASE_URL + "/buscar")
                        .param("estadoId", String.valueOf(estadoId))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().json(objectMapper.writeValueAsString(listaEsperada)))
                .andExpect(jsonPath("$.size()").value(1));

        verify(historialService, times(1)).findByEstadoId(estadoId);
    }

    @Test
    void buscarHistorialPorEstado_ShouldReturn204_WhenNoContent() throws Exception {
        // Arrange
        when(historialService.findByEstadoId(estadoId)).thenReturn(List.of());

        // Act & Assert
        mockMvc.perform(get(API_BASE_URL + "/buscar")
                        .param("estadoId", String.valueOf(estadoId))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNoContent());

        verify(historialService, times(1)).findByEstadoId(estadoId);
    }

    @Test
    void buscarHistorialPorEstado_ShouldReturn404_WhenEstadoIdNotFound() throws Exception {
        // Arrange: Simular que el estado ID no existe (NoSuchElementException)
        when(historialService.findByEstadoId(estadoId)).thenThrow(new NoSuchElementException());

        // Act & Assert
        mockMvc.perform(get(API_BASE_URL + "/buscar")
                        .param("estadoId", String.valueOf(estadoId))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());

        verify(historialService, times(1)).findByEstadoId(estadoId);
    }

    @Test
    void buscarHistorialPorEstado_ShouldReturn500_OnGenericException() throws Exception {
        // Arrange: Simular cualquier otra excepción genérica
        when(historialService.findByEstadoId(estadoId)).thenThrow(new RuntimeException("Error inesperado"));

        // Act & Assert
        mockMvc.perform(get(API_BASE_URL + "/buscar")
                        .param("estadoId", String.valueOf(estadoId))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isInternalServerError());

        verify(historialService, times(1)).findByEstadoId(estadoId);
    }

    // -------------------------------------------------------------------------
    // POST / (createHistorial)
    // -------------------------------------------------------------------------

    @Test
    void createHistorial_ShouldReturn201AndSavedHistorial() throws Exception {
        // Arrange
        Historial historialToSave = new Historial(); // Usar un objeto sin ID para simular la entrada
        historialToSave.setIdUsuarioReporte(1);
        when(historialService.save(any(Historial.class))).thenReturn(historialValido); // Devuelve el objeto con ID generado

        // Act & Assert
        mockMvc.perform(post(API_BASE_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(historialToSave)))
                .andExpect(status().isCreated())
                .andExpect(content().json(objectMapper.writeValueAsString(historialValido)))
                .andExpect(jsonPath("$.idHistorial").value(historialId));

        verify(historialService, times(1)).save(any(Historial.class));
    }


    // -------------------------------------------------------------------------
    // DELETE /{id} (deleteHistorial)
    // -------------------------------------------------------------------------

    @Test
    void deleteHistorial_ShouldReturn204_WhenSuccessful() throws Exception {
        // Arrange
        doNothing().when(historialService).delete(historialId);

        // Act & Assert
        mockMvc.perform(delete(API_BASE_URL + "/{id}", historialId)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNoContent());

        verify(historialService, times(1)).delete(historialId);
    }

    @Test
    void deleteHistorial_ShouldReturn400_OnAnyServiceException() throws Exception {
        // Arrange: Simular error (ej. Historial no existe o error de FK)
        doThrow(new RuntimeException("Error de borrado")).when(historialService).delete(historialId);

        // Act & Assert
        mockMvc.perform(delete(API_BASE_URL + "/{id}", historialId)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());

        verify(historialService, times(1)).delete(historialId);
    }
}