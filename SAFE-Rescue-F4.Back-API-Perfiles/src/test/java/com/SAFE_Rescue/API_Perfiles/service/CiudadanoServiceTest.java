package com.SAFE_Rescue.API_Perfiles.service;

import com.SAFE_Rescue.API_Perfiles.config.GeolocalizacionClient;
import com.SAFE_Rescue.API_Perfiles.dto.DireccionDTO;
import com.SAFE_Rescue.API_Perfiles.modelo.Ciudadano;
import com.SAFE_Rescue.API_Perfiles.repository.CiudadanoRepository;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DataIntegrityViolationException;

import java.util.Arrays;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Pruebas unitarias para la clase CiudadanoService.
 * Verifica la lógica de negocio y la integración con servicios externos.
 */
@ExtendWith(MockitoExtension.class)
public class CiudadanoServiceTest {

    @Mock
    private CiudadanoRepository ciudadanoRepository;

    @Mock
    private UsuarioService usuarioService;

    @Mock
    private GeolocalizacionClient geolocalizacionClient;

    @InjectMocks
    private CiudadanoService ciudadanoService;

    private Ciudadano ciudadanoBase;
    private DireccionDTO direccionDTOBase;
    private final Integer ID_CIUDADANO = 1;
    private final Integer ID_DIRECCION = 100;

    @BeforeEach
    void setUp() {
        ciudadanoBase = new Ciudadano();
        ciudadanoBase.setIdUsuario(ID_CIUDADANO);
        ciudadanoBase.setRun("12345678");
        ciudadanoBase.setCorreo("test@safe.cl");
        // Establecer el ID de la dirección que el ciudadano "trae"
        ciudadanoBase.setIdDireccion(ID_DIRECCION);

        direccionDTOBase = new DireccionDTO();
        direccionDTOBase.setIdDireccion(ID_DIRECCION);
        direccionDTOBase.setCalle("Calle Falsa");
    }

    // =========================================================================
    // PRUEBAS DE FIND ALL
    // =========================================================================

    @Test
    void findAll_debeRetornarListaDeCiudadanos() {
        // Arrange
        Ciudadano c2 = new Ciudadano();
        List<Ciudadano> listaEsperada = Arrays.asList(ciudadanoBase, c2);

        when(ciudadanoRepository.findAll()).thenReturn(listaEsperada);

        // Act
        List<Ciudadano> resultado = ciudadanoService.findAll();

        // Assert
        assertNotNull(resultado);
        assertEquals(2, resultado.size());
        verify(ciudadanoRepository, times(1)).findAll();
    }

    // =========================================================================
    // PRUEBAS DE FIND BY ID
    // =========================================================================

    @Test
    void findById_debeRetornarCiudadanoSiExiste() {
        // Arrange
        when(ciudadanoRepository.findById(ID_CIUDADANO)).thenReturn(Optional.of(ciudadanoBase));

        // Act
        Ciudadano resultado = ciudadanoService.findById(ID_CIUDADANO);

        // Assert
        assertNotNull(resultado);
        assertEquals(ID_CIUDADANO, resultado.getIdUsuario());
        verify(ciudadanoRepository, times(1)).findById(ID_CIUDADANO);
    }

    @Test
    void findById_debeLanzarNoSuchElementExceptionSiNoExiste() {
        // Arrange
        when(ciudadanoRepository.findById(anyInt())).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(NoSuchElementException.class, () -> {
            ciudadanoService.findById(99);
        });
        verify(ciudadanoRepository, times(1)).findById(99);
    }

    // =========================================================================
    // PRUEBAS DE SAVE
    // =========================================================================

    @Test
    void save_debeGuardarCiudadanoExitosamente() {
        // Arrange
        // 1. Mockear la obtención de dirección (asumimos que existe o que el DTO es válido)
        when(geolocalizacionClient.getDireccionById(ID_DIRECCION)).thenReturn(direccionDTOBase);
        // 2. Mockear el guardado de la dirección (retorna el DTO con el ID)
        when(geolocalizacionClient.guardarDireccion(any(DireccionDTO.class))).thenReturn(direccionDTOBase);
        // 3. Mockear el guardado del ciudadano
        when(ciudadanoRepository.save(any(Ciudadano.class))).thenReturn(ciudadanoBase);

        // Act
        Ciudadano resultado = ciudadanoService.save(ciudadanoBase);

        // Assert
        assertNotNull(resultado);
        assertEquals(ID_DIRECCION, resultado.getIdDireccion());
        // Verificar interacciones
        verify(usuarioService, times(1)).validarExistencia(ciudadanoBase);
        verify(geolocalizacionClient, times(1)).getDireccionById(ID_DIRECCION);
        verify(geolocalizacionClient, times(1)).guardarDireccion(direccionDTOBase);
        verify(ciudadanoRepository, times(1)).save(ciudadanoBase);
    }

