package com.SAFE_Rescue.API_Perfiles.service;

import com.SAFE_Rescue.API_Perfiles.modelo.Equipo;
import com.SAFE_Rescue.API_Perfiles.modelo.HistorialUsuario;
import com.SAFE_Rescue.API_Perfiles.modelo.Usuario;
import com.SAFE_Rescue.API_Perfiles.repositoy.HistorialUsuarioRepository;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Pruebas unitarias para la clase HistorialUsuarioService.
 * Verifica la lógica de auditoría y consulta de cambios de estado.
 */
@ExtendWith(MockitoExtension.class)
public class HistorialUsuarioServiceTest {

    @Mock
    private HistorialUsuarioRepository historialUsuarioRepository;

    @Mock
    private UsuarioService usuarioService;

    @Mock
    private EquipoService equipoService;

    @InjectMocks
    private HistorialUsuarioService historialUsuarioService;

    private Usuario usuarioBase;
    private Equipo equipoBase;
    private HistorialUsuario historialBase;

    @BeforeEach
    void setUp() {
        // Inicializar entidades base
        usuarioBase = new Usuario();
        usuarioBase.setIdUsuario(1);
        usuarioBase.setNombre("Juan Perez");
        // No se requiere inicializar todos los campos, solo lo necesario para la relación.

        equipoBase = new Equipo();
        equipoBase.setIdEquipo(10);
        equipoBase.setNombre("Equipo A");

        historialBase = new HistorialUsuario();
        historialBase.setIdHistorial(1);
        historialBase.setUsuario(usuarioBase);
        historialBase.setIdEstadoAnterior(1);
        historialBase.setIdEstadoNuevo(2);
        historialBase.setDetalle("Cambio a estado Activo");
        historialBase.setFechaHistorial(LocalDateTime.now());
    }

    // =========================================================================
    // PRUEBAS DE FIND ALL
    // =========================================================================

    @Test
    void findAll_debeRetornarTodosLosRegistrosDeHistorial() {
        // Arrange
        HistorialUsuario h2 = new HistorialUsuario();
        h2.setIdHistorial(2);
        List<HistorialUsuario> listaEsperada = Arrays.asList(historialBase, h2);

        when(historialUsuarioRepository.findAll()).thenReturn(listaEsperada);

        // Act
        List<HistorialUsuario> resultado = historialUsuarioService.findAll();

        // Assert
        assertNotNull(resultado);
        assertEquals(2, resultado.size());
        verify(historialUsuarioRepository, times(1)).findAll();
    }

    // =========================================================================
    // PRUEBAS DE REGISTRO DE CAMBIO DE ESTADO (USUARIO)
    // =========================================================================

    @Test
    void registrarCambioEstado_Usuario_debeGuardarRegistroCorrectamente() {
        // Arrange
        Integer estadoAnterior = 1;
        Integer estadoNuevo = 2;
        String detalle = "El usuario se inactivó por inactividad.";

        // Mockear el save, simulando que devuelve el objeto guardado con su ID generado
        when(historialUsuarioRepository.save(any(HistorialUsuario.class)))
                .thenAnswer(invocation -> {
                    HistorialUsuario reg = invocation.getArgument(0);
                    reg.setIdHistorial(50);
                    return reg;
                });

        // Act
        HistorialUsuario resultado = historialUsuarioService.registrarCambioEstado(
                usuarioBase, estadoAnterior, estadoNuevo, detalle);

        // Assert
        assertNotNull(resultado);
        assertEquals(50, resultado.getIdHistorial());
        assertEquals(usuarioBase, resultado.getUsuario());
        assertNull(resultado.getEquipo()); // Debe ser nulo para usuarios
        assertEquals(estadoAnterior, resultado.getIdEstadoAnterior());
        assertEquals(estadoNuevo, resultado.getIdEstadoNuevo());
        assertEquals(detalle, resultado.getDetalle());
        assertNotNull(resultado.getFechaHistorial());

        // Verificar que se llamó al repositorio para guardar
        verify(historialUsuarioRepository, times(1)).save(any(HistorialUsuario.class));
    }

