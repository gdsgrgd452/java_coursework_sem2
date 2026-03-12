package com.example.safedispatch.exception;

// Custom exception for 404 Not Found scenarios
public class ResourceNotFoundException extends RuntimeException {
    public ResourceNotFoundException(String message) {
        super(message);
    }
}
