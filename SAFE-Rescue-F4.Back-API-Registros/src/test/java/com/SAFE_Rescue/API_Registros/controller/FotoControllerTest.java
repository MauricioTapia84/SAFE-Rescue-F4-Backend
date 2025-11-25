package com.SAFE_Rescue.API_Registros.controller;

import com.SAFE_Rescue.API_Registros.modelo.Foto;
import com.SAFE_Rescue.API_Registros.service.FotoService;
import com.fasterxml.jackson.databind.ObjectMapper;
import net.datafaker.Faker;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.NoSuchElementException;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Pruebas unitarias para FotoController utilizando @WebMvcTest y MockMvc.
 * Se simulan las peticiones HTTP y se verifican las respuestas (status code y body).
 */
@WebMvcTest(FotoController.class)
public class FotoControllerTest {

    private static final String API_BASE_URL = "/api-registros/v1/fotos";

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    // Inyecta un mock del servicio en el contexto de Spring
    @MockitoBean
    private FotoService fotoService;

    private Faker faker;
    private Foto fotoValida;
    private Integer fotoId;

    @BeforeEach
    void setUp() {
        faker = new Faker();
        fotoId = faker.number().numberBetween(1, 100);

        fotoValida = new Foto();
        fotoValida.setIdFoto(fotoId);
        fotoValida.setDescripcion(faker.file().fileName());
        fotoValida.setUrl(faker.internet().image());
        fotoValida.setFechaSubida(LocalDateTime.now());
    }

    // -------------------------------------------------------------------------
    // GET / (listar)
    // -------------------------------------------------------------------------

    @Test
    void listar_ShouldReturn200AndList_WhenFound() throws Exception {
        // Arrange
        List<Foto> listaEsperada = List.of(fotoValida);
        when(fotoService.findAll()).thenReturn(listaEsperada);

        // Act & Assert
        mockMvc.perform(get(API_BASE_URL))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(content().json(objectMapper.writeValueAsString(listaEsperada)))
                .andExpect(jsonPath("$.size()").value(1));

        verify(fotoService, times(1)).findAll();
    }

    @Test
    void listar_ShouldReturn204_WhenNoContent() throws Exception {
        // Arrange
        when(fotoService.findAll()).thenReturn(List.of());

        // Act & Assert
        mockMvc.perform(get(API_BASE_URL))
                .andExpect(status().isNoContent())
                .andExpect(content().string("")); // El cuerpo debe estar vacío

        verify(fotoService, times(1)).findAll();
    }

    // -------------------------------------------------------------------------
    // GET /{id} (buscarFoto)
    // -------------------------------------------------------------------------

    @Test
    void buscarFoto_ShouldReturn200AndFoto_WhenFound() throws Exception {
        // Arrange
        when(fotoService.findById(fotoId)).thenReturn(fotoValida);

        // Act & Assert
        mockMvc.perform(get(API_BASE_URL + "/{id}", fotoId))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.idFoto").value(fotoId));

        verify(fotoService, times(1)).findById(fotoId);
    }

    @Test
    void buscarFoto_ShouldReturn404AndMessage_WhenNotFound() throws Exception {
        // Arrange
        when(fotoService.findById(fotoId)).thenThrow(new NoSuchElementException());

        // Act & Assert
        mockMvc.perform(get(API_BASE_URL + "/{id}", fotoId))
                .andExpect(status().isNotFound())
                .andExpect(content().string("Foto no encontrada"));

        verify(fotoService, times(1)).findById(fotoId);
    }

    // -------------------------------------------------------------------------
    // POST / (agregarFoto)
    // -------------------------------------------------------------------------

    @Test
    void agregarFoto_ShouldReturn201AndSuccessMessage_WhenValid() throws Exception {
        // Arrange
        doNothing().when(fotoService).save(any(Foto.class));

        // Act & Assert
        mockMvc.perform(post(API_BASE_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(fotoValida)))
                .andExpect(status().isCreated())
                .andExpect(content().string("Foto creada con éxito."));

        verify(fotoService, times(1)).save(any(Foto.class));
    }

    @Test
    void agregarFoto_ShouldReturn400AndErrorMessage_OnRuntimeException() throws Exception {
        // Arrange
        String errorMessage = "Error: La URL de la foto ya existe.";
        doThrow(new RuntimeException(errorMessage)).when(fotoService).save(any(Foto.class));

        // Act & Assert
        mockMvc.perform(post(API_BASE_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(fotoValida)))
                .andExpect(status().isBadRequest())
                .andExpect(content().string(errorMessage));

        verify(fotoService, times(1)).save(any(Foto.class));
    }

