package com.SAFE_Rescue.API_Incidentes.controller;

import com.SAFE_Rescue.API_Incidentes.modelo.TipoIncidente;
import com.SAFE_Rescue.API_Incidentes.service.TipoIncidenteService;
import com.fasterxml.jackson.databind.ObjectMapper;
import net.datafaker.Faker;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.NoSuchElementException;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;


@WebMvcTest(TipoIncidenteController.class)
public class TipoIncidenteControllerTest {

    private final String BASE_URL = "/api-incidentes/v1/tipos-incidentes";

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private TipoIncidenteService tipoIncidenteService;

    private TipoIncidente tipoIncidenteValido;
    private Integer idExistente;
    private Integer idNoExistente;

    @BeforeEach
    public void setUp() {
        Faker faker = new Faker();
        idExistente = faker.number().numberBetween(1, 100);
        idNoExistente = 999;
        tipoIncidenteValido = new TipoIncidente(idExistente, "Incendio Forestal");
    }

    // --- 1. GET /tipos-incidentes (Listar todos) ---

    @Test
    void listarTiposIncidente_shouldReturnOk_whenListIsNotEmpty() throws Exception {
        // Arrange
        List<TipoIncidente> lista = Arrays.asList(tipoIncidenteValido, new TipoIncidente(2, "Accidente"));
        when(tipoIncidenteService.findAll()).thenReturn(lista);

        // Act & Assert
        mockMvc.perform(get(BASE_URL)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.size()").value(2))
                .andExpect(jsonPath("$[0].nombre").value("Incendio Forestal"));

        verify(tipoIncidenteService, times(1)).findAll();
    }

    @Test
    void listarTiposIncidente_shouldReturnNoContent_whenListIsEmpty() throws Exception {
        // Arrange
        when(tipoIncidenteService.findAll()).thenReturn(Collections.emptyList());

        // Act & Assert
        mockMvc.perform(get(BASE_URL)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNoContent());

        verify(tipoIncidenteService, times(1)).findAll();
    }

    // --- 2. GET /tipos-incidentes/{id} (Buscar por ID) ---

    @Test
    void buscarTipoIncidente_shouldReturnOk_whenIdExists() throws Exception {
        // Arrange
        when(tipoIncidenteService.findById(idExistente)).thenReturn(tipoIncidenteValido);

        // Act & Assert
        mockMvc.perform(get(BASE_URL + "/{id}", idExistente)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nombre").value("Incendio Forestal"));

        verify(tipoIncidenteService, times(1)).findById(idExistente);
    }

    @Test
    void buscarTipoIncidente_shouldReturnNotFound_whenIdDoesNotExist() throws Exception {
        // Arrange
        when(tipoIncidenteService.findById(idNoExistente)).thenThrow(new NoSuchElementException());

        // Act & Assert
        mockMvc.perform(get(BASE_URL + "/{id}", idNoExistente)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound())
                .andExpect(content().string("Tipo Incidente no encontrado"));

        verify(tipoIncidenteService, times(1)).findById(idNoExistente);
    }

    // --- 3. POST /tipos-incidentes (Crear) ---

    @Test
    void agregarTipoIncidente_shouldReturnCreated_whenValid() throws Exception {
        // Arrange
        TipoIncidente nuevoTipo = new TipoIncidente(1,"Terremoto");
        // CORRECCIÓN: Usar when().thenReturn() porque TipoIncidenteService.save() devuelve un objeto, no es void.
        when(tipoIncidenteService.save(any(TipoIncidente.class))).thenReturn(tipoIncidenteValido);

        // Act & Assert
        mockMvc.perform(post(BASE_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(nuevoTipo)))
                .andExpect(status().isCreated())
                .andExpect(content().string("Tipo Incidente creado con éxito."));

        verify(tipoIncidenteService, times(1)).save(any(TipoIncidente.class));
    }

    @Test
    void agregarTipoIncidente_shouldReturnBadRequest_whenServiceThrowsRuntimeException() throws Exception {
        // Arrange
        TipoIncidente tipoInvalido = new TipoIncidente(1,"");
        String errorMsg = "El nombre es requerido";
        when(tipoIncidenteService.save(any(TipoIncidente.class))).thenThrow(new RuntimeException(errorMsg));

        // Act & Assert
        mockMvc.perform(post(BASE_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(tipoInvalido)))
                .andExpect(status().isBadRequest())
                .andExpect(content().string(errorMsg));

        verify(tipoIncidenteService, times(1)).save(any(TipoIncidente.class));
    }

    // --- 4. PUT /tipos-incidentes/{id} (Actualizar) ---

    @Test
    void actualizarTipoIncidente_shouldReturnOk_whenValidUpdate() throws Exception {
        // Arrange
        TipoIncidente datosActualizados = new TipoIncidente(1,"Tsunami");
        when(tipoIncidenteService.update(any(TipoIncidente.class), eq(idExistente))).thenReturn(tipoIncidenteValido);

        // Act & Assert
        mockMvc.perform(put(BASE_URL + "/{id}", idExistente)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(datosActualizados)))
                .andExpect(status().isOk())
                .andExpect(content().string("Actualizado con éxito"));

        verify(tipoIncidenteService, times(1)).update(any(TipoIncidente.class), eq(idExistente));
    }

    @Test
    void actualizarTipoIncidente_shouldReturnNotFound_whenIdDoesNotExist() throws Exception {
        // Arrange
        TipoIncidente datosActualizados = new TipoIncidente(1,"Tsunami");
        when(tipoIncidenteService.update(any(TipoIncidente.class), eq(idNoExistente))).thenThrow(new NoSuchElementException());

        // Act & Assert
        mockMvc.perform(put(BASE_URL + "/{id}", idNoExistente)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(datosActualizados)))
                .andExpect(status().isNotFound())
                .andExpect(content().string("Tipo Incidente no encontrado"));

        verify(tipoIncidenteService, times(1)).update(any(TipoIncidente.class), eq(idNoExistente));
    }

