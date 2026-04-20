package com.maplewood.exception;

import com.maplewood.dto.ApiError;
import org.springframework.http.HttpStatus;

public class NotFoundException extends EnrollmentException {
    public NotFoundException(String message) {
        super(ApiError.NOT_FOUND, message, HttpStatus.NOT_FOUND);
    }
}