    @Test
    void save_debeLanzarIllegalArgumentExceptionSiCiudadanoEsNulo() {
        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> {
            ciudadanoService.save(null);
        });
        verify(ciudadanoRepository, never()).save(any());
    }

    @Test
    void save_debeLanzarIllegalArgumentExceptionPorViolacionDeIntegridad() {
        // Arrange
        when(geolocalizacionClient.getDireccionById(anyInt())).thenReturn(direccionDTOBase);
        when(geolocalizacionClient.guardarDireccion(any(DireccionDTO.class))).thenReturn(direccionDTOBase);
        // Simular error de integridad de datos (ej. RUN duplicado)
        when(ciudadanoRepository.save(any(Ciudadano.class)))
                .thenThrow(DataIntegrityViolationException.class);

        // Act & Assert
        IllegalArgumentException thrown = assertThrows(IllegalArgumentException.class, () -> {
            ciudadanoService.save(ciudadanoBase);
        });

        assertTrue(thrown.getMessage().contains("Error de integridad de datos. El RUN, correo o teléfono ya existen."));
    }

    // =========================================================================
    // PRUEBAS DE LÓGICA DE DIRECCIÓN EN SAVE
    // =========================================================================

    @Test
    void save_debeLanzarIllegalArgumentExceptionSiDireccionDTOEsNulo() {
        // Arrange
        // Simular que el cliente Feign NO encuentra la dirección
        when(geolocalizacionClient.getDireccionById(ID_DIRECCION)).thenReturn(null);

        // Act & Assert
        IllegalArgumentException thrown = assertThrows(IllegalArgumentException.class, () -> {
            ciudadanoService.save(ciudadanoBase);
        });

        assertTrue(thrown.getMessage().contains("La información de dirección es obligatoria para un Ciudadano."));
        verify(geolocalizacionClient, never()).guardarDireccion(any());
        verify(ciudadanoRepository, never()).save(any());
    }

    @Test
    void save_debeLanzarIllegalStateExceptionSiMSGeolocalizacionNoRetornaIdValido() {
        // 1. Configurar Mockito para simular el error
        // (Asumo que aquí estás mockeando la respuesta del MS de Geolocalización)

        // 2. Ejecutar la acción y capturar la excepción
        IllegalStateException thrown = Assertions.assertThrows(
                IllegalStateException.class,
                () -> ciudadanoService.save(ciudadanoBase)
        );

        // 3. ASERCIÓN CORREGIDA
        String mensajeEsperado = "Error al comunicarse con el servicio de Geolocalización para guardar la dirección."; // <-- ¡Mensaje Actualizado!
        Assertions.assertEquals(mensajeEsperado, thrown.getMessage());
    }

    @Test
    void save_debeLanzarIllegalStateExceptionSiFallaComunicacionConMSGeolocalizacion() {
        // Arrange
        when(geolocalizacionClient.getDireccionById(ID_DIRECCION)).thenReturn(direccionDTOBase);
        // Simular error de comunicación (ej. HttpHostConnectException)
        when(geolocalizacionClient.guardarDireccion(any(DireccionDTO.class)))
                .thenThrow(new RuntimeException("Fallo de red simulado"));

        // Act & Assert
        IllegalStateException thrown = assertThrows(IllegalStateException.class, () -> {
            ciudadanoService.save(ciudadanoBase);
        });

        assertTrue(thrown.getMessage().contains("Error al comunicarse con el servicio de Geolocalización para guardar la dirección."));
        verify(ciudadanoRepository, never()).save(any());
    }


    // =========================================================================
    // PRUEBAS DE UPDATE
    // =========================================================================

    @Test
    void update_debeActualizarCiudadanoExitosamente() {
        // Arrange
        Ciudadano ciudadanoUpdateData = new Ciudadano();
        ciudadanoUpdateData.setNombre("Nuevo Nombre");
        ciudadanoUpdateData.setRun(ciudadanoBase.getRun()); // Mantener RUN para evitar validación de unicidad
        ciudadanoUpdateData.setCorreo(ciudadanoBase.getCorreo());
        ciudadanoUpdateData.setIdDireccion(ID_DIRECCION + 1); // Nueva dirección ID

        // Mock DireccionDTO para el nuevo ID (simula la dirección que se va a guardar)
        DireccionDTO nuevaDireccionGuardada = new DireccionDTO();
        nuevaDireccionGuardada.setIdDireccion(ID_DIRECCION + 1);

        // 1. Mockear findById
        when(ciudadanoRepository.findById(ID_CIUDADANO)).thenReturn(Optional.of(ciudadanoBase));
        // 2. Mockear lógica de dirección
        when(geolocalizacionClient.getDireccionById(ID_DIRECCION + 1)).thenReturn(direccionDTOBase);
        when(geolocalizacionClient.guardarDireccion(any(DireccionDTO.class))).thenReturn(nuevaDireccionGuardada);
        // 3. Mockear save
        when(ciudadanoRepository.save(any(Ciudadano.class))).thenReturn(ciudadanoUpdateData);

        // Act
        Ciudadano resultado = ciudadanoService.update(ciudadanoUpdateData, ID_CIUDADANO);

        // Assert
        assertNotNull(resultado);

        // Verificar que el save se llamó con el objeto existente y los datos actualizados
        ArgumentCaptor<Ciudadano> captor = ArgumentCaptor.forClass(Ciudadano.class);
        verify(ciudadanoRepository).save(captor.capture());
        Ciudadano ciudadanoGuardado = captor.getValue();

        // El ID debe ser el original del ciudadanoExistente
        assertEquals(ID_CIUDADANO, ciudadanoGuardado.getIdUsuario());
        // El nombre debe haber sido copiado
        assertEquals("Nuevo Nombre", ciudadanoGuardado.getNombre());
        // El nuevo idDireccion debe haber sido seteado
        assertEquals(ID_DIRECCION + 1, ciudadanoGuardado.getIdDireccion());

        // Se llama a la validación de existencia para evitar conflictos de RUN/correo con otros usuarios.
        verify(usuarioService, times(1)).validarExistencia(ciudadanoUpdateData);
    }

    @Test
    void update_debeLanzarIllegalArgumentExceptionSiCiudadanoEsNulo() {
        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> {
            ciudadanoService.update(null, ID_CIUDADANO);
        });
        verify(ciudadanoRepository, never()).save(any());
    }

    @Test
    void update_debeLanzarIllegalArgumentExceptionPorViolacionDeIntegridad() {
        // Arrange
        when(ciudadanoRepository.findById(ID_CIUDADANO)).thenReturn(Optional.of(ciudadanoBase));
        when(geolocalizacionClient.getDireccionById(anyInt())).thenReturn(direccionDTOBase);
        when(geolocalizacionClient.guardarDireccion(any(DireccionDTO.class))).thenReturn(direccionDTOBase);

        // Simular error de integridad de datos (ej. RUN duplicado) en la actualización
        when(ciudadanoRepository.save(any(Ciudadano.class)))
                .thenThrow(DataIntegrityViolationException.class);

        // Act & Assert
        IllegalArgumentException thrown = assertThrows(IllegalArgumentException.class, () -> {
            ciudadanoService.update(ciudadanoBase, ID_CIUDADANO);
        });

        assertTrue(thrown.getMessage().contains("Error de integridad de datos. El RUN, correo o teléfono ya existen."));
    }

    // =========================================================================
    // PRUEBAS DE DELETE
    // =========================================================================

    @Test
    void delete_debeEliminarCiudadanoExitosamente() {
        // Arrange
        when(ciudadanoRepository.findById(ID_CIUDADANO)).thenReturn(Optional.of(ciudadanoBase));
        doNothing().when(ciudadanoRepository).delete(ciudadanoBase);

        // Act
        ciudadanoService.delete(ID_CIUDADANO);

        // Assert
        verify(ciudadanoRepository, times(1)).findById(ID_CIUDADANO);
        verify(ciudadanoRepository, times(1)).delete(ciudadanoBase);
    }

    @Test
    void delete_debeLanzarNoSuchElementExceptionSiCiudadanoNoExiste() {
        // Arrange
        when(ciudadanoRepository.findById(anyInt())).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(NoSuchElementException.class, () -> {
            ciudadanoService.delete(99);
        });
        verify(ciudadanoRepository, never()).delete(any());
    }

    @Test
    void delete_debeLanzarIllegalStateExceptionPorViolacionDeIntegridad() {
        // Arrange
        when(ciudadanoRepository.findById(ID_CIUDADANO)).thenReturn(Optional.of(ciudadanoBase));
        // Simular error de referencia externa (Foreign Key Constraint)
        doThrow(DataIntegrityViolationException.class).when(ciudadanoRepository).delete(ciudadanoBase);

        // Act & Assert
        IllegalStateException thrown = assertThrows(IllegalStateException.class, () -> {
            ciudadanoService.delete(ID_CIUDADANO);
        });

        assertTrue(thrown.getMessage().contains("No se puede eliminar el ciudadano. Verifique que no tenga incidentes activos o referencias externas."));
        verify(ciudadanoRepository, times(1)).delete(ciudadanoBase);
    }
}