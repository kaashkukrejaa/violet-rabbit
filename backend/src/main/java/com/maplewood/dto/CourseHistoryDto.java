package com.maplewood.dto;

import java.math.BigDecimal;

public record CourseHistoryDto(
        Long id,
        Long courseId,
        String courseCode,
        String courseName,
        BigDecimal credits,
        String semesterName,
        Integer semesterYear,
        String status
) {}
