package com.SAFE_Rescue.API_Comunicacion.service;

import com.SAFE_Rescue.API_Comunicacion.modelo.Conversacion;
import com.SAFE_Rescue.API_Comunicacion.modelo.HistorialMensaje;
import com.SAFE_Rescue.API_Comunicacion.modelo.Mensaje;
import com.SAFE_Rescue.API_Comunicacion.repository.HistorialMensajeRepository;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.NoSuchElementException;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Pruebas unitarias para la lógica de negocio de HistorialMensajeService.
 */
@ExtendWith(MockitoExtension.class)
public class HistorialMensajeServiceTest {

    @Mock
    private HistorialMensajeRepository historialMensajeRepository;

    @Mock
    private MensajeService mensajeService;

    @InjectMocks
    private HistorialMensajeService historialMensajeService;

    // Datos de prueba
    private final Integer ID_MENSAJE = 50;
    private final Integer ESTADO_ANTERIOR = 1; // ENVIADO
    private final Integer ESTADO_NUEVO = 2;    // LEIDO
    private final String DETALLE = "Cambio automático de estado a LEIDO.";
    private Mensaje mensajeMock;
    private HistorialMensaje historialMock;

    @BeforeEach
    void setUp() {
        mensajeMock = new Mensaje(ID_MENSAJE,LocalDateTime.now(),"Hola",new Conversacion(),1,1);
        historialMock = new HistorialMensaje();
    }

    // =========================================================================
    // registrarCambioEstado
    // =========================================================================

    @Test
    @DisplayName("Debe registrar un cambio de estado con éxito y persistir el historial")
    void registrarCambioEstado_success() {
        // Simular que el repositorio devuelve el objeto guardado
        when(historialMensajeRepository.save(any(HistorialMensaje.class))).thenReturn(historialMock);

        // Ejecutar
        HistorialMensaje resultado = historialMensajeService.registrarCambioEstado(
                mensajeMock, ESTADO_ANTERIOR, ESTADO_NUEVO, DETALLE);

        // Verificar
        assertNotNull(resultado);

        // Verificar que el método save fue llamado una vez
        verify(historialMensajeRepository, times(1)).save(any(HistorialMensaje.class));
    }

    @Test
    @DisplayName("Debe lanzar IllegalArgumentException si el mensaje es null")
    void registrarCambioEstado_mensajeIsNull() {
        assertThrows(IllegalArgumentException.class, () -> {
            historialMensajeService.registrarCambioEstado(
                    null, ESTADO_ANTERIOR, ESTADO_NUEVO, DETALLE);
        }, "Debe lanzar excepción si Mensaje es nulo");

        verify(historialMensajeRepository, never()).save(any());
    }

    @Test
    @DisplayName("Debe lanzar IllegalArgumentException si el detalle es nulo o vacío")
    void registrarCambioEstado_detalleIsInvalid() {
        // Detalle nulo
        assertThrows(IllegalArgumentException.class, () -> {
            historialMensajeService.registrarCambioEstado(
                    mensajeMock, ESTADO_ANTERIOR, ESTADO_NUEVO, null);
        }, "Debe lanzar excepción si Detalle es nulo");

        // Detalle vacío (trim().isEmpty() incluido en la validación)
        assertThrows(IllegalArgumentException.class, () -> {
            historialMensajeService.registrarCambioEstado(
                    mensajeMock, ESTADO_ANTERIOR, ESTADO_NUEVO, "   ");
        }, "Debe lanzar excepción si Detalle está vacío o solo con espacios");

        verify(historialMensajeRepository, never()).save(any());
    }

    // También se podría probar con estados nulos (idEstadoAnterior/Nuevo)

    // =========================================================================
    // obtenerHistorialPorMensaje
    // =========================================================================

    @Test
    @DisplayName("Debe retornar el historial de cambios de estado para un mensaje existente")
    void obtenerHistorialPorMensaje_success() {
        // 1. Simular validación: Mensaje existe
        when(mensajeService.findById(ID_MENSAJE)).thenReturn(mensajeMock);

        // 2. Simular obtención del historial
        List<HistorialMensaje> historialList = List.of(
                new HistorialMensaje(),
                new HistorialMensaje()
        );
        when(historialMensajeRepository.findByMensajeIdMensaje(ID_MENSAJE)).thenReturn(historialList);

        // Ejecutar
        List<HistorialMensaje> resultado = historialMensajeService.obtenerHistorialPorMensaje(ID_MENSAJE);

        // Verificar
        assertNotNull(resultado);
        assertEquals(2, resultado.size());

        // Verificar interacciones
        verify(mensajeService, times(1)).findById(ID_MENSAJE);
        verify(historialMensajeRepository, times(1)).findByMensajeIdMensaje(ID_MENSAJE);
    }

    @Test
    @DisplayName("Debe retornar una lista vacía si el mensaje existe pero no tiene historial")
    void obtenerHistorialPorMensaje_emptyHistory() {
        // 1. Simular validación: Mensaje existe
        when(mensajeService.findById(ID_MENSAJE)).thenReturn(mensajeMock);

        // 2. Simular historial vacío
        when(historialMensajeRepository.findByMensajeIdMensaje(ID_MENSAJE)).thenReturn(Collections.emptyList());

        // Ejecutar
        List<HistorialMensaje> resultado = historialMensajeService.obtenerHistorialPorMensaje(ID_MENSAJE);

        // Verificar
        assertNotNull(resultado);
        assertTrue(resultado.isEmpty());
    }

    @Test
    @DisplayName("Debe lanzar EntityNotFoundException si el mensaje no existe")
    void obtenerHistorialPorMensaje_messageNotFound() {
        // 1. Simular validación: MensajeService lanza NoSuchElementException
        when(mensajeService.findById(ID_MENSAJE))
                .thenThrow(new NoSuchElementException("Mensaje no existe"));

        // Ejecutar y verificar la excepción
        EntityNotFoundException thrown = assertThrows(EntityNotFoundException.class, () -> {
            historialMensajeService.obtenerHistorialPorMensaje(ID_MENSAJE);
        });

        // Verificar el mensaje de la excepción convertida
        assertEquals("Mensaje con ID 50 no encontrado.", thrown.getMessage());

        // Verificar interacciones: no se debe llamar al repositorio de historial
        verify(mensajeService, times(1)).findById(ID_MENSAJE);
        verify(historialMensajeRepository, never()).findByMensajeIdMensaje(any());
    }

    // =========================================================================
    // findAll
    // =========================================================================

    @Test
    @DisplayName("Debe llamar al repositorio para obtener todos los registros de historial")
    void findAll_callsRepository() {
        // Simular que el repositorio devuelve una lista
        List<HistorialMensaje> allHistorial = List.of(historialMock);
        when(historialMensajeRepository.findAll()).thenReturn(allHistorial);

        // Ejecutar
        List<HistorialMensaje> resultado = historialMensajeService.findAll();

        // Verificar
        assertNotNull(resultado);
        assertEquals(1, resultado.size());

        // Verificar que el método findAll fue llamado exactamente una vez
        verify(historialMensajeRepository, times(1)).findAll();
    }
}