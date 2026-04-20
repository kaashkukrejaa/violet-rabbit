package com.maplewood.exception;

import com.maplewood.dto.ApiError;
import org.springframework.http.HttpStatus;

public class EnrollmentException extends RuntimeException {

    private final String type;
    private final HttpStatus status;

    public EnrollmentException(String type, String message) {
        this(type, message, HttpStatus.UNPROCESSABLE_ENTITY);
    }

    public EnrollmentException(String type, String message, HttpStatus status) {
        super(message);
        this.type = type;
        this.status = status;
    }

    public String getType() { return type; }
    public HttpStatus getStatus() { return status; }

    public ApiError toApiError() {
        return new ApiError(type, getMessage());
    }
}
