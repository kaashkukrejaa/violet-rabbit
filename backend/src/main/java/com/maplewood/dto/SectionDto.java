package com.maplewood.dto;

import com.maplewood.model.CourseSection;

import java.util.List;

public record SectionDto(
        Long id,
        List<String> daysOfWeek,
        String startTime,
        String endTime,
        String teacherName,
        String classroomName
) {
    public static SectionDto from(CourseSection section) {
        String teacher = section.getTeacher() == null ? null
                : section.getTeacher().getFirstName() + " " + section.getTeacher().getLastName();
        String room = section.getClassroom() == null ? null : section.getClassroom().getName();
        return new SectionDto(
                section.getId(),
                section.getDaysList(),
                section.getStartTime(),
                section.getEndTime(),
                teacher,
                room
        );
    }
}
