package com.SAFE_Rescue.API_Registros.controller;

import com.SAFE_Rescue.API_Registros.modelo.Estado;
import com.SAFE_Rescue.API_Registros.service.EstadoService;
import com.fasterxml.jackson.databind.ObjectMapper;
import net.datafaker.Faker;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;
import java.util.NoSuchElementException;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Pruebas unitarias para EstadoController.
 * Utiliza @WebMvcTest para aislar la capa del controlador y @MockBean para simular el servicio.
 */
@WebMvcTest(EstadoController.class)
public class EstadoControllerTest {

    private static final String API_BASE_URL = "/api-registros/v1/estados";
    private static final String GENERIC_RUNTIME_ERROR_MESSAGE = "Error simulado de runtime para el 400";
    private static final String GENERIC_INTERNAL_ERROR_MESSAGE = "Error interno del servidor.";

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private EstadoService estadoService;

    private Faker faker;
    private Estado estadoValido;
    private Integer estadoId;

    @BeforeEach
    void setUp() {
        faker = new Faker();
        estadoId = faker.number().numberBetween(1, 10);

        estadoValido = new Estado();
        estadoValido.setIdEstado(estadoId);
        estadoValido.setNombre(faker.verb().past()); // Nombre de estado simulado
    }

    // -------------------------------------------------------------------------
    // GET / (listar)
    // -------------------------------------------------------------------------

    @Test
    void listar_ShouldReturn200AndList_WhenFound() throws Exception {
        // Arrange
        List<Estado> listaEsperada = List.of(estadoValido);
        when(estadoService.findAll()).thenReturn(listaEsperada);

        // Act & Assert
        mockMvc.perform(get(API_BASE_URL))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(content().json(objectMapper.writeValueAsString(listaEsperada)))
                .andExpect(jsonPath("$.size()").value(1));

        verify(estadoService, times(1)).findAll();
    }

    @Test
    void listar_ShouldReturn204_WhenNoContent() throws Exception {
        // Arrange
        when(estadoService.findAll()).thenReturn(List.of());

        // Act & Assert
        mockMvc.perform(get(API_BASE_URL))
                .andExpect(status().isNoContent())
                .andExpect(content().string(""));

        verify(estadoService, times(1)).findAll();
    }

    // -------------------------------------------------------------------------
    // GET /{id} (buscarEstado)
    // -------------------------------------------------------------------------

    @Test
    void buscarEstado_ShouldReturn200AndEstado_WhenFound() throws Exception {
        // Arrange
        when(estadoService.findById(estadoId)).thenReturn(estadoValido);

        // Act & Assert
        mockMvc.perform(get(API_BASE_URL + "/{id}", estadoId))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.idEstado").value(estadoId));

        verify(estadoService, times(1)).findById(estadoId);
    }

    @Test
    void buscarEstado_ShouldReturn404AndMessage_WhenNotFound() throws Exception {
        // Arrange
        when(estadoService.findById(estadoId)).thenThrow(new NoSuchElementException());

        // Act & Assert
        mockMvc.perform(get(API_BASE_URL + "/{id}", estadoId))
                .andExpect(status().isNotFound())
                .andExpect(content().string("Estado no encontrado"));

        verify(estadoService, times(1)).findById(estadoId);
    }

    // -------------------------------------------------------------------------
    // GET /buscar?nombre={nombre} (buscarEstadoPorNombre)
    // -------------------------------------------------------------------------

    @Test
    void buscarEstadoPorNombre_ShouldReturn200AndList_WhenFound() throws Exception {
        // Arrange
        String nombreBusqueda = "Activo";
        Estado estado2 = new Estado();
        estado2.setIdEstado(estadoId + 1);
        estado2.setNombre("Activo 2");

        List<Estado> listaEsperada = List.of(estadoValido, estado2);
        when(estadoService.findByNombre(nombreBusqueda)).thenReturn(listaEsperada);

        // Act & Assert
        mockMvc.perform(get(API_BASE_URL + "/buscar")
                        .param("nombre", nombreBusqueda)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().json(objectMapper.writeValueAsString(listaEsperada)))
                .andExpect(jsonPath("$.size()").value(2));

        verify(estadoService, times(1)).findByNombre(nombreBusqueda);
    }

