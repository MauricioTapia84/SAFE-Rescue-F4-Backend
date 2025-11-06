package com.SAFE_Rescue.API_Geolocalizacion.controller;

import com.SAFE_Rescue.API_Geolocalizacion.modelo.Pais;
import com.SAFE_Rescue.API_Geolocalizacion.service.PaisService;
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

@WebMvcTest(PaisController.class)
public class PaisControllerTest {

    // Ruta base definida en el controlador
    private final String BASE_URL = "/api-geolocalizacion/v1/paises";

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private PaisService paisService;

    @Autowired
    private ObjectMapper objectMapper;

    private Faker faker;
    private Pais pais;
    private Integer id;

    @BeforeEach
    public void setUp() {
        faker = new Faker();
        id = 1;

        // Crear la entidad Pais
        pais = new Pais();
        pais.setIdPais(id);
        pais.setNombre(faker.country().name());
        pais.setCodigoIso(faker.country().countryCode3());
    }

    // --- Pruebas de operaciones exitosas (Happy Path) ---

    @Test
    public void listarPaisesTest_shouldReturnOkAndContent() throws Exception {
        // Arrange
        when(paisService.findAll()).thenReturn(List.of(pais));

        // Act & Assert
        mockMvc.perform(get(BASE_URL))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].idPais").value(pais.getIdPais()))
                .andExpect(jsonPath("$[0].nombre").value(pais.getNombre()));

        verify(paisService, times(1)).findAll();
    }

    @Test
    public void buscarPaisTest_shouldReturnOkAndPais() throws Exception {
        // Arrange
        when(paisService.findById(id)).thenReturn(pais);

        // Act & Assert
        mockMvc.perform(get(BASE_URL + "/{id}", id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nombre").value(pais.getNombre()))
                .andExpect(jsonPath("$.codigoIso").value(pais.getCodigoIso()));

        verify(paisService, times(1)).findById(id);
    }

    @Test
    public void agregarPaisTest_shouldReturnCreatedAndMessage() throws Exception {
        // Arrange
        when(paisService.save(any(Pais.class))).thenReturn(pais);

        // Act & Assert
        mockMvc.perform(post(BASE_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(pais)))
                .andExpect(status().isCreated())
                .andExpect(content().string("País creado con éxito."));

        verify(paisService, times(1)).save(any(Pais.class));
    }

    @Test
    public void actualizarPaisTest_shouldReturnOkAndMessage() throws Exception {
        // Arrange
        // Creamos una copia para simular el objeto actualizado que devuelve el servicio
        Pais updatedPais = new Pais();
        updatedPais.setIdPais(id);
        updatedPais.setNombre("Nuevo Nombre");
        updatedPais.setCodigoIso("NNN");

        // CORRECCIÓN: Usar thenReturn() ya que paisService.update() devuelve una entidad.
        when(paisService.update(any(Pais.class), eq(id))).thenReturn(updatedPais);

        // Act & Assert
        mockMvc.perform(put(BASE_URL + "/{id}", id)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updatedPais)))
                .andExpect(status().isOk())
                .andExpect(content().string("País actualizado con éxito"));

        verify(paisService, times(1)).update(any(Pais.class), eq(id));
    }

    @Test
    public void eliminarPaisTest_shouldReturnOkAndMessage() throws Exception {
        // Arrange
        doNothing().when(paisService).delete(id);

        // Act & Assert
        mockMvc.perform(delete(BASE_URL + "/{id}", id))
                .andExpect(status().isOk())
                .andExpect(content().string("País eliminado con éxito."));

        verify(paisService, times(1)).delete(id);
    }

    // --- Pruebas de escenarios de error ---

    @Test
    public void listarPaisesTest_NoContent() throws Exception {
        // Arrange
        when(paisService.findAll()).thenReturn(Collections.emptyList());

        // Act & Assert
        mockMvc.perform(get(BASE_URL))
                .andExpect(status().isNoContent()); // 204 NO CONTENT
    }

    @Test
    public void buscarPaisTest_NotFound() throws Exception {
        // Arrange
        when(paisService.findById(id)).thenThrow(new NoSuchElementException("País no encontrado"));

        // Act & Assert
        mockMvc.perform(get(BASE_URL + "/{id}", id))
                .andExpect(status().isNotFound()) // 404 NOT FOUND
                .andExpect(content().string("País no encontrado")); // Mensaje exacto del controlador
    }

    @Test
    public void agregarPaisTest_BadRequest_RuntimeError() throws Exception {
        // Arrange
        final String errorMessage = "Error: El código ISO ya existe.";
        when(paisService.save(any(Pais.class))).thenThrow(new IllegalArgumentException(errorMessage));

        // Act & Assert
        mockMvc.perform(post(BASE_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(pais)))
                .andExpect(status().isBadRequest()) // 400 BAD REQUEST
                .andExpect(content().string(errorMessage));
    }

    @Test
    public void actualizarPaisTest_NotFound() throws Exception {
        // Arrange
        doThrow(new NoSuchElementException("País no encontrado")).when(paisService).update(any(Pais.class), eq(id));

        // Act & Assert
        mockMvc.perform(put(BASE_URL + "/{id}", id)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(pais)))
                .andExpect(status().isNotFound()) // 404 NOT FOUND
                .andExpect(content().string("País no encontrado"));
    }

    @Test
    public void actualizarPaisTest_BadRequest_RuntimeError() throws Exception {
        // Arrange
        final String errorMessage = "Error: El nombre es obligatorio.";
        doThrow(new IllegalStateException(errorMessage)).when(paisService).update(any(Pais.class), eq(id));

        // Act & Assert
        mockMvc.perform(put(BASE_URL + "/{id}", id)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(pais)))
                .andExpect(status().isBadRequest()) // 400 BAD REQUEST
                .andExpect(content().string(errorMessage));
    }

    @Test
    public void eliminarPaisTest_NotFound() throws Exception {
        // Arrange
        doThrow(new NoSuchElementException("País no encontrado")).when(paisService).delete(id);

        // Act & Assert
        mockMvc.perform(delete(BASE_URL + "/{id}", id))
                .andExpect(status().isNotFound()) // 404 NOT FOUND
                .andExpect(content().string("País no encontrado"));
    }


    @Test
    public void eliminarPaisTest_BadRequest_DependencyError() throws Exception {
        // Arrange
        final String errorMessage = "No se puede eliminar el País porque tiene Regiones asociadas.";
        doThrow(new IllegalStateException(errorMessage)).when(paisService).delete(id);

        // Act & Assert
        mockMvc.perform(delete(BASE_URL + "/{id}", id))
                .andExpect(status().isBadRequest())
                .andExpect(content().string(errorMessage));
    }
}