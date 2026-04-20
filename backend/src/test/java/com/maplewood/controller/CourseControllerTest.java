package com.maplewood.controller;

import com.maplewood.dto.CourseDto;
import com.maplewood.dto.CourseOfferingDto;
import com.maplewood.dto.PrerequisiteDto;
import com.maplewood.dto.SectionDto;
import com.maplewood.service.CourseService;
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
 * Web-layer tests for {@link CourseController}.
 * Uses {@code @WebMvcTest} to load only the MVC slice and mock the service.
 */
@WebMvcTest(CourseController.class)
class CourseControllerTest {

    @Autowired private MockMvc mockMvc;
    @MockBean private CourseService courseService;

    @Test
    void get_courses_returnsCourseList_with200() throws Exception {
        CourseDto dto = new CourseDto(1L, "ENG101", "English 101", "desc",
                new BigDecimal("3.0"), 3, "core", 9, 12, null);
        when(courseService.listCourses(null, null)).thenReturn(List.of(dto));

        mockMvc.perform(get("/api/courses"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].code").value("ENG101"))
                .andExpect(jsonPath("$[0].name").value("English 101"));
    }

    @Test
    void get_courses_passesQueryParamsToService() throws Exception {
        CourseDto dto = new CourseDto(1L, "MAT101", "Math", "desc",
                new BigDecimal("3.0"), 3, "core", 10, 10,
                new PrerequisiteDto("ENG101", "English 101"));
        when(courseService.listCourses(10, 1)).thenReturn(List.of(dto));

        mockMvc.perform(get("/api/courses").param("grade", "10").param("semesterOrder", "1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].code").value("MAT101"))
                .andExpect(jsonPath("$[0].prerequisite.code").value("ENG101"));
    }

    @Test
    void get_offerings_returnsOfferingsWithSectionDetails() throws Exception {
        CourseOfferingDto offering = new CourseOfferingDto(
                new CourseDto(1L, "ENG101", "English 101", null,
                        new BigDecimal("3.0"), 3, "core", 9, 12, null),
                new SectionDto(100L, List.of("MON", "WED", "FRI"), "09:00", "10:00",
                        "Ms. Park", "Room 12")
        );
        when(courseService.listActiveOfferings()).thenReturn(List.of(offering));

        mockMvc.perform(get("/api/courses/offerings"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].course.code").value("ENG101"))
                .andExpect(jsonPath("$[0].section.teacherName").value("Ms. Park"))
                .andExpect(jsonPath("$[0].section.daysOfWeek", hasSize(3)));
    }

    @Test
    void get_offerings_returnsEmptyArray_whenNoActiveSemester() throws Exception {
        when(courseService.listActiveOfferings()).thenReturn(List.of());

        mockMvc.perform(get("/api/courses/offerings"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(0)));
    }
}
