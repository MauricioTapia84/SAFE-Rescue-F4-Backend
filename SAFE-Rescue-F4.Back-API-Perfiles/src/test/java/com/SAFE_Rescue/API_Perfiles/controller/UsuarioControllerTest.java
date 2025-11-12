package com.SAFE_Rescue.API_Perfiles.controller;

import com.SAFE_Rescue.API_Perfiles.modelo.Usuario;
import com.SAFE_Rescue.API_Perfiles.service.UsuarioService;
import com.fasterxml.jackson.databind.ObjectMapper;
import net.datafaker.Faker;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.multipart.MultipartFile;

import java.util.Collections;
import java.util.List;
import java.util.NoSuchElementException;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(UsuarioController.class)
public class UsuarioControllerTest {

    // Ruta base definida en el controlador
    private final String BASE_URL = "/api-perfiles/v1/usuarios";

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private UsuarioService usuarioService;

    @Autowired
    private ObjectMapper objectMapper;

    private Faker faker;
    private Usuario usuario;
    private Integer id;

    @BeforeEach
    public void setUp() {
        faker = new Faker();
        id = faker.number().numberBetween(1, 100);

        // Crear la entidad Usuario simulada
        usuario = new Usuario();
        usuario.setIdUsuario(id);
        usuario.setNombre(faker.name().firstName());
        usuario.setAPaterno(faker.name().lastName());
        usuario.setCorreo(faker.internet().emailAddress());
        // Inicializar idFoto para simular el DTO/Modelo real
        usuario.setIdFoto(null);
    }

    // --- Pruebas de operaciones CRUD exitosas (Happy Path) ---

    @Test
    public void listarUsuariosTest_shouldReturnOkAndContent() throws Exception {
        // Arrange
        when(usuarioService.findAll()).thenReturn(List.of(usuario));

        // Act & Assert
        mockMvc.perform(get(BASE_URL))
                .andExpect(status().isOk()) // 200 OK
                .andExpect(jsonPath("$[0].idUsuario").value(usuario.getIdUsuario()))
                .andExpect(jsonPath("$[0].nombre").value(usuario.getNombre()));

        verify(usuarioService, times(1)).findAll();
    }

    @Test
    public void listarUsuariosTest_shouldReturnNoContent() throws Exception {
        // Arrange
        when(usuarioService.findAll()).thenReturn(Collections.emptyList());

        // Act & Assert
        mockMvc.perform(get(BASE_URL))
                .andExpect(status().isNoContent()); // 204 NO CONTENT

        verify(usuarioService, times(1)).findAll();
    }

    @Test
    public void buscarUsuarioTest_shouldReturnOkAndUsuario() throws Exception {
        // Arrange
        when(usuarioService.findById(id)).thenReturn(usuario);

        // Act & Assert
        mockMvc.perform(get(BASE_URL + "/{id}", id))
                .andExpect(status().isOk()) // 200 OK
                .andExpect(jsonPath("$.nombre").value(usuario.getNombre()));

        verify(usuarioService, times(1)).findById(id);
    }

    @Test
    public void agregarUsuarioTest_shouldReturnCreatedAndUsuario() throws Exception {
        // Arrange
        when(usuarioService.save(any(Usuario.class))).thenReturn(usuario);

        // Act & Assert
        mockMvc.perform(post(BASE_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(usuario)))
                .andExpect(status().isCreated()) // 201 CREATED
                .andExpect(jsonPath("$.idUsuario").value(usuario.getIdUsuario()));

        verify(usuarioService, times(1)).save(any(Usuario.class));
    }

    @Test
    public void actualizarUsuarioTest_shouldReturnOkAndUsuario() throws Exception {
        // Arrange
        Usuario usuarioActualizado = usuario;
        usuarioActualizado.setNombre("Nuevo Nombre");
        when(usuarioService.update(any(Usuario.class), eq(id))).thenReturn(usuarioActualizado);

        // Act & Assert
        mockMvc.perform(put(BASE_URL + "/{id}", id)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(usuarioActualizado)))
                .andExpect(status().isOk()) // 200 OK
                .andExpect(jsonPath("$.nombre").value("Nuevo Nombre"));

