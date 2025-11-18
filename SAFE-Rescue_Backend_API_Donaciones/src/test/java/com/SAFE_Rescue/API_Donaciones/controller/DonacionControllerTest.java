package com.SAFE_Rescue.API_Donaciones.controller;

import com.SAFE_Rescue.API_Donaciones.modelo.Donacion;
import com.SAFE_Rescue.API_Donaciones.service.DonacionService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
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
import java.util.NoSuchElementException;

import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Clase de prueba unitaria para DonacionController.
 * Utiliza @WebMvcTest para un test enfocado en la capa Controller,
 * mockeando la dependencia DonacionService.
 */
@WebMvcTest(DonacionController.class)
public class DonacionControllerTest {

    private static final String BASE_URL = "/api-donaciones/v1/donaciones";

    @Autowired
    private MockMvc mockMvc; // Utilizado para simular las peticiones HTTP

    @Autowired
    private ObjectMapper objectMapper; // Utilizado para convertir objetos Java a JSON

    @MockitoBean
    private DonacionService donacionService; // Mock del servicio para aislar el controlador

    private Donacion donacionValida;

    @BeforeEach
    void setUp() {
        donacionValida = new Donacion();
        donacionValida.setIdDonacion(1);
        donacionValida.setIdDonante(101);
        donacionValida.setMonto(15000); // Monto CLP (Integer)
        donacionValida.setMetodoPago("Tarjeta");
        donacionValida.setFechaDonacion(LocalDateTime.now().minusDays(1));
    }

    // --- Tests para GET / (listar) ---

