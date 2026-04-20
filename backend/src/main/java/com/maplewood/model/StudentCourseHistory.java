package com.maplewood.model;

import jakarta.persistence.*;
import lombok.Getter;

@Getter
@Entity
@Table(name = "student_course_history")
public class StudentCourseHistory {

    @Id
    private Long id;

    @Column(name = "student_id", nullable = false)
    private Long studentId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "course_id", nullable = false)
    private Course course;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "semester_id", nullable = false)
    private Semester semester;

    @Column(name = "status", nullable = false)
    private String status;

    public boolean isPassed() {
        return "passed".equalsIgnoreCase(status);
    }
}
