package com.maplewood.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Getter
@NoArgsConstructor
@Entity
@Table(name = "enrollments")
public class Enrollment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "student_id", nullable = false)
    private Long studentId;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "section_id", nullable = false)
    private CourseSection section;

    @Column(name = "status", nullable = false)
    private String status = "enrolled";

    @Column(name = "enrolled_at", nullable = false, insertable = false, updatable = false)
    private Instant enrolledAt;

    public Enrollment(Long studentId, CourseSection section) {
        this.studentId = studentId;
        this.section = section;
        this.status = "enrolled";
    }
}
