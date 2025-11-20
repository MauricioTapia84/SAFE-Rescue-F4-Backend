package com.SAFE_Rescue.API_Perfiles.controller;

import com.SAFE_Rescue.API_Perfiles.dto.AuthResponseDTO;
import com.SAFE_Rescue.API_Perfiles.dto.LoginRequestDTO;
import com.SAFE_Rescue.API_Perfiles.modelo.Usuario;
import com.SAFE_Rescue.API_Perfiles.service.AuthService; // Nuevo Servicio de Autenticación
import com.SAFE_Rescue.API_Perfiles.service.UsuarioService;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.parameters.RequestBody;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.stream.Collectors;

@RestController
@RequestMapping("/api-perfiles/v1/auth") // Ruta dedicada a la seguridad
@Tag(name = "Autenticación", description = "Gestión de Login, Registro y Generación de Tokens")
public class AuthController {

    @Autowired
    private AuthService authService; // Inyectar el nuevo servicio de autenticación

    @Autowired
    private UsuarioService usuarioService; // También puedes usar el servicio de usuario aquí si el registro es simple

    public AuthController() {
        System.out.println(" AuthController instanciado!");
    }

    // Endpoint de prueba SIMPLE
    @GetMapping("/test")
    public String test() {
        System.out.println(" /test endpoint llamado!");
        return "AuthController funciona!";
    }


    @PostMapping("/login")
    public ResponseEntity<AuthResponseDTO> login(HttpServletRequest httpRequest) {
        try {
            // Leer el cuerpo manualmente
            String body = httpRequest.getReader().lines().collect(Collectors.joining());
            System.out.println(" Cuerpo RAW recibido: " + body);

            ObjectMapper objectMapper = new ObjectMapper();
            LoginRequestDTO request = objectMapper.readValue(body, LoginRequestDTO.class);

            System.out.println(" Objeto deserializado: " + request);

            AuthResponseDTO response = authService.authenticateAndGenerateToken(
                    request.getCorreo(),
                    request.getContrasena()
            );
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            System.out.println("Error deserializando: " + e.getMessage());
            return ResponseEntity.badRequest().build();
        }
    }


    @PostMapping("/register")
    @Operation(summary = "Registro de Nuevo Usuario", description = "Registra un nuevo Usuario (Bombero o Ciudadano).")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Registro exitoso. Devuelve el Usuario creado."),
            @ApiResponse(responseCode = "400", description = "Error de validación o usuario existente.")
    })
    public ResponseEntity<Usuario> register(@RequestBody @Valid Usuario usuario) {
        // NOTA: En un caso real, aquí usarías un DTO de registro, no la entidad Usuario
        // para manejar las contraseñas sin hashear y los campos de discriminación (tipoPerfil).
        Usuario nuevoUsuario = usuarioService.save(usuario);
        return ResponseEntity.status(HttpStatus.CREATED).body(nuevoUsuario);
    }
}