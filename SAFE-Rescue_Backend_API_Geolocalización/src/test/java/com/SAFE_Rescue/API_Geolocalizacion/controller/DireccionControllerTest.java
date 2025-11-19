package com.SAFE_Rescue.API_Geolocalizacion.controller;

import com.SAFE_Rescue.API_Geolocalizacion.modelo.Comuna;
import com.SAFE_Rescue.API_Geolocalizacion.modelo.Coordenadas;
import com.SAFE_Rescue.API_Geolocalizacion.modelo.Direccion;
import com.SAFE_Rescue.API_Geolocalizacion.modelo.Region;
import com.SAFE_Rescue.API_Geolocalizacion.service.DireccionService;
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

@WebMvcTest(DireccionController.class)
public class DireccionControllerTest {

    // Ruta base definida en el controlador
    private final String BASE_URL = "/api-cordenadas/v1/direcciones";

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private DireccionService direccionService;

    @Autowired
    private ObjectMapper objectMapper;

    private Faker faker;
    private Direccion direccion;
    private Integer id;
    private Comuna comuna;
    private Coordenadas cordenadas;

    @BeforeEach
    public void setUp() {
        faker = new Faker();
        id = 1;

        // 1. Crear dependencias jerárquicas (para simular Comuna)
        Region region = new Region(1, "Región Metropolitana","RM");
        comuna = new Comuna(1, "Santiago","7500000", region);

        // 2. Crear dependencia Cordenadas
        cordenadas = new Coordenadas();
        cordenadas.setIdCoordenadas(101);
        cordenadas.setLatitud((float) faker.number().randomDouble(6, -34, -33)); // Latitud de Santiago
        cordenadas.setLongitud((float) faker.number().randomDouble(6, -71, -70)); // Longitud de Santiago

        // 3. Crear la entidad Direccion
        direccion = new Direccion();
        direccion.setIdDireccion(id);
        direccion.setCalle(faker.address().streetName());
        direccion.setNumero(faker.address().buildingNumber());
        direccion.setComuna(comuna);
        direccion.setCoordenadas(cordenadas);
    }

    // --- Pruebas de operaciones exitosas (Happy Path) ---

    @Test
    public void listarDireccionesTest_shouldReturnOkAndContent() throws Exception {
        // Arrange
        when(direccionService.findAll()).thenReturn(List.of(direccion));

        // Act & Assert
        mockMvc.perform(get(BASE_URL))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].idDireccion").value(direccion.getIdDireccion()))
                .andExpect(jsonPath("$[0].calle").value(direccion.getCalle()));

