package com.SAFE_Rescue.API_Geolocalizacion.controller;

import com.SAFE_Rescue.API_Geolocalizacion.modelo.Comuna;
import com.SAFE_Rescue.API_Geolocalizacion.modelo.Region;
import com.SAFE_Rescue.API_Geolocalizacion.service.ComunaService;
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

@WebMvcTest(ComunaController.class)
public class ComunaControllerTest {

    // Ruta base definida en el controlador
    private final String BASE_URL = "/api-geolocalizacion/v1/comunas";

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private ComunaService comunaService;

    @Autowired
    private ObjectMapper objectMapper;

    private Faker faker;
    private Comuna comuna;
    private Integer id;

    @BeforeEach
    public void setUp() {
        faker = new Faker();
        id = 1;

        // Crear dependencias (Region)
        Region region = new Region();
        region.setIdRegion(1);
        region.setNombre("Región Metropolitana");

        // Crear el objeto Comuna.
        comuna = new Comuna();
        comuna.setIdComuna(id);
        comuna.setNombre(faker.address().city());
        comuna.setCodigoPostal(String.valueOf(faker.number().numberBetween(1000000, 9000000)));
        comuna.setRegion(region);
    }

    // --- Pruebas de operaciones exitosas (Happy Path) ---

    @Test
    public void listarComunasTest_shouldReturnOkAndContent() throws Exception {
        // Arrange
        when(comunaService.findAll()).thenReturn(List.of(comuna));

        // Act & Assert
        mockMvc.perform(get(BASE_URL))
                .andExpect(status().isOk()) // Coincide con ResponseEntity.ok(comunas)
                .andExpect(jsonPath("$[0].idComuna").value(comuna.getIdComuna()));
    }

    @Test
    public void buscarComunaTest_shouldReturnOkAndComuna() throws Exception {
        // Arrange
        when(comunaService.findById(id)).thenReturn(comuna);

        // Act & Assert
        mockMvc.perform(get(BASE_URL + "/{id}", id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nombre").value(comuna.getNombre()));
    }

    @Test
    public void agregarComunaTest_shouldReturnCreatedAndMessage() throws Exception {
        // Arrange
        // CORRECCIÓN: Usar when().thenReturn() ya que comunaService.save() devuelve una entidad.
        when(comunaService.save(any(Comuna.class))).thenReturn(comuna);

        // Act & Assert
        mockMvc.perform(post(BASE_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(comuna)))
                .andExpect(status().isCreated()) // 201 CREATED (Coincide con el controlador)
                .andExpect(content().string("Comuna creada con éxito.")); // Coincide con el controlador
    }

    @Test
    public void actualizarComunaTest_shouldReturnOkAndMessage() throws Exception {
        // Arrange
        Comuna updatedComuna = new Comuna();
        updatedComuna.setIdComuna(id);
        updatedComuna.setNombre("Comuna Actualizada");
        updatedComuna.setCodigoPostal(comuna.getCodigoPostal());
        updatedComuna.setRegion(comuna.getRegion());

        // CORRECCIÓN: Usar when().thenReturn() ya que comunaService.update() devuelve una entidad.
        when(comunaService.update(any(Comuna.class), eq(id))).thenReturn(updatedComuna);

        // Act & Assert
        mockMvc.perform(put(BASE_URL + "/{id}", id)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updatedComuna)))
                .andExpect(status().isOk()) // 200 OK (Coincide con el controlador)
                .andExpect(content().string("Comuna actualizada con éxito")); // Coincide con el controlador
    }

    @Test
    public void eliminarComunaTest_shouldReturnOkAndMessage() throws Exception {
        // Arrange
        // Correcto: doNothing() porque comunaService.delete() debe ser void.
        doNothing().when(comunaService).delete(id);

        // Act & Assert
        mockMvc.perform(delete(BASE_URL + "/{id}", id))
                .andExpect(status().isOk()) // 200 OK (Ajustado para coincidir con el controlador)
                .andExpect(content().string("Comuna eliminada con éxito.")); // Coincide con el controlador
    }

