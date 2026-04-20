package com.maplewood.exception;

import com.maplewood.dto.ApiError;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Unit tests for {@link GlobalExceptionHandler}.
 * Calls each handler directly to verify response shape and status.
 */
class GlobalExceptionHandlerTest {

    private final GlobalExceptionHandler handler = new GlobalExceptionHandler();

    @Test
    void handleEnrollment_mapsTypeAndStatusFromException() {
        EnrollmentException ex = new EnrollmentException(
                ApiError.CONFLICT, "time conflict", HttpStatus.UNPROCESSABLE_ENTITY);

        ResponseEntity<ApiError> response = handler.handleEnrollment(ex);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNPROCESSABLE_ENTITY);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().type()).isEqualTo(ApiError.CONFLICT);
        assertThat(response.getBody().message()).isEqualTo("time conflict");
    }

    @Test
    void handleEnrollment_propagatesNotFoundSubclassStatus() {
        ResponseEntity<ApiError> response = handler.handleEnrollment(
                new NotFoundException("Student not found: 1"));

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(response.getBody().type()).isEqualTo(ApiError.NOT_FOUND);
    }

    @Test
    void handleValidation_returnsFirstFieldError_as400() {
        BeanPropertyBindingResult bindingResult = new BeanPropertyBindingResult(new Object(), "req");
        bindingResult.addError(new FieldError("req", "studentId", "must not be null"));
        bindingResult.addError(new FieldError("req", "courseId", "must not be null"));

        MethodArgumentNotValidException ex = mock(MethodArgumentNotValidException.class);
        when(ex.getBindingResult()).thenReturn(bindingResult);

        ResponseEntity<ApiError> response = handler.handleValidation(ex);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody().type()).isEqualTo(ApiError.BAD_REQUEST);
        assertThat(response.getBody().message()).isEqualTo("studentId must not be null");
    }

    @Test
    void handleValidation_fallsBackTo_invalidRequest_whenNoFieldErrors() {
        BeanPropertyBindingResult bindingResult = new BeanPropertyBindingResult(new Object(), "req");
        MethodArgumentNotValidException ex = mock(MethodArgumentNotValidException.class);
        when(ex.getBindingResult()).thenReturn(bindingResult);

        ResponseEntity<ApiError> response = handler.handleValidation(ex);

        assertThat(response.getBody().message()).isEqualTo("invalid request");
    }

    @Test
    void handleIllegalArg_returns400WithMessage() {
        ResponseEntity<ApiError> response = handler.handleIllegalArg(
                new IllegalArgumentException("bad input"));

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody().type()).isEqualTo(ApiError.BAD_REQUEST);
        assertThat(response.getBody().message()).isEqualTo("bad input");
    }
}