        verify(direccionService, times(1)).findAll();
    }

    @Test
    public void buscarDireccionTest_shouldReturnOkAndDireccion() throws Exception {
        // Arrange
        when(direccionService.findById(id)).thenReturn(direccion);

        // Act & Assert
        mockMvc.perform(get(BASE_URL + "/{id}", id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.calle").value(direccion.getCalle()))
                // Verifica que las dependencias se serialicen correctamente
                .andExpect(jsonPath("$.comuna.nombre").value(direccion.getComuna().getNombre()))
                .andExpect(jsonPath("$.cordenadas.idGeolocalizacion").value(direccion.getCoordenadas().getIdCoordenadas()));

        verify(direccionService, times(1)).findById(id);
    }

    @Test
    public void agregarDireccionTest_shouldReturnCreatedAndMessage() throws Exception {
        // Arrange
        // CORRECCIÓN: Usar when().thenReturn() ya que direccionService.save() devuelve una entidad.
        when(direccionService.save(any(Direccion.class))).thenReturn(direccion);

        // Act & Assert
        mockMvc.perform(post(BASE_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(direccion)))
                .andExpect(status().isCreated()) // 201 CREATED
                .andExpect(content().string("Dirección creada con éxito."));

        verify(direccionService, times(1)).save(any(Direccion.class));
    }

    @Test
    public void actualizarDireccionTest_shouldReturnOkAndMessage() throws Exception {
        // Arrange
        Direccion updatedDireccion = new Direccion();
        updatedDireccion.setIdDireccion(id);
        updatedDireccion.setCalle("Calle Nueva");
        updatedDireccion.setNumero("9999");
        updatedDireccion.setComuna(comuna);
        updatedDireccion.setCoordenadas(cordenadas);

        // CORRECCIÓN: Usar when().thenReturn() ya que direccionService.update() devuelve una entidad.
        when(direccionService.update(any(Direccion.class), eq(id))).thenReturn(updatedDireccion);

        // Act & Assert
        mockMvc.perform(put(BASE_URL + "/{id}", id)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updatedDireccion)))
                .andExpect(status().isOk()) // 200 OK
                .andExpect(content().string("Dirección actualizada con éxito")); // Mensaje exacto

        verify(direccionService, times(1)).update(any(Direccion.class), eq(id));
    }

    @Test
    public void eliminarDireccionTest_shouldReturnOkAndMessage() throws Exception {
        // Arrange
        // Correcto: doNothing() porque direccionService.delete() debería ser void.
        doNothing().when(direccionService).delete(id);

        // Act & Assert
        mockMvc.perform(delete(BASE_URL + "/{id}", id))
                .andExpect(status().isOk()) // 200 OK
                .andExpect(content().string("Dirección eliminada con éxito.")); // Mensaje exacto

        verify(direccionService, times(1)).delete(id);
    }

    // --- Pruebas de escenarios de error ---

    @Test
    public void listarDireccionesTest_NoContent() throws Exception {
        // Arrange
        when(direccionService.findAll()).thenReturn(Collections.emptyList());

        // Act & Assert
        mockMvc.perform(get(BASE_URL))
                .andExpect(status().isNoContent()); // 204 NO CONTENT
    }

    @Test
    public void buscarDireccionTest_NotFound() throws Exception {
        // Arrange
        when(direccionService.findById(id)).thenThrow(new NoSuchElementException("Dirección no encontrada"));

        // Act & Assert
        mockMvc.perform(get(BASE_URL + "/{id}", id))
                .andExpect(status().isNotFound()) // 404 NOT FOUND
                .andExpect(content().string("Dirección no encontrada")); // Mensaje exacto
    }

    @Test
    public void agregarDireccionTest_BadRequest_ComunaInvalida() throws Exception {
        // Arrange
        final String errorMessage = "Error: La Comuna o Geolocalización asociadas no son válidas.";
        when(direccionService.save(any(Direccion.class))).thenThrow(new IllegalArgumentException(errorMessage));

        // Act & Assert
        mockMvc.perform(post(BASE_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(direccion)))
                .andExpect(status().isBadRequest()) // 400 BAD REQUEST
                .andExpect(content().string(errorMessage));
    }

    @Test
    public void actualizarDireccionTest_NotFound() throws Exception {
        // Arrange
        // Usamos when() para simular que un método que devuelve un objeto lanza una excepción.
        when(direccionService.update(any(Direccion.class), eq(id))).thenThrow(new NoSuchElementException("Dirección no encontrada"));

        // Act & Assert
        mockMvc.perform(put(BASE_URL + "/{id}", id)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(direccion)))
                .andExpect(status().isNotFound()) // 404 NOT FOUND
                .andExpect(content().string("Dirección no encontrada"));
    }

    @Test
    public void actualizarDireccionTest_BadRequest_DatosIncompletos() throws Exception {
        // Arrange
        final String errorMessage = "Error: La calle y el número son obligatorios.";
        // Usamos when() para simular que un método que devuelve un objeto lanza una excepción.
        when(direccionService.update(any(Direccion.class), eq(id))).thenThrow(new IllegalStateException(errorMessage));

        // Act & Assert
        mockMvc.perform(put(BASE_URL + "/{id}", id)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(direccion)))
                .andExpect(status().isBadRequest()) // 400 BAD REQUEST
                .andExpect(content().string(errorMessage));
    }

    @Test
    public void eliminarDireccionTest_NotFound() throws Exception {
        // Arrange
        doThrow(new NoSuchElementException("Dirección no encontrada")).when(direccionService).delete(id);

        // Act & Assert
        mockMvc.perform(delete(BASE_URL + "/{id}", id))
                .andExpect(status().isNotFound()) // 404 NOT FOUND
                .andExpect(content().string("Dirección no encontrada"));
    }
}