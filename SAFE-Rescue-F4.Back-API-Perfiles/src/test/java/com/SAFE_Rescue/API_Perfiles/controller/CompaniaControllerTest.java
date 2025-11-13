package com.SAFE_Rescue.API_Perfiles.controller;

import com.SAFE_Rescue.API_Perfiles.modelo.Compania;
import com.SAFE_Rescue.API_Perfiles.service.CompaniaService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Arrays;
import java.util.List;
import java.util.NoSuchElementException;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(CompaniaController.class)
class CompaniaControllerTest {

    private static final String API_URL = "/api-perfiles/v1/companias";

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    // Inyectamos un Mock del servicio para aislar el controlador
    @MockitoBean
    private CompaniaService companiaService;

    private Compania compania1;
    private Compania compania2;

    @BeforeEach
    void setUp() {
        // Inicialización de datos de prueba
        compania1 = new Compania();
        compania1.setIdCompania(1);
        compania1.setNombre("Compañía Central");

        compania2 = new Compania();
        compania2.setIdCompania(2);
        compania2.setNombre("Compañía Sur");
    }

    // --- GET ALL ---
    @Test
    void getAllCompanias_DebeRetornarListaYStatus200() throws Exception {
        List<Compania> companias = Arrays.asList(compania1, compania2);

        // Configuración del mock: cuando se llame a findAll, devuelve la lista
        when(companiaService.findAll()).thenReturn(companias);

        mockMvc.perform(get(API_URL)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk()) // Espera Status 200
                .andExpect(jsonPath("$.size()").value(companias.size()))
                .andExpect(jsonPath("$[0].nombre").value("Compañía Central"));

        // Verificación: se llamó al método del servicio una vez
        verify(companiaService, times(1)).findAll();
    }

    // --- GET BY ID ---
    @Test
    void getCompaniaById_DebeRetornarCompaniaYStatus200_CuandoExiste() throws Exception {
        // Configuración del mock: cuando se llame a findById con ID 1, devuelve compania1
        when(companiaService.findById(1)).thenReturn(compania1);

        mockMvc.perform(get(API_URL + "/1")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk()) // Espera Status 200
                .andExpect(jsonPath("$.idCompania").value(1))
                .andExpect(jsonPath("$.nombre").value("Compañía Central"));

        verify(companiaService, times(1)).findById(1);
    }

    @Test
    void getCompaniaById_DebeRetornarStatus404_CuandoNoExiste() throws Exception {
        // Configuración del mock: cuando se llame a findById con cualquier ID, lanza NoSuchElementException
        when(companiaService.findById(99)).thenThrow(new NoSuchElementException("Compañía con ID 99 no encontrada"));

        mockMvc.perform(get(API_URL + "/99")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound()); // Espera Status 404

        verify(companiaService, times(1)).findById(99);
    }

    // --- POST / CREATE ---
    @Test
    void createCompania_DebeRetornarCompaniaYStatus201_CuandoEsValida() throws Exception {
        Compania nuevaCompania = new Compania();
        nuevaCompania.setNombre("Nueva Compañía");

        Compania companiaGuardada = new Compania();
        companiaGuardada.setIdCompania(3);
        companiaGuardada.setNombre("Nueva Compañía");

        // Configuración del mock: cuando se llama a save, devuelve la compañía con el ID asignado
        when(companiaService.save(any(Compania.class))).thenReturn(companiaGuardada);

        mockMvc.perform(post(API_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(nuevaCompania))) // Cuerpo de la petición
                .andExpect(status().isCreated()) // Espera Status 201
                .andExpect(jsonPath("$.idCompania").value(3))
                .andExpect(jsonPath("$.nombre").value("Nueva Compañía"));

        // Verificación: se llamó al método save una vez
        verify(companiaService, times(1)).save(any(Compania.class));
    }

