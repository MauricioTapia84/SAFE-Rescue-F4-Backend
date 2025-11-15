package com.SAFE_Rescue.API_Comunicacion.controller;

import com.SAFE_Rescue.API_Comunicacion.modelo.Conversacion;
import com.SAFE_Rescue.API_Comunicacion.modelo.Notificacion;
import com.SAFE_Rescue.API_Comunicacion.modelo.NotificacionCreacionDTO;
import com.SAFE_Rescue.API_Comunicacion.service.ConversacionService;
import com.SAFE_Rescue.API_Comunicacion.service.NotificacionService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
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
 * Pruebas unitarias para NotificacionController.
 */
@WebMvcTest(NotificacionController.class)
public class NotificacionControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    // Inyección de mocks para los servicios
    @MockitoBean
    private NotificacionService notificacionService;

    @MockitoBean
    private ConversacionService conversacionService;

    // Constantes y objetos de prueba
    private final String BASE_URL = "/api-comunicaciones/v1";
    private final Integer ID_NOTIFICACION = 100;
    private final Integer ID_CONV = 5;
    private final String ID_RECEPTOR = "user-abc-123";
    private NotificacionCreacionDTO creacionDTO;
    private Notificacion notificacionMock;

    @BeforeEach
    void setUp() {
        // Inicializar DTO de entrada (con conversación vinculada)
        creacionDTO = new NotificacionCreacionDTO();
        creacionDTO.setIdUsuarioReceptor(ID_RECEPTOR);
        creacionDTO.setDetalle("Nuevo mensaje en conversación");
        creacionDTO.setIdConversacion(ID_CONV);

        // Inicializar Notificación de salida
        notificacionMock = new Notificacion(ID_NOTIFICACION,LocalDateTime.now(),"Emergencia",null, ID_RECEPTOR, 1);
    }

    // =========================================================================
    // POST /notificaciones (Creación)
    // =========================================================================

    @Test
    @DisplayName("POST /notificaciones - Debe crear una notificación con conversación y retornar 201 CREATED")
    void crearNotificacion_withConversation_success() throws Exception {
        // 1. Simular la validación de la conversación
        Conversacion conversacionMock = new Conversacion(ID_CONV,"privado",null, LocalDateTime.now());
        when(conversacionService.findById(ID_CONV)).thenReturn(conversacionMock);

        // 2. Simular la creación de la notificación
        when(notificacionService.crearNotificacion(
                eq(ID_RECEPTOR),
                eq(creacionDTO.getDetalle()),
                eq(conversacionMock) // Debe recibir la Conversacion mock
        )).thenReturn(notificacionMock);

        // Ejecutar y verificar
        mockMvc.perform(post(BASE_URL + "/notificaciones")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(creacionDTO)))

                .andExpect(status().isCreated()) // 201
                .andExpect(jsonPath("$.idNotificacion").value(ID_NOTIFICACION));

        // Verificar interacciones
        verify(conversacionService, times(1)).findById(ID_CONV);
        verify(notificacionService, times(1)).crearNotificacion(any(), any(), any());
    }

    @Test
    @DisplayName("POST /notificaciones - Debe crear una notificación sin conversación (Conversacion es null)")
    void crearNotificacion_withoutConversation_success() throws Exception {
        // Modificar DTO para que no tenga ID de conversación
        creacionDTO.setIdConversacion(null);

        // Simular la creación de la notificación con null como tercer argumento
        when(notificacionService.crearNotificacion(
                eq(ID_RECEPTOR),
                eq(creacionDTO.getDetalle()),
                isNull() // Espera null aquí
        )).thenReturn(notificacionMock);

        // Ejecutar y verificar
        mockMvc.perform(post(BASE_URL + "/notificaciones")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(creacionDTO)))

                .andExpect(status().isCreated());

        // Verificar interacciones
        verify(conversacionService, never()).findById(any());
        verify(notificacionService, times(1)).crearNotificacion(any(), any(), isNull());
    }

    @Test
    @DisplayName("POST /notificaciones - Debe retornar 404 NOT FOUND si la Conversación referenciada no existe")
    void crearNotificacion_conversationNotFound() throws Exception {
        // Simular que el servicio lanza NoSuchElementException
        when(conversacionService.findById(ID_CONV))
                .thenThrow(new NoSuchElementException("Conversación ID 5 no encontrada."));

        // Ejecutar y verificar
        mockMvc.perform(post(BASE_URL + "/notificaciones")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(creacionDTO)))

                .andExpect(status().isNotFound()) // 404
                .andExpect(content().string("Recurso no encontrado: Conversación ID 5 no encontrada."));
    }

    @Test
    @DisplayName("POST /notificaciones - Debe retornar 400 BAD REQUEST si falta un campo requerido (ej. idUsuarioReceptor es null)")
    void crearNotificacion_validationError() throws Exception {
        // Nota: Asume que la validación @Valid en el DTO falla si el campo es nulo
        NotificacionCreacionDTO invalidDto = new NotificacionCreacionDTO();
        invalidDto.setDetalle("Detalle sin receptor");

        when(notificacionService.crearNotificacion(
                isNull(), // Simular que el DTO mapea a un valor nulo para el servicio
                any(),
                any()
        )).thenThrow(new IllegalArgumentException("ID de usuario receptor es obligatorio."));

        // Modificamos el DTO para simular un error que el servicio atraparía
        creacionDTO.setIdUsuarioReceptor(null);

        mockMvc.perform(post(BASE_URL + "/notificaciones")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(creacionDTO)))

                // En este caso, si el servicio lanza IllegalArgumentException
                .andExpect(status().isBadRequest()) // 400
                .andExpect(content().string("Solicitud inválida: ID de usuario receptor es obligatorio."));
    }


    // =========================================================================
    // GET /notificaciones/usuario/{idUsuarioReceptor}/pendientes (Consulta Paginada)
    // =========================================================================

    @Test
    @DisplayName("GET /pendientes - Debe retornar notificaciones pendientes paginadas y 200 OK")
    void obtenerNotificacionesPendientes_success() throws Exception {
        // Preparar Page mock
        List<Notificacion> content = List.of(
                new Notificacion(1,LocalDateTime.now(),"Hola",null, ID_RECEPTOR, 1),
                new Notificacion(2,LocalDateTime.now(),"Hi",null, ID_RECEPTOR, 1)
        );
        Page<Notificacion> pageMock = new PageImpl<>(content, PageRequest.of(0, 10), 5); // Total 5 elementos

        // Simular el comportamiento del servicio
        when(notificacionService.cargarNotificacionesPendientes(eq(ID_RECEPTOR), any())).thenReturn(pageMock);

        // Ejecutar y verificar
        mockMvc.perform(get(BASE_URL + "/notificaciones/usuario/{idUsuarioReceptor}/pendientes", ID_RECEPTOR)
                        .param("page", "0")
                        .param("size", "10")
                        .contentType(MediaType.APPLICATION_JSON))

                .andExpect(status().isOk()) // 200
                // Verificar que se devuelven los datos de paginación
                .andExpect(jsonPath("$.content.size()").value(2))
                .andExpect(jsonPath("$.totalPages").value(1)) // 5 elementos / 10 por página = 1 página
                .andExpect(jsonPath("$.totalElements").value(5));

        // Verificar interacciones
        verify(notificacionService, times(1)).cargarNotificacionesPendientes(eq(ID_RECEPTOR), any());
    }

    // =========================================================================
    // PATCH /notificaciones/{idNotificacion}/leida (Marcar individual)
    // =========================================================================

    @Test
    @DisplayName("PATCH /leida - Debe marcar una notificación como leída y retornar 200 OK")
    void marcarNotificacionComoLeida_success() throws Exception {
        // Notificación de salida marcada como leída (estado 9)
        Notificacion leidaMock = new Notificacion(ID_NOTIFICACION,LocalDateTime.now(),"casa",new Conversacion(), ID_RECEPTOR, 9);

        // Simular el comportamiento del servicio
        when(notificacionService.marcarComoLeida(ID_NOTIFICACION)).thenReturn(leidaMock);

        // Ejecutar y verificar
        mockMvc.perform(patch(BASE_URL + "/notificaciones/{idNotificacion}/leida", ID_NOTIFICACION)
                        .contentType(MediaType.APPLICATION_JSON))

                .andExpect(status().isOk()) // 200
                .andExpect(jsonPath("$.idNotificacion").value(ID_NOTIFICACION))
                .andExpect(jsonPath("$.idEstado").value(9)); // Estado Leída

        // Verificar interacciones
        verify(notificacionService, times(1)).marcarComoLeida(ID_NOTIFICACION);
    }

    @Test
    @DisplayName("PATCH /leida - Debe retornar 404 NOT FOUND si la notificación a marcar no existe")
    void marcarNotificacionComoLeida_notFound() throws Exception {
        // Simular excepción
        when(notificacionService.marcarComoLeida(ID_NOTIFICACION))
                .thenThrow(new NoSuchElementException("Notificación ID 100 no existe."));

        // Ejecutar y verificar
        mockMvc.perform(patch(BASE_URL + "/notificaciones/{idNotificacion}/leida", ID_NOTIFICACION)
                        .contentType(MediaType.APPLICATION_JSON))

                .andExpect(status().isNotFound()) // 404
                .andExpect(content().string("Recurso no encontrado: Notificación ID 100 no existe."));
    }

    // =========================================================================
    // PATCH /notificaciones/usuario/{idUsuarioReceptor}/leidas (Marcar todas)
    // =========================================================================

    @Test
    @DisplayName("PATCH /leidas - Debe marcar todas las notificaciones pendientes de un usuario y retornar 200 OK")
    void marcarTodasNotificacionesComoLeidas_success() throws Exception {
        final int count = 3; // Simular que se marcaron 3 notificaciones

        // Simular el comportamiento del servicio
        when(notificacionService.marcarTodasComoLeidas(ID_RECEPTOR)).thenReturn(count);

        // Ejecutar y verificar
        mockMvc.perform(patch(BASE_URL + "/notificaciones/usuario/{idUsuarioReceptor}/leidas", ID_RECEPTOR)
                        .contentType(MediaType.APPLICATION_JSON))

                .andExpect(status().isOk()) // 200
                .andExpect(content().string("Se marcaron 3 notificaciones como leídas para el usuario " + ID_RECEPTOR));

        // Verificar interacciones
        verify(notificacionService, times(1)).marcarTodasComoLeidas(ID_RECEPTOR);
    }

    @Test
    @DisplayName("PATCH /leidas - Debe retornar 500 INTERNAL SERVER ERROR en caso de fallo del servicio (RuntimeException)")
    void marcarTodasNotificacionesComoLeidas_runtimeError() throws Exception {
        // Simular un fallo interno (ej. fallo de comunicación con otro microservicio)
        when(notificacionService.marcarTodasComoLeidas(ID_RECEPTOR))
                .thenThrow(new RuntimeException("Fallo al comunicarse con el servicio de Estado."));

        // Ejecutar y verificar
        mockMvc.perform(patch(BASE_URL + "/notificaciones/usuario/{idUsuarioReceptor}/leidas", ID_RECEPTOR)
                        .contentType(MediaType.APPLICATION_JSON))

                .andExpect(status().isInternalServerError()) // 500
                .andExpect(content().string("Error interno del servidor o fallo de comunicación con microservicios: Fallo al comunicarse con el servicio de Estado."));
    }
}