    @Test
    void agregarFoto_ShouldReturn500AndGenericMessage_OnInternalError() throws Exception {
        // Arrange
        doThrow(new Exception("Error de conexión inesperado")).when(fotoService).save(any(Foto.class));

        // Act & Assert
        mockMvc.perform(post(API_BASE_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(fotoValida)))
                .andExpect(status().isInternalServerError())
                .andExpect(content().string("Error interno del servidor."));

        verify(fotoService, times(1)).save(any(Foto.class));
    }

    // -------------------------------------------------------------------------
    // PUT /{id} (actualizarFoto)
    // -------------------------------------------------------------------------

    @Test
    void actualizarFoto_ShouldReturn200AndSuccessMessage_WhenFoundAndValid() throws Exception {
        // Arrange
        doNothing().when(fotoService).update(any(Foto.class), eq(fotoId));

        // Act & Assert
        mockMvc.perform(put(API_BASE_URL + "/{id}", fotoId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(fotoValida)))
                .andExpect(status().isOk())
                .andExpect(content().string("Foto actualizada con éxito."));

        verify(fotoService, times(1)).update(any(Foto.class), eq(fotoId));
    }

    @Test
    void actualizarFoto_ShouldReturn404AndMessage_WhenNotFound() throws Exception {
        // Arrange
        doThrow(new NoSuchElementException()).when(fotoService).update(any(Foto.class), eq(fotoId));

        // Act & Assert
        mockMvc.perform(put(API_BASE_URL + "/{id}", fotoId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(fotoValida)))
                .andExpect(status().isNotFound())
                .andExpect(content().string("Foto no encontrada."));

        verify(fotoService, times(1)).update(any(Foto.class), eq(fotoId));
    }

    @Test
    void actualizarFoto_ShouldReturn400AndErrorMessage_OnRuntimeException() throws Exception {
        // Arrange
        String errorMessage = "Error: Nuevo título ya en uso.";
        doThrow(new RuntimeException(errorMessage)).when(fotoService).update(any(Foto.class), eq(fotoId));

        // Act & Assert
        mockMvc.perform(put(API_BASE_URL + "/{id}", fotoId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(fotoValida)))
                .andExpect(status().isBadRequest())
                .andExpect(content().string(errorMessage));

        verify(fotoService, times(1)).update(any(Foto.class), eq(fotoId));
    }

    @Test
    void actualizarFoto_ShouldReturn500AndGenericMessage_OnInternalError() throws Exception {
        // Arrange
        doThrow(new Exception("Error de DB")).when(fotoService).update(any(Foto.class), eq(fotoId));

        // Act & Assert
        mockMvc.perform(put(API_BASE_URL + "/{id}", fotoId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(fotoValida)))
                .andExpect(status().isInternalServerError())
                .andExpect(content().string("Error interno del servidor."));

        verify(fotoService, times(1)).update(any(Foto.class), eq(fotoId));
    }

    // -------------------------------------------------------------------------
    // DELETE /{id} (eliminarFoto)
    // -------------------------------------------------------------------------

    @Test
    void eliminarFoto_ShouldReturn200AndSuccessMessage_WhenFound() throws Exception {
        // Arrange
        doNothing().when(fotoService).delete(fotoId);

        // Act & Assert
        mockMvc.perform(delete(API_BASE_URL + "/{id}", fotoId))
                .andExpect(status().isOk())
                .andExpect(content().string("Foto eliminada con éxito."));

        verify(fotoService, times(1)).delete(fotoId);
    }

    @Test
    void eliminarFoto_ShouldReturn404AndMessage_WhenNotFound() throws Exception {
        // Arrange
        doThrow(new NoSuchElementException()).when(fotoService).delete(fotoId);

        // Act & Assert
        mockMvc.perform(delete(API_BASE_URL + "/{id}", fotoId))
                .andExpect(status().isNotFound())
                .andExpect(content().string("Foto no encontrada."));

        verify(fotoService, times(1)).delete(fotoId);
    }

    @Test
    void eliminarFoto_ShouldReturn400AndErrorMessage_OnRuntimeException() throws Exception {
        // Arrange
        String errorMessage = "No se puede eliminar porque está en uso.";
        doThrow(new RuntimeException(errorMessage)).when(fotoService).delete(fotoId);

        // Act & Assert
        mockMvc.perform(delete(API_BASE_URL + "/{id}", fotoId))
                .andExpect(status().isBadRequest())
                .andExpect(content().string(errorMessage));

        verify(fotoService, times(1)).delete(fotoId);
    }

    @Test
    void eliminarFoto_ShouldReturn500AndGenericMessage_OnInternalError() throws Exception {
        // Arrange
        doThrow(new Exception("Error de red")).when(fotoService).delete(fotoId);

        // Act & Assert
        mockMvc.perform(delete(API_BASE_URL + "/{id}", fotoId))
                .andExpect(status().isInternalServerError())
                .andExpect(content().string("Error interno del servidor."));

        verify(fotoService, times(1)).delete(fotoId);
    }
}