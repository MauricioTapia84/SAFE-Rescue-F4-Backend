package com.SAFE_Rescue.API_Comunicacion.controller;

import com.SAFE_Rescue.API_Comunicacion.modelo.Conversacion;
import com.SAFE_Rescue.API_Comunicacion.modelo.Mensaje;
import com.SAFE_Rescue.API_Comunicacion.dto.MensajeCreacionDTO;
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
import java.util.List;
import java.util.NoSuchElementException;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Clase de prueba para MensajeController.
 * Usa @WebMvcTest para configurar solo la capa del controlador.
 */
@WebMvcTest(MensajeController.class)
public class MensajeControllerTest {

    @Autowired
    private MockMvc mockMvc; // Objeto para simular las peticiones HTTP

    @Autowired
    private ObjectMapper objectMapper; // Utilidad para convertir objetos Java a JSON

    @MockitoBean
    private MensajeService mensajeService; // Mock del servicio inyectado en el controlador

    // Constantes y objetos de prueba
    private final Integer ID_CONV = 10;
    private final Integer ID_USER = 101;
    private final Integer ID_MENSAJE = 1;
    private final String BASE_URL = "/api-comunicaciones/v1";
    private MensajeCreacionDTO creacionDTO;
    private Mensaje mensajeMock;

    @BeforeEach
    void setUp() {
        // Inicializar DTO de entrada
        creacionDTO = new MensajeCreacionDTO();
        creacionDTO.setDetalle("¡Ayuda!");
        creacionDTO.setIdUsuarioEmisor(ID_USER);
        creacionDTO.setIdEstado(1); // ENVIADO

        // Inicializar Mensaje que sería devuelto por el Servicio
        mensajeMock = new Mensaje(ID_MENSAJE,LocalDateTime.now(),"Hola", new Conversacion(), ID_USER,1);
        mensajeMock.setDetalle(creacionDTO.getDetalle());
        mensajeMock.setIdEstado(creacionDTO.getIdEstado());
    }

    // =========================================================================
    // POST /conversaciones/{conversacionId}/mensajes (Creación)
    // =========================================================================

    @Test
    @DisplayName("POST /mensajes - Debe crear un mensaje y retornar 201 CREATED")
    void crearMensajeEnConversacion_success() throws Exception {
        // Simular el comportamiento del servicio: debe devolver el mensaje mock
        when(mensajeService.createMessage(eq(ID_CONV), any(Mensaje.class))).thenReturn(mensajeMock);

        // Ejecutar la petición y verificar
        mockMvc.perform(post(BASE_URL + "/conversaciones/{conversacionId}/mensajes", ID_CONV)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(creacionDTO))) // Convertir DTO a JSON

                // 1. Verificar el estado HTTP
                .andExpect(status().isCreated()) // 201 Created

                // 2. Verificar el contenido JSON devuelto
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.idMensaje").value(ID_MENSAJE))
                .andExpect(jsonPath("$.idUsuarioEmisor").value(ID_USER))
                .andExpect(jsonPath("$.detalle").value("¡Ayuda!"));