    @Test
    @DisplayName("GET / - Debería retornar 200 OK y lista de donaciones")
    void listar_Success() throws Exception {
        // Arrange
        List<Donacion> donaciones = Arrays.asList(donacionValida, new Donacion());
        when(donacionService.findAll()).thenReturn(donaciones);

        // Act & Assert
        mockMvc.perform(get(BASE_URL)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.size()", is(2)));

        verify(donacionService, times(1)).findAll();
    }

    @Test
    @DisplayName("GET / - Debería retornar 204 NO_CONTENT si no hay donaciones")
    void listar_NoContent() throws Exception {
        // Arrange
        when(donacionService.findAll()).thenReturn(Collections.emptyList());

        // Act & Assert
        mockMvc.perform(get(BASE_URL)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNoContent());

        verify(donacionService, times(1)).findAll();
    }

    // --- Tests para GET /{id} (buscarDonacion) ---

    @Test
    @DisplayName("GET /{id} - Debería retornar 200 OK y la donación encontrada")
    void buscarDonacion_Success() throws Exception {
        // Arrange
        when(donacionService.findById(donacionValida.getIdDonacion())).thenReturn(donacionValida);

        // Act & Assert
        mockMvc.perform(get(BASE_URL + "/{id}", donacionValida.getIdDonacion())
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.idDonacion", is(donacionValida.getIdDonacion())))
                .andExpect(jsonPath("$.monto", is(donacionValida.getMonto())));

        verify(donacionService, times(1)).findById(donacionValida.getIdDonacion());
    }

    @Test
    @DisplayName("GET /{id} - Debería retornar 404 NOT_FOUND si la donación no existe")
    void buscarDonacion_NotFound() throws Exception {
        // Arrange
        Integer idInexistente = 99;
        when(donacionService.findById(idInexistente)).thenThrow(new NoSuchElementException());

        // Act & Assert
        mockMvc.perform(get(BASE_URL + "/{id}", idInexistente)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound())
                .andExpect(content().string("Donación no encontrada"));

        verify(donacionService, times(1)).findById(idInexistente);
    }

    // --- Tests para POST / (agregarDonacion) ---

    @Test
    @DisplayName("POST / - Debería retornar 201 CREATED al crear una donación válida")
    void agregarDonacion_Success() throws Exception {
        // Arrange
        Donacion donacionSinId = new Donacion();
        donacionSinId.setIdDonante(102);
        donacionSinId.setMonto(50000);
        donacionSinId.setMetodoPago("Transferencia");
        // No se mokea el retorno, solo se verifica la llamada al servicio

        // Act & Assert
        mockMvc.perform(post(BASE_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(donacionSinId)))
                .andExpect(status().isCreated())
                .andExpect(content().string("Donación creada con éxito."));

        verify(donacionService, times(1)).save(any(Donacion.class));
    }

    @Test
    @DisplayName("POST / - Debería retornar 400 BAD_REQUEST si falla la validación del Donante ID")
    void agregarDonacion_BadRequest_InvalidDonante() throws Exception {
        // Arrange
        String mensajeError = "Error de referencia: Donante no encontrado con ID: 999";
        doThrow(new IllegalArgumentException(mensajeError))
                .when(donacionService).save(any(Donacion.class));

        // Act & Assert
        mockMvc.perform(post(BASE_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(donacionValida)))
                .andExpect(status().isBadRequest())
                .andExpect(content().string("Error de validación: " + mensajeError));

        verify(donacionService, times(1)).save(any(Donacion.class));
    }

    @Test
    @DisplayName("POST / - Debería retornar 500 INTERNAL_SERVER_ERROR por error genérico")
    void agregarDonacion_InternalServerError() throws Exception {
        // Arrange
        doThrow(new RuntimeException("DB down"))
                .when(donacionService).save(any(Donacion.class));

        // Act & Assert
        mockMvc.perform(post(BASE_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(donacionValida)))
                .andExpect(status().isInternalServerError())
                .andExpect(content().string(org.hamcrest.Matchers.startsWith("Error interno del servidor:")));

        verify(donacionService, times(1)).save(any(Donacion.class));
    }

    // --- Tests para PUT /{id} (actualizarDonacion) ---

    @Test
    @DisplayName("PUT /{id} - Debería retornar 200 OK al actualizar con éxito")
    void actualizarDonacion_Success() throws Exception {
        // Arrange
        Donacion cambios = new Donacion();
        cambios.setMonto(90000); // Solo se cambia el monto
        doReturn(new Donacion()).when(donacionService).update(any(Donacion.class), eq(donacionValida.getIdDonacion()));

        // Act & Assert
        mockMvc.perform(put(BASE_URL + "/{id}", donacionValida.getIdDonacion())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(cambios)))
                .andExpect(status().isOk())
                .andExpect(content().string("Donación actualizada con éxito"));

        verify(donacionService, times(1)).update(any(Donacion.class), eq(donacionValida.getIdDonacion()));
    }

    @Test
    @DisplayName("PUT /{id} - Debería retornar 404 NOT_FOUND si la donación a actualizar no existe")
    void actualizarDonacion_NotFound() throws Exception {
        // Arrange
        Integer idInexistente = 99;
        doThrow(new NoSuchElementException("No se encontró Donacion con ID: 99"))
                .when(donacionService).update(any(Donacion.class), eq(idInexistente));

        // Act & Assert
        mockMvc.perform(put(BASE_URL + "/{id}", idInexistente)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(donacionValida)))
                .andExpect(status().isNotFound())
                .andExpect(content().string("Donación no encontrada"));

        verify(donacionService, times(1)).update(any(Donacion.class), eq(idInexistente));
    }

    @Test
    @DisplayName("PUT /{id} - Debería retornar 400 BAD_REQUEST si falla la validación (ej. Donante ID inválido)")
    void actualizarDonacion_BadRequest() throws Exception {
        // Arrange
        String mensajeError = "Error de referencia en la actualización: Donante no encontrado con ID: 999";
        doThrow(new IllegalArgumentException(mensajeError))
                .when(donacionService).update(any(Donacion.class), eq(donacionValida.getIdDonacion()));

        // Act & Assert
        mockMvc.perform(put(BASE_URL + "/{id}", donacionValida.getIdDonacion())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(donacionValida)))
                .andExpect(status().isBadRequest())
                .andExpect(content().string("Error de validación: " + mensajeError));

