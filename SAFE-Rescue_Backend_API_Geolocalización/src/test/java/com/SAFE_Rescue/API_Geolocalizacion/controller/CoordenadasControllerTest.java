package com.SAFE_Rescue.API_Geolocalizacion.controller;

import com.SAFE_Rescue.API_Geolocalizacion.modelo.Coordenadas;
import com.SAFE_Rescue.API_Geolocalizacion.service.CoordenadasService;
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

@WebMvcTest(CoordenadasController.class) // Cambiado de CordenadasController a CoordenadasController
public class CoordenadasControllerTest { // Cambiado de CordenadasControllerTest a CoordenadasControllerTest

    // Ruta base definida en el controlador (actualizada según el controlador)
    private final String BASE_URL = "/api-geolocalizacion/v1/coordenadas"; // Actualizada según el controlador

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private CoordenadasService coordenadasService; // Cambiado de CordenadasService a CoordenadasService

    @Autowired
    private ObjectMapper objectMapper;

    private Faker faker;
    private Coordenadas coordenadas; // Cambiado de Cordenadas a Coordenadas
    private Integer id;

    @BeforeEach
    public void setUp() {
        faker = new Faker();
        id = 1;

        // Crear la entidad Coordenadas con coordenadas aleatorias
        coordenadas = new Coordenadas(); // Cambiado de Cordenadas a Coordenadas
        coordenadas.setIdCoordenadas(id); // Cambiado de setIdGeolocalizacion a setIdCoordenadas
        coordenadas.setLatitud((float) faker.number().randomDouble(6, -90, 90));
        coordenadas.setLongitud((float) faker.number().randomDouble(6, -180, 180));
    }

    // --- Pruebas de operaciones exitosas (Happy Path) ---

    @Test
    public void listarCoordenadasTest_shouldReturnOkAndContent() throws Exception { // Nombre actualizado
        // Arrange
        when(coordenadasService.findAll()).thenReturn(List.of(coordenadas));

        // Act & Assert
        mockMvc.perform(get(BASE_URL))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].idCoordenadas").value(coordenadas.getIdCoordenadas())) // Cambiado de idGeolocalizacion a idCoordenadas
                .andExpect(jsonPath("$[0].latitud").value(coordenadas.getLatitud()));

