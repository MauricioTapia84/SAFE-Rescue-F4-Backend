package com.SAFE_Rescue.API_Incidentes.service;

import com.SAFE_Rescue.API_Incidentes.modelo.HistorialIncidente;
import com.SAFE_Rescue.API_Incidentes.modelo.Incidente;
import com.SAFE_Rescue.API_Incidentes.repository.HistorialIncidenteRepository;
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
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

/**
 * Pruebas unitarias para la clase HistorialIncidenteService.
 * Verifica la lógica de auditoría y consulta de cambios de estado para Incidentes.
 */
@ExtendWith(MockitoExtension.class)
public class HistorialIncidenteServiceTest {

    @Mock
    private HistorialIncidenteRepository historialIncidenteRepository;

    @Mock
    private IncidenteService incidenteService;

    @InjectMocks
    private HistorialIncidenteService historialIncidenteService;

    private Incidente incidenteBase;
    private HistorialIncidente historialBase;

    @BeforeEach
    void setUp() {
        // Inicializar entidad Incidente base
        incidenteBase = new Incidente();
        incidenteBase.setIdIncidente(100);
        // Asumiendo que el incidente tiene otros campos como descripcion, etc.

        // Inicializar entidad HistorialIncidentes base
        historialBase = new HistorialIncidente();
        historialBase.setIdHistorial(1);
        historialBase.setIncidente(incidenteBase);
        historialBase.setIdEstadoAnterior(1);
        historialBase.setIdEstadoNuevo(2);
        historialBase.setDetalle("Incidente escalado a estado 'En curso'");
        historialBase.setFechaHistorial(LocalDateTime.now());
    }

    // =========================================================================
    // PRUEBAS DE FIND ALL
    // =========================================================================

    @Test
    void findAll_debeRetornarTodosLosRegistrosDeHistorial() {
        // Arrange
        HistorialIncidente h2 = new HistorialIncidente();
        h2.setIdHistorial(2);
        List<HistorialIncidente> listaEsperada = Arrays.asList(historialBase, h2);

        when(historialIncidenteRepository.findAll()).thenReturn(listaEsperada);

        // Act
        List<HistorialIncidente> resultado = historialIncidenteService.findAll();

        // Assert
        assertNotNull(resultado);
        assertEquals(2, resultado.size());
        verify(historialIncidenteRepository, times(1)).findAll();
    }

    // =========================================================================
    // PRUEBAS DE REGISTRO DE CAMBIO DE ESTADO (INCIDENTE)
    // =========================================================================

    @Test
    void registrarCambioEstado_Incidente_debeGuardarRegistroCorrectamente() {
        // Arrange
        Integer estadoAnterior = 1;
        Integer estadoNuevo = 3;
        String detalle = "Se asignó equipo de rescate al incidente.";

        // Mockear el save, simulando que devuelve el objeto guardado con su ID generado
        when(historialIncidenteRepository.save(any(HistorialIncidente.class)))
                .thenAnswer(invocation -> {
                    HistorialIncidente reg = invocation.getArgument(0);
                    reg.setIdHistorial(50);
                    return reg;
                });

        // Act
        HistorialIncidente resultado = historialIncidenteService.registrarCambioEstado(
                incidenteBase, estadoAnterior, estadoNuevo, detalle);

        // Assert
        assertNotNull(resultado);
        assertEquals(50, resultado.getIdHistorial());
        assertEquals(incidenteBase, resultado.getIncidente());
        assertEquals(estadoAnterior, resultado.getIdEstadoAnterior());
        assertEquals(estadoNuevo, resultado.getIdEstadoNuevo());
        assertEquals(detalle, resultado.getDetalle());
        assertNotNull(resultado.getFechaHistorial());

        // Verificar que se llamó al repositorio para guardar
        verify(historialIncidenteRepository, times(1)).save(any(HistorialIncidente.class));
    }

    @Test
    void registrarCambioEstado_Incidente_debeLanzarExcepcionPorDatosFaltantes() {
        // Arrange: Incidente nulo
        Incidente incidenteNulo = null;
        Integer estadoAnterior = 1;
        Integer estadoNuevo = 2;
        String detalle = "Detalle";

        // Act & Assert
        IllegalArgumentException thrown = assertThrows(IllegalArgumentException.class, () -> {
            historialIncidenteService.registrarCambioEstado(incidenteNulo, estadoAnterior, estadoNuevo, detalle);
        });

        assertTrue(thrown.getMessage().contains("Faltan datos obligatorios o el incidente es nulo."));
        verify(historialIncidenteRepository, never()).save(any());
    }

    // Prueba para detalle vacío
    @Test
    void registrarCambioEstado_Incidente_debeLanzarExcepcionPorDetalleVacio() {
        // Arrange
        String detalleVacio = "  ";

        // Act & Assert
        IllegalArgumentException thrown = assertThrows(IllegalArgumentException.class, () -> {
            historialIncidenteService.registrarCambioEstado(incidenteBase, 1, 2, detalleVacio);
        });

        assertTrue(thrown.getMessage().contains("Faltan datos obligatorios"));
        verify(historialIncidenteRepository, never()).save(any());
    }

    // =========================================================================
    // PRUEBAS DE OBTENCIÓN DE HISTORIAL POR INCIDENTE
    // =========================================================================

    @Test
    void obtenerHistorialPorIncidente_debeRetornarListaSiIncidenteExiste() {
        // Arrange
        final Integer ID_INCIDENTE = 100;
        List<HistorialIncidente> listaHistorial = Arrays.asList(historialBase);

        // Mockear la existencia del incidente
        when(incidenteService.findById(ID_INCIDENTE)).thenReturn(incidenteBase);

        // Mockear la búsqueda en el repositorio (usando el método con orden descendente)
        when(historialIncidenteRepository.findByIncidenteIdIncidente(ID_INCIDENTE)).thenReturn(listaHistorial);

        // Act
        List<HistorialIncidente> resultado = historialIncidenteService.obtenerHistorialPorIncidente(ID_INCIDENTE);

        // Assert
        assertNotNull(resultado);
        assertEquals(1, resultado.size());
        assertEquals(historialBase, resultado.get(0));
        verify(incidenteService, times(1)).findById(ID_INCIDENTE);
        verify(historialIncidenteRepository, times(1)).findByIncidenteIdIncidente(ID_INCIDENTE);
    }

    @Test
    void obtenerHistorialPorIncidente_debeLanzarExcepcionSiIncidenteNoExiste() {
        // Arrange
        final Integer ID_INCIDENTE_NO_EXISTE = 999;

        // Mockear la inexistencia del incidente devolviendo null
        when(incidenteService.findById(ID_INCIDENTE_NO_EXISTE)).thenReturn(null);

        // Act & Assert
        EntityNotFoundException thrown = assertThrows(EntityNotFoundException.class, () -> {
            historialIncidenteService.obtenerHistorialPorIncidente(ID_INCIDENTE_NO_EXISTE);
        });

        assertTrue(thrown.getMessage().contains("Incidente con ID 999 no encontrado."));
        verify(incidenteService, times(1)).findById(ID_INCIDENTE_NO_EXISTE);
        verify(historialIncidenteRepository, never()).findByIncidenteIdIncidente(any());
    }
}