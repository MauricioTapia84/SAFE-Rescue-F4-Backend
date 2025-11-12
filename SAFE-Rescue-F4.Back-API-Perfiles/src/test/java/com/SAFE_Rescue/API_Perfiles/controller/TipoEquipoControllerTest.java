package com.SAFE_Rescue.API_Perfiles.controller;

import com.SAFE_Rescue.API_Perfiles.modelo.TipoEquipo;
import com.SAFE_Rescue.API_Perfiles.service.TipoEquipoService; // Importando tu servicio real
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
import static org.hamcrest.Matchers.is;


// ------------------- CLASE DE PRUEBA PRINCIPAL -------------------

/**
 * Prueba de integración unitaria para TipoEquipoController utilizando Spring MockMvc.
 * Se asume que las clases TipoEquipo y TipoEquipoService están en el classpath.
 */
@WebMvcTest(TipoEquipoController.class)
public class TipoEquipoControllerTest {

    // Ruta base definida en el controlador
    private final String BASE_URL = "/api-perfiles/v1/tipos-equipo";

    @Autowired
    private MockMvc mockMvc;

    // Se simula el servicio con Mockito
    @MockitoBean
    private TipoEquipoService tipoEquipoService;

    @Autowired
    private ObjectMapper objectMapper;

    private Faker faker;
    private TipoEquipo tipoEquipo;
    private Integer id;

    @BeforeEach
    public void setUp() {
        faker = new Faker();
        id = faker.number().numberBetween(1, 100);

        // Crear la entidad TipoEquipo simulada
        tipoEquipo = new TipoEquipo();
        tipoEquipo.setIdTipoEquipo(id); // Se asume que este método existe
        tipoEquipo.setNombre(faker.commerce().productName() + " Type"); // Se asume que este método existe
    }

    // --- Pruebas de operaciones CRUD exitosas (Happy Path) ---

    @Test
    public void listarTest_shouldReturnOkAndContent() throws Exception {
        // Arrange
        when(tipoEquipoService.findAll()).thenReturn(List.of(tipoEquipo));

        // Act & Assert
        mockMvc.perform(get(BASE_URL))
                .andExpect(status().isOk()) // 200 OK
                // Se asume que el ID se serializa como 'idTipoEquipo'
                .andExpect(jsonPath("$[0].idTipoEquipo").value(tipoEquipo.getIdTipoEquipo()))
                .andExpect(jsonPath("$[0].nombre").value(tipoEquipo.getNombre()));

        verify(tipoEquipoService, times(1)).findAll();
    }

    @Test
    public void listarTest_shouldReturnNoContent() throws Exception {
        // Arrange
        when(tipoEquipoService.findAll()).thenReturn(Collections.emptyList());

        // Act & Assert
        mockMvc.perform(get(BASE_URL))
                .andExpect(status().isNoContent()); // 204 NO CONTENT

        verify(tipoEquipoService, times(1)).findAll();
    }

    @Test
    public void buscarTipoEquipoTest_shouldReturnOkAndTipoEquipo() throws Exception {
        // Arrange
        when(tipoEquipoService.findById(id)).thenReturn(tipoEquipo);

        // Act & Assert
        mockMvc.perform(get(BASE_URL + "/{id}", id))
                .andExpect(status().isOk()) // 200 OK
                .andExpect(jsonPath("$.nombre").value(tipoEquipo.getNombre()));

        verify(tipoEquipoService, times(1)).findById(id);
    }

    @Test
    public void agregarTipoEquipoTest_shouldReturnCreatedAndTipoEquipo() throws Exception {
        // Arrange
        when(tipoEquipoService.save(any(TipoEquipo.class))).thenReturn(tipoEquipo);

        // Act & Assert
        mockMvc.perform(post(BASE_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(tipoEquipo)))
                .andExpect(status().isCreated()) // 201 CREATED
                .andExpect(jsonPath("$.idTipoEquipo").value(tipoEquipo.getIdTipoEquipo()));

        verify(tipoEquipoService, times(1)).save(any(TipoEquipo.class));
    }