    @Test
    void registrarCambioEstado_Usuario_debeLanzarExcepcionPorDatosFaltantes() {
        // Arrange: Usuario nulo
        Usuario usuarioNulo = null;
        Integer estadoAnterior = 1;
        Integer estadoNuevo = 2;
        String detalle = "Detalle";

        // Act & Assert
        IllegalArgumentException thrown = assertThrows(IllegalArgumentException.class, () -> {
            historialUsuarioService.registrarCambioEstado(usuarioNulo, estadoAnterior, estadoNuevo, detalle);
        });

        assertTrue(thrown.getMessage().contains("Faltan datos obligatorios"));
        verify(historialUsuarioRepository, never()).save(any());
    }

    // Prueba para detalle vacío
    @Test
    void registrarCambioEstado_Usuario_debeLanzarExcepcionPorDetalleVacio() {
        // Arrange
        String detalleVacio = "  ";

        // Act & Assert
        IllegalArgumentException thrown = assertThrows(IllegalArgumentException.class, () -> {
            historialUsuarioService.registrarCambioEstado(usuarioBase, 1, 2, detalleVacio);
        });

        assertTrue(thrown.getMessage().contains("Faltan datos obligatorios"));
        verify(historialUsuarioRepository, never()).save(any());
    }

    // =========================================================================
    // PRUEBAS DE REGISTRO DE CAMBIO DE ESTADO (EQUIPO)
    // =========================================================================

    @Test
    void registrarCambioEstado_Equipo_debeGuardarRegistroCorrectamente() {
        // Arrange
        Integer estadoAnterior = 5;
        Integer estadoNuevo = 6;
        String detalle = "El equipo pasó a estado En Misión.";

        // Mockear el save, simulando que devuelve el objeto guardado con su ID generado
        when(historialUsuarioRepository.save(any(HistorialUsuario.class)))
                .thenAnswer(invocation -> {
                    HistorialUsuario reg = invocation.getArgument(0);
                    reg.setIdHistorial(60);
                    return reg;
                });

        // Act
        HistorialUsuario resultado = historialUsuarioService.registrarCambioEstado(
                equipoBase, estadoAnterior, estadoNuevo, detalle);

        // Assert
        assertNotNull(resultado);
        assertEquals(60, resultado.getIdHistorial());
        assertNull(resultado.getUsuario()); // Debe ser nulo para equipos
        assertEquals(equipoBase, resultado.getEquipo());
        assertEquals(estadoAnterior, resultado.getIdEstadoAnterior());
        assertEquals(estadoNuevo, resultado.getIdEstadoNuevo());
        assertEquals(detalle, resultado.getDetalle());

        verify(historialUsuarioRepository, times(1)).save(any(HistorialUsuario.class));

        // Capturar el argumento para verificar que se setea el usuario a null
        ArgumentCaptor<HistorialUsuario> argumentCaptor = ArgumentCaptor.forClass(HistorialUsuario.class);
        verify(historialUsuarioRepository).save(argumentCaptor.capture());
        assertNull(argumentCaptor.getValue().getUsuario());
    }

    @Test
    void registrarCambioEstado_Equipo_debeLanzarExcepcionPorDatosFaltantes() {
        // Arrange: Equipo nulo
        Equipo equipoNulo = null;
        Integer estadoAnterior = 1;
        Integer estadoNuevo = 2;
        String detalle = "Detalle";

        // Act & Assert
        IllegalArgumentException thrown = assertThrows(IllegalArgumentException.class, () -> {
            historialUsuarioService.registrarCambioEstado(equipoNulo, estadoAnterior, estadoNuevo, detalle);
        });

        assertTrue(thrown.getMessage().contains("Faltan datos obligatorios"));
        verify(historialUsuarioRepository, never()).save(any());
    }

    // =========================================================================
    // PRUEBAS DE OBTENCIÓN DE HISTORIAL POR USUARIO
    // =========================================================================

