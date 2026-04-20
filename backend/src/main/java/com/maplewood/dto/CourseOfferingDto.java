package com.maplewood.dto;

import com.maplewood.model.CourseSection;

public record CourseOfferingDto(
        CourseDto course,
        SectionDto section
) {
    public static CourseOfferingDto from(CourseSection section) {
        return new CourseOfferingDto(
                CourseDto.from(section.getCourse()),
                SectionDto.from(section)
        );
    }
}
