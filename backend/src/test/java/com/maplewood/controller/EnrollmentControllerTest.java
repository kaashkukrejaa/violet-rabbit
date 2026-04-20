package com.maplewood.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.maplewood.dto.ApiError;
import com.maplewood.dto.EnrollmentRequest;
import com.maplewood.dto.ScheduleDto;
import com.maplewood.exception.EnrollmentException;
import com.maplewood.exception.NotFoundException;
import com.maplewood.service.EnrollmentService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Web-layer tests for {@link EnrollmentController}.
 * Covers both success paths, request-body validation, and business-rule error translation.
 */
@WebMvcTest(EnrollmentController.class)
class EnrollmentControllerTest {

    @Autowired private MockMvc mockMvc;
    @Autowired private ObjectMapper objectMapper;
    @MockBean private EnrollmentService enrollmentService;

    private final ScheduleDto emptySchedule = new ScheduleDto(
            "Fall", 2024, 0, 5, new BigDecimal("0.0"), List.of());

    @Test
    void enroll_returns201_onSuccess() throws Exception {
        when(enrollmentService.enroll(1L, 10L)).thenReturn(emptySchedule);

        mockMvc.perform(post("/api/enrollments")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new EnrollmentRequest(1L, 10L))))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.semesterName").value("Fall"));
    }

    @Test
    void enroll_returns422_withTypedApiError_onBusinessRuleViolation() throws Exception {
        when(enrollmentService.enroll(anyLong(), anyLong()))
                .thenThrow(new EnrollmentException(ApiError.PREREQUISITE, "Missing prerequisite"));

        mockMvc.perform(post("/api/enrollments")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new EnrollmentRequest(1L, 10L))))
                .andExpect(status().isUnprocessableEntity())
                .andExpect(jsonPath("$.type").value("prerequisite"))
                .andExpect(jsonPath("$.message").value("Missing prerequisite"));
    }

    @Test
    void enroll_returns404_whenStudentNotFound() throws Exception {
        when(enrollmentService.enroll(anyLong(), anyLong()))
                .thenThrow(new NotFoundException("Student not found: 99"));

        mockMvc.perform(post("/api/enrollments")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new EnrollmentRequest(99L, 10L))))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.type").value("not_found"));
    }

    @Test
    void enroll_returns400_whenRequestBodyFailsValidation() throws Exception {
        // studentId is @NotNull — omitting it should trigger MethodArgumentNotValidException
        mockMvc.perform(post("/api/enrollments")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"courseId\": 10}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.type").value("bad_request"))
                .andExpect(jsonPath("$.message").value(org.hamcrest.Matchers.containsString("studentId")));
    }

    @Test
    void drop_returns200_withUpdatedSchedule() throws Exception {
        when(enrollmentService.drop(1L, 10L)).thenReturn(emptySchedule);

        mockMvc.perform(delete("/api/enrollments")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new EnrollmentRequest(1L, 10L))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.semesterName").value("Fall"));
    }

    @Test
    void drop_returns404_whenStudentNotEnrolled() throws Exception {
        when(enrollmentService.drop(anyLong(), anyLong()))
                .thenThrow(new NotFoundException("Student is not enrolled in this course"));

        mockMvc.perform(delete("/api/enrollments")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new EnrollmentRequest(1L, 10L))))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.type").value("not_found"));
    }

    @Test
    void illegalArgumentException_isTranslatedTo400() throws Exception {
        when(enrollmentService.enroll(any(), any()))
                .thenThrow(new IllegalArgumentException("bad input"));

        mockMvc.perform(post("/api/enrollments")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new EnrollmentRequest(1L, 10L))))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.type").value("bad_request"))
                .andExpect(jsonPath("$.message").value("bad input"));
    }
}
