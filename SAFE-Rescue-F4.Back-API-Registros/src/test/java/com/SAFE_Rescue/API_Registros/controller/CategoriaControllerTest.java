package com.SAFE_Rescue.API_Registros.controller;

import com.SAFE_Rescue.API_Registros.modelo.Categoria;
import com.SAFE_Rescue.API_Registros.service.CategoriaService;
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
 * Pruebas unitarias para CategoriaController.
 * Utiliza @WebMvcTest para aislar la capa del controlador y @MockBean para simular el servicio.
 */
@WebMvcTest(CategoriaController.class) // Asegúrate que esta clase apunte al controlador correcto
public class CategoriaControllerTest {

    private static final String API_BASE_URL = "/api-registros/v1/categorias";

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private CategoriaService categoriaService;

    private Faker faker;
    private Categoria categoriaValida;
    private Integer categoriaId;

    @BeforeEach
    void setUp() {
        faker = new Faker();
        // Usamos un ID fijo para que las pruebas sean deterministas en los logs
        categoriaId = 123;

        categoriaValida = new Categoria();
        categoriaValida.setIdCategoria(categoriaId);
        categoriaValida.setNombre(faker.commerce().department()); // Nombre de categoría simulado
    }

    // -------------------------------------------------------------------------
    // GET / (listar)
    // -------------------------------------------------------------------------

    @Test
    void listar_ShouldReturn200AndList_WhenFound() throws Exception {
        // Arrange
        List<Categoria> listaEsperada = List.of(categoriaValida);
        when(categoriaService.findAll()).thenReturn(listaEsperada);

        // Act & Assert
        mockMvc.perform(get(API_BASE_URL))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(content().json(objectMapper.writeValueAsString(listaEsperada)))
                .andExpect(jsonPath("$.size()").value(1));

        verify(categoriaService, times(1)).findAll();
    }

    @Test
    void listar_ShouldReturn204_WhenNoContent() throws Exception {
        // Arrange
        when(categoriaService.findAll()).thenReturn(List.of());

        // Act & Assert
        mockMvc.perform(get(API_BASE_URL))
                .andExpect(status().isNoContent())
                .andExpect(content().string(""));

        verify(categoriaService, times(1)).findAll();
    }

    // -------------------------------------------------------------------------
    // GET /{id} (buscarCategoria)
    // -------------------------------------------------------------------------

    @Test
    void buscarCategoria_ShouldReturn200AndCategoria_WhenFound() throws Exception {
        // Arrange
        when(categoriaService.findById(categoriaId)).thenReturn(categoriaValida);

        // Act & Assert
        mockMvc.perform(get(API_BASE_URL + "/{id}", categoriaId))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.idCategoria").value(categoriaId));

        verify(categoriaService, times(1)).findById(categoriaId);
    }

    @Test
    void buscarCategoria_ShouldReturn404AndMessage_WhenNotFound() throws Exception {
        // Arrange
        when(categoriaService.findById(categoriaId)).thenThrow(new NoSuchElementException());

        // Act & Assert
        mockMvc.perform(get(API_BASE_URL + "/{id}", categoriaId))
                .andExpect(status().isNotFound())
                .andExpect(content().string("Categoría no encontrada"));

        verify(categoriaService, times(1)).findById(categoriaId);
    }

    // -------------------------------------------------------------------------
    // GET /buscar?nombre={nombre} (buscarCategoriaPorNombre)
    // -------------------------------------------------------------------------

    @Test
    void buscarCategoriaPorNombre_ShouldReturn200AndList_WhenFound() throws Exception {
        // Arrange
        String nombreBusqueda = "Emergencia";
        Categoria categoria2 = new Categoria();
        categoria2.setIdCategoria(categoriaId + 1);
        categoria2.setNombre("Emergencia Médica");

        List<Categoria> listaEsperada = List.of(categoriaValida, categoria2);
        when(categoriaService.findByNombre(nombreBusqueda)).thenReturn(listaEsperada);

        // Act & Assert
        mockMvc.perform(get(API_BASE_URL + "/buscar")
                        .param("nombre", nombreBusqueda)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().json(objectMapper.writeValueAsString(listaEsperada)))
                .andExpect(jsonPath("$.size()").value(2));

        verify(categoriaService, times(1)).findByNombre(nombreBusqueda);
    }

