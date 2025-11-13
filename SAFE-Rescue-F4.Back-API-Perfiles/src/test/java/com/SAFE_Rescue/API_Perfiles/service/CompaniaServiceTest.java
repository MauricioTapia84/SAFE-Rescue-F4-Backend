package com.SAFE_Rescue.API_Perfiles.service;

import com.SAFE_Rescue.API_Perfiles.config.GeolocalizacionClient;
import com.SAFE_Rescue.API_Perfiles.modelo.Compania;
import com.SAFE_Rescue.API_Perfiles.modelo.ComunaDTO;
import com.SAFE_Rescue.API_Perfiles.modelo.DireccionDTO;
import com.SAFE_Rescue.API_Perfiles.modelo.GeolocalizacionDTO;
import com.SAFE_Rescue.API_Perfiles.repositoy.CompaniaRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DataIntegrityViolationException;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
// Importar 'lenient'
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.*;

/**
 * Pruebas unitarias para la clase CompaniaService.
 * Utiliza Mockito para simular (mockear) las dependencias externas (Repository y Client).
 */
@ExtendWith(MockitoExtension.class)
public class CompaniaServiceTest {

    @Mock
    private CompaniaRepository companiaRepository;

    @Mock
    private GeolocalizacionClient geolocalizacionClient;

    @InjectMocks
    private CompaniaService companiaService;

    // Constante para el ID de dirección que se considera válido en las pruebas
    private static final Integer ID_DIRECCION_VALIDA = 1;
    private Compania compania; // Compañía base para las pruebas

    @BeforeEach
    void setUp() {
        // Inicialización de la compañía base
        compania = new Compania(1, "Compañía Central", "Alpha", LocalDate.now(), ID_DIRECCION_VALIDA);

        // 1. Crear el objeto DTO base.
        DireccionDTO baseDireccionDTO = new DireccionDTO(
                ID_DIRECCION_VALIDA,
                "Calle falsa",
                "123",
                "Villa villera",
                "Depto 123",
                new ComunaDTO(),
                new GeolocalizacionDTO()
        );

        // 2. Mocking del GeolocalizacionClient: por defecto, devuelve el DTO base para CUALQUIER ID.
        // Se usa lenient() para indicar a Mockito que no lance UnnecessaryStubbingException
        // si este stubbing no se utiliza en TODAS las pruebas.
        lenient().when(geolocalizacionClient.getDireccionById(anyInt()))
                .thenReturn(baseDireccionDTO);
    }

    // =========================================================================
    // PRUEBAS DE FIND ALL
    // =========================================================================

    @Test
    void findAll_debeRetornarListaDeCompanias() {
        // Arrange
        Compania c2 = new Compania(2, "Compañía Sur", "Beta", LocalDate.now(), 200);
        List<Compania> listaEsperada = Arrays.asList(compania, c2);
        when(companiaRepository.findAll()).thenReturn(listaEsperada);

        // Act
        List<Compania> resultado = companiaService.findAll();

        // Assert
        assertNotNull(resultado);
        assertEquals(2, resultado.size());
        assertEquals("Compañía Central", resultado.get(0).getNombre());
        verify(companiaRepository, times(1)).findAll();
    }

    // =========================================================================
    // PRUEBAS DE FIND BY ID
    // =========================================================================

    @Test
    void findById_debeRetornarCompaniaExistente() {
        // Arrange
        when(companiaRepository.findById(1)).thenReturn(Optional.of(compania));

        // Act
        Compania resultado = companiaService.findById(1);

        // Assert
        assertNotNull(resultado);
        assertEquals("Compañía Central", resultado.getNombre());
        verify(companiaRepository, times(1)).findById(1);
    }

    @Test
    void findById_debeLanzarExcepcionSiNoExiste() {
        // Arrange
        when(companiaRepository.findById(99)).thenReturn(Optional.empty());

        // Act & Assert
        NoSuchElementException thrown = assertThrows(NoSuchElementException.class, () -> {
            companiaService.findById(99);
        });

        assertTrue(thrown.getMessage().contains("Compañía no encontrada con ID: 99"));
        verify(companiaRepository, times(1)).findById(99);
    }

