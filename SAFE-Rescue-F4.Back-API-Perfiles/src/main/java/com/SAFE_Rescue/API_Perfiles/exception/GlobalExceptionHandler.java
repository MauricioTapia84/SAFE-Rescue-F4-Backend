package com.SAFE_Rescue.API_Perfiles.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import java.util.HashMap;
import java.util.Map;
import java.util.NoSuchElementException;

@ControllerAdvice
public class GlobalExceptionHandler {

    // --- 1. MANEJO DE NOT FOUND (404) ---
    // Captura las excepciones lanzadas por findById().orElseThrow()
    @ExceptionHandler(NoSuchElementException.class)
    public ResponseEntity<String> handleNoSuchElementException(NoSuchElementException ex) {
        // En tu caso, los mensajes ya son descriptivos: "Usuario no encontrado con ID: X"
        return new ResponseEntity<>(ex.getMessage(), HttpStatus.NOT_FOUND);
    }

    // --- 2. MANEJO DE ERRORES DE VALIDACIÓN DEL MODELO (@Valid) (400) ---
    // Captura los errores de validación de Jakarta (@NotBlank, @Size, @Email, etc.)
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, String>> handleValidationExceptions(MethodArgumentNotValidException ex) {
        Map<String, String> errors = new HashMap<>();

        // Recorre todos los errores de campo y los agrega al Map
        ex.getBindingResult().getFieldErrors().forEach(error -> {
            errors.put(error.getField(), error.getDefaultMessage());
        });

        return new ResponseEntity<>(errors, HttpStatus.BAD_REQUEST);
    }

    // --- 3. MANEJO DE ERRORES DE NEGOCIO (400) ---
    // Captura errores lanzados intencionalmente desde el Servicio (ej: RUN ya existe, dependencia no existe)
    @ExceptionHandler({IllegalArgumentException.class, IllegalStateException.class})
    public ResponseEntity<String> handleBadRequestExceptions(RuntimeException ex) {
        // El servicio ya contiene el mensaje de error de negocio (ej. "El RUN o correo electrónico ya existen.")
        return new ResponseEntity<>(ex.getMessage(), HttpStatus.BAD_REQUEST);
    }


}