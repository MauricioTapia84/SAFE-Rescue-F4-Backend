package com.SAFE_Rescue.API_Comunicacion.service;

import com.SAFE_Rescue.API_Comunicacion.modelo.Conversacion;
import com.SAFE_Rescue.API_Comunicacion.repository.ConversacionRepository;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
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
import static org.mockito.Mockito.*;

/**
 * Pruebas unitarias para la lógica de negocio de ConversacionService.
 * Se utiliza Mockito para simular el comportamiento del repositorio.
 */
@ExtendWith(MockitoExtension.class)
public class ConversacionServiceTest {

    // Simula el repositorio (dependencia)
    @Mock
    private ConversacionRepository conversacionRepository;

    // Inyecta las dependencias simuladas en el servicio que queremos probar
    @InjectMocks
    private ConversacionService conversacionService;

    // Datos de prueba
    private Conversacion conversacionValida;
    private final Integer ID_VALIDO = 1;
    private final String TIPO_VALIDO = "Emergencia";
    private final String NOMBRE_VALIDO = "Rescate 123";

    /**
     * Helper: Crea una instancia de Conversacion para pruebas.
     */
    private Conversacion createConversacion(Integer id, String tipo, String nombre) {
        Conversacion c = new Conversacion();
        // Asumiendo que la entidad tiene setters (aunque @Data de Lombok es común)
        c.setIdConversacion(id);
        c.setTipo(tipo);
        c.setNombre(nombre);
        c.setFechaCreacion(LocalDateTime.of(2025, 1, 1, 10, 0));
        return c;
    }

    @BeforeEach
    void setUp() {
        // Inicializa un objeto de conversación válido antes de cada prueba
        conversacionValida = createConversacion(ID_VALIDO, TIPO_VALIDO, NOMBRE_VALIDO);
    }

    // =========================================================================
    // iniciarNuevaConversacion(String tipo, String nombre)
    // =========================================================================

    @Test
    @DisplayName("Debe crear y guardar una nueva conversación con éxito")
    void iniciarNuevaConversacion_success() {
        // Simular el comportamiento del repositorio:
        // Cuando se llama a 'save' con cualquier objeto, devolverá el objeto de prueba.
        when(conversacionRepository.save(any(Conversacion.class))).thenReturn(conversacionValida);

        // Ejecutar el método del servicio
        Conversacion resultado = conversacionService.iniciarNuevaConversacion(TIPO_VALIDO, NOMBRE_VALIDO);

        // Verificar el resultado
        assertNotNull(resultado);
        assertEquals(TIPO_VALIDO, resultado.getTipo());
        assertEquals(NOMBRE_VALIDO, resultado.getNombre());

        // Verificar que el repositorio fue llamado exactamente una vez con 'save'
        verify(conversacionRepository, times(1)).save(any(Conversacion.class));
    }

    @Test
    @DisplayName("Debe lanzar IllegalArgumentException si el tipo es nulo")
    void iniciarNuevaConversacion_tipoNulo_throwsException() {
        // Verificar que se lanza la excepción esperada
        IllegalArgumentException thrown = assertThrows(IllegalArgumentException.class, () -> {
            conversacionService.iniciarNuevaConversacion(null, NOMBRE_VALIDO);
        });

        // Verificar el mensaje de la excepción
        assertEquals("El tipo de conversación es obligatorio.", thrown.getMessage());

        // Verificar que el repositorio NUNCA fue llamado
        verify(conversacionRepository, never()).save(any(Conversacion.class));
    }

    @Test
    @DisplayName("Debe lanzar IllegalArgumentException si el tipo está vacío")
    void iniciarNuevaConversacion_tipoVacio_throwsException() {
        // Verificar que se lanza la excepción esperada
        IllegalArgumentException thrown = assertThrows(IllegalArgumentException.class, () -> {
            conversacionService.iniciarNuevaConversacion(" ", NOMBRE_VALIDO);
        });

        // Verificar el mensaje de la excepción
        assertEquals("El tipo de conversación es obligatorio.", thrown.getMessage());

        // Verificar que el repositorio NUNCA fue llamado
        verify(conversacionRepository, never()).save(any(Conversacion.class));
    }

    // =========================================================================
    // findAll()
    // =========================================================================

