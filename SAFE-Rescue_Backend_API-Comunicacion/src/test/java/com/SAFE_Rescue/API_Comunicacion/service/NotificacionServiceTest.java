package com.SAFE_Rescue.API_Comunicacion.service;

import com.SAFE_Rescue.API_Comunicacion.config.EstadoClient;
import com.SAFE_Rescue.API_Comunicacion.config.UsuarioClient;
import com.SAFE_Rescue.API_Comunicacion.modelo.Conversacion;
import com.SAFE_Rescue.API_Comunicacion.modelo.Notificacion;
import com.SAFE_Rescue.API_Comunicacion.modelo.UsuarioDTO; // Importación necesaria para el mock
import com.SAFE_Rescue.API_Comunicacion.repository.NotificacionRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional; // Importación necesaria

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class NotificacionServiceTest {

    // Constantes simuladas para los estados de la notificación
    private static final Integer ESTADO_PENDIENTE = 8;
    private static final Integer ESTADO_LEIDA = 9;

    // Mocks de las dependencias
    @Mock
    private NotificacionRepository notificacionRepository;

    @Mock
    private UsuarioClient usuarioClient;

    @Mock
    private EstadoClient estadoClient;

    // Inyección del servicio a probar
    @InjectMocks
    private NotificacionService notificacionService;

    // Variables de prueba
    private Notificacion notificacionPendiente;
    private Conversacion conversacion;
    private final String ID_USUARIO_VALIDO = "101";
    private final String ID_USUARIO_INVALIDO = "999";
    private final Integer ID_NOTIFICACION_VALIDA = 5;

    @BeforeEach
    void setUp() throws Exception {
        // Setup de la Conversación
        conversacion = new Conversacion();
        conversacion.setIdConversacion(1);

        // Setup de una Notificación Pendiente
        notificacionPendiente = new Notificacion();
        notificacionPendiente.setIdNotificacion(ID_NOTIFICACION_VALIDA);
        notificacionPendiente.setIdUsuarioReceptor(ID_USUARIO_VALIDO);
        notificacionPendiente.setDetalle("Nuevo mensaje en el grupo.");
        notificacionPendiente.setIdEstado(ESTADO_PENDIENTE);
        notificacionPendiente.setConversacion(conversacion);
    }

    // =========================================================================
    // TEST: crearNotificacion
    // =========================================================================

    @Test
    @DisplayName("Debe crear la notificación con estado PENDIENTE si el usuario es válido")
    void crearNotificacion_usuarioValido_debeCrearConEstadoPendiente() {
        // Configuración de Mocks
        // CORRECCIÓN: Usar Optional.of para simular que el usuario existe (el servicio usa .isEmpty())
        when(usuarioClient.findById(eq(101))).thenReturn(Optional.of(mock(UsuarioDTO.class)));
        when(notificacionRepository.save(any(Notificacion.class))).thenReturn(notificacionPendiente);

        // Ejecución
        Notificacion resultado = notificacionService.crearNotificacion(
                ID_USUARIO_VALIDO,
                "Detalle de prueba",
                conversacion
        );

        // Verificación
        assertNotNull(resultado);
        assertEquals(ID_USUARIO_VALIDO, resultado.getIdUsuarioReceptor());
        assertEquals(ESTADO_PENDIENTE, resultado.getIdEstado(), "El estado inicial debe ser PENDIENTE (8)");
        verify(notificacionRepository, times(1)).save(any(Notificacion.class));
    }

    @Test
    @DisplayName("Debe lanzar IllegalArgumentException si el usuario no existe")
    void crearNotificacion_usuarioNoExiste_debeLanzarExcepcion() {
        // Configuración de Mocks
        when(usuarioClient.findById(eq(999))).thenReturn(Optional.empty()); // Simula que el usuario NO existe

        // Ejecución y Verificación
        assertThrows(IllegalArgumentException.class, () -> {
            notificacionService.crearNotificacion(ID_USUARIO_INVALIDO, "Detalle", conversacion);
        }, "Debe lanzar IllegalArgumentException si el usuario no es encontrado.");

        verify(notificacionRepository, never()).save(any(Notificacion.class));
    }

    @Test
    @DisplayName("Debe lanzar IllegalArgumentException si el ID del usuario no es numérico")
    void crearNotificacion_idNoNumerico_debeLanzarExcepcion() {
        // Ejecución y Verificación
        assertThrows(IllegalArgumentException.class, () -> {
            notificacionService.crearNotificacion("ABC", "Detalle", conversacion);
        }, "Debe lanzar IllegalArgumentException si el ID del usuario no se puede parsear a Integer.");

        verify(usuarioClient, never()).findById(any());
        verify(notificacionRepository, never()).save(any(Notificacion.class));
    }

    @Test
    @DisplayName("Debe lanzar RuntimeException si el microservicio de usuario falla")
    void crearNotificacion_clienteUsuarioFalla_debeLanzarRuntimeException() {
        // Configuración de Mocks: Simula un error de conexión o del cliente WebClient
        when(usuarioClient.findById(any())).thenThrow(new RuntimeException("Error de conexión Feign."));

        // Ejecución y Verificación
        assertThrows(RuntimeException.class, () -> {
            notificacionService.crearNotificacion(ID_USUARIO_VALIDO, "Detalle", conversacion);
        }, "Debe relanzar una RuntimeException si la validación con el microservicio falla.");

        verify(notificacionRepository, never()).save(any(Notificacion.class));
    }

    // =========================================================================
    // TEST: cargarNotificacionesPendientes
    // =========================================================================

    @Test
    @DisplayName("Debe retornar una página de notificaciones pendientes para el usuario")
    void cargarNotificacionesPendientes_debeRetornarPagina() {
        // Configuración de Mocks
        Pageable pageable = PageRequest.of(0, 10);
        List<Notificacion> listaNotificaciones = List.of(notificacionPendiente);
        Page<Notificacion> paginaMock = new PageImpl<>(listaNotificaciones, pageable, 1);

        when(notificacionRepository.findByIdUsuarioReceptorAndIdEstadoEqualsOrderByFechaCreacionDesc(
                eq(ID_USUARIO_VALIDO),
                eq(ESTADO_PENDIENTE),
                eq(pageable)
        )).thenReturn(paginaMock);

        // Ejecución
        Page<Notificacion> resultado = notificacionService.cargarNotificacionesPendientes(ID_USUARIO_VALIDO, pageable);

        // Verificación
        assertNotNull(resultado);
        assertFalse(resultado.isEmpty());
        assertEquals(1, resultado.getTotalElements());
        assertEquals(ESTADO_PENDIENTE, resultado.getContent().get(0).getIdEstado());
        verify(notificacionRepository, times(1)).findByIdUsuarioReceptorAndIdEstadoEqualsOrderByFechaCreacionDesc(any(), any(), any());
    }

    // =========================================================================
    // TEST: marcarComoLeida
    // =========================================================================

    @Test
    @DisplayName("Debe marcar la notificación como LEIDA (9) si estaba PENDIENTE (8)")
    void marcarComoLeida_pendiente_debeActualizarEstado() {
        // Notificación de entrada con estado 8
        Notificacion notif = notificacionPendiente;

        // Simular búsqueda (éxito)
        when(notificacionRepository.findById(ID_NOTIFICACION_VALIDA)).thenReturn(Optional.of(notif));

        // Simular guardado y retorno (el mock del servicio actualiza el estado antes de guardar)
        when(notificacionRepository.save(any(Notificacion.class)))
                .thenAnswer(invocation -> {
                    Notificacion savedNotif = invocation.getArgument(0);
                    assertEquals(ESTADO_LEIDA, savedNotif.getIdEstado()); // Aseguramos que el estado fue cambiado a 9
                    return savedNotif;
                });

        // Ejecución
        Notificacion resultado = notificacionService.marcarComoLeida(ID_NOTIFICACION_VALIDA);

        // Verificación
        assertNotNull(resultado);
        assertEquals(ESTADO_LEIDA, resultado.getIdEstado(), "El estado debe ser 9 (LEIDA)");
        verify(notificacionRepository, times(1)).save(any(Notificacion.class));
    }

    @Test
    @DisplayName("No debe guardar si la notificación ya está LEIDA")
    void marcarComoLeida_yaLeida_noDebeGuardar() {
        // Configuración: Simular que ya está en estado 9
        notificacionPendiente.setIdEstado(ESTADO_LEIDA);

        when(notificacionRepository.findById(ID_NOTIFICACION_VALIDA)).thenReturn(Optional.of(notificacionPendiente));

        // Ejecución
        Notificacion resultado = notificacionService.marcarComoLeida(ID_NOTIFICACION_VALIDA);

        // Verificación
        assertNotNull(resultado);
        assertEquals(ESTADO_LEIDA, resultado.getIdEstado());
        // Verificamos que no se llamó a save
        verify(notificacionRepository, never()).save(any(Notificacion.class));
    }

    @Test
    @DisplayName("Debe lanzar NoSuchElementException si la notificación no existe")
    void marcarComoLeida_noEncontrada_debeLanzarExcepcion() {
        // Configuración de Mocks
        when(notificacionRepository.findById(any())).thenReturn(Optional.empty());

        // Ejecución y Verificación
        assertThrows(NoSuchElementException.class, () -> {
            notificacionService.marcarComoLeida(999);
        }, "Debe lanzar NoSuchElementException si no se encuentra la notificación.");

        verify(notificacionRepository, never()).save(any(Notificacion.class));
    }

    // =========================================================================
    // TEST: marcarTodasComoLeidas
    // =========================================================================

    @Test
    @DisplayName("Debe llamar a actualizarEstadoPorUsuario y retornar el conteo")
    void marcarTodasComoLeidas_debeActualizarYRetornarConteo() {
        // Configuración de Mocks: Simular que se actualizaron 3 filas
        when(notificacionRepository.actualizarEstadoPorUsuario(
                eq(ID_USUARIO_VALIDO),
                eq(ESTADO_LEIDA),
                eq(ESTADO_PENDIENTE)
        )).thenReturn(3);

        // Ejecución
        int conteo = notificacionService.marcarTodasComoLeidas(ID_USUARIO_VALIDO);

        // Verificación
        assertEquals(3, conteo, "El conteo retornado debe ser el número de filas actualizadas.");
        verify(notificacionRepository, times(1)).actualizarEstadoPorUsuario(any(), any(), any());
    }
}