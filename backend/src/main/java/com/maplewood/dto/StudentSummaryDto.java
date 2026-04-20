package com.maplewood.dto;

import com.maplewood.model.Student;

public record StudentSummaryDto(
        Long id,
        String firstName,
        String lastName,
        Integer gradeLevel
) {
    public static StudentSummaryDto from(Student student) {
        return new StudentSummaryDto(
                student.getId(),
                student.getFirstName(),
                student.getLastName(),
                student.getGradeLevel()
        );
    }
}
