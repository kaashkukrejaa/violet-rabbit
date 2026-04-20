package com.maplewood.exception;

import com.maplewood.dto.ApiError;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Unit tests for {@link NotFoundException}.
 * Confirms the subclass sets the correct type and HTTP status.
 */
class NotFoundExceptionTest {

    @Test
    void presetsTypeAndStatusForNotFound() {
        NotFoundException ex = new NotFoundException("Student not found: 42");

        assertThat(ex.getType()).isEqualTo(ApiError.NOT_FOUND);
        assertThat(ex.getStatus()).isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(ex.getMessage()).isEqualTo("Student not found: 42");
    }

    @Test
    void isAnEnrollmentException() {
        NotFoundException ex = new NotFoundException("x");
        assertThat(ex).isInstanceOf(EnrollmentException.class);
    }

    @Test
    void toApiError_returnsNotFoundType() {
        ApiError err = new NotFoundException("missing").toApiError();
        assertThat(err.type()).isEqualTo(ApiError.NOT_FOUND);
        assertThat(err.message()).isEqualTo("missing");
    }
}
