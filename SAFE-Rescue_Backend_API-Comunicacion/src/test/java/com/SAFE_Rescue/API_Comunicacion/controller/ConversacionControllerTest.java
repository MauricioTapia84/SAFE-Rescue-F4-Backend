package com.SAFE_Rescue.API_Comunicacion.controller;

import com.SAFE_Rescue.API_Comunicacion.modelo.Conversacion;
import com.SAFE_Rescue.API_Comunicacion.modelo.Mensaje; // Se asume que esta clase existe
import com.SAFE_Rescue.API_Comunicacion.service.ConversacionService;
import com.SAFE_Rescue.API_Comunicacion.service.MensajeService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.NoSuchElementException;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Pruebas unitarias para el ConversacionController.
 * Enfocado en verificar las interacciones HTTP, códigos de respuesta,
 * y el correcto manejo de excepciones y servicios.
 */
@WebMvcTest(ConversacionController.class)
public class ConversacionControllerTest {

    private static final String API_BASE_URL = "/api-comunicaciones/v1/conversaciones";
    private static final Integer ID_CONVERSACION_VALIDA = 100;
    private static final String TIPO_CONVERSACION = "Emergencia";
    private static final String NOMBRE_CONVERSACION = "Incidente #45";

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    // Mock del servicio de Conversación
    @MockitoBean
    private ConversacionService conversacionService;

    // Mock del servicio de Mensaje
    @MockitoBean
    private MensajeService mensajeService;

    // Objetos de prueba
    private Conversacion conversacionMock;
    private Mensaje mensajeMock;

    /**
     * Helper: Crea un objeto mock que simula la entidad Conversacion.
     */
    private Conversacion createConversacionMock() {
        // Usamos Mockito para simular la entidad si no tenemos la clase completa
        Conversacion mock = mock(Conversacion.class);
        when(mock.getIdConversacion()).thenReturn(ID_CONVERSACION_VALIDA);
        when(mock.getTipo()).thenReturn(TIPO_CONVERSACION);
        when(mock.getNombre()).thenReturn(NOMBRE_CONVERSACION);
        when(mock.getFechaCreacion()).thenReturn(LocalDateTime.now());
        // Simulación para el @RequestBody en el POST
        when(mock.getTipo()).thenReturn(TIPO_CONVERSACION);
        when(mock.getNombre()).thenReturn(NOMBRE_CONVERSACION);
        return mock;
    }

    /**
     * Helper: Crea un objeto mock que simula la entidad Mensaje.
     */
    private Mensaje createMensajeMock(String contenido) {
        Mensaje mock = mock(Mensaje.class);
        when(mock.getIdMensaje()).thenReturn(1);
        when(mock.getDetalle()).thenReturn(contenido);
        when(mock.getFechaCreacion()).thenReturn(LocalDateTime.now());
        return mock;
    }

    @BeforeEach
    void setUp() {
        conversacionMock = createConversacionMock();
        mensajeMock = createMensajeMock("Primer mensaje de prueba.");
    }

    // =========================================================================
    // POST /conversaciones - Iniciar Conversación
    // =========================================================================

    @Test
    @DisplayName("POST / - Debe iniciar una conversación y retornar 201 CREATED")
    void iniciarConversacion_success() throws Exception {
        // Objeto de la solicitud (simulando que el cliente envía un cuerpo con tipo y nombre)
        Conversacion requestBody = createConversacionMock(); // Usamos el mock como cuerpo

        // Configuración del mock de servicio: devuelve la conversación con el ID generado
        when(conversacionService.iniciarNuevaConversacion(TIPO_CONVERSACION, NOMBRE_CONVERSACION))
                .thenReturn(conversacionMock);

        mockMvc.perform(post(API_BASE_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestBody)))
                .andExpect(status().isCreated()) // Espera código 201
                .andExpect(jsonPath("$.idConversacion").value(ID_CONVERSACION_VALIDA))
                .andExpect(jsonPath("$.tipo").value(TIPO_CONVERSACION));

