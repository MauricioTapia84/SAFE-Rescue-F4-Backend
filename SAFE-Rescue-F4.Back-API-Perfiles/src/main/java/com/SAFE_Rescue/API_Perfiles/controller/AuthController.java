package com.SAFE_Rescue.API_Perfiles.controller;

import com.SAFE_Rescue.API_Perfiles.dto.AuthResponseDTO;
import com.SAFE_Rescue.API_Perfiles.dto.LoginRequestDTO;
import com.SAFE_Rescue.API_Perfiles.dto.RegistroRequestDTO;
import com.SAFE_Rescue.API_Perfiles.exception.UserAlreadyExistsException;
import com.SAFE_Rescue.API_Perfiles.modelo.Ciudadano;
import com.SAFE_Rescue.API_Perfiles.service.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api-perfiles/v1/auth")
@Tag(name = "Autenticación", description = "Gestión de Login, Registro y Generación de Tokens")
public class AuthController {

    @Autowired
    private AuthService authService;

    public AuthController() {
        System.out.println("  AuthController instanciado!");
    }


    @PostMapping("/login")
    @Operation(summary = "Iniciar sesión", description = "Autentica un usuario y genera token JWT")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Login exitoso"),
            @ApiResponse(responseCode = "401", description = "Credenciales inválidas"),
            @ApiResponse(responseCode = "404", description = "Usuario no encontrado")
    })
    public ResponseEntity<AuthResponseDTO> login(@RequestBody @Valid LoginRequestDTO request) {
        try {
            System.out.println("  Intento de login para: " + request.getCorreo());

            AuthResponseDTO response = authService.authenticateAndGenerateToken(
                    request.getCorreo(),
                    request.getContrasena()
            );

            System.out.println("  Login exitoso para: " + request.getCorreo());
            return ResponseEntity.ok(response);

        } catch (Exception e) {
            System.err.println("  Error en login: " + e.getMessage());

            // Puedes personalizar las respuestas según el tipo de error
            if (e.getMessage().contains("no encontrado")) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
            } else if (e.getMessage().contains("Credenciales inválidas")) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
            } else {
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
            }
        }
    }

    @PostMapping("/register-ciudadano")
    @Operation(summary = "Registro Completo de Ciudadano", description = "Registra un nuevo Usuario y Ciudadano con dirección.")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Registro exitoso"),
            @ApiResponse(responseCode = "400", description = "Error de validación"),
            @ApiResponse(responseCode = "409", description = "Usuario ya existe"),
            @ApiResponse(responseCode = "500", description = "Error interno del servidor")
    })
    public ResponseEntity<?> registerCiudadano(@RequestBody @Valid RegistroRequestDTO request) {
        try {
            System.out.println(" 👤 Registrando nuevo ciudadano: " + request.getCorreo());

            Ciudadano nuevoCiudadano = authService.registerNewCiudadano(request);

            System.out.println("  Ciudadano registrado exitosamente - ID: " + nuevoCiudadano.getIdUsuario());
            return ResponseEntity.status(HttpStatus.CREATED).body(nuevoCiudadano);

        } catch (UserAlreadyExistsException e) {
            System.err.println("  Usuario ya existe: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.CONFLICT).body(e.getMessage());

        } catch (Exception e) {
            System.err.println("  Error en registro: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error interno del servidor: " + e.getMessage());
        }
    }
}