    @Test
    public void actualizarTipoEquipoTest_shouldReturnOkAndTipoEquipo() throws Exception {
        // Arrange
        TipoEquipo tipoActualizado = new TipoEquipo();
        tipoActualizado.setIdTipoEquipo(id);
        tipoActualizado.setNombre("Tipo Actualizado");

        when(tipoEquipoService.update(any(TipoEquipo.class), eq(id))).thenReturn(tipoActualizado);

        // Act & Assert
        mockMvc.perform(put(BASE_URL + "/{id}", id)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(tipoActualizado)))
                .andExpect(status().isOk()) // 200 OK
                .andExpect(jsonPath("$.nombre", is("Tipo Actualizado")));

        verify(tipoEquipoService, times(1)).update(any(TipoEquipo.class), eq(id));
    }

    @Test
    public void eliminarTipoEquipoTest_shouldReturnOkAndSuccessMessage() throws Exception {
        // Arrange
        doNothing().when(tipoEquipoService).delete(id);

        // Act & Assert
        mockMvc.perform(delete(BASE_URL + "/{id}", id))
                .andExpect(status().isOk()) // 200 OK
                .andExpect(content().string("Tipo de equipo eliminado con éxito."));

        verify(tipoEquipoService, times(1)).delete(id);
    }

    // --- Pruebas de escenarios de error (404 Not Found) ---

    @Test
    public void buscarTipoEquipoTest_NotFound() throws Exception {
        // Arrange
        when(tipoEquipoService.findById(id)).thenThrow(new NoSuchElementException("Tipo de equipo no encontrado"));

        // Act & Assert
        mockMvc.perform(get(BASE_URL + "/{id}", id))
                .andExpect(status().isNotFound()); // 404 NOT FOUND
    }

    @Test
    public void actualizarTipoEquipoTest_NotFound() throws Exception {
        // Arrange
        when(tipoEquipoService.update(any(TipoEquipo.class), eq(id)))
                .thenThrow(new NoSuchElementException("Tipo de equipo no encontrado"));

        // Act & Assert
        mockMvc.perform(put(BASE_URL + "/{id}", id)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(tipoEquipo)))
                .andExpect(status().isNotFound()); // 404 NOT FOUND
    }

    @Test
    public void eliminarTipoEquipoTest_NotFound() throws Exception {
        // Arrange
        doThrow(new NoSuchElementException("Tipo de equipo no encontrado")).when(tipoEquipoService).delete(id);

        // Act & Assert
        mockMvc.perform(delete(BASE_URL + "/{id}", id))
                .andExpect(status().isNotFound()); // 404 NOT FOUND
    }

    // --- Pruebas de escenarios de error (400 Bad Request) ---

    @Test
    public void agregarTipoEquipoTest_BadRequest_ValidationError() throws Exception {
        // Arrange
        final String errorMessage = "Error: El nombre del tipo de equipo no puede estar vacío o es muy largo.";
        // Simula un error de validación o lógica de negocio
        when(tipoEquipoService.save(any(TipoEquipo.class))).thenThrow(new IllegalArgumentException(errorMessage));

        // Act & Assert
        mockMvc.perform(post(BASE_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(tipoEquipo)))
                .andExpect(status().isBadRequest()) // 400 BAD REQUEST
                .andExpect(content().string(errorMessage));
    }

    @Test
    public void actualizarTipoEquipoTest_BadRequest_ValidationError() throws Exception {
        // Arrange
        final String errorMessage = "Error: El nombre del tipo de equipo ya está en uso.";
        // Simula un error de validación o lógica de negocio (e.g., nombre duplicado)
        when(tipoEquipoService.update(any(TipoEquipo.class), eq(id)))
                .thenThrow(new IllegalArgumentException(errorMessage));

        // Act & Assert
        mockMvc.perform(put(BASE_URL + "/{id}", id)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(tipoEquipo)))
                .andExpect(status().isBadRequest()) // 400 BAD REQUEST
                .andExpect(content().string(errorMessage));
    }

    @Test
    public void eliminarTipoEquipoTest_BadRequest_DependencyError() throws Exception {
        // Arrange
        final String errorMessage = "No se puede eliminar porque hay equipos asociados a este tipo.";
        // Simula un error de estado o integridad referencial
        doThrow(new IllegalStateException(errorMessage)).when(tipoEquipoService).delete(id);

        // Act & Assert
        mockMvc.perform(delete(BASE_URL + "/{id}", id))
                .andExpect(status().isBadRequest()) // 400 BAD REQUEST
                .andExpect(content().string(errorMessage));
    }
}