    @Test
    void obtenerHistorialPorUsuario_debeRetornarListaSiUsuarioExiste() {
        // Arrange
        final Integer ID_USUARIO = 1;
        List<HistorialUsuario> listaHistorial = Arrays.asList(historialBase);

        // Mockear la existencia del usuario
        when(usuarioService.findById(ID_USUARIO)).thenReturn(usuarioBase);

        // Mockear la búsqueda en el repositorio
        when(historialUsuarioRepository.findByUsuarioIdUsuario(ID_USUARIO)).thenReturn(listaHistorial);

        // Act
        List<HistorialUsuario> resultado = historialUsuarioService.obtenerHistorialPorUsuario(ID_USUARIO);

        // Assert
        assertNotNull(resultado);
        assertEquals(1, resultado.size());
        assertEquals(historialBase, resultado.get(0));
        verify(usuarioService, times(1)).findById(ID_USUARIO);
        verify(historialUsuarioRepository, times(1)).findByUsuarioIdUsuario(ID_USUARIO);
    }

    @Test
    void obtenerHistorialPorUsuario_debeLanzarExcepcionSiUsuarioNoExiste() {
        // Arrange
        final Integer ID_USUARIO_NO_EXISTE = 99;

        // Mockear la inexistencia del usuario devolviendo null
        when(usuarioService.findById(ID_USUARIO_NO_EXISTE)).thenReturn(null);

        // Act & Assert
        EntityNotFoundException thrown = assertThrows(EntityNotFoundException.class, () -> {
            historialUsuarioService.obtenerHistorialPorUsuario(ID_USUARIO_NO_EXISTE);
        });

        assertTrue(thrown.getMessage().contains("Usuario con ID 99 no encontrado."));
        verify(usuarioService, times(1)).findById(ID_USUARIO_NO_EXISTE);
        verify(historialUsuarioRepository, never()).findByUsuarioIdUsuario(any());
    }

    // =========================================================================
    // PRUEBAS DE OBTENCIÓN DE HISTORIAL POR EQUIPO
    // =========================================================================

    @Test
    void obtenerHistorialPorEquipo_debeRetornarListaSiEquipoExiste() {
        // Arrange
        final Integer ID_EQUIPO = 10;
        HistorialUsuario historialEquipo = new HistorialUsuario();
        historialEquipo.setEquipo(equipoBase);
        List<HistorialUsuario> listaHistorial = Arrays.asList(historialEquipo);

        // Mockear la existencia del equipo
        when(equipoService.findById(ID_EQUIPO)).thenReturn(equipoBase);

        // Mockear la búsqueda en el repositorio
        when(historialUsuarioRepository.findByEquipoIdEquipo(ID_EQUIPO)).thenReturn(listaHistorial);

        // Act
        List<HistorialUsuario> resultado = historialUsuarioService.obtenerHistorialPorEquipo(ID_EQUIPO);

        // Assert
        assertNotNull(resultado);
        assertEquals(1, resultado.size());
        assertEquals(equipoBase, resultado.get(0).getEquipo());
        verify(equipoService, times(1)).findById(ID_EQUIPO);
        verify(historialUsuarioRepository, times(1)).findByEquipoIdEquipo(ID_EQUIPO);
    }

    @Test
    void obtenerHistorialPorEquipo_debeLanzarExcepcionSiEquipoNoExiste() {
        // Arrange
        final Integer ID_EQUIPO_NO_EXISTE = 999;

        // Mockear la inexistencia del equipo devolviendo null
        when(equipoService.findById(ID_EQUIPO_NO_EXISTE)).thenReturn(null);

        // Act & Assert
        EntityNotFoundException thrown = assertThrows(EntityNotFoundException.class, () -> {
            historialUsuarioService.obtenerHistorialPorEquipo(ID_EQUIPO_NO_EXISTE);
        });

        assertTrue(thrown.getMessage().contains("Equipo con ID 999 no encontrado."));
        verify(equipoService, times(1)).findById(ID_EQUIPO_NO_EXISTE);
        verify(historialUsuarioRepository, never()).findByEquipoIdEquipo(any());
    }
}