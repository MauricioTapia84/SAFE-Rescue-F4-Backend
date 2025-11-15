package com.SAFE_Rescue.API_Comunicacion.service;

import com.SAFE_Rescue.API_Comunicacion.config.UsuarioClient;
import com.SAFE_Rescue.API_Comunicacion.modelo.Conversacion;
import com.SAFE_Rescue.API_Comunicacion.modelo.ParticipanteConversacion;
import com.SAFE_Rescue.API_Comunicacion.modelo.UsuarioDTO;
import com.SAFE_Rescue.API_Comunicacion.repository.ParticipanteConversacionRepository;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Pruebas unitarias para la lógica de negocio de ParticipanteConvService.
 */
@ExtendWith(MockitoExtension.class)
public class ParticipanteConvServiceTest {

    @Mock
    private ParticipanteConversacionRepository participanteConvRepository;

    @Mock
    private ConversacionService conversacionService;

    @Mock
    private UsuarioClient usuarioClient;

    @InjectMocks
    private ParticipanteConvService participanteConvService;

    // Datos de prueba
    private final Integer ID_CONV = 10;
    private final Integer ID_USER_VALIDO = 101;
    private final Integer ID_USER_DUPLICADO = 102;
    private Conversacion conversacionMock;
    private ParticipanteConversacion participanteMock;

    @BeforeEach
    void setUp() {
        conversacionMock = new Conversacion(ID_CONV, "Grupo","Grupo familiar",LocalDateTime.now());
        participanteMock = new ParticipanteConversacion(1, ID_USER_VALIDO,conversacionMock,LocalDateTime.now());
    }

    // =========================================================================
    // unirParticipanteAConversacion(Integer idConversacion, Integer idUsuario)
    // =========================================================================

    @Test
    @DisplayName("Debe unir un participante con éxito y guardar la asignación")
    void unirParticipanteAConversacion_success() {
        // 1. Simular validación de Conversación: Existe
        when(conversacionService.findById(ID_CONV)).thenReturn(conversacionMock);

        // 2. Simular validación de Usuario: Existe en el microservicio (CORREGIDO: usa new Usuario())
        when(usuarioClient.findById(ID_USER_VALIDO)).thenReturn(Optional.of(new UsuarioDTO()));

        // 3. Simular verificación de duplicado: No existe previamente
        when(participanteConvRepository.findByIdUsuarioAndConversacion_IdConversacion(ID_USER_VALIDO, ID_CONV))
                .thenReturn(Optional.empty());

        // 4. Simular el guardado en el repositorio:
        when(participanteConvRepository.save(any(ParticipanteConversacion.class))).thenReturn(participanteMock);

        // Ejecutar el método
        ParticipanteConversacion resultado = participanteConvService.unirParticipanteAConversacion(ID_CONV, ID_USER_VALIDO);

        // Verificar el resultado
        assertNotNull(resultado);
        assertEquals(ID_USER_VALIDO, resultado.getIdUsuario());
        assertEquals(ID_CONV, resultado.getConversacion().getIdConversacion());

        // Verificar las interacciones con los mocks
        verify(conversacionService, times(1)).findById(ID_CONV);
        verify(usuarioClient, times(1)).findById(ID_USER_VALIDO);
        verify(participanteConvRepository, times(1)).save(any(ParticipanteConversacion.class));
    }

    @Test
    @DisplayName("Debe lanzar NoSuchElementException si la conversación no existe")
    void unirParticipanteAConversacion_conversacionNotFound() {
        // 1. Simular validación de Conversación: Lanza excepción (NoSuchElementException)
        when(conversacionService.findById(ID_CONV))
                .thenThrow(new NoSuchElementException("Conversación no encontrada"));

        // Ejecutar y verificar la excepción
        assertThrows(NoSuchElementException.class, () -> {
            participanteConvService.unirParticipanteAConversacion(ID_CONV, ID_USER_VALIDO);
        });

        // Verificar que no se llamó a los pasos siguientes (Cliente o Repositorio)
        verify(conversacionService, times(1)).findById(ID_CONV);
        verify(usuarioClient, never()).findById(any());
        verify(participanteConvRepository, never()).save(any());
    }

