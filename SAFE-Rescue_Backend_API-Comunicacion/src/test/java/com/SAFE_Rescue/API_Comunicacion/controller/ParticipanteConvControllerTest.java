package com.SAFE_Rescue.API_Comunicacion.controller;

import com.SAFE_Rescue.API_Comunicacion.modelo.Conversacion;
import com.SAFE_Rescue.API_Comunicacion.modelo.ParticipanteConversacion;

import com.SAFE_Rescue.API_Comunicacion.service.ParticipanteConvService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.NoSuchElementException;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Clase de prueba unitaria para ParticipanteConvController.
 */
@WebMvcTest(ParticipanteConvController.class)
class ParticipanteConvControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private ParticipanteConvService participanteConvService;

    private final Integer ID_CONVERSACION = 1;
    private final Integer ID_USUARIO = 8;
    private ParticipanteConversacion participanteMock;

    @BeforeEach
    void setUp() {
        participanteMock = new ParticipanteConversacion(1,  ID_USUARIO, new Conversacion(),LocalDateTime.now());
    }

    // =========================================================================
    // POST ENDPOINT TESTS (unirParticipante)
    // =========================================================================

    @Test
    void testUnirParticipante_DebeRetornarCreated() throws Exception {
        // Configura el mock para retornar el objeto real de la clase ParticipanteConversacion
        when(participanteConvService.unirParticipanteAConversacion(ID_CONVERSACION, ID_USUARIO))
                .thenReturn(participanteMock); // <-- ¡Ahora el tipo coincide!

        // Realiza la petición POST y verifica la respuesta
        mockMvc.perform(post("/api-comunicaciones/v1/participantes/{idConversacion}/usuario/{idUsuario}",
                        ID_CONVERSACION, ID_USUARIO)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isCreated())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.idParticipanteConv").value(ID_CONVERSACION))
                .andExpect(jsonPath("$.idUsuario").value(ID_USUARIO));

        verify(participanteConvService, times(1)).unirParticipanteAConversacion(ID_CONVERSACION, ID_USUARIO);
    }

    // =========================================================================
    // GET ENDPOINTS TESTS
    // =========================================================================

    @Test
    void testObtenerParticipantesPorConversacion_DebeRetornarOkConLista() throws Exception {
        // Configura la lista de participantes simulados
        List<ParticipanteConversacion> listaMock = Arrays.asList(
                participanteMock,
                new ParticipanteConversacion()
        );

        // Configura el mock del servicio
        when(participanteConvService.findParticipantesByConversacion(ID_CONVERSACION))
                .thenReturn(listaMock);

        // Realiza la petición GET y verifica la respuesta
        mockMvc.perform(get("/api-comunicaciones/v1/participantes/conversacion/{idConversacion}", ID_CONVERSACION)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(listaMock.size()))
                .andExpect(jsonPath("$[0].idUsuario").value(ID_USUARIO));

        verify(participanteConvService, times(1)).findParticipantesByConversacion(ID_CONVERSACION);
    }

    @Test
    void testObtenerConversacionesPorUsuario_DebeRetornarOkConLista() throws Exception {
        // Configura la lista de conversaciones simuladas para un usuario
        List<ParticipanteConversacion> listaMock = Arrays.asList(
                new ParticipanteConversacion(1002, ID_USUARIO,new Conversacion(),LocalDateTime.now()),
                new ParticipanteConversacion(1,ID_USUARIO,new Conversacion(),LocalDateTime.now())
        );

        // Configura el mock del servicio
        when(participanteConvService.findConversacionesByParticipante(ID_USUARIO))
                .thenReturn(listaMock);

        // Realiza la petición GET y verifica la respuesta
        mockMvc.perform(get("/api-comunicaciones/v1/participantes/usuario/{idUsuario}", ID_USUARIO)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(listaMock.size()))
                .andExpect(jsonPath("$[1].idUsuario").value(8));

        verify(participanteConvService, times(1)).findConversacionesByParticipante(ID_USUARIO);
    }

    // =========================================================================
    // DELETE ENDPOINT TESTS (eliminarParticipante)
    // =========================================================================

    @Test
    void testEliminarParticipante_DebeRetornarNoContent() throws Exception {
        // Configura el mock del servicio para que no haga nada al llamar al método
        doNothing().when(participanteConvService).eliminarParticipanteDeConversacion(ID_CONVERSACION, ID_USUARIO);

        // Realiza la petición DELETE y verifica la respuesta
        mockMvc.perform(delete("/api-comunicaciones/v1/participantes/{idConversacion}/usuario/{idUsuario}",
                        ID_CONVERSACION, ID_USUARIO)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNoContent())
                .andExpect(content().string(""));

        verify(participanteConvService, times(1)).eliminarParticipanteDeConversacion(ID_CONVERSACION, ID_USUARIO);
    }

    // =========================================================================
    // EXCEPTION HANDLING TESTS
    // =========================================================================

    @Test
    void testManejoExcepcion_NoSuchElementException_DebeRetornarNotFound() throws Exception {
        String mensajeError = "Conversación o usuario no encontrado.";

        // Simula la excepción para el método DELETE
        doThrow(new NoSuchElementException(mensajeError))
                .when(participanteConvService).eliminarParticipanteDeConversacion(ID_CONVERSACION, ID_USUARIO);

        // Realiza la petición DELETE y espera el manejo de la excepción
        mockMvc.perform(delete("/api-comunicaciones/v1/participantes/{idConversacion}/usuario/{idUsuario}",
                        ID_CONVERSACION, ID_USUARIO))
                .andExpect(status().isNotFound())
                .andExpect(content().string("Recurso no encontrado: " + mensajeError));
    }

    @Test
    void testManejoExcepcion_IllegalStateException_DebeRetornarConflict() throws Exception {
        String mensajeError = "El usuario ya es un participante de esta conversación.";

        // Simula la excepción para el método POST (unirParticipante)
        when(participanteConvService.unirParticipanteAConversacion(ID_CONVERSACION, ID_USUARIO))
                .thenThrow(new IllegalStateException(mensajeError));

        // Realiza la petición POST y espera el manejo de la excepción
        mockMvc.perform(post("/api-comunicaciones/v1/participantes/{idConversacion}/usuario/{idUsuario}",
                        ID_CONVERSACION, ID_USUARIO))
                .andExpect(status().isConflict())
                .andExpect(content().string("Conflicto de estado: " + mensajeError));
    }

    @Test
    void testManejoExcepcion_IllegalArgumentException_DebeRetornarBadRequest() throws Exception {
        String mensajeError = "IDs de conversación o usuario inválidos.";

        // Simula la excepción para el método POST (unirParticipante)
        when(participanteConvService.unirParticipanteAConversacion(ID_CONVERSACION, ID_USUARIO))
                .thenThrow(new IllegalArgumentException(mensajeError));

        // Realiza la petición POST y espera el manejo de la excepción
        mockMvc.perform(post("/api-comunicaciones/v1/participantes/{idConversacion}/usuario/{idUsuario}",
                        ID_CONVERSACION, ID_USUARIO))
                .andExpect(status().isBadRequest())
                .andExpect(content().string("Solicitud inválida: " + mensajeError));
    }
}