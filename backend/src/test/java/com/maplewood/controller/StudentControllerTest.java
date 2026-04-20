package com.maplewood.controller;

import com.maplewood.dto.ScheduleDto;
import com.maplewood.dto.StudentProfileDto;
import com.maplewood.dto.StudentSummaryDto;
import com.maplewood.exception.NotFoundException;
import com.maplewood.service.EnrollmentService;
import com.maplewood.service.StudentService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.util.List;

import static org.hamcrest.Matchers.hasSize;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Web-layer tests for {@link StudentController}.
 * Verifies routing, JSON serialization, and 404 handling via the global advice.
 */
@WebMvcTest(StudentController.class)
class StudentControllerTest {

    @Autowired private MockMvc mockMvc;
    @MockBean private StudentService studentService;
    @MockBean private EnrollmentService enrollmentService;

    @Test
    void list_returnsStudentSummaries() throws Exception {
        when(studentService.listStudents()).thenReturn(List.of(
                new StudentSummaryDto(1L, "Emma", "Wilson", 10),
                new StudentSummaryDto(2L, "James", "Lee", 11)
        ));

        mockMvc.perform(get("/api/students"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].firstName").value("Emma"))
                .andExpect(jsonPath("$[1].lastName").value("Lee"));
    }

    @Test
    void get_returnsProfileForId() throws Exception {
        StudentProfileDto profile = new StudentProfileDto(
                1L, "Emma", "Wilson", 10, 2028,
                new BigDecimal("3.50"), new BigDecimal("12.0"), new BigDecimal("30"),
                new BigDecimal("40.0"), List.of()
        );
        when(studentService.getProfile(1L)).thenReturn(profile);

        mockMvc.perform(get("/api/students/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.firstName").value("Emma"))
                .andExpect(jsonPath("$.gpa").value(3.50))
                .andExpect(jsonPath("$.creditsRequired").value(30));
    }

    @Test
    void get_returns404_whenStudentNotFound() throws Exception {
        when(studentService.getProfile(99L))
                .thenThrow(new NotFoundException("Student not found: 99"));

        mockMvc.perform(get("/api/students/99"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.type").value("not_found"))
                .andExpect(jsonPath("$.message").value("Student not found: 99"));
    }

    @Test
    void schedule_returnsCurrentSemesterSchedule() throws Exception {
        when(enrollmentService.getSchedule(1L)).thenReturn(new ScheduleDto(
                "Fall", 2024, 0, 5, new BigDecimal("0.0"), List.of()
        ));

        mockMvc.perform(get("/api/students/1/schedule"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.semesterName").value("Fall"))
                .andExpect(jsonPath("$.semesterYear").value(2024))
                .andExpect(jsonPath("$.maxCourses").value(5));
    }
}
