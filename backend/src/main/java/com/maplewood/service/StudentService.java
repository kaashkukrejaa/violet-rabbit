package com.maplewood.service;

import com.maplewood.dto.CourseHistoryDto;
import com.maplewood.dto.StudentProfileDto;
import com.maplewood.dto.StudentSummaryDto;
import com.maplewood.exception.NotFoundException;
import com.maplewood.model.Course;
import com.maplewood.model.Student;
import com.maplewood.model.StudentCourseHistory;
import com.maplewood.repository.StudentCourseHistoryRepository;
import com.maplewood.repository.StudentRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Comparator;
import java.util.List;

@Service
@Transactional(readOnly = true)
public class StudentService {

    private static final BigDecimal CREDITS_TO_GRADUATE = new BigDecimal("30");
    private static final BigDecimal GPA_SCALE = new BigDecimal("4.0");

    private final StudentRepository studentRepository;
    private final StudentCourseHistoryRepository historyRepository;

    public StudentService(StudentRepository studentRepository,
                          StudentCourseHistoryRepository historyRepository) {
        this.studentRepository = studentRepository;
        this.historyRepository = historyRepository;
    }

    /**
     * Returns first 50 students ordered alphabetically for demo picker dropdown.
     */
    public List<StudentSummaryDto> listStudents() {
        return studentRepository.findTop50ByOrderByLastNameAscFirstNameAsc().stream()
                .map(StudentSummaryDto::from)
                .toList();
    }

    /**
     * Retrieves complete student profile including GPA, credits, and course history.
     * Calculates academic metrics from historical enrollment records.
     * 
     * GPA calculation: (credits earned / credits attempted) × 4.0 scale
     * Progress calculation: (credits earned / 30 required) × 100%
     */
    public StudentProfileDto getProfile(Long id) {
        Student student = studentRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Student not found: " + id));

        List<StudentCourseHistory> history = historyRepository.findByStudentId(id);

        // Calculate total credits attempted and earned (only passed courses count toward earned)
        BigDecimal creditsAttempted = BigDecimal.ZERO;
        BigDecimal creditsEarned = BigDecimal.ZERO;
        for (StudentCourseHistory record : history) {
            Course course = record.getCourse();
            if (course == null || course.getCredits() == null) continue;
            creditsAttempted = creditsAttempted.add(course.getCredits());
            if (record.isPassed()) creditsEarned = creditsEarned.add(course.getCredits());
        }

        // GPA = (earned / attempted) × 4.0, avoiding division by zero
        BigDecimal gpa = BigDecimal.ZERO;
        // Prevent division by zero: if student hasn't taken any courses yet, creditsAttempted = 0
        if (creditsAttempted.signum() > 0) {
            gpa = creditsEarned
                    .divide(creditsAttempted, 4, RoundingMode.HALF_UP)
                    .multiply(GPA_SCALE)
                    .setScale(2, RoundingMode.HALF_UP);
        }

        // Graduation progress = (earned / 30) × 100%, capped at 100%
        BigDecimal progress = creditsEarned
                .divide(CREDITS_TO_GRADUATE, 4, RoundingMode.HALF_UP)
                .multiply(new BigDecimal("100"))
                .setScale(1, RoundingMode.HALF_UP);
        // Cap at 100% even if student earned more than 30 credits (e.g., taking extra electives)
        if (progress.compareTo(new BigDecimal("100")) > 0) progress = new BigDecimal("100.0");

        // Sort history chronologically: by year first, then by semester within each year
        List<CourseHistoryDto> historyDtos = history.stream()
                .sorted(Comparator
                        .comparing((StudentCourseHistory record) -> record.getSemester().getYear())
                        .thenComparing(record -> record.getSemester().getOrderInYear()))
                .map(record -> new CourseHistoryDto(
                        record.getId(),
                        record.getCourse().getId(),
                        record.getCourse().getCode(),
                        record.getCourse().getName(),
                        record.getCourse().getCredits(),
                        record.getSemester().getName(),
                        record.getSemester().getYear(),
                        record.getStatus()
                ))
                .toList();

        return new StudentProfileDto(
                student.getId(),
                student.getFirstName(),
                student.getLastName(),
                student.getGradeLevel(),
                student.getExpectedGraduationYear(),
                gpa,
                creditsEarned.setScale(1, RoundingMode.HALF_UP),
                CREDITS_TO_GRADUATE,
                progress,
                historyDtos
        );
    }
}
