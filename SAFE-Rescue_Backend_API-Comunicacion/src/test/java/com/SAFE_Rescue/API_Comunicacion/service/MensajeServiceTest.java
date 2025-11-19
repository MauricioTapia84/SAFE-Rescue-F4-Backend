package com.SAFE_Rescue.API_Comunicacion.service;

import com.SAFE_Rescue.API_Comunicacion.config.EstadoClient;
import com.SAFE_Rescue.API_Comunicacion.config.UsuarioClient;
import com.SAFE_Rescue.API_Comunicacion.modelo.Conversacion;
import com.SAFE_Rescue.API_Comunicacion.dto.EstadoDTO;
import com.SAFE_Rescue.API_Comunicacion.modelo.Mensaje;
import com.SAFE_Rescue.API_Comunicacion.dto.UsuarioDTO;
import com.SAFE_Rescue.API_Comunicacion.repository.ConversacionRepository;
import com.SAFE_Rescue.API_Comunicacion.repository.MensajeRepository;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Sort;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.*;

/**
 * Pruebas unitarias para MensajeService utilizando Mockito.
 */
@ExtendWith(MockitoExtension.class)
class MensajeServiceTest {

    // Inyecta el servicio que queremos probar, inyectando los mocks en él
    @InjectMocks
    private MensajeService mensajeService;

    // Mocks de las dependencias
    @Mock
    private MensajeRepository mensajeRepository;
    @Mock
    private ConversacionRepository conversacionRepository;

    // Mocks de Clientes (Se asume que devuelven Optional<alguna_clase>)
    @Mock
    private UsuarioClient usuarioClient;
    @Mock
    private EstadoClient estadoClient;

    // Datos de prueba
    private Conversacion conversacion;
    private Mensaje mensaje;
    private final Integer CONVERSACION_ID = 1;
    private final Integer EMISOR_ID = 101;
    private final Integer ESTADO_ID = 1; // 1 = Enviado

    @BeforeEach
    void setUp() {
        // Inicialización de la Conversacion de prueba
        conversacion = new Conversacion();
        conversacion.setIdConversacion(CONVERSACION_ID);
        conversacion.setTipo("Privada");
        conversacion.setFechaCreacion(LocalDateTime.now().minusHours(1));

        // Inicialización del Mensaje de prueba
        mensaje = new Mensaje();
        mensaje.setIdUsuarioEmisor(EMISOR_ID);
        mensaje.setIdEstado(ESTADO_ID);
        mensaje.setDetalle("Mensaje de prueba.");
    }

    // --- PRUEBAS PARA createMessage ---

    @Test
    void testCreateMessage_Success() {
        // Configuración de Mocks para el escenario exitoso
        when(conversacionRepository.findById(CONVERSACION_ID)).thenReturn(Optional.of(conversacion));

        // Corregido: Usamos las clases de Mock para cumplir con el tipo de retorno esperado
        when(usuarioClient.findById(EMISOR_ID)).thenReturn(Optional.of(new UsuarioDTO()));
        when(estadoClient.findById(ESTADO_ID)).thenReturn(Optional.of(new EstadoDTO()));

        when(mensajeRepository.save(any(Mensaje.class))).thenReturn(mensaje);

        // Ejecución
        Mensaje result = mensajeService.createMessage(CONVERSACION_ID, mensaje);

        // Verificación
        assertNotNull(result);
        assertEquals(CONVERSACION_ID, result.getConversacion().getIdConversacion());
        verify(conversacionRepository, times(1)).findById(CONVERSACION_ID);
        verify(mensajeRepository, times(1)).save(mensaje);
    }

    @Test
    void testCreateMessage_ConversacionNotFound_ThrowsNoSuchElementException() {
        // Configuración: Conversación no encontrada
        when(conversacionRepository.findById(CONVERSACION_ID)).thenReturn(Optional.empty());

        // Ejecución y Verificación de Excepción
        NoSuchElementException exception = assertThrows(NoSuchElementException.class, () -> {
            mensajeService.createMessage(CONVERSACION_ID, mensaje);
        });

        assertTrue(exception.getMessage().contains("Conversación con ID " + CONVERSACION_ID + " no encontrada."));
        verify(usuarioClient, never()).findById(anyInt());
        verify(estadoClient, never()).findById(anyInt());
        verify(mensajeRepository, never()).save(any(Mensaje.class));
    }

