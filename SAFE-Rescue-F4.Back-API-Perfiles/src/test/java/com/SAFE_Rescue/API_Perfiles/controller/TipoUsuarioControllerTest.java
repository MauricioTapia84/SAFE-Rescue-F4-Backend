package com.SAFE_Rescue.API_Perfiles.controller;

import com.SAFE_Rescue.API_Perfiles.modelo.TipoUsuario;
import com.SAFE_Rescue.API_Perfiles.service.TipoUsuarioService;
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

@WebMvcTest(TipoUsuarioController.class)
public class TipoUsuarioControllerTest {

    // Ruta base definida en el controlador
    private final String BASE_URL = "/api-perfiles/v1/tipos-usuario";

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean // Uso de MockitoBean para seguir la estructura solicitada
    private TipoUsuarioService tipoUsuarioService;

    @Autowired
    private ObjectMapper objectMapper;

    private Faker faker;
    private TipoUsuario tipoUsuario;
    private Integer id;

    @BeforeEach
    public void setUp() {
        faker = new Faker();
        id = faker.number().numberBetween(1, 100);

        // Crear la entidad TipoUsuario simulada
        tipoUsuario = new TipoUsuario();
        tipoUsuario.setIdTipoUsuario(id);
        tipoUsuario.setNombre(faker.job().title());
    }

    // --- Pruebas de operaciones CRUD exitosas (Happy Path) ---

    @Test
    public void listarTest_shouldReturnOkAndContent() throws Exception {
        // Arrange
        when(tipoUsuarioService.findAll()).thenReturn(List.of(tipoUsuario));

        // Act & Assert
        mockMvc.perform(get(BASE_URL))
                .andExpect(status().isOk()) // 200 OK
                // CORRECCIÓN: Cambiado de $[0].idTipo a $[0].idTipoUsuario
                .andExpect(jsonPath("$[0].idTipoUsuario").value(tipoUsuario.getIdTipoUsuario()))
                .andExpect(jsonPath("$[0].nombre").value(tipoUsuario.getNombre()));

        verify(tipoUsuarioService, times(1)).findAll();
    }

    @Test
    public void listarTest_shouldReturnNoContent() throws Exception {
        // Arrange
        when(tipoUsuarioService.findAll()).thenReturn(Collections.emptyList());

        // Act & Assert
        mockMvc.perform(get(BASE_URL))
                .andExpect(status().isNoContent()); // 204 NO CONTENT

        verify(tipoUsuarioService, times(1)).findAll();
    }

    @Test
    public void buscarTipoUsuarioTest_shouldReturnOkAndTipoUsuario() throws Exception {
        // Arrange
        when(tipoUsuarioService.findById(id)).thenReturn(tipoUsuario);

        // Act & Assert
        mockMvc.perform(get(BASE_URL + "/{id}", id))
                .andExpect(status().isOk()) // 200 OK
                .andExpect(jsonPath("$.nombre").value(tipoUsuario.getNombre()));

        verify(tipoUsuarioService, times(1)).findById(id);
    }

    @Test
    public void agregarTipoUsuarioTest_shouldReturnCreatedAndTipoUsuario() throws Exception {
        // Arrange
        when(tipoUsuarioService.save(any(TipoUsuario.class))).thenReturn(tipoUsuario);

        // Act & Assert
        mockMvc.perform(post(BASE_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(tipoUsuario)))
                .andExpect(status().isCreated()) // 201 CREATED
                // CORRECCIÓN: Cambiado de $.idTipo a $.idTipoUsuario
                .andExpect(jsonPath("$.idTipoUsuario").value(tipoUsuario.getIdTipoUsuario()));

        verify(tipoUsuarioService, times(1)).save(any(TipoUsuario.class));
    }

    @Test
    public void actualizarTipoUsuarioTest_shouldReturnOkAndTipoUsuario() throws Exception {
        // Arrange
        // Nota: Asumiendo que TipoUsuario tiene un constructor con (id, nombre) o un setter para nombre
        TipoUsuario tipoUsuarioActualizado = new TipoUsuario();
        tipoUsuarioActualizado.setIdTipoUsuario(id);
        tipoUsuarioActualizado.setNombre("Nuevo Tipo");

        when(tipoUsuarioService.update(any(TipoUsuario.class), eq(id))).thenReturn(tipoUsuarioActualizado);

        // Act & Assert
        mockMvc.perform(put(BASE_URL + "/{id}", id)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(tipoUsuarioActualizado)))
                .andExpect(status().isOk()) // 200 OK
                .andExpect(jsonPath("$.nombre", is("Nuevo Tipo")));