    @Test
    void createCompania_DebeRetornarStatus400_CuandoHayErrorDeNegocio() throws Exception {
        Compania companiaConProblema = new Compania();
        companiaConProblema.setNombre("Nombre Duplicado");

        // Configuración del mock: simula error de negocio (ej: nombre duplicado)
        when(companiaService.save(any(Compania.class)))
                .thenThrow(new IllegalArgumentException("El nombre de la compañía ya existe."));

        mockMvc.perform(post(API_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(companiaConProblema)))
                .andExpect(status().isBadRequest()); // Espera Status 400

        verify(companiaService, times(1)).save(any(Compania.class));
    }


    // --- PUT / UPDATE ---
    @Test
    void updateCompania_DebeRetornarCompaniaYStatus200_CuandoActualiza() throws Exception {
        Compania detallesActualizados = new Compania();
        detallesActualizados.setNombre("Compañía Central (Modificada)");

        Compania companiaActualizada = new Compania();
        companiaActualizada.setIdCompania(1);
        companiaActualizada.setNombre("Compañía Central (Modificada)");

        // Configuración del mock: cuando se llama a update con ID 1, devuelve la versión actualizada
        when(companiaService.update(any(Compania.class), eq(1))).thenReturn(companiaActualizada);

        mockMvc.perform(put(API_URL + "/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(detallesActualizados)))
                .andExpect(status().isOk()) // Espera Status 200
                .andExpect(jsonPath("$.idCompania").value(1))
                .andExpect(jsonPath("$.nombre").value("Compañía Central (Modificada)"));

        verify(companiaService, times(1)).update(any(Compania.class), eq(1));
    }

    @Test
    void updateCompania_DebeRetornarStatus404_CuandoNoExiste() throws Exception {
        Compania detalles = new Compania();
        detalles.setNombre("Falsa");

        // Configuración del mock: simula que la compañía no existe
        when(companiaService.update(any(Compania.class), eq(99)))
                .thenThrow(new NoSuchElementException("Compañía a actualizar no encontrada"));

        mockMvc.perform(put(API_URL + "/99")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(detalles)))
                .andExpect(status().isNotFound()); // Espera Status 404

        verify(companiaService, times(1)).update(any(Compania.class), eq(99));
    }

    @Test
    void updateCompania_DebeRetornarStatus400_CuandoHayConflictoDeDatos() throws Exception {
        Compania detalles = new Compania();
        detalles.setNombre("Nombre de otra compañía existente");

        // Configuración del mock: simula error de integridad (ej: nombre duplicado)
        when(companiaService.update(any(Compania.class), eq(1)))
                .thenThrow(new IllegalArgumentException("El nombre ya está en uso."));

        mockMvc.perform(put(API_URL + "/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(detalles)))
                .andExpect(status().isBadRequest()); // Espera Status 400

        verify(companiaService, times(1)).update(any(Compania.class), eq(1));
    }

    // --- DELETE ---
    @Test
    void deleteCompania_DebeRetornarStatus204_CuandoEliminaExitosamente() throws Exception {
        // No se necesita configuración del mock para éxito (void method)

        mockMvc.perform(delete(API_URL + "/1")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNoContent()); // Espera Status 204

        // Verificación: se llamó al método delete con el ID correcto
        verify(companiaService, times(1)).delete(1);
    }

    @Test
    void deleteCompania_DebeRetornarStatus404_CuandoNoExiste() throws Exception {
        // Configuración del mock: simula que la compañía no existe
        doThrow(new NoSuchElementException("Compañía a eliminar no encontrada"))
                .when(companiaService).delete(99);

        mockMvc.perform(delete(API_URL + "/99")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound()); // Espera Status 404. FIX: Se agregó el punto y coma final.

        verify(companiaService, times(1)).delete(99);
    }

    @Test
    void deleteCompania_DebeRetornarStatus409_CuandoHayConflictoDeIntegridad() throws Exception {
        // Configuración del mock: simula una excepción genérica (que simula DataIntegrityViolation)
        doThrow(new RuntimeException("Error de integridad de datos"))
                .when(companiaService).delete(1);

        mockMvc.perform(delete(API_URL + "/1")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isConflict()); // Espera Status 409

        verify(companiaService, times(1)).delete(1);
    }
}