    @Test
    void buscarCategoriaPorNombre_ShouldReturn204_WhenNoContent() throws Exception {
        // Arrange
        String nombreBusqueda = "Inexistente";
        when(categoriaService.findByNombre(nombreBusqueda)).thenReturn(List.of());

        // Act & Assert
        mockMvc.perform(get(API_BASE_URL + "/buscar")
                        .param("nombre", nombreBusqueda)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNoContent());

        verify(categoriaService, times(1)).findByNombre(nombreBusqueda);
    }

    // -------------------------------------------------------------------------
    // POST / (agregarCategoria)
    // -------------------------------------------------------------------------

    @Test
    void agregarCategoria_ShouldReturn201AndSuccessMessage_WhenValid() throws Exception {
        // Arrange
        Categoria categoriaToSave = new Categoria();
        categoriaToSave.setNombre("Nueva Categoria");
        when(categoriaService.save(any(Categoria.class))).thenReturn(categoriaValida);

        // Act & Assert
        mockMvc.perform(post(API_BASE_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(categoriaToSave)))
                .andExpect(status().isCreated())
                .andExpect(content().string("Categoría creada con éxito."));

        verify(categoriaService, times(1)).save(any(Categoria.class));
    }

    @Test
    void agregarCategoria_ShouldReturn400AndErrorMessage_OnBusinessRuntimeException() throws Exception {
        // Arrange
        String errorMessage = "Error: El nombre de la categoría ya existe.";
        // Usamos IllegalStateException (subclase de RuntimeException) para simular un error de negocio
        doThrow(new IllegalStateException(errorMessage)).when(categoriaService).save(any(Categoria.class));

        // Act & Assert
        mockMvc.perform(post(API_BASE_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(categoriaValida)))
                .andExpect(status().isBadRequest()) // Espera 400
                .andExpect(content().string(errorMessage));

        verify(categoriaService, times(1)).save(any(Categoria.class));
    }

    /**
     * NOTA: Debido a la estructura del controlador (catch RuntimeException antes que catch Exception),
     * cualquier excepción UNCHECKED inesperada caerá en el bloque 400.
     * Simulamos un error interno para asegurarnos de que el bloque 400 lo maneje correctamente.
     */
    @Test
    void agregarCategoria_ShouldReturn400AndGenericMessage_OnInternalRuntimeException() throws Exception {
        // Arrange
        // Usamos RuntimeException genérica para simular un error inesperado
        doThrow(new RuntimeException("Error inesperado en el servicio")).when(categoriaService).save(any(Categoria.class));

        // Act & Assert
        mockMvc.perform(post(API_BASE_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(categoriaValida)))
                .andExpect(status().isBadRequest()) // Espera 400 (el catch(RuntimeException) en el controller)
                .andExpect(content().string("Error inesperado en el servicio"));

        verify(categoriaService, times(1)).save(any(Categoria.class));
    }

    // -------------------------------------------------------------------------
    // PUT /{id} (actualizarCategoria)
    // -------------------------------------------------------------------------

    @Test
    void actualizarCategoria_ShouldReturn200AndSuccessMessage_WhenFoundAndValid() throws Exception {
        // Arrange
        doNothing().when(categoriaService).update(any(Categoria.class), eq(categoriaId));

        // Act & Assert
        mockMvc.perform(put(API_BASE_URL + "/{id}", categoriaId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(categoriaValida)))
                .andExpect(status().isOk())
                .andExpect(content().string("Categoría actualizada con éxito."));

        verify(categoriaService, times(1)).update(any(Categoria.class), eq(categoriaId));
    }

    @Test
    void actualizarCategoria_ShouldReturn404AndMessage_WhenNotFound() throws Exception {
        // Arrange
        doThrow(new NoSuchElementException()).when(categoriaService).update(any(Categoria.class), eq(categoriaId));

        // Act & Assert
        mockMvc.perform(put(API_BASE_URL + "/{id}", categoriaId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(categoriaValida)))
                .andExpect(status().isNotFound())
                .andExpect(content().string("Categoría no encontrada."));

        verify(categoriaService, times(1)).update(any(Categoria.class), eq(categoriaId));
    }

