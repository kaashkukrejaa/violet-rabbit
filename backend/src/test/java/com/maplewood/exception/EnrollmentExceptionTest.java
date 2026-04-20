package com.maplewood.exception;

import com.maplewood.dto.ApiError;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Unit tests for {@link EnrollmentException}.
 * Verifies defaults and the ApiError conversion used by the global advice.
 */
class EnrollmentExceptionTest {

    @Test
    void defaultConstructor_usesUnprocessableEntityStatus() {
        EnrollmentException ex = new EnrollmentException(ApiError.PREREQUISITE, "Missing prereq");

        assertThat(ex.getStatus()).isEqualTo(HttpStatus.UNPROCESSABLE_ENTITY);
        assertThat(ex.getType()).isEqualTo(ApiError.PREREQUISITE);
        assertThat(ex.getMessage()).isEqualTo("Missing prereq");
    }

    @Test
    void customStatusConstructor_overridesStatus() {
        EnrollmentException ex = new EnrollmentException(
                ApiError.NOT_FOUND, "Missing", HttpStatus.NOT_FOUND);

        assertThat(ex.getStatus()).isEqualTo(HttpStatus.NOT_FOUND);
    }

    @Test
    void toApiError_preservesTypeAndMessage() {
        EnrollmentException ex = new EnrollmentException(ApiError.CONFLICT, "Time conflict");

        ApiError err = ex.toApiError();

        assertThat(err.type()).isEqualTo(ApiError.CONFLICT);
        assertThat(err.message()).isEqualTo("Time conflict");
    }
}