    @Test
    @DisplayName("Debe lanzar IllegalArgumentException si el usuario no existe en UsuarioClient")
    void unirParticipanteAConversacion_usuarioNotFound() {
        final Integer ID_USER_NO_EXISTE = 999;

        // 1. Simular validación de Conversación: Existe
        when(conversacionService.findById(ID_CONV)).thenReturn(conversacionMock);

        // 2. Simular validación de Usuario: No existe (Optional.empty)
        when(usuarioClient.findById(ID_USER_NO_EXISTE)).thenReturn(Optional.empty());

        // Ejecutar y verificar la excepción
        IllegalArgumentException thrown = assertThrows(IllegalArgumentException.class, () -> {
            participanteConvService.unirParticipanteAConversacion(ID_CONV, ID_USER_NO_EXISTE);
        });

        // Verificar el mensaje
        assertEquals("Usuario con ID 999 no encontrado en el microservicio de Usuarios. No se puede unir a la conversación.", thrown.getMessage());

        // Verificar interacciones
        verify(conversacionService, times(1)).findById(ID_CONV);
        verify(usuarioClient, times(1)).findById(ID_USER_NO_EXISTE);
        verify(participanteConvRepository, never()).findByIdUsuarioAndConversacion_IdConversacion(any(), any());
        verify(participanteConvRepository, never()).save(any());
    }

    @Test
    @DisplayName("Debe lanzar IllegalStateException si el usuario ya está en la conversación")
    void unirParticipanteAConversacion_alreadyExists() {
        // 1. Simular validación de Conversación: Existe
        when(conversacionService.findById(ID_CONV)).thenReturn(conversacionMock);

        // 2. Simular validación de Usuario: Existe (CORREGIDO: usa new Usuario())
        when(usuarioClient.findById(ID_USER_DUPLICADO)).thenReturn(Optional.of(new UsuarioDTO()));

        // 3. Simular verificación de duplicado: Ya existe (Optional con contenido)
        ParticipanteConversacion participanteDuplicado = new ParticipanteConversacion(2,ID_USER_DUPLICADO, conversacionMock, LocalDateTime.now());
        when(participanteConvRepository.findByIdUsuarioAndConversacion_IdConversacion(ID_USER_DUPLICADO, ID_CONV))
                .thenReturn(Optional.of(participanteDuplicado));

        // Ejecutar y verificar la excepción
        IllegalStateException thrown = assertThrows(IllegalStateException.class, () -> {
            participanteConvService.unirParticipanteAConversacion(ID_CONV, ID_USER_DUPLICADO);
        });

        // Verificar el mensaje
        assertEquals("El usuario con ID 102 ya es un participante en la conversación 10", thrown.getMessage());

        // Verificar interacciones
        verify(participanteConvRepository, never()).save(any());
    }

    // =========================================================================
    // eliminarParticipanteDeConversacion(Integer idConversacion, Integer idUsuario)
    // =========================================================================

    @Test
    @DisplayName("Debe eliminar un participante con éxito")
    void eliminarParticipanteDeConversacion_success() {
        // Simular que la asignación existe
        when(participanteConvRepository.findByIdUsuarioAndConversacion_IdConversacion(ID_USER_VALIDO, ID_CONV))
                .thenReturn(Optional.of(participanteMock));

        // Simular el borrado
        doNothing().when(participanteConvRepository).deleteByIdUsuarioAndConversacion_IdConversacion(ID_USER_VALIDO, ID_CONV);

        // Ejecutar y verificar que no lanza excepción
        assertDoesNotThrow(() -> participanteConvService.eliminarParticipanteDeConversacion(ID_CONV, ID_USER_VALIDO));

        // Verificar las interacciones
        verify(participanteConvRepository, times(1))
                .findByIdUsuarioAndConversacion_IdConversacion(ID_USER_VALIDO, ID_CONV);
        verify(participanteConvRepository, times(1))
                .deleteByIdUsuarioAndConversacion_IdConversacion(ID_USER_VALIDO, ID_CONV);

        // Se verifica que el cliente de usuario y el servicio de conversación NO se llaman
        verify(conversacionService, never()).findById(any());
        verify(usuarioClient, never()).findById(any());
    }