    @Test
    void actualizarCategoria_ShouldReturn400AndErrorMessage_OnBusinessRuntimeException() throws Exception {
        // Arrange
        String errorMessage = "Error: El nuevo nombre ya está en uso.";
        // Usamos IllegalStateException (subclase de RuntimeException)
        doThrow(new IllegalStateException(errorMessage)).when(categoriaService).update(any(Categoria.class), eq(categoriaId));

        // Act & Assert
        mockMvc.perform(put(API_BASE_URL + "/{id}", categoriaId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(categoriaValida)))
                .andExpect(status().isBadRequest())
                .andExpect(content().string(errorMessage));

        verify(categoriaService, times(1)).update(any(Categoria.class), eq(categoriaId));
    }

    @Test
    void actualizarCategoria_ShouldReturn400AndGenericMessage_OnInternalRuntimeException() throws Exception {
        // Arrange
        // Usamos RuntimeException genérica para simular un error inesperado
        doThrow(new RuntimeException("Error de conexión")).when(categoriaService).update(any(Categoria.class), eq(categoriaId));

        // Act & Assert
        mockMvc.perform(put(API_BASE_URL + "/{id}", categoriaId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(categoriaValida)))
                .andExpect(status().isBadRequest())
                .andExpect(content().string("Error de conexión"));

        verify(categoriaService, times(1)).update(any(Categoria.class), eq(categoriaId));
    }


    // -------------------------------------------------------------------------
    // DELETE /{id} (eliminarCategoria)
    // -------------------------------------------------------------------------

    @Test
    void eliminarCategoria_ShouldReturn200AndSuccessMessage_WhenFound() throws Exception {
        // Arrange
        doNothing().when(categoriaService).delete(categoriaId);

        // Act & Assert
        mockMvc.perform(delete(API_BASE_URL + "/{id}", categoriaId))
                .andExpect(status().isOk())
                .andExpect(content().string("Categoría eliminada con éxito."));

        verify(categoriaService, times(1)).delete(categoriaId);
    }

    @Test
    void eliminarCategoria_ShouldReturn404AndMessage_WhenNotFound() throws Exception {
        // Arrange
        doThrow(new NoSuchElementException()).when(categoriaService).delete(categoriaId);

        // Act & Assert
        mockMvc.perform(delete(API_BASE_URL + "/{id}", categoriaId))
                .andExpect(status().isNotFound())
                .andExpect(content().string("Categoría no encontrada."));

        verify(categoriaService, times(1)).delete(categoriaId);
    }

    @Test
    void eliminarCategoria_ShouldReturn400AndErrorMessage_OnBusinessRuntimeException() throws Exception {
        // Arrange
        String errorMessage = "No se puede eliminar porque está asociada a un registro.";
        // Usamos IllegalStateException (subclase de RuntimeException)
        doThrow(new IllegalStateException(errorMessage)).when(categoriaService).delete(categoriaId);

        // Act & Assert
        mockMvc.perform(delete(API_BASE_URL + "/{id}", categoriaId))
                .andExpect(status().isBadRequest()) // Espera 400
                .andExpect(content().string(errorMessage));

        verify(categoriaService, times(1)).delete(categoriaId);
    }

    @Test
    void eliminarCategoria_ShouldReturn400AndGenericMessage_OnInternalRuntimeException() throws Exception {
        // Arrange
        // Usamos RuntimeException genérica para simular un error inesperado
        doThrow(new RuntimeException("Fallo de red al intentar eliminar")).when(categoriaService).delete(categoriaId);

        // Act & Assert
        mockMvc.perform(delete(API_BASE_URL + "/{id}", categoriaId))
                .andExpect(status().isBadRequest()) // Espera 400
                .andExpect(content().string("Fallo de red al intentar eliminar"));

        verify(categoriaService, times(1)).delete(categoriaId);
    }
}