package com.SAFE_Rescue.API_Geolocalizacion.controller;

import com.SAFE_Rescue.API_Geolocalizacion.modelo.Geolocalizacion;
import com.SAFE_Rescue.API_Geolocalizacion.service.GeolocalizacionService;
import com.fasterxml.jackson.databind.ObjectMapper;
import net.datafaker.Faker;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Collections;
import java.util.List;
import java.util.NoSuchElementException;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(GeolocalizacionController.class)
public class GeolocalizacionControllerTest {

    // Ruta base definida en el controlador
    private final String BASE_URL = "/api-geolocalizacion/v1/localizaciones";

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private GeolocalizacionService geolocalizacionService;

    @Autowired
    private ObjectMapper objectMapper;

    private Faker faker;
    private Geolocalizacion geolocalizacion;
    private Integer id;

    @BeforeEach
    public void setUp() {
        faker = new Faker();
        id = 1;

        // Crear la entidad Geolocalizacion con coordenadas aleatorias
        geolocalizacion = new Geolocalizacion();
        geolocalizacion.setIdGeolocalizacion(id);
        geolocalizacion.setLatitud((float) faker.number().randomDouble(6, -90, 90));
        geolocalizacion.setLongitud((float) faker.number().randomDouble(6, -180, 180));
    }

    // --- Pruebas de operaciones exitosas (Happy Path) ---

    @Test
    public void listarGeolocalizacionesTest_shouldReturnOkAndContent() throws Exception {
        // Arrange
        when(geolocalizacionService.findAll()).thenReturn(List.of(geolocalizacion));

        // Act & Assert
        mockMvc.perform(get(BASE_URL))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].idGeolocalizacion").value(geolocalizacion.getIdGeolocalizacion()))
                .andExpect(jsonPath("$[0].latitud").value(geolocalizacion.getLatitud()));

