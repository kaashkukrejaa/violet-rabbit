package com.maplewood.dto;

import java.math.BigDecimal;
import java.util.List;

public record StudentProfileDto(
        Long id,
        String firstName,
        String lastName,
        Integer gradeLevel,
        Integer expectedGraduationYear,
        BigDecimal gpa,
        BigDecimal creditsEarned,
        BigDecimal creditsRequired,
        BigDecimal graduationProgress,
        List<CourseHistoryDto> courseHistory
) {}