        verify(usuarioService, times(1)).update(any(Usuario.class), eq(id));
    }

    @Test
    public void eliminarUsuarioTest_shouldReturnOkAndSuccessMessage() throws Exception {
        // Arrange
        doNothing().when(usuarioService).delete(id);

        // Act & Assert
        mockMvc.perform(delete(BASE_URL + "/{id}", id))
                .andExpect(status().isOk()) // 200 OK
                .andExpect(content().string("Usuario eliminado con éxito."));

        verify(usuarioService, times(1)).delete(id);
    }

    // --- Pruebas de GESTIÓN DE FOTOS exitosas ---

    @Test
    public void subirFotoUsuarioTest_shouldReturnOkAndUpdatedUsuario() throws Exception {
        // Arrange
        MockMultipartFile mockFile = new MockMultipartFile(
                "foto", // debe coincidir con @RequestParam("foto")
                "perfil.jpg",
                MediaType.IMAGE_JPEG_VALUE,
                "contenido de prueba".getBytes()
        );

        Usuario usuarioConFoto = usuario;
        final Integer mockIdFoto = 99;
        usuarioConFoto.setIdFoto(mockIdFoto);

        when(usuarioService.subirYActualizarFotoUsuario(eq(id), any(MultipartFile.class))).thenReturn(usuarioConFoto);

        // Act & Assert
        mockMvc.perform(multipart(BASE_URL + "/{id}/subir-foto", id)
                        .file(mockFile))
                .andExpect(status().isOk()) // 200 OK
                .andExpect(jsonPath("$.idFoto").value(mockIdFoto));

        verify(usuarioService, times(1)).subirYActualizarFotoUsuario(eq(id), any(MultipartFile.class));
    }

    // --- Pruebas de escenarios de error (404 Not Found) ---

    @Test
    public void buscarUsuarioTest_NotFound() throws Exception {
        // Arrange
        when(usuarioService.findById(id)).thenThrow(new NoSuchElementException("Usuario no encontrado"));

        // Act & Assert
        mockMvc.perform(get(BASE_URL + "/{id}", id))
                .andExpect(status().isNotFound()); // 404 NOT FOUND
    }

    @Test
    public void actualizarUsuarioTest_NotFound() throws Exception {
        // Arrange
        when(usuarioService.update(any(Usuario.class), eq(id)))
                .thenThrow(new NoSuchElementException("Usuario no encontrado"));

        // Act & Assert
        mockMvc.perform(put(BASE_URL + "/{id}", id)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(usuario)))
                .andExpect(status().isNotFound()); // 404 NOT FOUND
    }

    @Test
    public void eliminarUsuarioTest_NotFound() throws Exception {
        // Arrange
        doThrow(new NoSuchElementException("Usuario no encontrado")).when(usuarioService).delete(id);

        // Act & Assert
        mockMvc.perform(delete(BASE_URL + "/{id}", id))
                .andExpect(status().isNotFound()); // 404 NOT FOUND
    }

    @Test
    public void subirFotoUsuarioTest_UserNotFound() throws Exception {
        // Arrange
        MockMultipartFile mockFile = new MockMultipartFile("foto", "perfil.jpg", MediaType.IMAGE_JPEG_VALUE, "contenido".getBytes());
        when(usuarioService.subirYActualizarFotoUsuario(eq(id), any(MultipartFile.class)))
                .thenThrow(new NoSuchElementException("Usuario no encontrado"));

        // Act & Assert
        mockMvc.perform(multipart(BASE_URL + "/{id}/subir-foto", id).file(mockFile))
                .andExpect(status().isNotFound()); // 404 NOT FOUND
    }

    // --- Pruebas de escenarios de error (400 Bad Request) ---

    @Test
    public void agregarUsuarioTest_BadRequest_ValidationError() throws Exception {
        // Arrange
        final String errorMessage = "Error: El correo ya está registrado.";
        when(usuarioService.save(any(Usuario.class))).thenThrow(new IllegalArgumentException(errorMessage));

        // Act & Assert
        mockMvc.perform(post(BASE_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(usuario)))
                .andExpect(status().isBadRequest()) // 400 BAD REQUEST
                .andExpect(content().string(errorMessage));
    }

    @Test
    public void eliminarUsuarioTest_BadRequest_DependencyError() throws Exception {
        // Arrange
        final String errorMessage = "No se puede eliminar el usuario porque es jefe de un equipo.";
        doThrow(new IllegalStateException(errorMessage)).when(usuarioService).delete(id);

        // Act & Assert
        mockMvc.perform(delete(BASE_URL + "/{id}", id))
                .andExpect(status().isBadRequest()) // 400 BAD REQUEST
                .andExpect(content().string(errorMessage));
    }

    // --- Pruebas de escenarios de error (500 Internal Server Error) ---

    @Test
    public void subirFotoUsuarioTest_InternalServerError() throws Exception {
        // Arrange
        MockMultipartFile mockFile = new MockMultipartFile("foto", "perfil.jpg", MediaType.IMAGE_JPEG_VALUE, "contenido".getBytes());
        final String errorMessage = "Error al conectar con el servicio de almacenamiento.";

        // Simular el fallo del servicio para forzar el 500
        doThrow(new RuntimeException(errorMessage))
                .when(usuarioService)
                .subirYActualizarFotoUsuario(eq(id), any(MultipartFile.class));

        // Act & Assert
        mockMvc.perform(multipart(BASE_URL + "/{id}/subir-foto", id).file(mockFile))
                .andExpect(status().isInternalServerError());
    }
}