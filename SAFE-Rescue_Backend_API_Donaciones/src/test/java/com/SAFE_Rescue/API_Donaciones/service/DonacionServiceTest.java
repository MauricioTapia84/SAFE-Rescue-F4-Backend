package com.SAFE_Rescue.API_Donaciones.service;

import com.SAFE_Rescue.API_Donaciones.config.UsuarioClient;
import com.SAFE_Rescue.API_Donaciones.modelo.Donacion;
import com.SAFE_Rescue.API_Donaciones.modelo.UsuarioDTO;
import com.SAFE_Rescue.API_Donaciones.repository.DonacionRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Clase de prueba unitaria para DonacionService.
 * Utiliza Mockito para simular las dependencias DonacionRepository y UsuarioClient.
 */
@ExtendWith(MockitoExtension.class)
public class DonacionServiceTest {

    // Mocks de las dependencias
    @Mock
    private DonacionRepository donacionRepository;
    @Mock
    private UsuarioClient usuarioClient;

    // Inyección de Mocks en el servicio a probar
    @InjectMocks
    private DonacionService donacionService;

    private Donacion donacionValida;
    private final Integer DONANTE_ID_EXISTENTE = 101;
    private final Integer DONANTE_ID_INEXISTENTE = 999;
    private final Integer DONACION_ID_EXISTENTE = 1;

    @BeforeEach
    void setUp() {
        donacionValida = new Donacion();
        donacionValida.setIdDonacion(DONACION_ID_EXISTENTE);
        donacionValida.setIdDonante(DONANTE_ID_EXISTENTE);
        donacionValida.setMonto(20000); // Monto en CLP (Integer)
        donacionValida.setMetodoPago("Tarjeta de Crédito");
        donacionValida.setFechaDonacion(LocalDateTime.now());
    }

    // --- Tests para findAll() ---

    @Test
    @DisplayName("Debe retornar una lista de donaciones cuando existen registros")
    void findAll_Success() {
        // Arrange
        Donacion d2 = new Donacion();
        d2.setIdDonacion(2);
        d2.setIdDonante(102);
        d2.setMonto(5000);
        List<Donacion> listaEsperada = Arrays.asList(donacionValida, d2);
        when(donacionRepository.findAll()).thenReturn(listaEsperada);

        // Act
        List<Donacion> resultado = donacionService.findAll();

        // Assert
        assertNotNull(resultado);
        assertEquals(2, resultado.size());
        verify(donacionRepository, times(1)).findAll();
    }

    @Test
    @DisplayName("Debe retornar una lista vacía cuando no hay donaciones")
    void findAll_EmptyList() {
        // Arrange
        when(donacionRepository.findAll()).thenReturn(List.of());

        // Act
        List<Donacion> resultado = donacionService.findAll();

        // Assert
        assertNotNull(resultado);
        assertTrue(resultado.isEmpty());
        verify(donacionRepository, times(1)).findAll();
    }

    // --- Tests para findById() ---

    @Test
    @DisplayName("Debe encontrar una donación por ID")
    void findById_Success() {
        // Arrange
        when(donacionRepository.findById(DONACION_ID_EXISTENTE)).thenReturn(Optional.of(donacionValida));

        // Act
        Donacion resultado = donacionService.findById(DONACION_ID_EXISTENTE);

        // Assert
        assertNotNull(resultado);
        assertEquals(DONACION_ID_EXISTENTE, resultado.getIdDonacion());
        verify(donacionRepository, times(1)).findById(DONACION_ID_EXISTENTE);
    }

