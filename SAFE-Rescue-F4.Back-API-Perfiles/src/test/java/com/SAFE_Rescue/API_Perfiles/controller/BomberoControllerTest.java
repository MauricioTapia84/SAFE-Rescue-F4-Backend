package com.SAFE_Rescue.API_Perfiles.controller;

import com.SAFE_Rescue.API_Perfiles.modelo.Bombero;
import com.SAFE_Rescue.API_Perfiles.service.BomberoService;
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


/**
 * Prueba de integración unitaria para BomberoController utilizando Spring MockMvc.
 * Se simulan todos los escenarios de éxito y fallo para las operaciones CRUD.
 *
 * NOTA: Se asume que:
 * 1. NoSuchElementException se traduce a 404 Not Found (típico con un GlobalExceptionHandler).
 * 2. RuntimeException se traduce a 400 Bad Request, devolviendo el mensaje de la excepción
 * en el cuerpo, para errores de validación o negocio (ej. "RUN ya existe").
 */
@WebMvcTest(BomberoController.class)
public class BomberoControllerTest {

    private final String BASE_URL = "/api-perfiles/v1/bomberos";
    private final String NOMBRE_BOMBERO = "Juan Pérez";
    private final String RUN_BOMBERO = "12345678-K";

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private BomberoService bomberoService;

    @Autowired
    private ObjectMapper objectMapper;

    private Faker faker;
    private Bombero bombero;
    private Integer id;

    @BeforeEach
    public void setUp() {
        faker = new Faker();
        // Generamos un ID aleatorio para simular escenarios reales
        id = faker.number().numberBetween(1, 100);

        // Creamos un objeto Bombero simulado para las respuestas esperadas
        bombero = new Bombero();
        bombero.setIdUsuario(id);
        bombero.setRun(RUN_BOMBERO);
        bombero.setNombre(NOMBRE_BOMBERO);
    }

    // ------------------- GET: Listar todos (findAll) -------------------

    @Test
    public void listarTest_shouldReturnOkAndContent() throws Exception {
        // Arrange
        when(bomberoService.findAll()).thenReturn(List.of(bombero));

        // Act & Assert
        mockMvc.perform(get(BASE_URL))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].idUsuario", is(id)))
                .andExpect(jsonPath("$[0].nombre", is(NOMBRE_BOMBERO)));

