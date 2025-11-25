package com.SAFE_Rescue.API_Incidentes.controller;

import com.SAFE_Rescue.API_Incidentes.modelo.Incidente;
import com.SAFE_Rescue.API_Incidentes.modelo.TipoIncidente;
import com.SAFE_Rescue.API_Incidentes.service.IncidenteService;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import net.datafaker.Faker;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.NoSuchElementException;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(IncidenteController.class)
public class IncidenteControllerTest {

    private static final String BASE_URL = "/api-incidentes/v1/incidentes";

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private IncidenteService incidenteService;

    private Faker faker;
    private Incidente incidente1, incidente2, incidenteExistente;
    private Integer idExistente, invalidId, relatedId;

    private LocalDateTime fechaCreacion, fechaActualizacion;
    private String latitud, longitud, estado;


    @BeforeEach
    void setUp() {
        faker = new Faker();
        idExistente = 1;
        invalidId = 999;
        relatedId = 101;

        // INICIALIZACIÓN DE LAS 4 NUEVAS PROPIEDADES
        fechaCreacion = LocalDateTime.now().minusHours(2);
        fechaActualizacion = LocalDateTime.now();
        latitud = faker.address().latitude();
        longitud = faker.address().longitude();
        estado = "ACTIVO";

        // Configurar ObjectMapper para manejar LocalDateTime
        objectMapper.findAndRegisterModules();
        objectMapper.configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false);

        incidente1 = new Incidente(
                idExistente,
                faker.lorem().sentence(3),
                faker.lorem().paragraph(2),
                fechaCreacion,
                fechaActualizacion,
                latitud,
                longitud,
                estado,
                new TipoIncidente(1,"Incendio"),
                relatedId,
                relatedId,
                null,
                relatedId
        );

        // CONSTRUCTOR CORREGIDO - (CORRECCIÓN: TipoIncidente.Accidente)
        incidente2 = new Incidente(
                2,
                faker.lorem().sentence(3),
                faker.lorem().paragraph(2),
                fechaCreacion.minusHours(1),
                fechaActualizacion.minusHours(1),
                faker.address().latitude(),
                faker.address().longitude(),
                "RESUELTO",
                new TipoIncidente(2,"Accidente"),
                relatedId + 1,
                relatedId,
                relatedId + 2,
                null
        );