    @Test
    @DisplayName("Debe lanzar NoSuchElementException si la donación no existe")
    void findById_NotFound() {
        // Arrange
        Integer idInexistente = 99;
        when(donacionRepository.findById(idInexistente)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(NoSuchElementException.class, () -> donacionService.findById(idInexistente),
                "Debe lanzar NoSuchElementException al no encontrar la donación.");
        verify(donacionRepository, times(1)).findById(idInexistente);
    }

    // --- Tests para save() ---

    @Test
    @DisplayName("Debe guardar una donación si el Donante ID es válido")
    void save_Success() {
        // Arrange
        // Simular que el Donante existe (API externa)
        when(usuarioClient.findById(DONANTE_ID_EXISTENTE)).thenReturn(Optional.of(new UsuarioDTO()));
        // Simular el guardado en el repositorio (devuelve la misma entidad con ID)
        when(donacionRepository.save(any(Donacion.class))).thenReturn(donacionValida);

        // Act
        Donacion resultado = donacionService.save(donacionValida);

        // Assert
        assertNotNull(resultado);
        assertEquals(DONACION_ID_EXISTENTE, resultado.getIdDonacion());
        verify(usuarioClient, times(1)).findById(DONANTE_ID_EXISTENTE);
        verify(donacionRepository, times(1)).save(donacionValida);
    }

    @Test
    @DisplayName("Debe lanzar IllegalArgumentException si el Donante ID no existe")
    void save_DonanteNotFound_ThrowsIllegalArgumentException() {
        // Arrange
        Donacion donacionInvalida = new Donacion();
        donacionInvalida.setIdDonante(DONANTE_ID_INEXISTENTE);
        donacionInvalida.setMonto(1000);
        donacionInvalida.setMetodoPago("Efectivo");

        // Simular que la API externa devuelve null (Donante no encontrado)
        when(usuarioClient.findById(DONANTE_ID_INEXISTENTE)).thenReturn(null);

        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> donacionService.save(donacionInvalida),
                "Debe lanzar IllegalArgumentException debido a Donante no encontrado.");
        verify(usuarioClient, times(1)).findById(DONANTE_ID_INEXISTENTE);
        verify(donacionRepository, never()).save(any(Donacion.class)); // Asegura que no se guarda nada
    }

