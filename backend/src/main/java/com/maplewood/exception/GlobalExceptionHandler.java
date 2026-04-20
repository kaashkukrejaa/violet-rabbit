package com.maplewood.exception;

import com.maplewood.dto.ApiError;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

/**
 * Global exception handler that converts exceptions into structured API error responses.
 * Ensures consistent error format across all endpoints.
 */
@ControllerAdvice
public class GlobalExceptionHandler {

    /**
     * Handles business rule violations during enrollment (prerequisites, conflicts, etc.).
     * Returns appropriate HTTP status with typed error message.
     */
    @ExceptionHandler(EnrollmentException.class)
    public ResponseEntity<ApiError> handleEnrollment(EnrollmentException ex) {
        return ResponseEntity.status(ex.getStatus()).body(ex.toApiError());
    }

    /**
     * Handles request validation failures (missing required fields, invalid formats).
     * Returns 400 Bad Request with first validation error.
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiError> handleValidation(MethodArgumentNotValidException ex) {
        String message = ex.getBindingResult().getFieldErrors().stream()
                .findFirst()
                .map(error -> error.getField() + " " + error.getDefaultMessage())
                .orElse("invalid request");
        return ResponseEntity.badRequest().body(new ApiError(ApiError.BAD_REQUEST, message));
    }

    /**
     * Handles general illegal argument errors.
     */
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ApiError> handleIllegalArg(IllegalArgumentException ex) {
        return ResponseEntity.badRequest().body(new ApiError(ApiError.BAD_REQUEST, ex.getMessage()));
    }
}
