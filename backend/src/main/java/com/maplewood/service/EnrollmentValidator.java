package com.maplewood.service;

import com.maplewood.dto.ApiError;
import com.maplewood.exception.EnrollmentException;
import com.maplewood.model.Course;
import com.maplewood.model.CourseSection;
import com.maplewood.model.Enrollment;
import com.maplewood.model.Student;
import com.maplewood.model.StudentCourseHistory;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Enforces the business rules that gate enrolment.
 * <p>
 * This class is deliberately stateless and free of repository/Spring dependencies:
 * the caller fetches the required data once and hands it in. That keeps the rules
 * easy to test in isolation and makes the dependency graph of {@link EnrollmentService}
 * much smaller.
 */
@Component
public class EnrollmentValidator {

    public static final int MAX_COURSES_PER_SEMESTER = 5;

    /**
     * Runs every rule in the canonical order used by {@link EnrollmentService#enroll}.
     * Short-circuits on the first failure (rules throw).
     */
    public void validateCanEnroll(Student student,
                                  Course course,
                                  CourseSection section,
                                  List<StudentCourseHistory> history,
                                  List<Enrollment> currentEnrollments) {
        validateGradeLevel(student, course);
        validateNotAlreadyPassed(history, course);
        validateNotAlreadyEnrolled(section, currentEnrollments);
        validateMaxCourses(currentEnrollments);
        validatePrerequisite(history, course);
        validateTimeConflict(section, currentEnrollments);
    }

    /**
     * Student's grade must fall within the course's grade range (if the course declares one).
     * Example: AP courses typically restricted to grades 11–12.
     */
    public void validateGradeLevel(Student student, Course course) {
        if (course.getGradeLevelMin() == null || course.getGradeLevelMax() == null) return;
        int grade = student.getGradeLevel();
        if (grade < course.getGradeLevelMin() || grade > course.getGradeLevelMax()) {
            throw new EnrollmentException(ApiError.GRADE_LEVEL,
                    course.getCode() + " is restricted to grades "
                            + course.getGradeLevelMin() + "-" + course.getGradeLevelMax()
                            + " (student is grade " + grade + ")");
        }
    }

    /**
     * A student may not re-enrol in a course they have already passed.
     */
    public void validateNotAlreadyPassed(List<StudentCourseHistory> history, Course course) {
        boolean alreadyPassed = history.stream()
                .anyMatch(record -> record.getCourse().getId().equals(course.getId()) && record.isPassed());
        if (alreadyPassed) {
            throw new EnrollmentException(ApiError.ALREADY_PASSED,
                    "Student has already passed " + course.getCode());
        }
    }

    /**
     * A student cannot be enrolled in the same section twice in the active semester.
     */
    public void validateNotAlreadyEnrolled(CourseSection section, List<Enrollment> currentEnrollments) {
        boolean already = currentEnrollments.stream()
                .anyMatch(e -> e.getSection().getId().equals(section.getId()));
        if (already) {
            throw new EnrollmentException(ApiError.ALREADY_ENROLLED,
                    "Student is already enrolled in " + section.getCourse().getCode());
        }
    }

    /**
     * Cap of {@value #MAX_COURSES_PER_SEMESTER} courses per semester.
     */
    public void validateMaxCourses(List<Enrollment> currentEnrollments) {
        if (currentEnrollments.size() >= MAX_COURSES_PER_SEMESTER) {
            throw new EnrollmentException(ApiError.MAX_COURSES,
                    "Cannot enroll in more than " + MAX_COURSES_PER_SEMESTER + " courses per semester");
        }
    }

    /**
     * If the course declares a prerequisite, the student must have a passed record for it.
     */
    public void validatePrerequisite(List<StudentCourseHistory> history, Course course) {
        Course prereq = course.getPrerequisite();
        if (prereq == null) return;

        Set<Long> passedCourseIds = history.stream()
                .filter(StudentCourseHistory::isPassed)
                .map(record -> record.getCourse().getId())
                .collect(Collectors.toSet());

        if (!passedCourseIds.contains(prereq.getId())) {
            throw new EnrollmentException(ApiError.PREREQUISITE,
                    "Missing prerequisite: must pass " + prereq.getCode() + " (" + prereq.getName() + ")"
                            + " before enrolling in " + course.getCode());
        }
    }

    /**
     * The new section must not overlap any currently-enrolled section on a shared day.
     * Overlap arithmetic lives on the domain entity ({@code CourseSection#overlapsWith}).
     */
    public void validateTimeConflict(CourseSection newSection, List<Enrollment> currentEnrollments) {
        for (Enrollment enrollment : currentEnrollments) {
            CourseSection existing = enrollment.getSection();
            if (newSection.overlapsWith(existing)) {
                throw new EnrollmentException(ApiError.CONFLICT,
                        "Time conflict with " + existing.getCourse().getCode()
                                + " (" + existing.getDaysOfWeek() + " "
                                + existing.getStartTime() + "-" + existing.getEndTime() + ")");
            }
        }
    }
}