        verify(coordenadasService, times(1)).findAll();
    }

    @Test
    public void buscarCoordenadasTest_shouldReturnOkAndCoordenadas() throws Exception { // Nombre actualizado
        // Arrange
        when(coordenadasService.findById(id)).thenReturn(coordenadas);

        // Act & Assert
        mockMvc.perform(get(BASE_URL + "/{id}", id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.latitud").value(coordenadas.getLatitud()));

        verify(coordenadasService, times(1)).findById(id);
    }

    @Test
    public void agregarCoordenadasTest_shouldReturnCreatedAndMessage() throws Exception { // Nombre actualizado
        // Arrange
        when(coordenadasService.save(any(Coordenadas.class))).thenReturn(coordenadas); // Cambiado Cordenadas.class a Coordenadas.class

        // Act & Assert
        mockMvc.perform(post(BASE_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(coordenadas)))
                .andExpect(status().isCreated())
                .andExpect(content().string("Coordenadas creadas con éxito.")); // Mensaje actualizado

        verify(coordenadasService, times(1)).save(any(Coordenadas.class)); // Cambiado Cordenadas.class a Coordenadas.class
    }

    @Test
    public void actualizarCoordenadasTest_shouldReturnOkAndMessage() throws Exception { // Nombre actualizado
        // Arrange
        // Creamos una copia para simular el objeto actualizado que devuelve el servicio
        Coordenadas updatedCoordenadas = new Coordenadas(); // Variable actualizada
        updatedCoordenadas.setIdCoordenadas(id); // Cambiado de setIdGeolocalizacion a setIdCoordenadas
        updatedCoordenadas.setLatitud((float) (coordenadas.getLatitud() + 0.001));

        when(coordenadasService.update(any(Coordenadas.class), eq(id))).thenReturn(updatedCoordenadas); // Cambiado Cordenadas.class a Coordenadas.class

        // Act & Assert
        mockMvc.perform(put(BASE_URL + "/{id}", id)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updatedCoordenadas)))
                .andExpect(status().isOk())
                .andExpect(content().string("Coordenadas actualizadas con éxito")); // Mensaje actualizado

        verify(coordenadasService, times(1)).update(any(Coordenadas.class), eq(id)); // Cambiado Cordenadas.class a Coordenadas.class
    }

    @Test
    public void eliminarCoordenadasTest_shouldReturnOkAndMessage() throws Exception { // Nombre actualizado
        // Arrange
        doNothing().when(coordenadasService).delete(id);

        // Act & Assert
        mockMvc.perform(delete(BASE_URL + "/{id}", id))
                .andExpect(status().isOk())
                .andExpect(content().string("Coordenadas eliminadas con éxito.")); // Mensaje actualizado

        verify(coordenadasService, times(1)).delete(id);
    }

    // --- Pruebas de escenarios de error ---

    @Test
    public void listarCoordenadasTest_NoContent() throws Exception { // Nombre actualizado
        // Arrange
        when(coordenadasService.findAll()).thenReturn(Collections.emptyList());

        // Act & Assert
        mockMvc.perform(get(BASE_URL))
                .andExpect(status().isNoContent());
    }

    @Test
    public void buscarCoordenadasTest_NotFound() throws Exception { // Nombre actualizado
        // Arrange
        when(coordenadasService.findById(id)).thenThrow(new NoSuchElementException("Coordenadas no encontradas")); // Mensaje actualizado

        // Act & Assert
        mockMvc.perform(get(BASE_URL + "/{id}", id))
                .andExpect(status().isNotFound())
                .andExpect(content().string("Coordenadas no encontradas")); // Mensaje actualizado
    }

    @Test
    public void agregarCoordenadasTest_BadRequest_RuntimeError() throws Exception { // Nombre actualizado
        // Arrange
        final String errorMessage = "Error: La latitud debe estar entre -90 y 90.";
        when(coordenadasService.save(any(Coordenadas.class))).thenThrow(new IllegalArgumentException(errorMessage)); // Cambiado Cordenadas.class a Coordenadas.class

        // Act & Assert
        mockMvc.perform(post(BASE_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(coordenadas)))
                .andExpect(status().isBadRequest())
                .andExpect(content().string(errorMessage));
    }

    @Test
    public void actualizarCoordenadasTest_NotFound() throws Exception { // Nombre actualizado
        // Arrange
        when(coordenadasService.update(any(Coordenadas.class), eq(id))) // Cambiado Cordenadas.class a Coordenadas.class
                .thenThrow(new NoSuchElementException("Coordenadas no encontradas")); // Mensaje actualizado

        // Act & Assert
        mockMvc.perform(put(BASE_URL + "/{id}", id)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(coordenadas)))
                .andExpect(status().isNotFound())
                .andExpect(content().string("Coordenadas no encontradas")); // Mensaje actualizado
    }

    @Test
    public void actualizarCoordenadasTest_BadRequest_RuntimeError() throws Exception { // Nombre actualizado
        // Arrange
        final String errorMessage = "Error: Coordenadas inválidas.";
        when(coordenadasService.update(any(Coordenadas.class), eq(id))) // Cambiado Cordenadas.class a Coordenadas.class
                .thenThrow(new IllegalArgumentException(errorMessage));

        // Act & Assert
        mockMvc.perform(put(BASE_URL + "/{id}", id)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(coordenadas)))
                .andExpect(status().isBadRequest())
                .andExpect(content().string(errorMessage));
    }

    @Test
    public void eliminarCoordenadasTest_NotFound() throws Exception { // Nombre actualizado
        // Arrange
        doThrow(new NoSuchElementException("Coordenadas no encontradas")).when(coordenadasService).delete(id); // Mensaje actualizado

        // Act & Assert
        mockMvc.perform(delete(BASE_URL + "/{id}", id))
                .andExpect(status().isNotFound())
                .andExpect(content().string("Coordenadas no encontradas")); // Mensaje actualizado
    }

    @Test
    public void eliminarCoordenadasTest_BadRequest_DependencyError() throws Exception { // Nombre actualizado
        // Arrange
        final String errorMessage = "No se pueden eliminar las coordenadas porque están asociadas a una Dirección."; // Mensaje actualizado
        doThrow(new IllegalStateException(errorMessage)).when(coordenadasService).delete(id);

        // Act & Assert
        mockMvc.perform(delete(BASE_URL + "/{id}", id))
                .andExpect(status().isBadRequest())
                .andExpect(content().string(errorMessage));
    }
}