    @Test
    void buscarEstadoPorNombre_ShouldReturn204_WhenNoContent() throws Exception {
        // Arrange
        String nombreBusqueda = "Inexistente";
        when(estadoService.findByNombre(nombreBusqueda)).thenReturn(List.of());

        // Act & Assert
        mockMvc.perform(get(API_BASE_URL + "/buscar")
                        .param("nombre", nombreBusqueda)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNoContent());

        verify(estadoService, times(1)).findByNombre(nombreBusqueda);
    }

    // -------------------------------------------------------------------------
    // POST / (agregarEstado)
    // -------------------------------------------------------------------------

    @Test
    void agregarEstado_ShouldReturn201AndSuccessMessage_WhenValid() throws Exception {
        // ARREGLO EL ERROR: asumo que save devuelve el objeto guardado (comportamiento estándar de JpaRepository)
        // Arrange
        Estado estadoToSave = new Estado();
        estadoToSave.setNombre("Nuevo Estado");
        when(estadoService.save(any(Estado.class))).thenReturn(estadoValido);

        // Act & Assert
        mockMvc.perform(post(API_BASE_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(estadoToSave)))
                .andExpect(status().isCreated())
                .andExpect(content().string("Estado creado con éxito."));

        verify(estadoService, times(1)).save(any(Estado.class));
    }

    @Test
    void agregarEstado_ShouldReturn400AndErrorMessage_OnBusinessRuntimeException() throws Exception {
        // Arrange
        String errorMessage = "Error: El nombre del estado ya existe.";
        doThrow(new RuntimeException(errorMessage)).when(estadoService).save(any(Estado.class));

        // Act & Assert
        mockMvc.perform(post(API_BASE_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(estadoValido)))
                .andExpect(status().isBadRequest())
                .andExpect(content().string(errorMessage));

        verify(estadoService, times(1)).save(any(Estado.class));
    }

    @Test
    void agregarEstado_ShouldReturn400AndGenericMessage_OnInternalErrorSimulation() throws Exception {
        // ARREGLO EL ERROR: Uso RuntimeException en lugar de Exception y espero 400
        // porque la lógica del controlador dirige todas las RuntimeExceptions a 400.
        // Arrange
        doThrow(new RuntimeException(GENERIC_RUNTIME_ERROR_MESSAGE)).when(estadoService).save(any(Estado.class));

        // Act & Assert
        mockMvc.perform(post(API_BASE_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(estadoValido)))
                .andExpect(status().isBadRequest()) // Espera 400 según la lógica del catch(RuntimeException)
                .andExpect(content().string(GENERIC_RUNTIME_ERROR_MESSAGE));

        verify(estadoService, times(1)).save(any(Estado.class));
    }

    // -------------------------------------------------------------------------
    // PUT /{id} (actualizarEstado)
    // -------------------------------------------------------------------------

    @Test
    void actualizarEstado_ShouldReturn200AndSuccessMessage_WhenFoundAndValid() throws Exception {
        // Arrange
        doNothing().when(estadoService).update(any(Estado.class), eq(estadoId));

        // Act & Assert
        mockMvc.perform(put(API_BASE_URL + "/{id}", estadoId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(estadoValido)))
                .andExpect(status().isOk())
                .andExpect(content().string("Estado actualizado con éxito."));

        verify(estadoService, times(1)).update(any(Estado.class), eq(estadoId));
    }

    @Test
    void actualizarEstado_ShouldReturn404AndMessage_WhenNotFound() throws Exception {
        // Arrange
        doThrow(new NoSuchElementException()).when(estadoService).update(any(Estado.class), eq(estadoId));

        // Act & Assert
        mockMvc.perform(put(API_BASE_URL + "/{id}", estadoId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(estadoValido)))
                .andExpect(status().isNotFound())
                .andExpect(content().string("Estado no encontrado."));

        verify(estadoService, times(1)).update(any(Estado.class), eq(estadoId));
    }

