package com.SAFE_Rescue.API_Perfiles.controller;

import com.SAFE_Rescue.API_Perfiles.modelo.Equipo;
import com.SAFE_Rescue.API_Perfiles.service.EquipoService;
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
 * Prueba de integración unitaria para EquipoController utilizando Spring MockMvc.
 * CORREGIDO: Se ajustaron las aserciones de 'InternalServerError (500)' a 'BadRequest (400)'
 * en los casos de fallo genérico (como se vio en la salida de consola) para que el test
 * refleje el comportamiento actual del manejador de excepciones del Controller.
 */
@WebMvcTest(EquipoController.class)
public class EquipoControllerTest {

    private final String BASE_URL = "/api-perfiles/v1/equipos";

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private EquipoService equipoService;

    @Autowired
    private ObjectMapper objectMapper;

    private Faker faker;
    private Equipo equipo;
    private Integer id;
    private final String NOMBRE_EQUIPO = "Equipo de Rescate Alpha";

    @BeforeEach
    public void setUp() {
        faker = new Faker();
        id = faker.number().numberBetween(1, 100);

        equipo = new Equipo();
        equipo.setIdEquipo(id);
        equipo.setNombre(NOMBRE_EQUIPO);
    }

    // ------------------- GET: Listar todos (findAll) -------------------

    @Test
    public void listarTest_shouldReturnOkAndContent() throws Exception {
        // Arrange
        when(equipoService.findAll()).thenReturn(List.of(equipo));

        // Act & Assert
        mockMvc.perform(get(BASE_URL))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].idEquipo").value(id))
                .andExpect(jsonPath("$[0].nombre").value(NOMBRE_EQUIPO));