    @Test
    @DisplayName("Debe lanzar NoSuchElementException si la asignación no existe al intentar eliminar")
    void eliminarParticipanteDeConversacion_assignmentNotFound() {
        // Simular que la asignación NO existe
        when(participanteConvRepository.findByIdUsuarioAndConversacion_IdConversacion(ID_USER_VALIDO, ID_CONV))
                .thenReturn(Optional.empty());

        // Ejecutar y verificar la excepción
        NoSuchElementException thrown = assertThrows(NoSuchElementException.class, () -> {
            participanteConvService.eliminarParticipanteDeConversacion(ID_CONV, ID_USER_VALIDO);
        });

        // Verificar el mensaje
        assertEquals("El participante con ID 101 no está asociado a la conversación 10", thrown.getMessage());

        // Verificar interacciones
        verify(participanteConvRepository, times(1))
                .findByIdUsuarioAndConversacion_IdConversacion(ID_USER_VALIDO, ID_CONV);
        verify(participanteConvRepository, never()).deleteByIdUsuarioAndConversacion_IdConversacion(any(), any());
    }

    // =========================================================================
    // findParticipantesByConversacion(Integer idConversacion)
    // =========================================================================

    @Test
    @DisplayName("Debe retornar la lista de participantes de una conversación existente")
    void findParticipantesByConversacion_success() {
        // 1. Simular validación de Conversación: Existe
        when(conversacionService.findById(ID_CONV)).thenReturn(conversacionMock);

        // 2. Simular la obtención de participantes
        List<ParticipanteConversacion> lista = List.of(
                participanteMock,
                new ParticipanteConversacion(2, 103, conversacionMock,LocalDateTime.now())
        );
        when(participanteConvRepository.findByConversacion_IdConversacion(ID_CONV)).thenReturn(lista);

        // Ejecutar
        List<ParticipanteConversacion> resultado = participanteConvService.findParticipantesByConversacion(ID_CONV);

        // Verificar
        assertNotNull(resultado);
        assertEquals(2, resultado.size());

        // Verificar interacciones
        verify(conversacionService, times(1)).findById(ID_CONV);
        verify(participanteConvRepository, times(1)).findByConversacion_IdConversacion(ID_CONV);
    }

    @Test
    @DisplayName("Debe lanzar NoSuchElementException si la conversación no existe al buscar participantes")
    void findParticipantesByConversacion_conversacionNotFound() {
        // 1. Simular validación de Conversación: Lanza excepción
        when(conversacionService.findById(ID_CONV))
                .thenThrow(new NoSuchElementException("Conversación no existe"));

        // Ejecutar y verificar
        assertThrows(NoSuchElementException.class, () -> {
            participanteConvService.findParticipantesByConversacion(ID_CONV);
        });

        // Verificar interacciones
        verify(conversacionService, times(1)).findById(ID_CONV);
        verify(participanteConvRepository, never()).findByConversacion_IdConversacion(any());
    }

    // =========================================================================
    // findConversacionesByParticipante(Integer idUsuario)
    // =========================================================================

    @Test
    @DisplayName("Debe retornar la lista de conversaciones donde participa el usuario")
    void findConversacionesByParticipante_success() {
        // 1. Simular la obtención de conversaciones por usuario
        Conversacion conv2 = new Conversacion(20, "Privada",null,LocalDateTime.now());
        List<ParticipanteConversacion> lista = List.of(
                new ParticipanteConversacion(3,ID_USER_VALIDO, conv2,LocalDateTime.now() ),
                participanteMock
        );
        when(participanteConvRepository.findByIdUsuarioOrderByFechaUnionDesc(ID_USER_VALIDO)).thenReturn(lista);

        // Ejecutar
        List<ParticipanteConversacion> resultado = participanteConvService.findConversacionesByParticipante(ID_USER_VALIDO);

        // Verificar
        assertNotNull(resultado);
        assertEquals(2, resultado.size());
        assertTrue(resultado.stream().allMatch(p -> p.getIdUsuario().equals(ID_USER_VALIDO)));

        // Verificar interacciones
        verify(participanteConvRepository, times(1)).findByIdUsuarioOrderByFechaUnionDesc(ID_USER_VALIDO);
        // Se verifica que no se llama al servicio de conversación ni al cliente de usuario
        verify(conversacionService, never()).findById(any());
        verify(usuarioClient, never()).findById(any());
    }
}