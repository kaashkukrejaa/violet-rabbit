package com.maplewood.model;

import jakarta.persistence.*;
import lombok.Getter;

import java.time.LocalTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

@Getter
@Entity
@Table(name = "course_sections")
public class CourseSection {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "course_id", nullable = false)
    private Course course;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "semester_id", nullable = false)
    private Semester semester;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "teacher_id")
    private Teacher teacher;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "classroom_id")
    private Classroom classroom;

    @Column(name = "days_of_week", nullable = false)
    private String daysOfWeek;

    @Column(name = "start_time", nullable = false)
    private String startTime;

    @Column(name = "end_time", nullable = false)
    private String endTime;

    @Column(name = "capacity", nullable = false)
    private Integer capacity;

    /**
     * Parses the comma-separated days string into a clean list.
     * Example: "Mon, Wed, Fri" → ["Mon", "Wed", "Fri"]
     */
    public List<String> getDaysList() {
        if (daysOfWeek == null || daysOfWeek.isBlank()) {
            return Collections.emptyList();
        }
        return Arrays.stream(daysOfWeek.split(","))
                .map(String::trim)
                .filter(day -> !day.isEmpty())
                .toList();
    }

    /**
     * Checks if this section conflicts with another section.
     * Sections overlap if they share at least one day AND their time ranges intersect.
     *
     * Time overlap logic: Two ranges [A_start, A_end) and [B_start, B_end) overlap if:
     * A_start < B_end AND B_start < A_end
     */
    public boolean overlapsWith(CourseSection other) {
        List<String> currentDays = getDaysList();
        List<String> newDays = other.getDaysList();
        boolean shareDay = currentDays.stream().anyMatch(newDays::contains);
        if (!shareDay) return false;

        LocalTime currentStart = LocalTime.parse(startTime);
        LocalTime currentEnd = LocalTime.parse(endTime);
        LocalTime newStart = LocalTime.parse(other.startTime);
        LocalTime newEnd = LocalTime.parse(other.endTime);

        return currentStart.isBefore(newEnd) && newStart.isBefore(currentEnd);
    }
}