    @Test
    @DisplayName("Debe retornar todas las conversaciones ordenadas por fecha de creación descendente")
    void findAll_success() {
        Conversacion conv2 = createConversacion(2, "Grupo", "Reunión", LocalDateTime.of(2025, 1, 2, 10, 0));
        List<Conversacion> listaConversaciones = Arrays.asList(conv2, conversacionValida);

        // Simular el comportamiento de findAll con Sort
        // Usamos eq(Sort.class) para igualar el argumento Sort.
        when(conversacionRepository.findAll(any(Sort.class))).thenReturn(listaConversaciones);

        // Ejecutar el método
        List<Conversacion> resultado = conversacionService.findAll();

        // Verificar el resultado
        assertNotNull(resultado);
        assertEquals(2, resultado.size());
        // No verificamos el orden aquí, solo la lista devuelta, ya que Mockito no ejecuta la lógica de ordenamiento real
        // Solo verificamos que se llamó al repositorio con el Sort correcto (a continuación).

        // Verificar que se llamó al repositorio con el Sort.by(DESC, "fechaCreacion")
        Sort expectedSort = Sort.by(Sort.Direction.DESC, "fechaCreacion");
        verify(conversacionRepository, times(1)).findAll(eq(expectedSort));
    }

    // =========================================================================
    // findById(Integer idConversacion)
    // =========================================================================

    @Test
    @DisplayName("Debe retornar la conversación si el ID existe")
    void findById_success() {
        // Simular que el repositorio devuelve la conversación
        when(conversacionRepository.findById(ID_VALIDO)).thenReturn(Optional.of(conversacionValida));

        // Ejecutar y verificar
        Conversacion resultado = conversacionService.findById(ID_VALIDO);

        assertNotNull(resultado);
        assertEquals(ID_VALIDO, resultado.getIdConversacion());

        verify(conversacionRepository, times(1)).findById(ID_VALIDO);
    }

    @Test
    @DisplayName("Debe lanzar NoSuchElementException si el ID no existe")
    void findById_notFound_throwsException() {
        final Integer ID_NO_EXISTE = 999;

        // Simular que el repositorio devuelve un Optional vacío
        when(conversacionRepository.findById(ID_NO_EXISTE)).thenReturn(Optional.empty());

        // Ejecutar y verificar la excepción
        NoSuchElementException thrown = assertThrows(NoSuchElementException.class, () -> {
            conversacionService.findById(ID_NO_EXISTE);
        });

        assertEquals("Conversación no encontrada con ID: " + ID_NO_EXISTE, thrown.getMessage());
        verify(conversacionRepository, times(1)).findById(ID_NO_EXISTE);
    }

    // =========================================================================
    // findByTipo(String tipo)
    // =========================================================================

    @Test
    @DisplayName("Debe retornar la lista de conversaciones filtradas por tipo")
    void findByTipo_success() {
        Conversacion conv2 = createConversacion(2, TIPO_VALIDO, "Otro rescate");
        List<Conversacion> listaFiltrada = Arrays.asList(conversacionValida, conv2);

        // Simular que el repositorio devuelve la lista filtrada
        when(conversacionRepository.findByTipo(TIPO_VALIDO)).thenReturn(listaFiltrada);

        // Ejecutar y verificar
        List<Conversacion> resultado = conversacionService.findByTipo(TIPO_VALIDO);

        assertNotNull(resultado);
        assertEquals(2, resultado.size());
        assertTrue(resultado.stream().allMatch(c -> c.getTipo().equals(TIPO_VALIDO)));

        verify(conversacionRepository, times(1)).findByTipo(TIPO_VALIDO);
    }

    // =========================================================================
    // delete(Integer idConversacion)
    // =========================================================================

    @Test
    @DisplayName("Debe eliminar la conversación si el ID existe")
    void delete_success() {
        // 1. Simular la búsqueda (findById)
        when(conversacionRepository.findById(ID_VALIDO)).thenReturn(Optional.of(conversacionValida));
        // 2. Simular la eliminación (no hace nada)
        doNothing().when(conversacionRepository).delete(conversacionValida);

        // Ejecutar el método
        assertDoesNotThrow(() -> conversacionService.delete(ID_VALIDO));

        // Verificar las interacciones: buscar y eliminar
        verify(conversacionRepository, times(1)).findById(ID_VALIDO);
        verify(conversacionRepository, times(1)).delete(conversacionValida);
    }

    @Test
    @DisplayName("Debe lanzar NoSuchElementException si la conversación a eliminar no existe")
    void delete_notFound_throwsException() {
        final Integer ID_NO_EXISTE = 999;

        // 1. Simular la búsqueda (findById) devuelve vacío
        when(conversacionRepository.findById(ID_NO_EXISTE)).thenReturn(Optional.empty());

        // Ejecutar y verificar la excepción
        assertThrows(NoSuchElementException.class, () -> {
            conversacionService.delete(ID_NO_EXISTE);
        });

        // Verificar que solo se llamó a findById, pero no a delete
        verify(conversacionRepository, times(1)).findById(ID_NO_EXISTE);
        verify(conversacionRepository, never()).delete(any(Conversacion.class));
    }

    // Helper: Sobrecarga para facilitar la creación de datos de prueba en findAll
    private Conversacion createConversacion(Integer id, String tipo, String nombre, LocalDateTime fecha) {
        Conversacion c = createConversacion(id, tipo, nombre);
        c.setFechaCreacion(fecha);
        return c;
    }
}