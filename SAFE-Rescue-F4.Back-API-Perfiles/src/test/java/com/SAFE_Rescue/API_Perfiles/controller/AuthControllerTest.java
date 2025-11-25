package com.SAFE_Rescue.API_Perfiles.controller;

import com.SAFE_Rescue.API_Perfiles.config.SecurityConfig;
import com.SAFE_Rescue.API_Perfiles.dto.CoordenadasDTO;
import com.SAFE_Rescue.API_Perfiles.dto.DireccionRequestDTO;
import com.SAFE_Rescue.API_Perfiles.dto.RegistroRequestDTO;
import com.SAFE_Rescue.API_Perfiles.exception.UserAlreadyExistsException;
import com.SAFE_Rescue.API_Perfiles.modelo.Ciudadano;
import com.SAFE_Rescue.API_Perfiles.service.AuthService;
import com.SAFE_Rescue.API_Perfiles.service.UsuarioService; // Usado solo si es necesario, pero AuthService es el principal
import com.SAFE_Rescue.API_Perfiles.util.JwtUtil;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;


import static org.hamcrest.Matchers.containsString;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Pruebas de Integración para AuthController utilizando MockMvc.
 * Se simula el flujo HTTP y se aísla la capa de servicio (AuthService).
 */
@WebMvcTest(controllers = AuthController.class,
        excludeAutoConfiguration = SecurityAutoConfiguration.class,
        properties = "spring.main.allow-bean-definition-overriding=true")
@Import({SecurityConfig.class, JwtUtil.class})
public class AuthControllerTest {

    private static final String AUTH_URL = "/api-perfiles/v1/auth";
    private static final String REGISTER_CIUDADANO_URL = AUTH_URL + "/register-ciudadano";

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    // Inyectamos mocks para las dependencias del Controller
    @MockitoBean
    private AuthService authService;

    @MockitoBean
    private UsuarioService usuarioService;

    @MockitoBean
    private JwtUtil jwtUtil;

    private RegistroRequestDTO registroValido;
    private Ciudadano ciudadanoEsperado;

    @BeforeEach
    void setUp() {
        CoordenadasDTO coordenadas = new CoordenadasDTO();
        coordenadas.setLatitud(-33.45694);
        coordenadas.setLongitud(-70.64827);

        DireccionRequestDTO direccion = new DireccionRequestDTO();
        direccion.setCalle("Avenida Principal");
        direccion.setNumero("123");
        direccion.setVilla("Villa Los Olivos");
        direccion.setComplemento("Casa 45");
        direccion.setIdComuna(1);
        direccion.setCoordenadas(coordenadas);

        registroValido = new RegistroRequestDTO();
        registroValido.setRun("17123456");
        registroValido.setDv("7");
        registroValido.setNombre("Ricardo");
        registroValido.setAPaterno("Vargas");
        registroValido.setAMaterno("Muñoz");
        registroValido.setTelefono("987654321");
        registroValido.setCorreo("ricardo.vargas@test.com");
        registroValido.setContrasenia("passwordSeguro123");
        registroValido.setDireccion(direccion);


        // --- 2. Simular el objeto que devuelve el servicio ---
        ciudadanoEsperado = new Ciudadano();
        ciudadanoEsperado.setIdUsuario(10);
        ciudadanoEsperado.setNombre(registroValido.getNombre());
        ciudadanoEsperado.setCorreo(registroValido.getCorreo());
        ciudadanoEsperado.setRun(registroValido.getRun());
    }

    // =================================================================
    // 🚀 TESTS DE REGISTRO
    // =================================================================

    @Test
    void registerCiudadano_Success_Returns201() throws Exception {
        // Arrange
        when(authService.registerNewCiudadano(any(RegistroRequestDTO.class)))
                .thenReturn(ciudadanoEsperado);

        // Act & Assert
        mockMvc.perform(post(REGISTER_CIUDADANO_URL) // URL CORREGIDA
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(registroValido)))
                .andExpect(status().isCreated()) // Espera 201
                .andExpect(jsonPath("$.nombre").value(registroValido.getNombre()))
                .andExpect(jsonPath("$.run").value(registroValido.getRun()));

        verify(authService, times(1)).registerNewCiudadano(any(RegistroRequestDTO.class));
    }

    @Test
    void registerCiudadano_UserAlreadyExists_Returns409() throws Exception {
        // Arrange
        final String errorMessage = "El RUN ya se encuentra registrado.";

        // Mock: Simular la excepción de negocio
        when(authService.registerNewCiudadano(any(RegistroRequestDTO.class)))
                .thenThrow(new UserAlreadyExistsException(errorMessage));

        // Act & Assert
        mockMvc.perform(post(REGISTER_CIUDADANO_URL) // URL CORREGIDA
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(registroValido)))
                .andExpect(status().isConflict()) // Espera 409
                .andExpect(content().string(errorMessage)); // Verifica el cuerpo del mensaje

        verify(authService, times(1)).registerNewCiudadano(any(RegistroRequestDTO.class));
    }

    @Test
    void registerCiudadano_InvalidBody_Returns400() throws Exception {
        // Arrange
        RegistroRequestDTO registroInvalido = new RegistroRequestDTO();
        registroInvalido.setRun(null);

        // Act & Assert
        mockMvc.perform(post(REGISTER_CIUDADANO_URL) // URL CORREGIDA
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(registroInvalido)))
                .andExpect(status().isBadRequest()); // Espera 400

        // Verificar que el servicio NUNCA fue llamado, ya que falló la validación
        verify(authService, never()).registerNewCiudadano(any(RegistroRequestDTO.class));
    }

    @Test
    void registerCiudadano_InternalError_Returns500() throws Exception {
        // Arrange
        final String internalErrorMessage = "El servicio de Geolocalización no responde.";

        // Mock: Lanza una RuntimeException para simular un fallo interno no controlado.
        // Esto soluciona el error de "Checked exception is invalid" de Mockito.
        when(authService.registerNewCiudadano(any(RegistroRequestDTO.class)))
                .thenThrow(new RuntimeException(internalErrorMessage));

        // Act & Assert
        mockMvc.perform(post(REGISTER_CIUDADANO_URL) // URL CORREGIDA
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(registroValido)))
                .andExpect(status().isInternalServerError()) // Espera 500
                // Verifica el mensaje exacto que devuelve el controlador al capturar la Exception
                .andExpect(content().string(containsString("Error interno del servidor: " + internalErrorMessage)));

        verify(authService, times(1)).registerNewCiudadano(any(RegistroRequestDTO.class));
    }
}