    @Test
    void testCreateMessage_EmisorIdMissing_ThrowsIllegalArgumentException() {
        // Configuración
        mensaje.setIdUsuarioEmisor(null);
        when(conversacionRepository.findById(CONVERSACION_ID)).thenReturn(Optional.of(conversacion));

        // Ejecución y Verificación de Excepción
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            mensajeService.createMessage(CONVERSACION_ID, mensaje);
        });

        assertTrue(exception.getMessage().contains("El ID del usuario emisor es obligatorio."));
    }

    @Test
    void testCreateMessage_EmisorNotFound_ThrowsIllegalArgumentException() {
        // Configuración
        when(conversacionRepository.findById(CONVERSACION_ID)).thenReturn(Optional.of(conversacion));
        when(usuarioClient.findById(EMISOR_ID)).thenReturn(Optional.empty()); // Usuario no encontrado

        // Ejecución y Verificación de Excepción
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            mensajeService.createMessage(CONVERSACION_ID, mensaje);
        });

        assertTrue(exception.getMessage().contains("no encontrado en el microservicio de Usuarios."));
        verify(estadoClient, never()).findById(anyInt());
        verify(mensajeRepository, never()).save(any(Mensaje.class));
    }

    @Test
    void testCreateMessage_EstadoIdMissing_ThrowsIllegalArgumentException() {
        // Configuración
        mensaje.setIdEstado(null);
        when(conversacionRepository.findById(CONVERSACION_ID)).thenReturn(Optional.of(conversacion));
        // Corregido: Usuario encontrado
        when(usuarioClient.findById(EMISOR_ID)).thenReturn(Optional.of(new UsuarioDTO()));

        // Ejecución y Verificación de Excepción
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            mensajeService.createMessage(CONVERSACION_ID, mensaje);
        });

        assertTrue(exception.getMessage().contains("El ID del estado del mensaje es obligatorio"));
    }

    @Test
    void testCreateMessage_EstadoNotFound_ThrowsIllegalArgumentException() {
        // Configuración
        when(conversacionRepository.findById(CONVERSACION_ID)).thenReturn(Optional.of(conversacion));
        // Corregido: Usuario encontrado
        when(usuarioClient.findById(EMISOR_ID)).thenReturn(Optional.of(new UsuarioDTO()));

        when(estadoClient.findById(ESTADO_ID)).thenReturn(Optional.empty()); // Estado no encontrado

        // Ejecución y Verificación de Excepción
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            mensajeService.createMessage(CONVERSACION_ID, mensaje);
        });

        assertTrue(exception.getMessage().contains("no encontrado en el microservicio de Estados."));
        verify(mensajeRepository, never()).save(any(Mensaje.class));
    }

    // --- PRUEBAS PARA findAll ---

    @Test
    void testFindAll_ReturnsSortedList() {
        // Configuración
        Mensaje m1 = new Mensaje(1, LocalDateTime.now().minusMinutes(5), "Detalle 1", conversacion, 1, 1);
        Mensaje m2 = new Mensaje(2, LocalDateTime.now().minusMinutes(10), "Detalle 2", conversacion, 2, 1);
        List<Mensaje> mensajesSimulados = Arrays.asList(m1, m2);

        // Configuración de Mock: Esperamos que llame a findAll con el Sort específico
        Sort expectedSort = Sort.by(Sort.Direction.DESC, "fechaCreacion");
        when(mensajeRepository.findAll(expectedSort)).thenReturn(mensajesSimulados);

        // Ejecución
        List<Mensaje> result = mensajeService.findAll();

        // Verificación
        assertNotNull(result);
        assertEquals(2, result.size());
        assertEquals(m1.getIdMensaje(), result.get(0).getIdMensaje(), "Debe estar ordenado DESC (el más reciente primero)");
        verify(mensajeRepository, times(1)).findAll(expectedSort);
    }

    // --- PRUEBAS PARA findMessagesByConversation ---

    @Test
    void testFindMessagesByConversation_Success() {
        // Configuración
        Mensaje m1 = new Mensaje(1, LocalDateTime.now().minusMinutes(5), "Detalle 1", conversacion, 1, 1);
        List<Mensaje> mensajesSimulados = Arrays.asList(m1);

        when(conversacionRepository.existsById(CONVERSACION_ID)).thenReturn(true);
        when(mensajeRepository.findByConversacionIdConversacion(CONVERSACION_ID)).thenReturn(mensajesSimulados);

        // Ejecución
        List<Mensaje> result = mensajeService.findMessagesByConversation(CONVERSACION_ID);

        // Verificación
        assertNotNull(result);
        assertFalse(result.isEmpty());
        verify(conversacionRepository, times(1)).existsById(CONVERSACION_ID);
        verify(mensajeRepository, times(1)).findByConversacionIdConversacion(CONVERSACION_ID);
    }

    @Test
    void testFindMessagesByConversation_ConversacionNotFound_ThrowsNoSuchElementException() {
        // Configuración
        when(conversacionRepository.existsById(CONVERSACION_ID)).thenReturn(false);

        // Ejecución y Verificación de Excepción
        NoSuchElementException exception = assertThrows(NoSuchElementException.class, () -> {
            mensajeService.findMessagesByConversation(CONVERSACION_ID);
        });

        assertTrue(exception.getMessage().contains("Conversación con ID " + CONVERSACION_ID + " no encontrada."));
        verify(mensajeRepository, never()).findByConversacionIdConversacion(anyInt());
    }

    // --- PRUEBAS PARA findById ---

    @Test
    void testFindById_Success() {
        // Configuración
        Integer MENSAJE_ID = 5;
        mensaje.setIdMensaje(MENSAJE_ID);
        when(mensajeRepository.findById(MENSAJE_ID)).thenReturn(Optional.of(mensaje));

        // Ejecución
        Mensaje result = mensajeService.findById(MENSAJE_ID);

        // Verificación
        assertNotNull(result);
        assertEquals(MENSAJE_ID, result.getIdMensaje());
        verify(mensajeRepository, times(1)).findById(MENSAJE_ID);
    }

    @Test
    void testFindById_NotFound_ThrowsNoSuchElementException() {
        // Configuración
        Integer MENSAJE_ID = 99;
        when(mensajeRepository.findById(MENSAJE_ID)).thenReturn(Optional.empty());

        // Ejecución y Verificación de Excepción
        NoSuchElementException exception = assertThrows(NoSuchElementException.class, () -> {
            mensajeService.findById(MENSAJE_ID);
        });

        assertTrue(exception.getMessage().contains("Mensaje no encontrado con ID: " + MENSAJE_ID));
    }

    // --- PRUEBAS PARA delete ---

    @Test
    void testDelete_Success() {
        // Configuración
        Integer MENSAJE_ID = 5;
        mensaje.setIdMensaje(MENSAJE_ID);
        when(mensajeRepository.findById(MENSAJE_ID)).thenReturn(Optional.of(mensaje));
        doNothing().when(mensajeRepository).delete(mensaje);

        // Ejecución
        mensajeService.delete(MENSAJE_ID);

        // Verificación
        verify(mensajeRepository, times(1)).findById(MENSAJE_ID);
        verify(mensajeRepository, times(1)).delete(mensaje);
    }

    @Test
    void testDelete_NotFound_ThrowsNoSuchElementException() {
        // Configuración
        Integer MENSAJE_ID = 99;
        when(mensajeRepository.findById(MENSAJE_ID)).thenReturn(Optional.empty());

        // Ejecución y Verificación de Excepción
        NoSuchElementException exception = assertThrows(NoSuchElementException.class, () -> {
            mensajeService.delete(MENSAJE_ID);
        });

        assertTrue(exception.getMessage().contains("Mensaje no encontrado con ID: " + MENSAJE_ID + " para eliminar."));
        verify(mensajeRepository, never()).delete(any(Mensaje.class));
    }
}