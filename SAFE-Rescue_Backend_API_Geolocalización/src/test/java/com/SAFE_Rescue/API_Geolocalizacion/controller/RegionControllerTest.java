package com.SAFE_Rescue.API_Geolocalizacion.controller;

import com.SAFE_Rescue.API_Geolocalizacion.modelo.Region;
import com.SAFE_Rescue.API_Geolocalizacion.service.RegionService;
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

@WebMvcTest(RegionController.class)
public class RegionControllerTest {

    // Ruta base definida en el controlador
    private final String BASE_URL = "/api-geolocalizacion/v1/regiones";

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private RegionService regionService;

    @Autowired
    private ObjectMapper objectMapper;

    private Faker faker;
    private Region region;
    private Integer id;

    @BeforeEach
    public void setUp() {
        faker = new Faker();
        id = 1;

        // Crear la entidad Region
        region = new Region();
        region.setIdRegion(id);
        region.setNombre(faker.address().state());
    }

    // --- Pruebas de operaciones exitosas (Happy Path) ---

    @Test
    public void listarRegionesTest_shouldReturnOkAndContent() throws Exception {
        // Arrange
        when(regionService.findAll()).thenReturn(List.of(region));

        // Act & Assert
        mockMvc.perform(get(BASE_URL))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].idRegion").value(region.getIdRegion()))
                .andExpect(jsonPath("$[0].nombre").value(region.getNombre()));

        verify(regionService, times(1)).findAll();
    }

    @Test
    public void buscarRegionTest_shouldReturnOkAndRegion() throws Exception {
        // Arrange
        when(regionService.findById(id)).thenReturn(region);

        // Act & Assert
        mockMvc.perform(get(BASE_URL + "/{id}", id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nombre").value(region.getNombre()));

        verify(regionService, times(1)).findById(id);
    }

    @Test
    public void agregarRegionTest_shouldReturnCreatedAndMessage() throws Exception {
        // Arrange
        // CORRECCIÓN: Usar thenReturn() ya que regionService.save() devuelve una entidad.
        when(regionService.save(any(Region.class))).thenReturn(region);

        // Act & Assert
        mockMvc.perform(post(BASE_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(region)))
                .andExpect(status().isCreated()) // 201 CREATED
                .andExpect(content().string("Región creada con éxito."));

        verify(regionService, times(1)).save(any(Region.class));
    }

    @Test
    public void actualizarRegionTest_shouldReturnOkAndMessage() throws Exception {
        // Arrange
        // Crear una región simulada que representa el resultado de la actualización
        Region updatedRegion = new Region();
        updatedRegion.setIdRegion(id);
        updatedRegion.setNombre("Región Actualizada");

        // CORRECCIÓN: Usar thenReturn() ya que regionService.update() devuelve una entidad.
        when(regionService.update(any(Region.class), eq(id))).thenReturn(updatedRegion);

        // Act & Assert
        mockMvc.perform(put(BASE_URL + "/{id}", id)
                        .contentType(MediaType.APPLICATION_JSON)
                        // Usamos el objeto original o actualizado para el contenido del PUT
                        .content(objectMapper.writeValueAsString(updatedRegion)))
                .andExpect(status().isOk()) // 200 OK
                .andExpect(content().string("Región actualizada con éxito")); // Mensaje exacto del controlador

        verify(regionService, times(1)).update(any(Region.class), eq(id));
    }

    @Test
    public void eliminarRegionTest_shouldReturnOkAndMessage() throws Exception {
        // Arrange
        // Correcto: doNothing() porque regionService.delete() debería ser void.
        doNothing().when(regionService).delete(id);

        // Act & Assert
        mockMvc.perform(delete(BASE_URL + "/{id}", id))
                .andExpect(status().isOk()) // 200 OK
                .andExpect(content().string("Región eliminada con éxito.")); // Mensaje exacto del controlador

        verify(regionService, times(1)).delete(id);
    }

    // --- Pruebas de escenarios de error ---

    @Test
    public void listarRegionesTest_NoContent() throws Exception {
        // Arrange
        when(regionService.findAll()).thenReturn(Collections.emptyList());

        // Act & Assert
        mockMvc.perform(get(BASE_URL))
                .andExpect(status().isNoContent()); // 204 NO CONTENT
    }

    @Test
    public void buscarRegionTest_NotFound() throws Exception {
        // Arrange
        when(regionService.findById(id)).thenThrow(new NoSuchElementException("Región no encontrada"));

        // Act & Assert
        mockMvc.perform(get(BASE_URL + "/{id}", id))
                .andExpect(status().isNotFound()) // 404 NOT FOUND
                .andExpect(content().string("Región no encontrada")); // Mensaje exacto del controlador
    }

    @Test
    public void agregarRegionTest_BadRequest_RuntimeError() throws Exception {
        // Arrange
        final String errorMessage = "Error: El País asociado no es válido o no existe.";
        when(regionService.save(any(Region.class))).thenThrow(new IllegalArgumentException(errorMessage));

        // Act & Assert
        mockMvc.perform(post(BASE_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(region)))
                .andExpect(status().isBadRequest()) // 400 BAD REQUEST
                .andExpect(content().string(errorMessage));
    }

    @Test
    public void actualizarRegionTest_NotFound() throws Exception {
        // Arrange
        // Nota: Si update lanza NoSuchElementException, también debe devolver un valor simulado (aunque lance)
        when(regionService.update(any(Region.class), eq(id))).thenThrow(new NoSuchElementException("Región no encontrada"));

        // Act & Assert
        mockMvc.perform(put(BASE_URL + "/{id}", id)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(region)))
                .andExpect(status().isNotFound()) // 404 NOT FOUND
                .andExpect(content().string("Región no encontrada"));
    }

    @Test
    public void actualizarRegionTest_BadRequest_RuntimeError() throws Exception {
        // Arrange
        final String errorMessage = "Error: El nombre de la región ya está en uso.";
        // Nota: Si update lanza IllegalStateException, también debe devolver un valor simulado (aunque lance)
        when(regionService.update(any(Region.class), eq(id))).thenThrow(new IllegalStateException(errorMessage));

        // Act & Assert
        mockMvc.perform(put(BASE_URL + "/{id}", id)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(region)))
                .andExpect(status().isBadRequest()) // 400 BAD REQUEST
                .andExpect(content().string(errorMessage));
    }

    @Test
    public void eliminarRegionTest_NotFound() throws Exception {
        // Arrange
        doThrow(new NoSuchElementException("Región no encontrada")).when(regionService).delete(id);

        // Act & Assert
        mockMvc.perform(delete(BASE_URL + "/{id}", id))
                .andExpect(status().isNotFound()) // 404 NOT FOUND
                .andExpect(content().string("Región no encontrada"));
    }

    @Test
    public void eliminarRegionTest_BadRequest_DependencyError() throws Exception {
        // Arrange
        final String errorMessage = "No se puede eliminar la Región porque tiene Comunas asociadas.";
        doThrow(new IllegalStateException(errorMessage)).when(regionService).delete(id);

        // Act & Assert
        mockMvc.perform(delete(BASE_URL + "/{id}", id))
                .andExpect(status().isBadRequest()) // 400 BAD REQUEST
                .andExpect(content().string(errorMessage));
    }
}