    @Test
    void actualizarTipoIncidente_shouldReturnBadRequest_whenValidationFails() throws Exception {
        // Arrange
        TipoIncidente datosActualizados = new TipoIncidente(1,"Nombre demasiado largo...");
        String errorMsg = "El nombre no puede exceder 50 caracteres";
        when(tipoIncidenteService.update(any(TipoIncidente.class), eq(idExistente))).thenThrow(new RuntimeException(errorMsg));

        // Act & Assert
        mockMvc.perform(put(BASE_URL + "/{id}", idExistente)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(datosActualizados)))
                .andExpect(status().isBadRequest())
                .andExpect(content().string(errorMsg));

        verify(tipoIncidenteService, times(1)).update(any(TipoIncidente.class), eq(idExistente));
    }


    // --- 5. DELETE /tipos-incidentes/{id} (Eliminar) ---

    @Test
    void eliminarTipoIncidente_shouldReturnOk_whenIdExists() throws Exception {
        // Arrange
        // doNothing() es correcto aquí porque TipoIncidenteService.delete() es void
        doNothing().when(tipoIncidenteService).delete(idExistente);

        // Act & Assert
        mockMvc.perform(delete(BASE_URL + "/{id}", idExistente)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().string("Tipo Incidente eliminado con éxito."));

        verify(tipoIncidenteService, times(1)).delete(idExistente);
    }

    @Test
    void eliminarTipoIncidente_shouldReturnNotFound_whenIdDoesNotExist() throws Exception {
        // Arrange
        doThrow(new NoSuchElementException()).when(tipoIncidenteService).delete(idNoExistente);

        // Act & Assert
        mockMvc.perform(delete(BASE_URL + "/{id}", idNoExistente)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound())
                .andExpect(content().string("Tipo Incidente no encontrado"));

        verify(tipoIncidenteService, times(1)).delete(idNoExistente);
    }
}
