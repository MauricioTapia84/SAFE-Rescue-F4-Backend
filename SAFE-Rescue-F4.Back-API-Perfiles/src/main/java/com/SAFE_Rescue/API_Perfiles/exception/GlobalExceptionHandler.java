package com.SAFE_Rescue.API_Perfiles.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.server.ResponseStatusException; // ¡Importante!

import java.util.HashMap;
import java.util.Map;
import java.util.NoSuchElementException;

@ControllerAdvice
public class GlobalExceptionHandler {

    // 1. MANEJO DE 404 (Recurso no encontrado)
    @ExceptionHandler(NoSuchElementException.class)
    public ResponseEntity<String> handleNoSuchElementException(NoSuchElementException ex) {
        System.err.println("Error 404 NOT FOUND: " + ex.getMessage());
        return new ResponseEntity<>(ex.getMessage(), HttpStatus.NOT_FOUND); // Devuelve 404
    }

    // 2. MANEJO DE 400 (Errores de Negocio)
    // Argumentos ilegales (ej. nombre duplicado) y estados ilegales
    @ExceptionHandler({IllegalArgumentException.class, IllegalStateException.class})
    public ResponseEntity<String> handleBadRequestExceptions(RuntimeException ex) {
        System.err.println("Error 400 BAD REQUEST (Negocio): " + ex.getMessage());
        return new ResponseEntity<>(ex.getMessage(), HttpStatus.BAD_REQUEST); // Devuelve 400
    }

    // 3. MANEJO DE 400 (Errores de Validación de Modelo @Valid)
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, String>> handleValidationExceptions(MethodArgumentNotValidException ex) {
        Map<String, String> errors = new HashMap<>();
        ex.getBindingResult().getFieldErrors().forEach(error -> {
            errors.put(error.getField(), error.getDefaultMessage());
        });
        return new ResponseEntity<>(errors, HttpStatus.BAD_REQUEST); // Devuelve 400
    }

    // --- 4. NUEVO MANEJADOR CLAVE: MANEJA ResponseStatusException (404/400/409 envueltos) ---
    /*
     * Cuando MockMvc resuelve una excepción con @ResponseStatus o ResponseEntity,
     * a menudo se traduce internamente a ResponseStatusException.
     * Este manejador extrae el código de estado correcto.
     */
    @ExceptionHandler(ResponseStatusException.class)
    public ResponseEntity<Object> handleResponseStatusException(ResponseStatusException ex) {
        System.err.println("Error Resuelto (ResponseStatusException): Status " + ex.getStatusCode() + " -> " + ex.getReason());
        // Devuelve el estado y el mensaje ya contenido en la excepción.
        return new ResponseEntity<>(ex.getReason(), ex.getStatusCode());
    }

    // 5. MANEJO GENÉRICO DE RUNTIMEEXCEPTION (Conflictos 409 e Internal Server Error 500)
    // Debe ir al final para que los manejadores específicos se ejecuten primero.
    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<String> handleConflictOrInternalError(RuntimeException ex) {
        String errorMessage = ex.getMessage();

        // Lógica para mapear a 409 CONFLICT (Conflicto de integridad por "referencias activas")
        if (errorMessage != null && errorMessage.toLowerCase().contains("referencias activas")) {
            System.err.println("Error 409 CONFLICT: " + errorMessage);
            return new ResponseEntity<>(errorMessage, HttpStatus.CONFLICT); // Devuelve 409
        }

        // Si la excepción no es ninguna de las anteriores ni un 409, asume un error inesperado 500.
        System.err.println("Error 500 INTERNAL SERVER ERROR (Inesperado): " + errorMessage);
        return new ResponseEntity<>("Error interno del servidor: " + errorMessage, HttpStatus.INTERNAL_SERVER_ERROR); // Devuelve 500
    }
}