    // --- Pruebas de escenarios de error ---

    @Test
    public void listarComunasTest_NoContent() throws Exception {
        // Arrange
        when(comunaService.findAll()).thenReturn(Collections.emptyList());

        // Act & Assert
        mockMvc.perform(get(BASE_URL))
                .andExpect(status().isNoContent()); // 204 NO CONTENT (Coincide con el controlador)
    }

    @Test
    public void buscarComunaTest_NotFound() throws Exception {
        // Arrange
        when(comunaService.findById(id)).thenThrow(new NoSuchElementException("Comuna no encontrada"));

        // Act & Assert
        mockMvc.perform(get(BASE_URL + "/{id}", id))
                .andExpect(status().isNotFound()) // 404 NOT FOUND
                .andExpect(content().string("Comuna no encontrada")); // Coincide con el mensaje del controlador
    }

    @Test
    public void agregarComunaTest_BadRequest_RuntimeError() throws Exception {
        // Arrange
        final String errorMessage = "Error: La Región asociada no existe.";
        when(comunaService.save(any(Comuna.class))).thenThrow(new IllegalArgumentException(errorMessage)); // RuntimeException

        // Act & Assert
        mockMvc.perform(post(BASE_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(comuna)))
                .andExpect(status().isBadRequest()) // 400 BAD REQUEST (Capturado por el catch (RuntimeException e))
                .andExpect(content().string(errorMessage)); // El mensaje de error del servicio
    }

    @Test
    public void actualizarComunaTest_NotFound() throws Exception {
        // Arrange
        // Si update lanza una excepción (porque no es void), debemos usar when()
        when(comunaService.update(any(Comuna.class), eq(id))).thenThrow(new NoSuchElementException("Comuna no encontrada"));

        // Act & Assert
        mockMvc.perform(put(BASE_URL + "/{id}", id)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(comuna)))
                .andExpect(status().isNotFound()) // 404 NOT FOUND
                .andExpect(content().string("Comuna no encontrada")); // Coincide con el mensaje del controlador
    }

    @Test
    public void actualizarComunaTest_BadRequest_RuntimeError() throws Exception {
        // Arrange
        final String errorMessage = "El nombre de la comuna es requerido.";
        // Si update lanza una excepción (porque no es void), debemos usar when()
        when(comunaService.update(any(Comuna.class), eq(id))).thenThrow(new IllegalArgumentException(errorMessage));

        // Act & Assert
        mockMvc.perform(put(BASE_URL + "/{id}", id)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(comuna)))
                .andExpect(status().isBadRequest()) // 400 BAD REQUEST
                .andExpect(content().string(errorMessage)); // Coincide con el mensaje del controlador
    }

    @Test
    public void eliminarComunaTest_NotFound() throws Exception {
        // Arrange
        doThrow(new NoSuchElementException("Comuna no encontrada")).when(comunaService).delete(id);

        // Act & Assert
        mockMvc.perform(delete(BASE_URL + "/{id}", id))
                .andExpect(status().isNotFound()) // 404 NOT FOUND
                .andExpect(content().string("Comuna no encontrada")); // Coincide con el mensaje del controlador
    }

    @Test
    public void eliminarComunaTest_BadRequest_RuntimeError() throws Exception {
        // Arrange
        final String errorMessage = "No se puede eliminar la comuna porque tiene direcciones asociadas.";
        doThrow(new IllegalStateException(errorMessage)).when(comunaService).delete(id); // Simula el error del service (RuntimeException)

        // Act & Assert
        mockMvc.perform(delete(BASE_URL + "/{id}", id))
                .andExpect(status().isBadRequest()) // 400 BAD REQUEST
                .andExpect(content().string(errorMessage)); // Coincide con el mensaje del controlador
    }
}