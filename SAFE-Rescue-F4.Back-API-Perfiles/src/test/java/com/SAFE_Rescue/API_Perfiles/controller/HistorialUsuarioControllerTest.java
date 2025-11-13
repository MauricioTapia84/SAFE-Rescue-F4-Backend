package com.SAFE_Rescue.API_Perfiles.controller;

import com.SAFE_Rescue.API_Perfiles.modelo.Equipo;
import com.SAFE_Rescue.API_Perfiles.modelo.HistorialUsuario;
import com.SAFE_Rescue.API_Perfiles.controlador.HistorialUsuarioController;
import com.SAFE_Rescue.API_Perfiles.modelo.Usuario;
import com.SAFE_Rescue.API_Perfiles.service.HistorialUsuarioService;
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

@WebMvcTest(HistorialUsuarioController.class)
public class HistorialUsuarioControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private HistorialUsuarioService historialUsuarioService;

    private List<HistorialUsuario> historialConDatos;
    private HistorialUsuario registroEjemplo;

    @BeforeEach
    void setUp() {
        // Inicializar un registro de ejemplo
        registroEjemplo = new HistorialUsuario();
        registroEjemplo.setIdHistorial(1);
        registroEjemplo.setUsuario(new Usuario());
        registroEjemplo.setIdEstadoAnterior(2);
        registroEjemplo.setIdEstadoNuevo(1);
        registroEjemplo.setFechaHistorial(LocalDateTime.of(2025, 1, 1, 10, 0));

        // Lista con datos para el caso exitoso
        historialConDatos = Arrays.asList(registroEjemplo);
    }

    // =========================================================================
    // TEST: GET /api/v1/historial (Todos los registros)
    // =========================================================================

    @Test
    void getAllHistorial_DebeRetornarStatus200_Y_ListaDeRegistros() throws Exception {
        // GIVEN: El servicio retorna una lista con datos
        when(historialUsuarioService.findAll()).thenReturn(historialConDatos);

        // WHEN & THEN
        mockMvc.perform(get("/api/v1/historial")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk()) // Espera HTTP 200
                .andExpect(jsonPath("$[0].idHistorial").value(1))
                .andExpect(jsonPath("$[0].tipoPerfil").value("USUARIO"))
                .andExpect(jsonPath("$.length()").value(1)); // Verifica que hay un elemento

        verify(historialUsuarioService, times(1)).findAll();
    }

    @Test
    void getAllHistorial_DebeRetornarStatus204_CuandoLaListaEstaVacia() throws Exception {
        // GIVEN: El servicio retorna una lista vacía
        when(historialUsuarioService.findAll()).thenReturn(Collections.emptyList());

        // WHEN & THEN
        mockMvc.perform(get("/api/v1/historial")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNoContent()) // Espera HTTP 204
                .andExpect(content().string("")); // El cuerpo debe estar vacío

        verify(historialUsuarioService, times(1)).findAll();
    }

    // =========================================================================
    // TEST: GET /api/v1/usuarios/{idUsuario}/historial (Por Usuario)
    // =========================================================================

    @Test
    void getHistorialPorUsuario_DebeRetornarStatus200_Y_ListaDeRegistros() throws Exception {
        // GIVEN
        int idUsuario = 10;
        when(historialUsuarioService.obtenerHistorialPorUsuario(idUsuario)).thenReturn(historialConDatos);

        // WHEN & THEN
        mockMvc.perform(get("/api/v1/usuarios/{idUsuario}/historial", idUsuario)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk()) // Espera HTTP 200
                .andExpect(jsonPath("$[0].idPerfil").value(idUsuario))
                .andExpect(jsonPath("$.length()").value(1));

        verify(historialUsuarioService, times(1)).obtenerHistorialPorUsuario(idUsuario);
    }

    @Test
    void getHistorialPorUsuario_DebeRetornarStatus204_CuandoNoHayHistorial() throws Exception {
        // GIVEN
        int idUsuario = 99;
        when(historialUsuarioService.obtenerHistorialPorUsuario(idUsuario)).thenReturn(Collections.emptyList());

        // WHEN & THEN
        mockMvc.perform(get("/api/v1/usuarios/{idUsuario}/historial", idUsuario)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNoContent()) // Espera HTTP 204
                .andExpect(content().string(""));

        verify(historialUsuarioService, times(1)).obtenerHistorialPorUsuario(idUsuario);
    }

    // **Nota sobre 404:** Si el usuario *no existe*, la capa de servicio debería lanzar una
    // `NoSuchElementException`. Tu GlobalExceptionHandler debería mapearla a 404.
    @Test
    void getHistorialPorUsuario_DebeRetornarStatus404_CuandoElUsuarioNoExiste() throws Exception {
        // GIVEN
        int idUsuarioNoExistente = 999;
        String errorMessage = "El usuario con ID 999 no existe.";
        when(historialUsuarioService.obtenerHistorialPorUsuario(idUsuarioNoExistente))
                .thenThrow(new java.util.NoSuchElementException(errorMessage));

        // WHEN & THEN
        mockMvc.perform(get("/api/v1/usuarios/{idUsuario}/historial", idUsuarioNoExistente)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound()) // Espera HTTP 404
                .andExpect(content().string(errorMessage));
    }


    // =========================================================================
    // TEST: GET /api/v1/equipos/{idEquipo}/historial (Por Equipo)
    // =========================================================================

    @Test
    void getHistorialPorEquipo_DebeRetornarStatus200_Y_ListaDeRegistros() throws Exception {
        // GIVEN
        int idEquipo = 20;
        HistorialUsuario registroEquipo = new HistorialUsuario();
        registroEquipo.setEquipo(new Equipo());
        List<HistorialUsuario> historialEquipo = Arrays.asList(registroEquipo);

        when(historialUsuarioService.obtenerHistorialPorEquipo(idEquipo)).thenReturn(historialEquipo);

        // WHEN & THEN
        mockMvc.perform(get("/api/v1/equipos/{idEquipo}/historial", idEquipo)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk()) // Espera HTTP 200
                .andExpect(jsonPath("$[0].idPerfil").value(idEquipo))
                .andExpect(jsonPath("$[0].tipoPerfil").value("EQUIPO"))
                .andExpect(jsonPath("$.length()").value(1));

        verify(historialUsuarioService, times(1)).obtenerHistorialPorEquipo(idEquipo);
    }

    @Test
    void getHistorialPorEquipo_DebeRetornarStatus204_CuandoNoHayHistorial() throws Exception {
        // GIVEN
        int idEquipo = 99;
        when(historialUsuarioService.obtenerHistorialPorEquipo(idEquipo)).thenReturn(Collections.emptyList());

        // WHEN & THEN
        mockMvc.perform(get("/api/v1/equipos/{idEquipo}/historial", idEquipo)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNoContent()) // Espera HTTP 204
                .andExpect(content().string(""));

        verify(historialUsuarioService, times(1)).obtenerHistorialPorEquipo(idEquipo);
    }

    // **Nota sobre 404:** Si el equipo *no existe*, la capa de servicio debería lanzar una
    // `NoSuchElementException`.
    @Test
    void getHistorialPorEquipo_DebeRetornarStatus404_CuandoElEquipoNoExiste() throws Exception {
        // GIVEN
        int idEquipoNoExistente = 999;
        String errorMessage = "El equipo con ID 999 no existe.";
        when(historialUsuarioService.obtenerHistorialPorEquipo(idEquipoNoExistente))
                .thenThrow(new java.util.NoSuchElementException(errorMessage));

        // WHEN & THEN
        mockMvc.perform(get("/api/v1/equipos/{idEquipo}/historial", idEquipoNoExistente)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound()) // Espera HTTP 404
                .andExpect(content().string(errorMessage));
    }
}