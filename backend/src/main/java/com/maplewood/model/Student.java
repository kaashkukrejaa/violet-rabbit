package com.maplewood.model;

import jakarta.persistence.*;
import lombok.Getter;

@Getter
@Entity
@Table(name = "students")
public class Student {

    @Id
    private Long id;

    @Column(name = "first_name", nullable = false)
    private String firstName;

    @Column(name = "last_name", nullable = false)
    private String lastName;

    @Column(name = "email")
    private String email;

    @Column(name = "grade_level", nullable = false)
    private Integer gradeLevel;

    @Column(name = "enrollment_year", nullable = false)
    private Integer enrollmentYear;

    @Column(name = "expected_graduation_year")
    private Integer expectedGraduationYear;

    @Column(name = "status")
    private String status;
}