        verify(donacionService, times(1)).update(any(Donacion.class), eq(donacionValida.getIdDonacion()));
    }

    @Test
    @DisplayName("PUT /{id} - Debería retornar 500 INTERNAL_SERVER_ERROR por error genérico")
    void actualizarDonacion_InternalServerError() throws Exception {
        // Arrange
        doThrow(new RuntimeException("Unknown error"))
                .when(donacionService).update(any(Donacion.class), eq(donacionValida.getIdDonacion()));

        // Act & Assert
        mockMvc.perform(put(BASE_URL + "/{id}", donacionValida.getIdDonacion())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(donacionValida)))
                .andExpect(status().isInternalServerError())
                .andExpect(content().string(org.hamcrest.Matchers.startsWith("Error interno del servidor.")));

        verify(donacionService, times(1)).update(any(Donacion.class), eq(donacionValida.getIdDonacion()));
    }

    // --- Tests para DELETE /{id} (eliminarDonacion) ---

    @Test
    @DisplayName("DELETE /{id} - Debería retornar 200 OK al eliminar con éxito")
    void eliminarDonacion_Success() throws Exception {
        // Arrange
        doNothing().when(donacionService).delete(donacionValida.getIdDonacion());

        // Act & Assert
        mockMvc.perform(delete(BASE_URL + "/{id}", donacionValida.getIdDonacion()))
                .andExpect(status().isOk())
                .andExpect(content().string("Donación eliminada con éxito."));

        verify(donacionService, times(1)).delete(donacionValida.getIdDonacion());
    }

    @Test
    @DisplayName("DELETE /{id} - Debería retornar 404 NOT_FOUND si la donación no existe")
    void eliminarDonacion_NotFound() throws Exception {
        // Arrange
        Integer idInexistente = 99;
        doThrow(new NoSuchElementException()).when(donacionService).delete(idInexistente);

        // Act & Assert
        mockMvc.perform(delete(BASE_URL + "/{id}", idInexistente))
                .andExpect(status().isNotFound())
                .andExpect(content().string("Donación no encontrada"));

        verify(donacionService, times(1)).delete(idInexistente);
    }

    @Test
    @DisplayName("DELETE /{id} - Debería retornar 500 INTERNAL_SERVER_ERROR por error genérico")
    void eliminarDonacion_InternalServerError() throws Exception {
        // Arrange
        doThrow(new RuntimeException()).when(donacionService).delete(donacionValida.getIdDonacion());

        // Act & Assert
        mockMvc.perform(delete(BASE_URL + "/{id}", donacionValida.getIdDonacion()))
                .andExpect(status().isInternalServerError())
                .andExpect(content().string("Error interno del servidor."));

        verify(donacionService, times(1)).delete(donacionValida.getIdDonacion());
    }

    // --- Tests para GET /por-donante/{donanteId} (buscarPorDonante) ---

    @Test
    @DisplayName("GET /por-donante/{donanteId} - Debería retornar 200 OK y lista de donaciones por donante")
    void buscarPorDonante_Success() throws Exception {
        // Arrange
        Integer donanteId = 101;
        List<Donacion> donaciones = List.of(donacionValida);
        when(donacionService.findByDonante(donanteId)).thenReturn(donaciones);

        // Act & Assert
        mockMvc.perform(get(BASE_URL + "/por-donante/{donanteId}", donanteId)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.size()", is(1)))
                .andExpect(jsonPath("$[0].idDonante", is(donanteId)));

        verify(donacionService, times(1)).findByDonante(donanteId);
    }

    @Test
    @DisplayName("GET /por-donante/{donanteId} - Debería retornar 204 NO_CONTENT si no hay donaciones para el donante")
    void buscarPorDonante_NoContent() throws Exception {
        // Arrange
        Integer donanteId = 999;
        when(donacionService.findByDonante(donanteId)).thenReturn(Collections.emptyList());

        // Act & Assert
        mockMvc.perform(get(BASE_URL + "/por-donante/{donanteId}", donanteId)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNoContent());

        verify(donacionService, times(1)).findByDonante(donanteId);
    }
}