        incidenteExistente = incidente1;
    }

    // --- GET / ---
    @Test
    void findAll_shouldReturnListOfIncidentes() throws Exception {
        List<Incidente> incidentes = Arrays.asList(incidente1, incidente2);
        when(incidenteService.findAll()).thenReturn(incidentes);

        mockMvc.perform(get(BASE_URL)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.size()").value(incidentes.size()))
                .andExpect(jsonPath("$[0].idIncidente").value(incidente1.getIdIncidente()))
                .andExpect(jsonPath("$[1].idIncidente").value(incidente2.getIdIncidente()));

        verify(incidenteService, times(1)).findAll();
    }

    @Test
    void findAll_shouldReturn204_whenNoContent() throws Exception {
        when(incidenteService.findAll()).thenReturn(Collections.emptyList());

        mockMvc.perform(get(BASE_URL)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNoContent())
                .andExpect(content().string(""));

        verify(incidenteService, times(1)).findAll();
    }

    // --- GET /{id} ---
    @Test
    void findById_shouldReturnIncidente_whenFound() throws Exception {
        when(incidenteService.findById(idExistente)).thenReturn(incidenteExistente);

        mockMvc.perform(get(BASE_URL + "/{id}", idExistente)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.idIncidente").value(idExistente))
                .andExpect(jsonPath("$.titulo").value(incidenteExistente.getTitulo()));

        verify(incidenteService, times(1)).findById(idExistente);
    }

    @Test
    void findById_shouldReturn404_whenNotFound() throws Exception {
        when(incidenteService.findById(invalidId)).thenThrow(new NoSuchElementException());

        mockMvc.perform(get(BASE_URL + "/{id}", invalidId)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());

        verify(incidenteService, times(1)).findById(invalidId);
    }

    // --- POST / ---
    @Test
    void create_shouldReturn201AndIncidente_whenSuccess() throws Exception {
        // CONSTRUCTOR CORREGIDO - (CORRECCIÓN: TipoIncidente.Incendio)
        Incidente nuevoIncidente = new Incidente(
                null,
                faker.lorem().sentence(3),
                faker.lorem().paragraph(2),
                fechaCreacion,
                fechaActualizacion,
                latitud,
                longitud,
                estado,
                new TipoIncidente(1,"Incendio"),
                relatedId,
                relatedId,
                null,
                relatedId
        );

        when(incidenteService.save(any(Incidente.class))).thenReturn(incidente1);

        mockMvc.perform(post(BASE_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(nuevoIncidente)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.idIncidente").value(incidente1.getIdIncidente()))
                .andExpect(jsonPath("$.titulo").value(incidente1.getTitulo()));

        verify(incidenteService, times(1)).save(any(Incidente.class));
    }

    @Test
    void create_shouldReturn400_whenValidationError() throws Exception {
        // CONSTRUCTOR CORREGIDO - (CORRECCIÓN: TipoIncidente.Incendio)
        Incidente incidenteInvalido = new Incidente(
                null,
                "",
                faker.lorem().paragraph(2),
                fechaCreacion,
                fechaActualizacion,
                latitud,
                longitud,
                estado,
                new TipoIncidente(1,"Incendio"),
                relatedId,
                relatedId,
                null,
                relatedId
        );

        when(incidenteService.save(any(Incidente.class))).thenThrow(new IllegalArgumentException("Validación fallida"));

        mockMvc.perform(post(BASE_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(incidenteInvalido)))
                .andExpect(status().isBadRequest());

        verify(incidenteService, times(1)).save(any(Incidente.class));
    }

    // --- PUT /{id} ---
    @Test
    void update_shouldReturn200AndUpdatedIncidente_whenFound() throws Exception {
        // CONSTRUCTOR CORREGIDO - (CORRECCIÓN: string para Descripción y TipoIncidente.Incendio)
        Incidente incidenteAActualizar = new Incidente(
                idExistente,
                "Título Actualizado",
                "Descripción del incidente 1",
                fechaCreacion,
                fechaActualizacion,
                latitud,
                longitud,
                estado,
                new TipoIncidente(1,"Incendio"),
                relatedId,
                relatedId,
                null,
                relatedId
        );

        when(incidenteService.update(any(Incidente.class), eq(idExistente))).thenReturn(incidenteAActualizar);

        mockMvc.perform(put(BASE_URL + "/{id}", idExistente)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(incidenteAActualizar)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.idIncidente").value(idExistente))
                .andExpect(jsonPath("$.titulo").value("Título Actualizado"));

        verify(incidenteService, times(1)).update(any(Incidente.class), eq(idExistente));
    }

    @Test
    void update_shouldReturn404_whenNotFound() throws Exception {
        // CONSTRUCTOR CORREGIDO - (CORRECCIÓN: TipoIncidente.Accidente)
        Incidente incidenteInexistente = new Incidente(
                invalidId,
                "Título",
                faker.lorem().paragraph(2),
                fechaCreacion,
                fechaActualizacion,
                latitud,
                longitud,
                estado,
                new TipoIncidente(2,"Accidente"),
                relatedId,
                relatedId,
                null,
                relatedId
        );

        when(incidenteService.update(any(Incidente.class), eq(invalidId))).thenThrow(new NoSuchElementException());

        mockMvc.perform(put(BASE_URL + "/{id}", invalidId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(incidenteInexistente)))
                .andExpect(status().isNotFound());

        verify(incidenteService, times(1)).update(any(Incidente.class), eq(invalidId));
    }

    // --- DELETE /{id} ---
    @Test
    void delete_shouldReturn204_whenSuccess() throws Exception {
        doNothing().when(incidenteService).delete(idExistente);

        mockMvc.perform(delete(BASE_URL + "/{id}", idExistente))
                .andExpect(status().isNoContent());

        verify(incidenteService, times(1)).delete(idExistente);
    }

    @Test
    void delete_shouldReturn404_whenNotFound() throws Exception {
        doThrow(new NoSuchElementException()).when(incidenteService).delete(invalidId);

        mockMvc.perform(delete(BASE_URL + "/{id}", invalidId))
                .andExpect(status().isNotFound());

        verify(incidenteService, times(1)).delete(invalidId);
    }

    // --- POST /agregar-ubicacion ---
    @Test
    void agregarUbicacionAIncidente_shouldReturnOk_whenSuccess() throws Exception {
        String latitudLongitud = "-33.456,-70.648";
        doNothing().when(incidenteService).agregarUbicacionAIncidente(idExistente, latitudLongitud);

        mockMvc.perform(post(BASE_URL + "/{incidenteId}/agregar-ubicacion", idExistente)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(latitudLongitud))
                .andExpect(status().isOk())
                .andExpect(content().string("Dirección (Ubicación) agregada/actualizada al incidente exitosamente"));

        verify(incidenteService, times(1)).agregarUbicacionAIncidente(eq(idExistente), eq(latitudLongitud));
    }

    @Test
    void agregarUbicacionAIncidente_shouldReturnNotFound_whenIncidenteNotFound() throws Exception {
        String latitudLongitud = "-33.456,-70.648";
        String errorMsg = "Incidente no encontrado con ID: " + invalidId;
        doThrow(new NoSuchElementException(errorMsg)).when(incidenteService).agregarUbicacionAIncidente(invalidId, latitudLongitud);

        mockMvc.perform(post(BASE_URL + "/{incidenteId}/agregar-ubicacion", invalidId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(latitudLongitud))
                .andExpect(status().isNotFound())
                .andExpect(content().string(errorMsg));

        verify(incidenteService, times(1)).agregarUbicacionAIncidente(eq(invalidId), eq(latitudLongitud));
    }

    @Test
    void agregarUbicacionAIncidente_shouldReturnBadRequest_whenInvalidCoordinates() throws Exception {
        String latitudLongitud = "coordenadas invalidas";
        String errorMsg = "Formato de coordenadas incorrecto";
        doThrow(new IllegalArgumentException(errorMsg)).when(incidenteService).agregarUbicacionAIncidente(idExistente, latitudLongitud);

        mockMvc.perform(post(BASE_URL + "/{incidenteId}/agregar-ubicacion", idExistente)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(latitudLongitud))
                .andExpect(status().isBadRequest())
                .andExpect(content().string("Error al crear o asignar la dirección: " + errorMsg));

        verify(incidenteService, times(1)).agregarUbicacionAIncidente(eq(idExistente), eq(latitudLongitud));
    }

    @Test
    void agregarUbicacionAIncidente_shouldReturnBadRequest_whenGeolocationFails() throws Exception {
        String latitudLongitud = "-33.456,-70.648";
        String errorMsg = "Fallo la llamada a la API de Geolocalización";
        doThrow(new RuntimeException(errorMsg)).when(incidenteService).agregarUbicacionAIncidente(idExistente, latitudLongitud);

        mockMvc.perform(post(BASE_URL + "/{incidenteId}/agregar-ubicacion", idExistente)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(latitudLongitud))
                .andExpect(status().isBadRequest())
                .andExpect(content().string("Error al crear o asignar la dirección: " + errorMsg));

        verify(incidenteService, times(1)).agregarUbicacionAIncidente(eq(idExistente), eq(latitudLongitud));
    }

    // --- POST /asignar-direccion/{direccionId} ---
    @Test
    void asignarDireccion_shouldReturnOk_whenSuccess() throws Exception {
        doNothing().when(incidenteService).asignarDireccion(idExistente, relatedId);

        mockMvc.perform(post(BASE_URL + "/{incidenteId}/asignar-direccion/{direccionId}", idExistente, relatedId)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().string("Dirección asignada al incidente exitosamente"));

        verify(incidenteService, times(1)).asignarDireccion(idExistente, relatedId);
    }

    @Test
    void asignarDireccion_shouldReturnNotFound_whenReferenceNotFound() throws Exception {
        String errorMsg = "Incidente o Dirección no encontrada";
        // Usamos NoSuchElementException para simular el 404 que devuelve el controlador
        doThrow(new NoSuchElementException(errorMsg)).when(incidenteService).asignarDireccion(idExistente, invalidId);

        mockMvc.perform(post(BASE_URL + "/{incidenteId}/asignar-direccion/{direccionId}", idExistente, invalidId)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound())
                .andExpect(content().string(errorMsg));

        verify(incidenteService, times(1)).asignarDireccion(idExistente, invalidId);
    }

    @Test
    void asignarDireccion_shouldReturnBadRequest_onRuntimeException() throws Exception {
        String errorMsg = "Error de negocio al asignar dirección";
        doThrow(new RuntimeException(errorMsg)).when(incidenteService).asignarDireccion(invalidId, relatedId);

        mockMvc.perform(post(BASE_URL + "/{incidenteId}/asignar-direccion/{direccionId}", invalidId, relatedId)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(content().string(errorMsg));

        verify(incidenteService, times(1)).asignarDireccion(invalidId, relatedId);
    }
}