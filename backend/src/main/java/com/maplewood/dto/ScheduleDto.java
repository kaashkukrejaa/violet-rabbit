package com.maplewood.dto;

import java.math.BigDecimal;
import java.util.List;

public record ScheduleDto(
        String semesterName,
        Integer semesterYear,
        int courseCount,
        int maxCourses,
        BigDecimal totalCredits,
        List<CourseOfferingDto> items
) {}
