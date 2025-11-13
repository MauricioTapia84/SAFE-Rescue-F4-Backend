package com.SAFE_Rescue.API_Perfiles.controller;

import com.SAFE_Rescue.API_Perfiles.modelo.Ciudadano;
import com.SAFE_Rescue.API_Perfiles.service.CiudadanoService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;

import java.util.NoSuchElementException;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(CiudadanoController.class)
public class CiudadanoControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    // Simula la capa de servicio para aislar el controlador
    @MockitoBean
    private CiudadanoService ciudadanoService;

    private Ciudadano ciudadanoEjemplo;

    @BeforeEach
    void setUp() {
        // Inicialización de un objeto Ciudadano de ejemplo
        ciudadanoEjemplo = new Ciudadano();
        ciudadanoEjemplo.setIdUsuario(1);
        ciudadanoEjemplo.setRun("11.111.111-1");
        ciudadanoEjemplo.setNombre("Juan");
        ciudadanoEjemplo.setAPaterno("Perez");
        ciudadanoEjemplo.setCorreo("juan.perez@test.cl");
    }

    // -------------------------------------------------------------------------
    //                              TEST: CREACIÓN (POST)
    // -------------------------------------------------------------------------

    @Test
    void crearCiudadano_DebeRetornarStatus201_Y_CiudadanoCreado() throws Exception {
        // GIVEN: Cuando se llama a save en el servicio, retorna el ciudadanoEjemplo
        when(ciudadanoService.save(any(Ciudadano.class))).thenReturn(ciudadanoEjemplo);

        // WHEN & THEN
        mockMvc.perform(post("/api/v1/ciudadanos")
                        .contentType(MediaType.APPLICATION_JSON)
                        // Convierte el objeto a JSON para el cuerpo de la petición
                        .content(objectMapper.writeValueAsString(ciudadanoEjemplo)))
                .andExpect(status().isCreated()) // Espera HTTP 201
                .andExpect(jsonPath("$.idUsuario").value(1))
                .andExpect(jsonPath("$.nombre").value("Juan"));
    }

    @Test
    void crearCiudadano_DebeRetornarStatus400_CuandoHayErrorDeNegocio() throws Exception {
        // GIVEN: Simular que el servicio lanza una excepción de negocio (ej. DNI/RUN duplicado)
        String errorMessage = "El RUN o correo electrónico ya están registrados.";
        when(ciudadanoService.save(any(Ciudadano.class)))
                .thenThrow(new IllegalArgumentException(errorMessage));

        // WHEN & THEN
        mockMvc.perform(post("/api/v1/ciudadanos")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(ciudadanoEjemplo)))
                .andExpect(status().isBadRequest()) // Espera HTTP 400
                .andExpect(content().string(errorMessage));
    }

    // -------------------------------------------------------------------------
    //                              TEST: OBTENER POR ID (GET)
    // -------------------------------------------------------------------------

    @Test
    void getCiudadanoById_DebeRetornarStatus200_Y_Ciudadano() throws Exception {
        // GIVEN
        int idExistente = 1;
        when(ciudadanoService.findById(idExistente)).thenReturn(ciudadanoEjemplo);

        // WHEN & THEN
        mockMvc.perform(get("/api/v1/ciudadanos/{id}", idExistente)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk()) // Espera HTTP 200
                .andExpect(jsonPath("$.idUsuario").value(idExistente))
                .andExpect(jsonPath("$.nombre").value("Juan"));
    }

    @Test
    void getCiudadanoById_DebeRetornarStatus404_CuandoNoExiste() throws Exception {
        // GIVEN: Simular que el servicio no encuentra el recurso
        int idNoExistente = 99;
        String errorMessage = "Ciudadano con ID 99 no encontrado";
        when(ciudadanoService.findById(idNoExistente))
                .thenThrow(new NoSuchElementException(errorMessage));

        // WHEN & THEN
        mockMvc.perform(get("/api/v1/ciudadanos/{id}", idNoExistente)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound()) // Espera HTTP 404
                .andExpect(content().string(errorMessage));
    }

    // -------------------------------------------------------------------------
    //                              TEST: ACTUALIZACIÓN (PUT)
    // -------------------------------------------------------------------------

    @Test
    void actualizarCiudadano_DebeRetornarStatus200_Y_CiudadanoActualizado() throws Exception {
        // GIVEN
        int idAActualizar = 1;
        Ciudadano ciudadanoModificado = new Ciudadano();
        ciudadanoModificado.setIdUsuario(idAActualizar);
        ciudadanoModificado.setNombre("Javier"); // Nuevo nombre

        when(ciudadanoService.save(any(Ciudadano.class))).thenReturn(ciudadanoModificado);

        // WHEN & THEN
        mockMvc.perform(put("/api/v1/ciudadanos/{id}", idAActualizar)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(ciudadanoModificado)))
                .andExpect(status().isOk()) // Espera HTTP 200
                .andExpect(jsonPath("$.idUsuario").value(idAActualizar))
                .andExpect(jsonPath("$.nombre").value("Javier"));
    }

    @Test
    void actualizarCiudadano_DebeRetornarStatus404_CuandoNoExiste() throws Exception {
        // GIVEN: Simular que el servicio no encuentra el ID para actualizar
        int idNoExistente = 99;
        String errorMessage = "Ciudadano a actualizar no encontrado";
        Ciudadano datosActualizar = new Ciudadano();
        datosActualizar.setNombre("Falsa"); // Cuerpo de la petición

        when(ciudadanoService.save(any(Ciudadano.class)))
                .thenThrow(new NoSuchElementException(errorMessage));

        // WHEN & THEN
        mockMvc.perform(put("/api/v1/ciudadanos/{id}", idNoExistente)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(datosActualizar)))
                .andExpect(status().isNotFound()) // Espera HTTP 404
                .andExpect(content().string(errorMessage));
    }

    // -------------------------------------------------------------------------
    //                              TEST: ELIMINACIÓN (DELETE)
    // -------------------------------------------------------------------------

    @Test
    void eliminarCiudadano_DebeRetornarStatus204_CuandoEliminacionExitosa() throws Exception {
        // GIVEN: No se espera que el servicio devuelva nada, solo que se ejecute.
        int idAEliminar = 1;
        doNothing().when(ciudadanoService).delete(idAEliminar);

        // WHEN & THEN
        mockMvc.perform(delete("/api/v1/ciudadanos/{id}", idAEliminar)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNoContent()) // Espera HTTP 204
                .andExpect(content().string("")); // El cuerpo debe estar vacío

        // Verifica que el método delete haya sido llamado una vez con el ID correcto
        verify(ciudadanoService, times(1)).delete(idAEliminar);
    }

    @Test
    void eliminarCiudadano_DebeRetornarStatus404_CuandoNoExiste() throws Exception {
        // GIVEN: Simular que el servicio lanza 404 porque no existe el recurso
        int idNoExistente = 99;
        String errorMessage = "Ciudadano a eliminar no encontrado";
        doThrow(new NoSuchElementException(errorMessage))
                .when(ciudadanoService).delete(idNoExistente);

        // WHEN & THEN
        mockMvc.perform(delete("/api/v1/ciudadanos/{id}", idNoExistente)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound()) // Espera HTTP 404
                .andExpect(content().string(errorMessage));
    }

    @Test
    void eliminarCiudadano_DebeRetornarStatus409_CuandoHayConflictoDeIntegridad() throws Exception {
        // GIVEN: Simular un conflicto de integridad de datos (ej. usuario tiene dependencias)
        int idConDependencias = 2;
        String errorMessage = "No se pudo eliminar el ciudadano. Posiblemente existan referencias activas (Dependientes) que dependen de esta persona.";

        // El test asume que lanzas una RuntimeException (o una excepción custom)
        // con un mensaje clave que tu GlobalExceptionHandler mapeará a 409 CONFLICT.
        doThrow(new RuntimeException(errorMessage))
                .when(ciudadanoService).delete(idConDependencias);

        // WHEN & THEN
        mockMvc.perform(delete("/api/v1/ciudadanos/{id}", idConDependencias)
                        .contentType(MediaType.APPLICATION_JSON))
                // ¡Este test depende de que tu GlobalExceptionHandler mapee el mensaje a 409!
                .andExpect(status().isConflict()) // Espera HTTP 409
                .andExpect(content().string(errorMessage));
    }

}