        // 3. Verificar la interacción con el servicio
        verify(mensajeService, times(1)).createMessage(eq(ID_CONV), any(Mensaje.class));
    }

    @Test
    @DisplayName("POST /mensajes - Debe retornar 404 NOT FOUND si la conversación no existe")
    void crearMensajeEnConversacion_conversationNotFound() throws Exception {
        // Simular que el servicio lanza NoSuchElementException (Controlador maneja esto a 404)
        when(mensajeService.createMessage(eq(ID_CONV), any(Mensaje.class)))
                .thenThrow(new NoSuchElementException("Conversación no encontrada"));

        // Ejecutar la petición y verificar
        mockMvc.perform(post(BASE_URL + "/conversaciones/{conversacionId}/mensajes", ID_CONV)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(creacionDTO)))

                // 1. Verificar el estado HTTP
                .andExpect(status().isNotFound()) // 404 Not Found

                // 2. Verificar el cuerpo de la respuesta de la excepción
                .andExpect(content().string("Recurso no encontrado: Conversación no encontrada"));

        // 3. Verificar la interacción con el servicio
        verify(mensajeService, times(1)).createMessage(eq(ID_CONV), any(Mensaje.class));
    }

    @Test
    @DisplayName("POST /mensajes - Debe retornar 400 BAD REQUEST si el emisor no existe (IllegalArgumentException)")
    void crearMensajeEnConversacion_badRequest() throws Exception {
        // Simular que el servicio lanza IllegalArgumentException (Controlador maneja esto a 400)
        when(mensajeService.createMessage(eq(ID_CONV), any(Mensaje.class)))
                .thenThrow(new IllegalArgumentException("Emisor ID 101 no encontrado"));

        // Ejecutar la petición y verificar
        mockMvc.perform(post(BASE_URL + "/conversaciones/{conversacionId}/mensajes", ID_CONV)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(creacionDTO)))

                // 1. Verificar el estado HTTP
                .andExpect(status().isBadRequest()) // 400 Bad Request

                // 2. Verificar el cuerpo de la respuesta de la excepción
                .andExpect(content().string("Solicitud inválida: Emisor ID 101 no encontrado"));

        // 3. Verificar la interacción con el servicio
        verify(mensajeService, times(1)).createMessage(eq(ID_CONV), any(Mensaje.class));
    }


    // =========================================================================
    // GET /mensajes (Consulta General)
    // =========================================================================

    @Test
    @DisplayName("GET /mensajes - Debe retornar todos los mensajes y 200 OK")
    void obtenerTodosLosMensajes_success() throws Exception {
        // Preparar datos de prueba
        Mensaje msg1 = new Mensaje(1,LocalDateTime.now(),"Hi",new Conversacion(),10, 101);
        Mensaje msg2 = new Mensaje(2,LocalDateTime.now(),"Hola 2",new Conversacion(), 10, 102);
        List<Mensaje> mensajes = List.of(msg1, msg2);

        // Simular el comportamiento del servicio
        when(mensajeService.findAll()).thenReturn(mensajes);

        // Ejecutar la petición y verificar
        mockMvc.perform(get(BASE_URL + "/mensajes")
                        .contentType(MediaType.APPLICATION_JSON))

                // 1. Verificar el estado HTTP
                .andExpect(status().isOk()) // 200 OK

                // 2. Verificar el contenido JSON devuelto
                .andExpect(jsonPath("$.size()").value(2))
                .andExpect(jsonPath("$[0].idMensaje").value(1))
                .andExpect(jsonPath("$[1].idMensaje").value(2));

        // 3. Verificar la interacción con el servicio
        verify(mensajeService, times(1)).findAll();
    }

    // =========================================================================
    // GET /mensajes/{idMensaje} (Consulta por ID)
    // =========================================================================

    @Test
    @DisplayName("GET /mensajes/{id} - Debe retornar el mensaje solicitado y 200 OK")
    void obtenerMensajePorId_success() throws Exception {
        // Simular el comportamiento del servicio
        when(mensajeService.findById(ID_MENSAJE)).thenReturn(mensajeMock);

        // Ejecutar la petición y verificar
        mockMvc.perform(get(BASE_URL + "/mensajes/{idMensaje}", ID_MENSAJE)
                        .contentType(MediaType.APPLICATION_JSON))

                // 1. Verificar el estado HTTP
                .andExpect(status().isOk()) // 200 OK

                // 2. Verificar el contenido JSON
                .andExpect(jsonPath("$.idMensaje").value(ID_MENSAJE));

        // 3. Verificar la interacción con el servicio
        verify(mensajeService, times(1)).findById(ID_MENSAJE);
    }

    @Test
    @DisplayName("GET /mensajes/{id} - Debe retornar 404 NOT FOUND si el mensaje no existe")
    void obtenerMensajePorId_notFound() throws Exception {
        // Simular que el servicio lanza NoSuchElementException
        when(mensajeService.findById(ID_MENSAJE))
                .thenThrow(new NoSuchElementException("Mensaje con ID 1 no existe"));

        // Ejecutar la petición y verificar
        mockMvc.perform(get(BASE_URL + "/mensajes/{idMensaje}", ID_MENSAJE)
                        .contentType(MediaType.APPLICATION_JSON))

                // 1. Verificar el estado HTTP
                .andExpect(status().isNotFound()) // 404 Not Found

                // 2. Verificar el cuerpo de la respuesta de la excepción
                .andExpect(content().string("Recurso no encontrado: Mensaje con ID 1 no existe"));

        // 3. Verificar la interacción con el servicio
        verify(mensajeService, times(1)).findById(ID_MENSAJE);
    }

    // =========================================================================
    // DELETE /mensajes/{idMensaje} (Eliminación)
    // =========================================================================

    @Test
    @DisplayName("DELETE /mensajes/{id} - Debe eliminar el mensaje y retornar 204 NO CONTENT")
    void eliminarMensaje_success() throws Exception {
        // Simular que el servicio no hace nada (void)
        doNothing().when(mensajeService).delete(ID_MENSAJE);

        // Ejecutar la petición y verificar
        mockMvc.perform(delete(BASE_URL + "/mensajes/{idMensaje}", ID_MENSAJE)
                        .contentType(MediaType.APPLICATION_JSON))

                // 1. Verificar el estado HTTP
                .andExpect(status().isNoContent()); // 204 No Content

        // 2. Verificar la interacción con el servicio
        verify(mensajeService, times(1)).delete(ID_MENSAJE);
    }

    @Test
    @DisplayName("DELETE /mensajes/{id} - Debe retornar 404 NOT FOUND si el mensaje a eliminar no existe")
    void eliminarMensaje_notFound() throws Exception {
        // Simular que el servicio lanza NoSuchElementException al intentar eliminar
        doThrow(new NoSuchElementException("Mensaje a eliminar no existe"))
                .when(mensajeService).delete(ID_MENSAJE);

        // Ejecutar la petición y verificar
        mockMvc.perform(delete(BASE_URL + "/mensajes/{idMensaje}", ID_MENSAJE)
                        .contentType(MediaType.APPLICATION_JSON))

                // 1. Verificar el estado HTTP
                .andExpect(status().isNotFound()) // 404 Not Found

                // 2. Verificar el cuerpo de la respuesta
                .andExpect(content().string("Recurso no encontrado: Mensaje a eliminar no existe"));

        // 3. Verificar la interacción con el servicio
        verify(mensajeService, times(1)).delete(ID_MENSAJE);
    }
}