    // =========================================================================
    // PRUEBAS DE SAVE (GUARDAR)
    // =========================================================================

    @Test
    void save_debeGuardarCompaniaValidaExitosamente() {
        // Arrange
        compania.setIdCompania(null); // Simulamos que es una nueva compañía
        when(companiaRepository.save(any(Compania.class))).thenReturn(compania);

        // Act
        Compania resultado = companiaService.save(compania);

        // Assert
        assertNotNull(resultado);
        assertEquals("Compañía Central", resultado.getNombre());
        // Verificar que se llamó a la validación de dirección y al repositorio
        verify(geolocalizacionClient, times(1)).getDireccionById(ID_DIRECCION_VALIDA);
        verify(companiaRepository, times(1)).save(compania);
    }

    @Test
    void save_debeLanzarExcepcionSiNombreYaExiste() {
        // Arrange
        compania.setIdCompania(null);
        // Simulamos que el repositorio lanza DataIntegrityViolationException por nombre duplicado
        when(companiaRepository.save(any(Compania.class))).thenThrow(DataIntegrityViolationException.class);

        // Act & Assert
        IllegalArgumentException thrown = assertThrows(IllegalArgumentException.class, () -> {
            companiaService.save(compania);
        });

        assertTrue(thrown.getMessage().contains("El nombre de la compañía ya existe o faltan datos obligatorios."));
        // La validación de la dirección se llama ANTES de intentar el save en el repositorio
        verify(geolocalizacionClient, times(1)).getDireccionById(ID_DIRECCION_VALIDA);
    }

    // =========================================================================
    // PRUEBAS DE VALIDACIÓN INTERNA (SAVE & UPDATE)
    // =========================================================================

    @Test
    void save_debeLanzarExcepcionSiNombreEsNulo() {
        // Arrange
        compania.setNombre(null);

        // Act & Assert
        IllegalArgumentException thrown = assertThrows(IllegalArgumentException.class, () -> {
            companiaService.save(compania);
        });
        assertTrue(thrown.getMessage().contains("El nombre de la compañía es obligatorio."));
        verify(geolocalizacionClient, never()).getDireccionById(anyInt());
        verify(companiaRepository, never()).save(any(Compania.class));
    }

    @Test
    void save_debeLanzarExcepcionSiNombreExcedeLimite() {
        // Arrange
        // Generar un nombre de 51 caracteres (límite es 50)
        String nombreLargo = "a".repeat(51);
        compania.setNombre(nombreLargo);

        // Act & Assert
        IllegalArgumentException thrown = assertThrows(IllegalArgumentException.class, () -> {
            companiaService.save(compania);
        });
        assertTrue(thrown.getMessage().contains("El nombre no puede exceder los 50 caracteres."));
        verify(geolocalizacionClient, never()).getDireccionById(anyInt());
    }

    @Test
    void save_debeLanzarExcepcionSiIdDireccionEsNulo() {
        // Arrange
        compania.setIdDireccion(null);

        // Act & Assert
        IllegalArgumentException thrown = assertThrows(IllegalArgumentException.class, () -> {
            companiaService.save(compania);
        });
        assertTrue(thrown.getMessage().contains("El ID de la dirección de la compañía es obligatorio."));
        verify(geolocalizacionClient, never()).getDireccionById(anyInt());
    }

    @Test
    void save_debeLanzarExcepcionSiIdDireccionNoExisteEnApiExterna() {
        // Arrange
        Integer idDireccionInvalida = 999;
        compania.setIdDireccion(idDireccionInvalida);

        // Simular que el cliente externo lanza NoSuchElementException para la dirección inválida
        // Sobrescribir el comportamiento por defecto de anyInt() solo para el ID inválido
        when(geolocalizacionClient.getDireccionById(idDireccionInvalida))
                .thenThrow(new NoSuchElementException("Dirección no encontrada"));

        // Act & Assert
        IllegalArgumentException thrown = assertThrows(IllegalArgumentException.class, () -> {
            companiaService.save(compania);
        });
        assertTrue(thrown.getMessage().contains("no existe en la API externa de Geolocalización."));
        verify(geolocalizacionClient, times(1)).getDireccionById(idDireccionInvalida);
        verify(companiaRepository, never()).save(any(Compania.class));
    }