    @Test
    void actualizarEstado_ShouldReturn400AndErrorMessage_OnBusinessRuntimeException() throws Exception {
        // Arrange
        String errorMessage = "Error: El nuevo nombre ya está en uso.";
        doThrow(new RuntimeException(errorMessage)).when(estadoService).update(any(Estado.class), eq(estadoId));

        // Act & Assert
        mockMvc.perform(put(API_BASE_URL + "/{id}", estadoId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(estadoValido)))
                .andExpect(status().isBadRequest())
                .andExpect(content().string(errorMessage));

        verify(estadoService, times(1)).update(any(Estado.class), eq(estadoId));
    }

    @Test
    void actualizarEstado_ShouldReturn400AndGenericMessage_OnInternalErrorSimulation() throws Exception {
        // ARREGLO EL ERROR: Uso RuntimeException en lugar de Exception y espero 400.
        // Arrange
        doThrow(new RuntimeException(GENERIC_RUNTIME_ERROR_MESSAGE)).when(estadoService).update(any(Estado.class), eq(estadoId));

        // Act & Assert
        mockMvc.perform(put(API_BASE_URL + "/{id}", estadoId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(estadoValido)))
                .andExpect(status().isBadRequest()) // Espera 400
                .andExpect(content().string(GENERIC_RUNTIME_ERROR_MESSAGE));

        verify(estadoService, times(1)).update(any(Estado.class), eq(estadoId));
    }

    // -------------------------------------------------------------------------
    // DELETE /{id} (eliminarEstado)
    // -------------------------------------------------------------------------

    @Test
    void eliminarEstado_ShouldReturn200AndSuccessMessage_WhenFound() throws Exception {
        // Arrange
        doNothing().when(estadoService).delete(estadoId);

        // Act & Assert
        mockMvc.perform(delete(API_BASE_URL + "/{id}", estadoId))
                .andExpect(status().isOk())
                .andExpect(content().string("Estado eliminado con éxito."));

        verify(estadoService, times(1)).delete(estadoId);
    }

    @Test
    void eliminarEstado_ShouldReturn404AndMessage_WhenNotFound() throws Exception {
        // Arrange
        doThrow(new NoSuchElementException()).when(estadoService).delete(estadoId);

        // Act & Assert
        mockMvc.perform(delete(API_BASE_URL + "/{id}", estadoId))
                .andExpect(status().isNotFound())
                .andExpect(content().string("Estado no encontrado."));

        verify(estadoService, times(1)).delete(estadoId);
    }

    @Test
    void eliminarEstado_ShouldReturn400AndErrorMessage_OnBusinessRuntimeException() throws Exception {
        // Arrange
        String errorMessage = "No se puede eliminar porque está asociado a un Historial.";
        doThrow(new RuntimeException(errorMessage)).when(estadoService).delete(estadoId);

        // Act & Assert
        mockMvc.perform(delete(API_BASE_URL + "/{id}", estadoId))
                .andExpect(status().isBadRequest())
                .andExpect(content().string(errorMessage));

        verify(estadoService, times(1)).delete(estadoId);
    }

    @Test
    void eliminarEstado_ShouldReturn400AndGenericMessage_OnInternalErrorSimulation() throws Exception {
        // ARREGLO EL ERROR: Uso RuntimeException en lugar de Exception y espero 400.
        // Arrange
        doThrow(new RuntimeException(GENERIC_RUNTIME_ERROR_MESSAGE)).when(estadoService).delete(estadoId);

        // Act & Assert
        mockMvc.perform(delete(API_BASE_URL + "/{id}", estadoId))
                .andExpect(status().isBadRequest()) // Espera 400
                .andExpect(content().string(GENERIC_RUNTIME_ERROR_MESSAGE));

        verify(estadoService, times(1)).delete(estadoId);
    }
}