        verify(tipoUsuarioService, times(1)).update(any(TipoUsuario.class), eq(id));
    }

    @Test
    public void eliminarTipoUsuarioTest_shouldReturnOkAndSuccessMessage() throws Exception {
        // Arrange
        doNothing().when(tipoUsuarioService).delete(id);

        // Act & Assert
        mockMvc.perform(delete(BASE_URL + "/{id}", id))
                .andExpect(status().isOk()) // 200 OK
                .andExpect(content().string("Tipo de usuario eliminado con éxito."));

        verify(tipoUsuarioService, times(1)).delete(id);
    }

    // --- Pruebas de escenarios de error (404 Not Found) ---

    @Test
    public void buscarTipoUsuarioTest_NotFound() throws Exception {
        // Arrange
        when(tipoUsuarioService.findById(id)).thenThrow(new NoSuchElementException("Tipo de usuario no encontrado"));

        // Act & Assert
        mockMvc.perform(get(BASE_URL + "/{id}", id))
                .andExpect(status().isNotFound()); // 404 NOT FOUND
    }

    @Test
    public void actualizarTipoUsuarioTest_NotFound() throws Exception {
        // Arrange
        when(tipoUsuarioService.update(any(TipoUsuario.class), eq(id)))
                .thenThrow(new NoSuchElementException("Tipo de usuario no encontrado"));

        // Act & Assert
        mockMvc.perform(put(BASE_URL + "/{id}", id)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(tipoUsuario)))
                .andExpect(status().isNotFound()); // 404 NOT FOUND
    }

    @Test
    public void eliminarTipoUsuarioTest_NotFound() throws Exception {
        // Arrange
        doThrow(new NoSuchElementException("Tipo de usuario no encontrado")).when(tipoUsuarioService).delete(id);

        // Act & Assert
        mockMvc.perform(delete(BASE_URL + "/{id}", id))
                .andExpect(status().isNotFound()); // 404 NOT FOUND
    }

    // --- Pruebas de escenarios de error (400 Bad Request) ---

    @Test
    public void agregarTipoUsuarioTest_BadRequest_ValidationError() throws Exception {
        // Arrange
        final String errorMessage = "Error: El nombre del tipo de usuario no puede estar vacío.";
        // Simula un error de validación o lógica de negocio
        when(tipoUsuarioService.save(any(TipoUsuario.class))).thenThrow(new IllegalArgumentException(errorMessage));

        // Act & Assert
        mockMvc.perform(post(BASE_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(tipoUsuario)))
                .andExpect(status().isBadRequest()) // 400 BAD REQUEST
                .andExpect(content().string(errorMessage));
    }

    @Test
    public void actualizarTipoUsuarioTest_BadRequest_ValidationError() throws Exception {
        // Arrange
        final String errorMessage = "Error: El nombre del tipo de usuario ya está en uso.";
        // Simula un error de validación o lógica de negocio
        when(tipoUsuarioService.update(any(TipoUsuario.class), eq(id)))
                .thenThrow(new IllegalArgumentException(errorMessage));

        // Act & Assert
        mockMvc.perform(put(BASE_URL + "/{id}", id)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(tipoUsuario)))
                .andExpect(status().isBadRequest()) // 400 BAD REQUEST
                .andExpect(content().string(errorMessage));
    }

    @Test
    public void eliminarTipoUsuarioTest_BadRequest_DependencyError() throws Exception {
        // Arrange
        final String errorMessage = "No se puede eliminar porque hay usuarios asociados a este tipo.";
        // Simula un error de estado o integridad referencial
        doThrow(new IllegalStateException(errorMessage)).when(tipoUsuarioService).delete(id);

        // Act & Assert
        mockMvc.perform(delete(BASE_URL + "/{id}", id))
                .andExpect(status().isBadRequest()) // 400 BAD REQUEST
                .andExpect(content().string(errorMessage));
    }
}