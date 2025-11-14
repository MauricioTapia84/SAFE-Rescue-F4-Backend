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

    private final String BASE_URL = "/api-incidentes/v1/incidentes";

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private IncidenteService incidenteService;

    private Incidente incidenteValido;
    private Integer idExistente;
    private Integer idNoExistente;
    private Integer relatedId;
    private Integer invalidId;
    private final Faker faker = new Faker();

    // Fecha y hora constantes para asegurar que la aserción de JSON sea fiable
    private final LocalDateTime MOCK_DATETIME = LocalDateTime.of(2025, 1, 1, 10, 30, 0);

    @BeforeEach
    public void setUp() {
        // --- Configuración de Jackson para manejar Fechas correctamente ---
        objectMapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        // --- Fin de la Configuración ---

        // Inicialización de IDs de prueba
        idExistente = faker.number().numberBetween(1, 100);
        idNoExistente = 999;
        relatedId = faker.number().numberBetween(1, 10);
        invalidId = 999;

        // Objeto base para las pruebas: Se asume que el constructor ahora recibe 5 FKs (TipoIncidente, Ciudadano, Estado, Dirección, UsuarioAsignado)
        // Usaremos '1' para el ID de Usuario Asignado.
        incidenteValido = new Incidente(idExistente, "Accidente de tráfico en la ruta 5","Este incidente es una prueba", MOCK_DATETIME,new TipoIncidente(),1,1,1,1);
    }

    // ====================================================================
    // TESTS PARA OPERACIONES CRUD BÁSICAS
    // ====================================================================

    // --- GET /incidentes (Listar todos) ---
    @Test
    void listar_shouldReturnOk_whenListIsNotEmpty() throws Exception {
        // Usamos una fecha constante para hacer la aserción más predecible
        LocalDateTime fechaDePrueba = MOCK_DATETIME;

        // Corregido: Los constructores deben recibir 5 FKs, incluyendo el idUsuarioAsignado (último '1')
        List<Incidente> lista = Arrays.asList(
                new Incidente(idExistente, "Accidente de tráfico en la ruta 5","Desc", fechaDePrueba,new TipoIncidente(),1,1,1,1),
                new Incidente(2, "Incendio reportado","Esta es la descripción",fechaDePrueba,new TipoIncidente(),2,2,2,2));

        when(incidenteService.findAll()).thenReturn(lista);

        mockMvc.perform(get(BASE_URL)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.size()").value(2))
                .andExpect(jsonPath("$[0].titulo").value("Accidente de tráfico en la ruta 5"))
                // La aserción de la fecha/hora debe ser el formato ISO de LocalDateTime
                .andExpect(jsonPath("$[0].fechaRegistro").value("2025-01-01T10:30:00"));

        verify(incidenteService, times(1)).findAll();
    }

    @Test
    void listar_shouldReturnNoContent_whenListIsEmpty() throws Exception {
        when(incidenteService.findAll()).thenReturn(Collections.emptyList());

        mockMvc.perform(get(BASE_URL)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNoContent());

        verify(incidenteService, times(1)).findAll();
    }

    // --- GET /incidentes/{id} (Buscar por ID) ---
    @Test
    void buscarIncidente_shouldReturnOk_whenIdExists() throws Exception {
        when(incidenteService.findById(idExistente)).thenReturn(incidenteValido);

        mockMvc.perform(get(BASE_URL + "/{id}", idExistente)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk()) // 200 OK
                .andExpect(jsonPath("$.detalle").value(incidenteValido.getDetalle()));

        verify(incidenteService, times(1)).findById(idExistente);
    }

    @Test
    void buscarIncidente_shouldReturnNotFound_whenIdDoesNotExist() throws Exception {
        when(incidenteService.findById(idNoExistente)).thenThrow(new NoSuchElementException());

        mockMvc.perform(get(BASE_URL + "/{id}", idNoExistente)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound()) // 404 Not Found
                .andExpect(content().string("Incidente no encontrado"));

        verify(incidenteService, times(1)).findById(idNoExistente);
    }

    // --- POST /incidentes (Crear) ---
    @Test
    void agregarIncidente_shouldReturnCreated_whenValid() throws Exception {
        // Nuevo Incidente con 5 FKs, incluyendo idUsuarioAsignado: 1
        Incidente nuevoIncidente = new Incidente(null, "Fuga de gas","Descripcion",LocalDateTime.now(),new TipoIncidente(),1,1,1,1);
        when(incidenteService.save(any(Incidente.class))).thenReturn(incidenteValido);

        mockMvc.perform(post(BASE_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(nuevoIncidente)))
                .andExpect(status().isCreated()) // 201 Created
                .andExpect(content().string("Incidente creado con éxito."));

        verify(incidenteService, times(1)).save(any(Incidente.class));
    }

    @Test
    void agregarIncidente_shouldReturnBadRequest_whenReferenceIdIsInvalid() throws Exception {
        // Incidente con 5 FKs
        Incidente incidenteInvalido = new Incidente(null, "Falta relación","Descripcion",LocalDateTime.now(),new TipoIncidente(),1,1,1,1);
        String errorMsg = "El ID de TipoIncidente no existe.";
        // Simular excepción lanzada por el Service (ej. si falla la validación del Ciudadano)
        when(incidenteService.save(any(Incidente.class))).thenThrow(new IllegalArgumentException(errorMsg));

        mockMvc.perform(post(BASE_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(incidenteInvalido)))
                .andExpect(status().isBadRequest()) // 400 Bad Request
                .andExpect(content().string(errorMsg));

        verify(incidenteService, times(1)).save(any(Incidente.class));
    }

    // --- PUT /incidentes/{id} (Actualizar) ---
    @Test
    void actualizarIncidente_shouldReturnOk_whenValidUpdate() throws Exception {
        // Datos Actualizados con 5 FKs
        Incidente datosActualizados = new Incidente(idExistente, "Descripción actualizada","Descripcion Actualizada",LocalDateTime.now(),new TipoIncidente(),1,1,1,1);
        when(incidenteService.update(any(Incidente.class), eq(idExistente))).thenReturn(datosActualizados);

        mockMvc.perform(put(BASE_URL + "/{id}", idExistente)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(datosActualizados)))
                .andExpect(status().isOk()) // 200 OK
                .andExpect(content().string("Actualizado con éxito"));

        verify(incidenteService, times(1)).update(any(Incidente.class), eq(idExistente));
    }

    @Test
    void actualizarIncidente_shouldReturnNotFound_whenIdDoesNotExist() throws Exception {
        // Datos Actualizados con 5 FKs
        Incidente datosActualizados = new Incidente(idNoExistente, "Intento de actualización","Descripcion",LocalDateTime.now(),new TipoIncidente(),1,1,1,1);
        when(incidenteService.update(any(Incidente.class), eq(idNoExistente))).thenThrow(new NoSuchElementException());

        mockMvc.perform(put(BASE_URL + "/{id}", idNoExistente)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(datosActualizados)))
                .andExpect(status().isNotFound()) // 404 Not Found
                .andExpect(content().string("Incidente no encontrado"));

        verify(incidenteService, times(1)).update(any(Incidente.class), eq(idNoExistente));
    }

    // --- DELETE /incidentes/{id} (Eliminar) ---
    @Test
    void eliminarIncidente_shouldReturnOk_whenIdExists() throws Exception {
        doNothing().when(incidenteService).delete(idExistente);

        mockMvc.perform(delete(BASE_URL + "/{id}", idExistente)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk()) // 200 OK
                .andExpect(content().string("Incidente eliminado con éxito."));

        verify(incidenteService, times(1)).delete(idExistente);
    }

    @Test
    void eliminarIncidente_shouldReturnNotFound_whenIdDoesNotExist() throws Exception {
        doThrow(new NoSuchElementException()).when(incidenteService).delete(idNoExistente);

        mockMvc.perform(delete(BASE_URL + "/{id}", idNoExistente)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound()) // 404 Not Found
                .andExpect(content().string("Incidente no encontrado"));

        verify(incidenteService, times(1)).delete(idNoExistente);
    }

    // ====================================================================
    // TESTS PARA GESTIÓN DE RELACIONES (AJUSTADO)
    // ====================================================================

    // --- POST /asignar-usuario-asignado/{usuarioAsignadoId} (NUEVO ENDPOINT) ---
    @Test
    void asignarUsuarioAsignado_shouldReturnOk_whenSuccess() throws Exception {
        doNothing().when(incidenteService).asignarUsuarioAsignado(idExistente, relatedId);

        mockMvc.perform(post(BASE_URL + "/{incidenteId}/asignar-usuario-asignado/{usuarioAsignadoId}", idExistente, relatedId)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().string("Usuario Asignado al Incidente exitosamente"));

        verify(incidenteService, times(1)).asignarUsuarioAsignado(idExistente, relatedId);
    }

    @Test
    void asignarUsuarioAsignado_shouldReturnNotFound_whenIncidenteOrUserNotFound() throws Exception {
        String errorMsg = "Incidente o Usuario Asignado no encontrado";
        doThrow(new RuntimeException(errorMsg)).when(incidenteService).asignarUsuarioAsignado(idExistente, invalidId);

        mockMvc.perform(post(BASE_URL + "/{incidenteId}/asignar-usuario-asignado/{usuarioAsignadoId}", idExistente, invalidId)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound()) // 404 Not Found
                .andExpect(content().string(errorMsg));

        verify(incidenteService, times(1)).asignarUsuarioAsignado(idExistente, invalidId);
    }

    // --- POST /asignar-ciudadano/{ciudadanoId} ---
    @Test
    void asignacCiudadano_shouldReturnOk_whenSuccess() throws Exception {
        doNothing().when(incidenteService).asignarCiudadano(idExistente, relatedId);

        mockMvc.perform(post(BASE_URL + "/{incidenteId}/asignar-ciudadano/{ciudadanoId}", idExistente, relatedId)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().string("UsuarioDTO asignado al Incidente exitosamente"));

        verify(incidenteService, times(1)).asignarCiudadano(idExistente, relatedId);
    }

    @Test
    void asignacCiudadano_shouldReturnNotFound_whenIncidenteOrCiudadanoNotFound() throws Exception {
        String errorMsg = "Incidente o Ciudadano no encontrado";
        doThrow(new RuntimeException(errorMsg)).when(incidenteService).asignarCiudadano(idExistente, invalidId);

        mockMvc.perform(post(BASE_URL + "/{incidenteId}/asignar-ciudadano/{ciudadanoId}", idExistente, invalidId)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound()) // 404 Not Found
                .andExpect(content().string(errorMsg));

        verify(incidenteService, times(1)).asignarCiudadano(idExistente, invalidId);
    }


    // --- POST /asignar-estado-incidente/{estadoIncidenteId} ---
    @Test
    void asignarEstadoIncidente_shouldReturnOk_whenSuccess() throws Exception {
        doNothing().when(incidenteService).asignarEstadoIncidente(idExistente, relatedId);

        mockMvc.perform(post(BASE_URL + "/{incidenteId}/asignar-estado-incidente/{estadoIncidenteId}", idExistente, relatedId)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().string("Estado Incidente asignado al Incidente exitosamente"));

        verify(incidenteService, times(1)).asignarEstadoIncidente(idExistente, relatedId);
    }

    @Test
    void asignarEstadoIncidente_shouldReturnNotFound_whenReferenceNotFound() throws Exception {
        String errorMsg = "Estado de Incidente no encontrado";
        doThrow(new RuntimeException(errorMsg)).when(incidenteService).asignarEstadoIncidente(idExistente, invalidId);

        mockMvc.perform(post(BASE_URL + "/{incidenteId}/asignar-estado-incidente/{estadoIncidenteId}", idExistente, invalidId)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound()) // 404 Not Found
                .andExpect(content().string(errorMsg));

        verify(incidenteService, times(1)).asignarEstadoIncidente(idExistente, invalidId);
    }

    // --- POST /asignar-tipo-incidente/{tipoIncidenteId} ---
    @Test
    void asignarTipoIncidente_shouldReturnOk_whenSuccess() throws Exception {
        doNothing().when(incidenteService).asignarTipoIncidente(idExistente, relatedId);

        mockMvc.perform(post(BASE_URL + "/{incidenteId}/asignar-tipo-incidente/{tipoIncidenteId}", idExistente, relatedId)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().string("Tipo Incidente asignado al Incidente exitosamente"));

        verify(incidenteService, times(1)).asignarTipoIncidente(idExistente, relatedId);
    }

    // --- POST /agregar-ubicacion (Crear y Asignar Dirección) ---
    @Test
    void agregarUbicacionAIncidente_shouldReturnOk_whenSuccess() throws Exception {
        String ubicacionJson = "{\"calle\": \"Av. Principal\", \"coordenadas\": \"-33,-70\"}";
        // Incidente con 5 FKs
        Incidente incidenteConUbicacion = new Incidente(idExistente, "Incidente con Dir","Descripcion",LocalDateTime.now(),new TipoIncidente(),1,1,1,1);

        when(incidenteService.agregarUbicacionAIncidente(eq(idExistente), eq(ubicacionJson)))
                .thenReturn(incidenteConUbicacion);

        mockMvc.perform(post(BASE_URL + "/{incidenteId}/agregar-ubicacion", idExistente)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(ubicacionJson))
                .andExpect(status().isOk()) // 200 OK
                .andExpect(jsonPath("$.titulo").value("Incidente con Dir"));

        verify(incidenteService, times(1)).agregarUbicacionAIncidente(eq(idExistente), eq(ubicacionJson));
    }

    @Test
    void agregarUbicacionAIncidente_shouldReturnNotFound_whenIncidenteDoesNotExist() throws Exception {
        String ubicacionJson = "{}";
        when(incidenteService.agregarUbicacionAIncidente(eq(idNoExistente), anyString()))
                .thenThrow(new NoSuchElementException());

        mockMvc.perform(post(BASE_URL + "/{incidenteId}/agregar-ubicacion", idNoExistente)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(ubicacionJson))
                .andExpect(status().isNotFound()) // 404 Not Found
                .andExpect(content().string("Incidente no encontrado."));

        verify(incidenteService, times(1)).agregarUbicacionAIncidente(eq(idNoExistente), anyString());
    }

    @Test
    void agregarUbicacionAIncidente_shouldReturnBadRequest_whenServiceFails() throws Exception {
        String ubicacionJson = "{\"calle\": \"\"}";
        String errorMsg = "Error en el microservicio de Geolocalización";
        when(incidenteService.agregarUbicacionAIncidente(eq(idExistente), anyString()))
                .thenThrow(new RuntimeException(errorMsg));

        mockMvc.perform(post(BASE_URL + "/{incidenteId}/agregar-ubicacion", idExistente)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(ubicacionJson))
                .andExpect(status().isBadRequest()) // 400 Bad Request
                .andExpect(content().string("Error al crear o asignar la dirección: " + errorMsg));

        verify(incidenteService, times(1)).agregarUbicacionAIncidente(eq(idExistente), anyString());
    }

    // --- POST /asignar-direccion/{direccionId} ---
    @Test
    void asignarDireccion_shouldReturnOk_whenSuccess() throws Exception {
        doNothing().when(incidenteService).asignarDireccion(idExistente, relatedId);

        mockMvc.perform(post(BASE_URL + "/{incidenteId}/asignar-direccion/{direccionId}", idExistente, relatedId)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().string("DireccionDTO asignada al incidente exitosamente"));

        verify(incidenteService, times(1)).asignarDireccion(idExistente, relatedId);
    }

    @Test
    void asignarDireccion_shouldReturnNotFound_whenReferenceNotFound() throws Exception {
        String errorMsg = "Direccion no encontrada";
        doThrow(new RuntimeException(errorMsg)).when(incidenteService).asignarDireccion(idExistente, invalidId);

        mockMvc.perform(post(BASE_URL + "/{incidenteId}/asignar-direccion/{direccionId}", idExistente, invalidId)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound()) // 404 Not Found
                .andExpect(content().string(errorMsg));

        verify(incidenteService, times(1)).asignarDireccion(idExistente, invalidId);
    }
}