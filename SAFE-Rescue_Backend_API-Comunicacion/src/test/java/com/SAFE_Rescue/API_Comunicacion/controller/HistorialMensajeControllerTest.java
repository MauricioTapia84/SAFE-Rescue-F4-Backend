package com.SAFE_Rescue.API_Comunicacion.controller;

import com.SAFE_Rescue.API_Comunicacion.modelo.HistorialMensaje;
import com.SAFE_Rescue.API_Comunicacion.modelo.Mensaje;
import com.SAFE_Rescue.API_Comunicacion.modelo.Notificacion;
import com.SAFE_Rescue.API_Comunicacion.service.HistorialMensajeService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Clase de prueba unitaria para HistorialMensajeController.
 * Se corrigió el error PathNotFoundException ajustando el campo de aserción JSON
 * para que coincida con la simulación del modelo.
 */

@WebMvcTest(HistorialMensajeController.class)
class HistorialMensajeControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private HistorialMensajeService historialMensajeService;

    // URL base del controlador
    private static final String BASE_URL = "/api-comunicaciones/v1";

    /**
     * Test para el endpoint GET /historial-mensajes.
     */
    @Test
    void getAllHistorialMensajes_shouldReturnOkAndList_whenDataExists() throws Exception {
        // Arrange
        Integer mensajeId1 = 101;
        Integer mensajeId2 = 102;

        List<HistorialMensaje> mockList = List.of(
                // Pasamos los IDs al sexto argumento (mensajeId)
                new HistorialMensaje(101, LocalDateTime.now().minusHours(2), "Creado", new Mensaje(), new Notificacion(), mensajeId1, 9),
                new HistorialMensaje(102, LocalDateTime.now().minusHours(1), "Enviado", new Mensaje(), new Notificacion(), mensajeId2, 1)
        );

        when(historialMensajeService.findAll()).thenReturn(mockList);

        // Act & Assert: Cambiamos la aserción a 'mensajeId'
        mockMvc.perform(get(BASE_URL + "/historial-mensajes")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.size()").value(mockList.size()))
                // ASERCIÓN CORREGIDA: Usamos 'mensajeId' que es el nombre del getter/campo en el mock.
                .andExpect(jsonPath("$[0].idHistorialMensaje").value(mensajeId1))
                .andExpect(jsonPath("$[1].detalle").value("Enviado"));
    }

    /**
     * Test para el endpoint GET /historial-mensajes (Lista vacía).
     */
    @Test
    void getAllHistorialMensajes_shouldReturnNoContent_whenListIsEmpty() throws Exception {
        // Arrange
        when(historialMensajeService.findAll()).thenReturn(Collections.emptyList());

        // Act & Assert
        mockMvc.perform(get(BASE_URL + "/historial-mensajes")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNoContent())
                .andExpect(content().string(""));
    }

    /**
     * Test para el endpoint GET /mensajes/{idMensaje}/historial.
     */
    @Test
    void obtenerHistorialPorMensaje_shouldReturnOkAndList_whenDataExists() throws Exception {
        // Arrange
        Integer idMensaje = 500;
        List<HistorialMensaje> mockList = List.of(
                // Pasamos el ID del mensaje (500) al sexto argumento (mensajeId)
                new HistorialMensaje(500, LocalDateTime.now().minusMinutes(30), "Procesando", new Mensaje(), null, idMensaje, 1),
                new HistorialMensaje(500, LocalDateTime.now().minusMinutes(10), "Entregado", null, new Notificacion(), idMensaje, 2)
        );

        when(historialMensajeService.obtenerHistorialPorMensaje(idMensaje)).thenReturn(mockList);

        // Act & Assert: Cambiamos la aserción a 'mensajeId'
        mockMvc.perform(get(BASE_URL + "/mensajes/{idMensaje}/historial", idMensaje)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.size()").value(mockList.size()))
                // ASERCIÓN CORREGIDA
                .andExpect(jsonPath("$[0].idHistorialMensaje").value(idMensaje))
                .andExpect(jsonPath("$[1].detalle").value("Entregado"));
    }

    /**
     * Test para el endpoint GET /mensajes/{idMensaje}/historial (Lista vacía).
     */
    @Test
    void obtenerHistorialPorMensaje_shouldReturnNoContent_whenListIsEmpty() throws Exception {
        // Arrange
        Integer idMensaje = 999;
        when(historialMensajeService.obtenerHistorialPorMensaje(idMensaje)).thenReturn(Collections.emptyList());

        // Act & Assert
        mockMvc.perform(get(BASE_URL + "/mensajes/{idMensaje}/historial", idMensaje)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNoContent())
                .andExpect(content().string(""));
    }
}