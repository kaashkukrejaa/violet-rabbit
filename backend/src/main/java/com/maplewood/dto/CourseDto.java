package com.maplewood.dto;

import com.maplewood.model.Course;

import java.math.BigDecimal;

public record CourseDto(
        Long id,
        String code,
        String name,
        String description,
        BigDecimal credits,
        int hoursPerWeek,
        String courseType,
        Integer gradeLevelMin,
        Integer gradeLevelMax,
        PrerequisiteDto prerequisite
) {
    public static CourseDto from(Course course) {
        PrerequisiteDto prereq = null;
        if (course.getPrerequisite() != null) {
            Course prerequisite = course.getPrerequisite();
            prereq = new PrerequisiteDto(prerequisite.getCode(), prerequisite.getName());
        }
        return new CourseDto(
                course.getId(),
                course.getCode(),
                course.getName(),
                course.getDescription(),
                course.getCredits(),
                course.getHoursPerWeek(),
                course.getCourseType(),
                course.getGradeLevelMin(),
                course.getGradeLevelMax(),
                prereq
        );
    }
}