        verify(bomberoService, times(1)).findAll();
    }

    @Test
    public void listarTest_shouldReturnNoContent() throws Exception {
        // Arrange
        when(bomberoService.findAll()).thenReturn(Collections.emptyList());

        // Act & Assert
        mockMvc.perform(get(BASE_URL))
                .andExpect(status().isNoContent());

        verify(bomberoService, times(1)).findAll();
    }

    // ------------------- GET: Buscar por ID (findById) -------------------

    @Test
    public void buscarBomberoTest_shouldReturnOkAndBombero() throws Exception {
        // Arrange
        when(bomberoService.findById(id)).thenReturn(bombero);

        // Act & Assert
        mockMvc.perform(get(BASE_URL + "/{id}", id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.idUsuario", is(id)))
                .andExpect(jsonPath("$.run", is(RUN_BOMBERO)))
                .andExpect(jsonPath("$.nombre", is(NOMBRE_BOMBERO)));

        verify(bomberoService, times(1)).findById(id);
    }

    @Test
    public void buscarBomberoTest_shouldReturnNotFound() throws Exception {
        // Arrange
        when(bomberoService.findById(id)).thenThrow(new NoSuchElementException("Bombero no encontrado."));

        // Act & Assert
        mockMvc.perform(get(BASE_URL + "/{id}", id))
                .andExpect(status().isNotFound());

        verify(bomberoService, times(1)).findById(id);
    }

    // ------------------- POST: Agregar Bombero (save) -------------------

    @Test
    public void agregarBomberoTest_shouldReturnCreated() throws Exception {
        // Arrange
        when(bomberoService.save(any(Bombero.class))).thenReturn(bombero);

        // Act & Assert
        mockMvc.perform(post(BASE_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(bombero)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.idUsuario", is(id)));

        verify(bomberoService, times(1)).save(any(Bombero.class));
    }

    @Test
    public void agregarBomberoTest_shouldReturnBadRequest_RuntimeException() throws Exception {
        // Arrange: Simula un error de validación/negocio (ej. RUN ya existe)
        final String errorMessage = "Error de validación: RUN ya registrado.";
        when(bomberoService.save(any(Bombero.class))).thenThrow(new RuntimeException(errorMessage));

        // Act & Assert
        mockMvc.perform(post(BASE_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(bombero)))
                .andExpect(status().isBadRequest())
                .andExpect(content().string(errorMessage)); // Verifica que el mensaje de error se devuelva

        verify(bomberoService, times(1)).save(any(Bombero.class));
    }

    // ------------------- PUT: Actualizar Bombero (update) -------------------

    @Test
    public void actualizarBomberoTest_shouldReturnOk() throws Exception {
        // Arrange
        Bombero bomberoModificado = new Bombero();
        bomberoModificado.setIdUsuario(id);
        bomberoModificado.setNombre("Juan Carlos"); // Nuevo nombre para verificar el update
        when(bomberoService.update(any(Bombero.class), eq(id))).thenReturn(bomberoModificado);

        // Act & Assert
        mockMvc.perform(put(BASE_URL + "/{id}", id)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(bomberoModificado)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nombre", is("Juan Carlos")));

        verify(bomberoService, times(1)).update(any(Bombero.class), eq(id));
    }

    @Test
    public void actualizarBomberoTest_shouldReturnNotFound() throws Exception {
        // Arrange
        when(bomberoService.update(any(Bombero.class), eq(id))).thenThrow(new NoSuchElementException());

        // Act & Assert
        mockMvc.perform(put(BASE_URL + "/{id}", id)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(bombero)))
                .andExpect(status().isNotFound());

        verify(bomberoService, times(1)).update(any(Bombero.class), eq(id));
    }

    @Test
    public void actualizarBomberoTest_shouldReturnBadRequest_RuntimeException() throws Exception {
        // Arrange: Simula un error de integridad o de validación durante la actualización
        final String errorMessage = "Error de integridad: El nuevo RUN ya está en uso.";
        when(bomberoService.update(any(Bombero.class), eq(id))).thenThrow(new RuntimeException(errorMessage));

        // Act & Assert
        mockMvc.perform(put(BASE_URL + "/{id}", id)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(bombero)))
                .andExpect(status().isBadRequest())
                .andExpect(content().string(errorMessage));

        verify(bomberoService, times(1)).update(any(Bombero.class), eq(id));
    }

    // ------------------- DELETE: Eliminar Bombero (delete) -------------------

    @Test
    public void eliminarBomberoTest_shouldReturnOk() throws Exception {
        // Arrange
        doNothing().when(bomberoService).delete(id);

        // Act & Assert
        mockMvc.perform(delete(BASE_URL + "/{id}", id))
                .andExpect(status().isOk())
                .andExpect(content().string("Bombero eliminado con éxito.")); // Verifica el mensaje de éxito

        verify(bomberoService, times(1)).delete(id);
    }

    @Test
    public void eliminarBomberoTest_shouldReturnNotFound() throws Exception {
        // Arrange
        doThrow(new NoSuchElementException()).when(bomberoService).delete(id);

        // Act & Assert
        mockMvc.perform(delete(BASE_URL + "/{id}", id))
                .andExpect(status().isNotFound());

        verify(bomberoService, times(1)).delete(id);
    }

    @Test
    public void eliminarBomberoTest_shouldReturnBadRequest_RuntimeException() throws Exception {
        // Arrange: Simula un error de dependencia (ej. es líder de un equipo)
        final String errorMessage = "Error de dependencia: El bombero es líder de un equipo.";
        doThrow(new RuntimeException(errorMessage)).when(bomberoService).delete(id);

        // Act & Assert
        mockMvc.perform(delete(BASE_URL + "/{id}", id))
                .andExpect(status().isBadRequest())
                .andExpect(content().string(errorMessage));

        verify(bomberoService, times(1)).delete(id);
    }
}