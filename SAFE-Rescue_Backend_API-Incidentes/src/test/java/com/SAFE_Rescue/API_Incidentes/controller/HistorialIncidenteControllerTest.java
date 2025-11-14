package com.SAFE_Rescue.API_Incidentes.controller;

import com.SAFE_Rescue.API_Incidentes.modelo.HistorialIncidente;
import com.SAFE_Rescue.API_Incidentes.modelo.Incidente;
import com.SAFE_Rescue.API_Incidentes.service.HistorialIncidenteService;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Pruebas de integración a nivel de controlador para HistorialIncidenteController.
 * Simula peticiones HTTP y verifica la respuesta del controlador.
 */
@WebMvcTest(HistorialIncidenteController.class)
public class HistorialIncidenteControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private HistorialIncidenteService historialIncidenteService;

    private List<HistorialIncidente> historialConDatos;
    private HistorialIncidente registroEjemplo;
    private final int ID_INCIDENTE = 100;

    @BeforeEach
    void setUp() {
        // Inicializar un registro de ejemplo para un Incidente
        Incidente incidente = new Incidente();
        incidente.setIdIncidente(ID_INCIDENTE);

        registroEjemplo = new HistorialIncidente();
        registroEjemplo.setIdHistorial(1);
        registroEjemplo.setIncidente(incidente);
        registroEjemplo.setIdEstadoAnterior(1);
        registroEjemplo.setIdEstadoNuevo(2);
        registroEjemplo.setDetalle("Cambio a estado 'Atendido'");
        registroEjemplo.setFechaHistorial(LocalDateTime.of(2025, 1, 1, 10, 0));

        // Lista con datos para el caso exitoso
        historialConDatos = Arrays.asList(registroEjemplo);
    }

    // =========================================================================
    // TEST: GET /api/v1/historial/incidentes (Todos los registros)
    // =========================================================================

    @Test
    void getAllHistorial_DebeRetornarStatus200_Y_ListaDeRegistros() throws Exception {
        // GIVEN: El servicio retorna una lista con datos
        when(historialIncidenteService.findAll()).thenReturn(historialConDatos);

        // WHEN & THEN
        mockMvc.perform(get("/api/v1/historial/incidentes")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk()) // Espera HTTP 200
                .andExpect(jsonPath("$[0].idHistorial").value(1))
                // Verificamos un campo del incidente asociado para confirmar la estructura
                .andExpect(jsonPath("$[0].incidente.idIncidente").value(ID_INCIDENTE))
                .andExpect(jsonPath("$.length()").value(1)); // Verifica que hay un elemento

        verify(historialIncidenteService, times(1)).findAll();
    }

    @Test
    void getAllHistorial_DebeRetornarStatus204_CuandoLaListaEstaVacia() throws Exception {
        // GIVEN: El servicio retorna una lista vacía
        when(historialIncidenteService.findAll()).thenReturn(Collections.emptyList());

        // WHEN & THEN
        mockMvc.perform(get("/api/v1/historial/incidentes")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNoContent()) // Espera HTTP 204
                .andExpect(content().string("")); // El cuerpo debe estar vacío

        verify(historialIncidenteService, times(1)).findAll();
    }

    // =========================================================================
    // TEST: GET /api/v1/incidentes/{idIncidente}/historial (Por Incidente)
    // =========================================================================

    @Test
    void getHistorialPorIncidente_DebeRetornarStatus200_Y_ListaDeRegistros() throws Exception {
        // GIVEN
        when(historialIncidenteService.obtenerHistorialPorIncidente(ID_INCIDENTE)).thenReturn(historialConDatos);

        // WHEN & THEN
        mockMvc.perform(get("/api/v1/incidentes/{idIncidente}/historial", ID_INCIDENTE)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk()) // Espera HTTP 200
                .andExpect(jsonPath("$[0].idHistorial").value(1))
                .andExpect(jsonPath("$[0].incidente.idIncidente").value(ID_INCIDENTE))
                .andExpect(jsonPath("$.length()").value(1));

        verify(historialIncidenteService, times(1)).obtenerHistorialPorIncidente(ID_INCIDENTE);
    }

    @Test
    void getHistorialPorIncidente_DebeRetornarStatus204_CuandoNoHayHistorial() throws Exception {
        // GIVEN
        when(historialIncidenteService.obtenerHistorialPorIncidente(ID_INCIDENTE)).thenReturn(Collections.emptyList());

        // WHEN & THEN
        mockMvc.perform(get("/api/v1/incidentes/{idIncidente}/historial", ID_INCIDENTE)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNoContent()) // Espera HTTP 204
                .andExpect(content().string(""));

        verify(historialIncidenteService, times(1)).obtenerHistorialPorIncidente(ID_INCIDENTE);
    }

    @Test
    void getHistorialPorIncidente_DebeRetornarStatus404_CuandoElIncidenteNoExiste() throws Exception {
        // GIVEN
        int idNoExistente = 999;
        String errorMessage = "Incidente con ID 999 no encontrado.";
        // Usamos EntityNotFoundException ya que es la que se recomienda lanzar desde la capa Service
        when(historialIncidenteService.obtenerHistorialPorIncidente(idNoExistente))
                .thenThrow(new EntityNotFoundException(errorMessage));

        // WHEN & THEN
        mockMvc.perform(get("/api/v1/incidentes/{idIncidente}/historial", idNoExistente)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound()); // Espera HTTP 404 (asumiendo un GlobalExceptionHandler)

    }
}