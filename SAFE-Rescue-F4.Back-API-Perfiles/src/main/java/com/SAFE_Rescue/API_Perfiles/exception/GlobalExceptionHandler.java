package com.SAFE_Rescue.API_Perfiles.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.server.ResponseStatusException;

import java.util.HashMap;
import java.util.Map;
import java.util.NoSuchElementException;

@ControllerAdvice
public class GlobalExceptionHandler {

    // =======================================================================
    // 1. MANEJO DE AUTENTICACIÓN Y SEGURIDAD (401 / 403)
    // =======================================================================

    /**
     * Maneja credenciales inválidas. Retorna 401 UNAUTHORIZED.
     */
    @ExceptionHandler(InvalidCredentialsException.class)
    public ResponseEntity<String> handleInvalidCredentialsException(InvalidCredentialsException ex) {
        System.err.println("Error 401 UNAUTHORIZED: Credenciales Inválidas.");
        return new ResponseEntity<>(ex.getMessage(), HttpStatus.UNAUTHORIZED);
    }

    /**
     * Maneja cuentas inactivas o con restricciones. Retorna 403 FORBIDDEN (o 401).
     * Usamos 403 para indicar que la acción está prohibida para ese usuario.
     */
    @ExceptionHandler(UserNotActiveException.class)
    public ResponseEntity<String> handleUserNotActiveException(UserNotActiveException ex) {
        System.err.println("Error 403 FORBIDDEN: Cuenta Inactiva/Restringida.");
        return new ResponseEntity<>(ex.getMessage(), HttpStatus.FORBIDDEN); // Usamos 403
    }

    // =======================================================================
    // 2. MANEJO DE CONFLICTOS DE REGISTRO (409)
    // =======================================================================

    /**
     * Maneja intentos de registro con RUN/Email ya existentes. Retorna 409 CONFLICT.
     */
    @ExceptionHandler(UserAlreadyExistsException.class)
    public ResponseEntity<String> handleUserAlreadyExistsException(UserAlreadyExistsException ex) {
        System.err.println("Error 409 CONFLICT: Usuario ya existe.");
        return new ResponseEntity<>(ex.getMessage(), HttpStatus.CONFLICT);
    }

    // =======================================================================
    // 3. MANEJO DE ERRORES COMUNES (404 / 400)
    // =======================================================================

    // MANEJO DE 404 (Recurso no encontrado - CRUD o Usuario no encontrado en login)
    @ExceptionHandler({NoSuchElementException.class, UserNotFoundException.class})
    public ResponseEntity<String> handleNotFoundExceptions(RuntimeException ex) {
        System.err.println("Error 404 NOT FOUND: " + ex.getMessage());
        return new ResponseEntity<>(ex.getMessage(), HttpStatus.NOT_FOUND); // Devuelve 404
    }

    // MANEJO DE 400 (Errores de Negocio)
    @ExceptionHandler({IllegalArgumentException.class, IllegalStateException.class})
    public ResponseEntity<String> handleBadRequestExceptions(RuntimeException ex) {
        System.err.println("Error 400 BAD REQUEST (Negocio): " + ex.getMessage());
        return new ResponseEntity<>(ex.getMessage(), HttpStatus.BAD_REQUEST); // Devuelve 400
    }

    // MANEJO DE 400 (Errores de Validación de Modelo @Valid)
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, String>> handleValidationExceptions(MethodArgumentNotValidException ex) {
        Map<String, String> errors = new HashMap<>();
        ex.getBindingResult().getFieldErrors().forEach(error -> {
            errors.put(error.getField(), error.getDefaultMessage());
        });
        return new ResponseEntity<>(errors, HttpStatus.BAD_REQUEST); // Devuelve 400
    }

    // --- MANEJADOR ResponseStatusException ---
    @ExceptionHandler(ResponseStatusException.class)
    public ResponseEntity<Object> handleResponseStatusException(ResponseStatusException ex) {
        System.err.println("Error Resuelto (ResponseStatusException): Status " + ex.getStatusCode() + " -> " + ex.getReason());
        return new ResponseEntity<>(ex.getReason(), ex.getStatusCode());
    }

    // =======================================================================
    // 4. MANEJO GENÉRICO (Conflictos 409 / Internal Server Error 500)
    // =======================================================================

    // Último recurso: Captura cualquier RuntimeException no manejada específicamente
    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<String> handleConflictOrInternalError(RuntimeException ex) {
        String errorMessage = ex.getMessage();

        // Lógica para mapear a 409 CONFLICT (Mantenemos la lógica de referencias activas)
        if (errorMessage != null && errorMessage.toLowerCase().contains("referencias activas")) {
            System.err.println("Error 409 CONFLICT: " + errorMessage);
            return new ResponseEntity<>(errorMessage, HttpStatus.CONFLICT);
        }

        // Si la excepción no es ninguna de las anteriores ni un 409 específico, asume 500.
        System.err.println("Error 500 INTERNAL SERVER ERROR (Inesperado): " + errorMessage);
        ex.printStackTrace(); // Imprime el stack trace para debugging
        return new ResponseEntity<>("Error interno del servidor: " + errorMessage, HttpStatus.INTERNAL_SERVER_ERROR);
    }
}