    // =========================================================================
    // PRUEBAS DE UPDATE (ACTUALIZAR)
    // =========================================================================

    @Test
    void update_debeActualizarCompaniaExistenteExitosamente() {
        // Arrange
        final int ID_COMPANIA = 1;
        final String NOMBRE_NUEVO = "Compañía Actualizada";
        final int ID_DIRECCION_NUEVO = 300;

        // Crear el DTO que el mock DEBE DEVOLVER cuando se le pide ID_DIRECCION_NUEVO
        DireccionDTO nuevaDireccionDTO = new DireccionDTO(
                ID_DIRECCION_NUEVO,
                "Calle verdadera",
                "1231",
                "Villa villosa",
                "Depto 1231",
                new ComunaDTO(),
                new GeolocalizacionDTO()
        );

        // Compañía con los nuevos detalles (solo se usan nombre e idDireccion)
        Compania detallesNuevos = new Compania(null, NOMBRE_NUEVO, "Gamma", LocalDate.now(), ID_DIRECCION_NUEVO);

        // Compañía existente (la que se busca y se modifica)
        Compania companiaExistente = new Compania(ID_COMPANIA, "Compañía Antigua", "Antiguo", LocalDate.now().minusYears(1), 100);

        // 1. Mockeamos la búsqueda
        when(companiaRepository.findById(ID_COMPANIA)).thenReturn(Optional.of(companiaExistente));

        // 2. Mockeamos la validación de la nueva dirección (Sobrescribimos el comportamiento default)
        when(geolocalizacionClient.getDireccionById(ID_DIRECCION_NUEVO))
                .thenReturn(nuevaDireccionDTO);

        // 3. Mockeamos el guardado final (Devolvemos el objeto modificado)
        when(companiaRepository.save(any(Compania.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // Act
        Compania resultado = companiaService.update(detallesNuevos, ID_COMPANIA);

        // Assert
        assertNotNull(resultado);
        // Verifica que el objeto devuelto es el existente con los datos actualizados
        assertEquals(ID_COMPANIA, resultado.getIdCompania());
        assertEquals(NOMBRE_NUEVO, resultado.getNombre());
        assertEquals(ID_DIRECCION_NUEVO, resultado.getIdDireccion());

        // Verifica que los otros campos se mantuvieron (si la lógica del servicio es correcta)
        assertEquals("Antiguo", resultado.getCodigo());

        // Verificaciones de interacción
        verify(geolocalizacionClient, times(1)).getDireccionById(ID_DIRECCION_NUEVO);
        verify(companiaRepository, times(1)).findById(ID_COMPANIA);
        // Verifica que se llamó a save con la instancia EXISTENTE y MODIFICADA
        verify(companiaRepository, times(1)).save(companiaExistente);
    }

    @Test
    void update_debeLanzarExcepcionSiCompaniaNoExiste() {
        // Arrange
        final int ID_COMPANIA_NO_EXISTE = 99;
        final int ID_DIRECCION_NUEVO = 300;

        Compania detallesNuevos = new Compania(null, "Compañía Actualizada", "Gamma", LocalDate.now(), ID_DIRECCION_NUEVO);

        // Mockear que no se encuentra la compañía
        when(companiaRepository.findById(ID_COMPANIA_NO_EXISTE)).thenReturn(Optional.empty());

        // La validación de la nueva dirección pasará automáticamente gracias al lenient().when(anyInt()) en setUp()

        // Act & Assert
        NoSuchElementException thrown = assertThrows(NoSuchElementException.class, () -> {
            companiaService.update(detallesNuevos, ID_COMPANIA_NO_EXISTE);
        });

        assertTrue(thrown.getMessage().contains("Compañía no encontrada con ID: 99"));
        // La validación de la nueva dirección se llama ANTES de buscar la entidad
        verify(geolocalizacionClient, times(1)).getDireccionById(ID_DIRECCION_NUEVO);
        verify(companiaRepository, times(1)).findById(ID_COMPANIA_NO_EXISTE);
        verify(companiaRepository, never()).save(any(Compania.class));
    }

    @Test
    void update_debeLanzarExcepcionSiNuevoNombreYaExiste() {
        // Arrange
        final int ID_DIRECCION_NUEVO = 300;

        Compania detallesNuevos = new Compania(null, "Nombre Duplicado", "Gamma", LocalDate.now(), ID_DIRECCION_NUEVO);
        Compania companiaExistente = compania; // ID 1

        // Crear el DTO para el mock de la nueva dirección
        DireccionDTO nuevaDireccionDTO = new DireccionDTO(
                ID_DIRECCION_NUEVO,
                "Calle verdadera",
                "1231",
                "Villa villosa",
                "Depto 1123",
                new ComunaDTO(),
                new GeolocalizacionDTO()
        );

        // 1. Mockeamos la búsqueda (encuentra la entidad)
        when(companiaRepository.findById(1)).thenReturn(Optional.of(companiaExistente));

        // 2. Mockeamos la validación de dirección (nueva dirección)
        when(geolocalizacionClient.getDireccionById(ID_DIRECCION_NUEVO))
                .thenReturn(nuevaDireccionDTO);

        // 3. Mockeamos el guardado con DataIntegrityViolationException
        when(companiaRepository.save(any(Compania.class))).thenThrow(DataIntegrityViolationException.class);

        // Act & Assert
        IllegalArgumentException thrown = assertThrows(IllegalArgumentException.class, () -> {
            companiaService.update(detallesNuevos, 1);
        });

        assertTrue(thrown.getMessage().contains("El nombre de la compañía ya existe."));
        verify(geolocalizacionClient, times(1)).getDireccionById(ID_DIRECCION_NUEVO);
        // Se verifica que se intentó guardar la instancia existente, aunque falló
        verify(companiaRepository, times(1)).save(companiaExistente);
    }


    // =========================================================================
    // PRUEBAS DE DELETE (ELIMINAR)
    // =========================================================================

    @Test
    void delete_debeEliminarCompaniaExistenteExitosamente() {
        // Arrange
        when(companiaRepository.existsById(1)).thenReturn(true);
        // doNothing() es el comportamiento por defecto de void methods, pero se usa explícitamente por claridad
        doNothing().when(companiaRepository).deleteById(1);

        // Act
        companiaService.delete(1);

        // Assert
        verify(companiaRepository, times(1)).existsById(1);
        verify(companiaRepository, times(1)).deleteById(1);
    }

    @Test
    void delete_debeLanzarExcepcionSiCompaniaNoExiste() {
        // Arrange
        when(companiaRepository.existsById(99)).thenReturn(false);

        // Act & Assert
        NoSuchElementException thrown = assertThrows(NoSuchElementException.class, () -> {
            companiaService.delete(99);
        });

        assertTrue(thrown.getMessage().contains("Compañía no encontrada con ID: 99"));
        verify(companiaRepository, times(1)).existsById(99);
        verify(companiaRepository, never()).deleteById(anyInt());
    }

    @Test
    void delete_debeLanzarExcepcionDeIntegridadSiHayRegistrosAsociados() {
        // Arrange
        when(companiaRepository.existsById(1)).thenReturn(true);
        // Simulamos la excepción de Foreign Key Constraint
        doThrow(DataIntegrityViolationException.class).when(companiaRepository).deleteById(1);

        // Act & Assert
        DataIntegrityViolationException thrown = assertThrows(DataIntegrityViolationException.class, () -> {
            companiaService.delete(1);
        });

        assertTrue(thrown.getMessage().contains("No se puede eliminar la compañía con ID 1 porque tiene registros asociados"));
        verify(companiaRepository, times(1)).existsById(1);
        verify(companiaRepository, times(1)).deleteById(1);
    }
}