        verify(geolocalizacionService, times(1)).findAll();
    }

    @Test
    public void buscarGeolocalizacionTest_shouldReturnOkAndGeolocalizacion() throws Exception {
        // Arrange
        when(geolocalizacionService.findById(id)).thenReturn(geolocalizacion);

        // Act & Assert
        mockMvc.perform(get(BASE_URL + "/{id}", id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.latitud").value(geolocalizacion.getLatitud()));

        verify(geolocalizacionService, times(1)).findById(id);
    }

    @Test
    public void agregarGeolocalizacionTest_shouldReturnCreatedAndMessage() throws Exception {
        // Arrange
        when(geolocalizacionService.save(any(Geolocalizacion.class))).thenReturn(geolocalizacion);

        // Act & Assert
        mockMvc.perform(post(BASE_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(geolocalizacion)))
                .andExpect(status().isCreated()) // 201 CREATED (Coincide con el controlador)
                .andExpect(content().string("Geolocalización creada con éxito.")); // Coincide con el controlador

        verify(geolocalizacionService, times(1)).save(any(Geolocalizacion.class));
    }

    @Test
    public void actualizarGeolocalizacionTest_shouldReturnOkAndMessage() throws Exception {
        // Arrange
        // Creamos una copia para simular el objeto actualizado que devuelve el servicio
        Geolocalizacion updatedGeo = new Geolocalizacion();
        updatedGeo.setIdGeolocalizacion(id);
        updatedGeo.setLatitud((float) (geolocalizacion.getLatitud() + 0.001));

        // **CORRECCIÓN AQUÍ:** Si el método update devuelve la entidad, usamos thenReturn.
        when(geolocalizacionService.update(any(Geolocalizacion.class), eq(id))).thenReturn(updatedGeo);

        // Act & Assert
        mockMvc.perform(put(BASE_URL + "/{id}", id)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updatedGeo)))
                .andExpect(status().isOk()) // 200 OK (Coincide con el controlador)
                .andExpect(content().string("Geolocalización actualizada con éxito")); // Coincide con el controlador

        verify(geolocalizacionService, times(1)).update(any(Geolocalizacion.class), eq(id));
    }

    @Test
    public void eliminarGeolocalizacionTest_shouldReturnOkAndMessage() throws Exception {
        // Arrange
        doNothing().when(geolocalizacionService).delete(id);

        // Act & Assert
        mockMvc.perform(delete(BASE_URL + "/{id}", id))
                .andExpect(status().isOk()) // 200 OK (Coincide con el controlador)
                .andExpect(content().string("Geolocalización eliminada con éxito.")); // Coincide con el controlador

        verify(geolocalizacionService, times(1)).delete(id);
    }

    // --- Pruebas de escenarios de error ---
    // (El resto de las pruebas no usaban doNothing() para save/update, por lo que permanecen iguales)

    @Test
    public void listarGeolocalizacionesTest_NoContent() throws Exception {
        // Arrange
        when(geolocalizacionService.findAll()).thenReturn(Collections.emptyList());

        // Act & Assert
        mockMvc.perform(get(BASE_URL))
                .andExpect(status().isNoContent()); // 204 NO CONTENT (Coincide con el controlador)
    }

    @Test
    public void buscarGeolocalizacionTest_NotFound() throws Exception {
        // Arrange
        when(geolocalizacionService.findById(id)).thenThrow(new NoSuchElementException("Geolocalización no encontrada"));

        // Act & Assert
        mockMvc.perform(get(BASE_URL + "/{id}", id))
                .andExpect(status().isNotFound()) // 404 NOT FOUND
                .andExpect(content().string("Geolocalización no encontrada")); // Coincide con el mensaje del controlador
    }

    @Test
    public void agregarGeolocalizacionTest_BadRequest_RuntimeError() throws Exception {
        // Arrange
        final String errorMessage = "Error: La latitud debe estar entre -90 y 90.";
        // Usamos when/thenThrow aquí, lo cual es correcto si el método save devuelve algo o es void
        when(geolocalizacionService.save(any(Geolocalizacion.class))).thenThrow(new IllegalArgumentException(errorMessage));

        // Act & Assert
        mockMvc.perform(post(BASE_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(geolocalizacion)))
                .andExpect(status().isBadRequest()) // 400 BAD REQUEST
                .andExpect(content().string(errorMessage));
    }

    @Test
    public void actualizarGeolocalizacionTest_NotFound() throws Exception {
        // Arrange
        // Usamos when/thenThrow, lo cual es correcto.
        when(geolocalizacionService.update(any(Geolocalizacion.class), eq(id)))
                .thenThrow(new NoSuchElementException("Geolocalización no encontrada"));

        // Act & Assert
        mockMvc.perform(put(BASE_URL + "/{id}", id)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(geolocalizacion)))
                .andExpect(status().isNotFound()) // 404 NOT FOUND
                .andExpect(content().string("Geolocalización no encontrada"));
    }

    @Test
    public void actualizarGeolocalizacionTest_BadRequest_RuntimeError() throws Exception {
        // Arrange
        final String errorMessage = "Error: Coordenadas inválidas.";
        // Usamos when/thenThrow, lo cual es correcto.
        when(geolocalizacionService.update(any(Geolocalizacion.class), eq(id)))
                .thenThrow(new IllegalArgumentException(errorMessage));

        // Act & Assert
        mockMvc.perform(put(BASE_URL + "/{id}", id)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(geolocalizacion)))
                .andExpect(status().isBadRequest()) // 400 BAD REQUEST
                .andExpect(content().string(errorMessage));
    }

    @Test
    public void eliminarGeolocalizacionTest_NotFound() throws Exception {
        // Arrange
        doThrow(new NoSuchElementException("Geolocalización no encontrada")).when(geolocalizacionService).delete(id);

        // Act & Assert
        mockMvc.perform(delete(BASE_URL + "/{id}", id))
                .andExpect(status().isNotFound()) // 404 NOT FOUND
                .andExpect(content().string("Geolocalización no encontrada"));
    }

    @Test
    public void eliminarGeolocalizacionTest_BadRequest_DependencyError() throws Exception {
        // Arrange
        final String errorMessage = "No se puede eliminar la geolocalización porque está asociada a una Dirección.";
        doThrow(new IllegalStateException(errorMessage)).when(geolocalizacionService).delete(id);

        // Act & Assert
        mockMvc.perform(delete(BASE_URL + "/{id}", id))
                .andExpect(status().isBadRequest()) // 400 BAD REQUEST (captura RuntimeException)
                .andExpect(content().string(errorMessage));
    }
}