        verify(conversacionService, times(1)).iniciarNuevaConversacion(anyString(), anyString());
    }

    @Test
    @DisplayName("POST / - Debe retornar 400 BAD REQUEST si el servicio lanza IllegalArgumentException")
    void iniciarConversacion_badRequest() throws Exception {
        Conversacion requestBody = createConversacionMock();

        // Simula que el servicio falla por argumento inválido (ej. tipo nulo/vacío)
        when(conversacionService.iniciarNuevaConversacion(anyString(), anyString()))
                .thenThrow(new IllegalArgumentException("El tipo de conversación es obligatorio."));

        mockMvc.perform(post(API_BASE_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestBody)))
                .andExpect(status().isBadRequest()) // Espera código 400
                .andExpect(content().string("Solicitud inválida: El tipo de conversación es obligatorio."));
    }

    // =========================================================================
    // GET /conversaciones - Obtener Todas
    // =========================================================================

    @Test
    @DisplayName("GET / - Debe obtener todas las conversaciones y retornar 200 OK")
    void obtenerTodasLasConversaciones_success() throws Exception {
        List<Conversacion> lista = List.of(conversacionMock, createConversacionMock());

        when(conversacionService.findAll()).thenReturn(lista);

        mockMvc.perform(get(API_BASE_URL)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk()) // Espera código 200
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].tipo").value(TIPO_CONVERSACION));

        verify(conversacionService, times(1)).findAll();
    }

    // =========================================================================
    // GET /{idConversacion} - Obtener por ID
    // =========================================================================

    @Test
    @DisplayName("GET /{id} - Debe obtener la conversación por ID y retornar 200 OK")
    void obtenerConversacionPorId_success() throws Exception {
        when(conversacionService.findById(ID_CONVERSACION_VALIDA)).thenReturn(conversacionMock);

        mockMvc.perform(get(API_BASE_URL + "/{id}", ID_CONVERSACION_VALIDA)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk()) // Espera código 200
                .andExpect(jsonPath("$.idConversacion").value(ID_CONVERSACION_VALIDA));

        verify(conversacionService, times(1)).findById(ID_CONVERSACION_VALIDA);
    }

    @Test
    @DisplayName("GET /{id} - Debe retornar 404 NOT FOUND si la conversación no existe")
    void obtenerConversacionPorId_notFound() throws Exception {
        // Simula la excepción que se maneja en el @ExceptionHandler del controlador
        when(conversacionService.findById(ID_CONVERSACION_VALIDA))
                .thenThrow(new NoSuchElementException("Conversación no encontrada."));

        mockMvc.perform(get(API_BASE_URL + "/{id}", ID_CONVERSACION_VALIDA)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound()) // Espera código 404
                .andExpect(content().string("Recurso no encontrado: Conversación no encontrada."));

        verify(conversacionService, times(1)).findById(ID_CONVERSACION_VALIDA);
    }

    // =========================================================================
    // GET /{idConversacion}/mensajes - Obtener Mensajes
    // =========================================================================

    @Test
    @DisplayName("GET /{id}/mensajes - Debe obtener los mensajes y retornar 200 OK")
    void obtenerMensajesPorConversacion_success() throws Exception {
        List<Mensaje> mensajes = List.of(mensajeMock, createMensajeMock("Segundo mensaje."));

        when(mensajeService.findMessagesByConversation(ID_CONVERSACION_VALIDA)).thenReturn(mensajes);

        mockMvc.perform(get(API_BASE_URL + "/{id}/mensajes", ID_CONVERSACION_VALIDA)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk()) // Espera código 200
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].detalle").value(mensajeMock.getDetalle()));

        verify(mensajeService, times(1)).findMessagesByConversation(ID_CONVERSACION_VALIDA);
    }

    @Test
    @DisplayName("GET /{id}/mensajes - Debe retornar 404 NOT FOUND si la conversación no existe")
    void obtenerMensajesPorConversacion_conversationNotFound() throws Exception {
        // Simula la excepción lanzada por el servicio (ej. al validar que la conversación existe)
        when(mensajeService.findMessagesByConversation(ID_CONVERSACION_VALIDA))
                .thenThrow(new NoSuchElementException("Conversación no encontrada para mensajes."));

        mockMvc.perform(get(API_BASE_URL + "/{id}/mensajes", ID_CONVERSACION_VALIDA)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound()) // Espera código 404
                .andExpect(content().string("Recurso no encontrado: Conversación no encontrada para mensajes."));

        verify(mensajeService, times(1)).findMessagesByConversation(ID_CONVERSACION_VALIDA);
    }

    // =========================================================================
    // DELETE /{idConversacion} - Eliminar Conversación
    // =========================================================================

    @Test
    @DisplayName("DELETE /{id} - Debe eliminar la conversación y retornar 204 NO CONTENT")
    void eliminarConversacion_success() throws Exception {
        // Simula que la eliminación es exitosa
        doNothing().when(conversacionService).delete(ID_CONVERSACION_VALIDA);

        mockMvc.perform(delete(API_BASE_URL + "/{id}", ID_CONVERSACION_VALIDA))
                .andExpect(status().isNoContent()) // Espera código 204
                .andExpect(content().string("")); // El cuerpo debe estar vacío

        verify(conversacionService, times(1)).delete(ID_CONVERSACION_VALIDA);
    }

    @Test
    @DisplayName("DELETE /{id} - Debe retornar 404 NOT FOUND si la conversación a eliminar no existe")
    void eliminarConversacion_notFound() throws Exception {
        // Simula la excepción lanzada por el servicio al intentar eliminar
        doThrow(new NoSuchElementException("Conversación a eliminar no existe."))
                .when(conversacionService).delete(ID_CONVERSACION_VALIDA);

        mockMvc.perform(delete(API_BASE_URL + "/{id}", ID_CONVERSACION_VALIDA))
                .andExpect(status().isNotFound()) // Espera código 404
                .andExpect(content().string("Recurso no encontrado: Conversación a eliminar no existe."));

        verify(conversacionService, times(1)).delete(ID_CONVERSACION_VALIDA);
    }
}