    @Test
    @DisplayName("Debe lanzar IllegalArgumentException si el Donante ID es nulo")
    void save_DonanteIdNull_ThrowsIllegalArgumentException() {
        // Arrange
        Donacion donacionInvalida = new Donacion();
        donacionInvalida.setIdDonante(null);

        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> donacionService.save(donacionInvalida),
                "Debe lanzar IllegalArgumentException si el ID del donante es nulo.");
        verify(usuarioClient, never()).findById(any());
        verify(donacionRepository, never()).save(any(Donacion.class));
    }

    // --- Tests para update() ---

    @Test
    @DisplayName("Debe actualizar monto y método de pago con éxito")
    void update_MontoAndMetodoPago_Success() {
        // Arrange
        Donacion donacionActualizada = new Donacion();
        donacionActualizada.setMonto(50000); // Nuevo monto
        donacionActualizada.setMetodoPago("Transferencia"); // Nuevo método

        // 1. Simular que la donación existe
        when(donacionRepository.findById(DONACION_ID_EXISTENTE)).thenReturn(Optional.of(donacionValida));
        // 2. Simular el guardado
        when(donacionRepository.save(any(Donacion.class))).thenAnswer(i -> i.getArguments()[0]); // Devuelve el objeto pasado al save

        // Act
        Donacion resultado = donacionService.update(donacionActualizada, DONACION_ID_EXISTENTE);

        // Assert
        assertNotNull(resultado);
        assertEquals(50000, resultado.getMonto());
        assertEquals("Transferencia", resultado.getMetodoPago());
        // El Donante ID no debe cambiar ya que no se proporcionó uno nuevo
        assertEquals(DONANTE_ID_EXISTENTE, resultado.getIdDonante());
        verify(donacionRepository, times(1)).findById(DONACION_ID_EXISTENTE);
        verify(donacionRepository, times(1)).save(any(Donacion.class));
        verify(usuarioClient, never()).findById(any()); // No se llama a la validación de Donante
    }

    @Test
    @DisplayName("Debe actualizar el Donante ID si es diferente y el nuevo Donante existe")
    void update_DonanteIdChange_Success() {
        // Arrange
        Integer nuevoDonanteId = 105;
        Donacion donacionCambioDonante = new Donacion();
        donacionCambioDonante.setIdDonante(nuevoDonanteId);
        donacionCambioDonante.setMonto(80000);

        // 1. Simular Donación existente
        when(donacionRepository.findById(DONACION_ID_EXISTENTE)).thenReturn(Optional.of(donacionValida));
        // 2. Simular que el nuevo Donante existe
        when(usuarioClient.findById(nuevoDonanteId)).thenReturn(Optional.of(new UsuarioDTO()));
        // 3. Simular el guardado
        when(donacionRepository.save(any(Donacion.class))).thenAnswer(i -> i.getArguments()[0]);

        // Act
        Donacion resultado = donacionService.update(donacionCambioDonante, DONACION_ID_EXISTENTE);

        // Assert
        assertNotNull(resultado);
        assertEquals(nuevoDonanteId, resultado.getIdDonante());
        assertEquals(80000, resultado.getMonto());
        verify(usuarioClient, times(1)).findById(nuevoDonanteId); // Se verifica el nuevo Donante
        verify(donacionRepository, times(1)).save(any(Donacion.class));
    }

    @Test
    @DisplayName("Debe lanzar NoSuchElementException si intenta actualizar una donación inexistente")
    void update_NotFound_ThrowsNoSuchElementException() {
        // Arrange
        Integer idInexistente = 99;
        when(donacionRepository.findById(idInexistente)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(NoSuchElementException.class,
                () -> donacionService.update(donacionValida, idInexistente),
                "Debe lanzar NoSuchElementException al no encontrar la donación a actualizar.");
        verify(donacionRepository, times(1)).findById(idInexistente);
        verify(usuarioClient, never()).findById(any());
        verify(donacionRepository, never()).save(any(Donacion.class));
    }

    @Test
    @DisplayName("Debe lanzar IllegalArgumentException si intenta actualizar el Donante a uno inexistente")
    void update_NewDonanteNotFound_ThrowsIllegalArgumentException() {
        // Arrange
        Donacion donacionCambioDonante = new Donacion();
        donacionCambioDonante.setIdDonante(DONANTE_ID_INEXISTENTE); // Donante no existe

        // 1. Simular Donación existente
        when(donacionRepository.findById(DONACION_ID_EXISTENTE)).thenReturn(Optional.of(donacionValida));
        // 2. Simular que el nuevo Donante no existe
        when(usuarioClient.findById(DONANTE_ID_INEXISTENTE)).thenReturn(null);

        // Act & Assert
        assertThrows(IllegalArgumentException.class,
                () -> donacionService.update(donacionCambioDonante, DONACION_ID_EXISTENTE),
                "Debe lanzar IllegalArgumentException si el nuevo Donante ID no existe.");
        verify(donacionRepository, times(1)).findById(DONACION_ID_EXISTENTE);
        verify(usuarioClient, times(1)).findById(DONANTE_ID_INEXISTENTE);
        verify(donacionRepository, never()).save(any(Donacion.class));
    }

    @Test
    @DisplayName("Debe lanzar IllegalArgumentException si la entidad Donacion para actualizar es nula")
    void update_NullEntity_ThrowsIllegalArgumentException() {
        // Act & Assert
        assertThrows(IllegalArgumentException.class,
                () -> donacionService.update(null, DONACION_ID_EXISTENTE),
                "Debe lanzar IllegalArgumentException si la donación de entrada es nula.");
        verify(donacionRepository, never()).findById(any());
        verify(usuarioClient, never()).findById(any());
        verify(donacionRepository, never()).save(any(Donacion.class));
    }

    // --- Tests para delete() ---

    @Test
    @DisplayName("Debe eliminar una donación existente")
    void delete_Success() {
        // Arrange
        when(donacionRepository.findById(DONACION_ID_EXISTENTE)).thenReturn(Optional.of(donacionValida));
        doNothing().when(donacionRepository).delete(donacionValida);

        // Act
        donacionService.delete(DONACION_ID_EXISTENTE);

        // Assert
        verify(donacionRepository, times(1)).findById(DONACION_ID_EXISTENTE);
        verify(donacionRepository, times(1)).delete(donacionValida);
    }

    @Test
    @DisplayName("Debe lanzar NoSuchElementException si intenta eliminar una donación inexistente")
    void delete_NotFound_ThrowsNoSuchElementException() {
        // Arrange
        Integer idInexistente = 99;
        when(donacionRepository.findById(idInexistente)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(NoSuchElementException.class, () -> donacionService.delete(idInexistente),
                "Debe lanzar NoSuchElementException al no encontrar la donación a eliminar.");
        verify(donacionRepository, times(1)).findById(idInexistente);
        verify(donacionRepository, never()).delete(any(Donacion.class));
    }

    // --- Tests para findByDonante() ---

    @Test
    @DisplayName("Debe encontrar donaciones por ID de Donante")
    void findByDonante_Success() {
        // Arrange
        Donacion d2 = new Donacion();
        d2.setIdDonacion(2);
        d2.setIdDonante(DONANTE_ID_EXISTENTE);
        d2.setMonto(10000);
        List<Donacion> listaEsperada = Arrays.asList(donacionValida, d2);
        when(donacionRepository.findByIdDonante(DONANTE_ID_EXISTENTE)).thenReturn(listaEsperada);

        // Act
        List<Donacion> resultado = donacionService.findByDonante(DONANTE_ID_EXISTENTE);

        // Assert
        assertNotNull(resultado);
        assertEquals(2, resultado.size());
        verify(donacionRepository, times(1)).findByIdDonante(DONANTE_ID_EXISTENTE);
    }
}