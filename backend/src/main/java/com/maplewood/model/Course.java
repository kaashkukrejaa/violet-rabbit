package com.maplewood.model;

import jakarta.persistence.*;
import lombok.Getter;

import java.math.BigDecimal;

@Getter
@Entity
@Table(name = "courses")
public class Course {

    @Id
    private Long id;

    @Column(name = "code", nullable = false, unique = true)
    private String code;

    @Column(name = "name", nullable = false)
    private String name;

    @Column(name = "description")
    private String description;

    @Column(name = "credits", nullable = false)
    private BigDecimal credits;

    @Column(name = "hours_per_week", nullable = false)
    private Integer hoursPerWeek;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "specialization_id")
    private Specialization specialization;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "prerequisite_id")
    private Course prerequisite;

    @Column(name = "course_type", nullable = false)
    private String courseType;

    @Column(name = "grade_level_min")
    private Integer gradeLevelMin;

    @Column(name = "grade_level_max")
    private Integer gradeLevelMax;

    @Column(name = "semester_order", nullable = false)
    private Integer semesterOrder;
}
