package com.SAFE_Rescue.API_Perfiles.exception;

// Hereda de RuntimeException para que Spring la capture automáticamente
public class InvalidCredentialsException extends RuntimeException {

    public InvalidCredentialsException(String message) {
        super(message);
    }
}