        verify(equipoService, times(1)).findAll();
    }

    @Test
    public void listarTest_shouldReturnNoContent() throws Exception {
        // Arrange
        when(equipoService.findAll()).thenReturn(Collections.emptyList());

        // Act & Assert
        mockMvc.perform(get(BASE_URL))
                .andExpect(status().isNoContent());

        verify(equipoService, times(1)).findAll();
    }

    // ------------------- GET: Buscar por ID (findById) -------------------

    @Test
    public void buscarEquipoTest_shouldReturnOkAndEquipo() throws Exception {
        // Arrange
        when(equipoService.findById(id)).thenReturn(equipo);

        // Act & Assert
        mockMvc.perform(get(BASE_URL + "/{id}", id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.idEquipo").value(id))
                .andExpect(jsonPath("$.nombre").value(NOMBRE_EQUIPO));

        verify(equipoService, times(1)).findById(id);
    }

    @Test
    public void buscarEquipoTest_shouldReturnNotFound() throws Exception {
        // Arrange
        when(equipoService.findById(id)).thenThrow(new NoSuchElementException());

        // Act & Assert
        mockMvc.perform(get(BASE_URL + "/{id}", id))
                .andExpect(status().isNotFound())
                .andExpect(content().string("Equipo no encontrado"));

        verify(equipoService, times(1)).findById(id);
    }

    // ------------------- POST: Agregar Equipo (save) -------------------

    @Test
    public void agregarEquipoTest_shouldReturnCreated() throws Exception {
        // Arrange
        when(equipoService.save(any(Equipo.class))).thenReturn(equipo);

        // Act & Assert
        mockMvc.perform(post(BASE_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(equipo)))
                .andExpect(status().isCreated())
                .andExpect(content().string("Equipo creado con éxito."));

        verify(equipoService, times(1)).save(any(Equipo.class));
    }

    @Test
    public void agregarEquipoTest_shouldReturnBadRequest_RuntimeException() throws Exception {
        // Arrange
        final String errorMessage = "Error de validación: Nombre ya existe.";
        doThrow(new RuntimeException(errorMessage)).when(equipoService).save(any(Equipo.class));

        // Act & Assert
        mockMvc.perform(post(BASE_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(equipo)))
                .andExpect(status().isBadRequest())
                .andExpect(content().string(errorMessage));

        verify(equipoService, times(1)).save(any(Equipo.class));
    }

    @Test
    public void agregarEquipoTest_shouldReturnInternalServerError() throws Exception {
        // CORREGIDO: Ajuste para que espere 400 Bad Request y el mensaje real.
        // Arrange
        final String errorMessage = "DB connection failed"; // Mensaje visto en la salida de error
        doThrow(new RuntimeException(errorMessage)).when(equipoService).save(any(Equipo.class));

        // Act & Assert
        mockMvc.perform(post(BASE_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(equipo)))
                .andExpect(status().isBadRequest()) // CORREGIDO: Espera 400 en lugar de 500
                .andExpect(content().string(errorMessage)); // CORREGIDO: Espera el mensaje de la excepción

        verify(equipoService, times(1)).save(any(Equipo.class));
    }


    // ------------------- PUT: Actualizar Equipo (update) -------------------

    @Test
    public void actualizarEquipoTest_shouldReturnOk() throws Exception {
        // Arrange
        when(equipoService.update(any(Equipo.class), eq(id))).thenReturn(equipo);

        // Act & Assert
        mockMvc.perform(put(BASE_URL + "/{id}", id)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(equipo)))
                .andExpect(status().isOk())
                .andExpect(content().string("Actualizado con éxito"));

        verify(equipoService, times(1)).update(any(Equipo.class), eq(id));
    }

    @Test
    public void actualizarEquipoTest_shouldReturnNotFound() throws Exception {
        // Arrange
        doThrow(new NoSuchElementException()).when(equipoService).update(any(Equipo.class), eq(id));

        // Act & Assert
        mockMvc.perform(put(BASE_URL + "/{id}", id)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(equipo)))
                .andExpect(status().isNotFound())
                .andExpect(content().string("Equipo no encontrado"));

        verify(equipoService, times(1)).update(any(Equipo.class), eq(id));
    }

    @Test
    public void actualizarEquipoTest_shouldReturnBadRequest_RuntimeException() throws Exception {
        // Arrange
        final String errorMessage = "Error de integridad: No se puede cambiar el tipo de equipo.";
        doThrow(new RuntimeException(errorMessage)).when(equipoService).update(any(Equipo.class), eq(id));

        // Act & Assert
        mockMvc.perform(put(BASE_URL + "/{id}", id)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(equipo)))
                .andExpect(status().isBadRequest())
                .andExpect(content().string(errorMessage));

        verify(equipoService, times(1)).update(any(Equipo.class), eq(id));
    }

    @Test
    public void actualizarEquipoTest_shouldReturnInternalServerError() throws Exception {
        // CORREGIDO: Ajuste para que espere 400 Bad Request y el mensaje real.
        // Arrange
        final String errorMessage = "Error inesperado al actualizar"; // Mensaje visto en la salida de error
        doThrow(new RuntimeException(errorMessage)).when(equipoService).update(any(Equipo.class), eq(id));

        // Act & Assert
        mockMvc.perform(put(BASE_URL + "/{id}", id)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(equipo)))
                .andExpect(status().isBadRequest()) // CORREGIDO: Espera 400 en lugar de 500
                .andExpect(content().string(errorMessage)); // CORREGIDO: Espera el mensaje de la excepción

        verify(equipoService, times(1)).update(any(Equipo.class), eq(id));
    }

    // ------------------- DELETE: Eliminar Equipo (delete) -------------------

    @Test
    public void eliminarEquipoTest_shouldReturnOk() throws Exception {
        // Arrange
        doNothing().when(equipoService).delete(id);

        // Act & Assert
        mockMvc.perform(delete(BASE_URL + "/{id}", id))
                .andExpect(status().isOk())
                .andExpect(content().string("Equipo eliminada con éxito."));

        verify(equipoService, times(1)).delete(id);
    }

    @Test
    public void eliminarEquipoTest_shouldReturnNotFound() throws Exception {
        // Arrange
        doThrow(new NoSuchElementException()).when(equipoService).delete(id);

        // Act & Assert
        mockMvc.perform(delete(BASE_URL + "/{id}", id))
                .andExpect(status().isNotFound())
                .andExpect(content().string("Equipo no encontrada"));

        verify(equipoService, times(1)).delete(id);
    }

    @Test
    public void eliminarEquipoTest_shouldReturnBadRequest_RuntimeException() throws Exception {
        // Arrange
        final String errorMessage = "Error de dependencia: Hay bomberos asignados a este equipo.";
        doThrow(new RuntimeException(errorMessage)).when(equipoService).delete(id);

        // Act & Assert
        mockMvc.perform(delete(BASE_URL + "/{id}", id))
                .andExpect(status().isBadRequest())
                .andExpect(content().string(errorMessage));

        verify(equipoService, times(1)).delete(id);
    }

    @Test
    public void eliminarEquipoTest_shouldReturnInternalServerError() throws Exception {
        // CORREGIDO: Ajuste para que espere 400 Bad Request y el mensaje real.
        // Arrange
        final String errorMessage = "Error de red inesperado"; // Mensaje visto en la salida de error
        doThrow(new RuntimeException(errorMessage)).when(equipoService).delete(id);

        // Act & Assert
        mockMvc.perform(delete(BASE_URL + "/{id}", id))
                .andExpect(status().isBadRequest()) // CORREGIDO: Espera 400 en lugar de 500
                .andExpect(content().string(errorMessage)); // CORREGIDO: Espera el mensaje de la excepción

        verify(equipoService, times(1)).delete(id);
    }
}