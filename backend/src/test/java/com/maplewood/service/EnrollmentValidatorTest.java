package com.maplewood.service;

import com.maplewood.dto.ApiError;
import com.maplewood.exception.EnrollmentException;
import com.maplewood.model.Course;
import com.maplewood.model.CourseSection;
import com.maplewood.model.Enrollment;
import com.maplewood.model.Semester;
import com.maplewood.model.Student;
import com.maplewood.model.StudentCourseHistory;
import com.maplewood.support.Fixtures;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Unit tests for {@link EnrollmentValidator}.
 * <p>
 * The validator is pure and has no dependencies, so tests construct it directly
 * and feed it fully-built fixture data — no Mockito, no Spring, no database.
 */
class EnrollmentValidatorTest {

    private EnrollmentValidator validator;
    private Semester semester;
    private Student gradeTenStudent;
    private Course englishCourse;
    private CourseSection englishSection;

    @BeforeEach
    void setUp() {
        validator = new EnrollmentValidator();
        semester = Fixtures.semester(100L, "Fall", 2024, 1, true);
        gradeTenStudent = Fixtures.student(1L, "Test", "Student", 10);
        englishCourse = Fixtures.course(10L, "ENG101", "English 101", null,
                9, 12, 1, new BigDecimal("3.0"), "core");
        englishSection = Fixtures.section(1000L, englishCourse, semester,
                "MON,WED,FRI", "09:00", "10:00");
    }

    // ---------------------------------------------------------------------
    // validateGradeLevel
    // ---------------------------------------------------------------------

    @Nested
    class GradeLevel {

        @Test
        void passes_whenStudentInRange() {
            assertThatCode(() -> validator.validateGradeLevel(gradeTenStudent, englishCourse))
                    .doesNotThrowAnyException();
        }

        @Test
        void throws_whenStudentBelowMinimum() {
            Course apCourse = Fixtures.course(20L, "ENG201", "AP English", null,
                    11, 12, 1, new BigDecimal("3.0"), "core");

            assertThatThrownBy(() -> validator.validateGradeLevel(gradeTenStudent, apCourse))
                    .isInstanceOf(EnrollmentException.class)
                    .hasFieldOrPropertyWithValue("type", ApiError.GRADE_LEVEL)
                    .hasMessageContaining("ENG201")
                    .hasMessageContaining("grades 11-12");
        }

        @Test
        void throws_whenStudentAboveMaximum() {
            Student grade12 = Fixtures.student(2L, "Senior", "Student", 12);
            Course freshmanOnly = Fixtures.course(21L, "FYE100", "Freshman Experience", null,
                    9, 9, 1, new BigDecimal("1.0"), "elective");

            assertThatThrownBy(() -> validator.validateGradeLevel(grade12, freshmanOnly))
                    .isInstanceOf(EnrollmentException.class)
                    .hasFieldOrPropertyWithValue("type", ApiError.GRADE_LEVEL);
        }

        @Test
        void passes_whenCourseHasNoGradeRange() {
            Course open = Fixtures.course(22L, "OPEN100", "Open Course", null,
                    null, null, 1, new BigDecimal("3.0"), "elective");

            assertThatCode(() -> validator.validateGradeLevel(gradeTenStudent, open))
                    .doesNotThrowAnyException();
        }
    }

    // ---------------------------------------------------------------------
    // validateNotAlreadyPassed
    // ---------------------------------------------------------------------

    @Nested
    class AlreadyPassed {

        @Test
        void throws_whenStudentPassedCourseBefore() {
            StudentCourseHistory passed = Fixtures.history(1L, 1L, englishCourse, semester, "passed");

            assertThatThrownBy(() ->
                    validator.validateNotAlreadyPassed(List.of(passed), englishCourse))
                    .isInstanceOf(EnrollmentException.class)
                    .hasFieldOrPropertyWithValue("type", ApiError.ALREADY_PASSED);
        }

        @Test
        void allowsRetake_whenPreviousAttemptFailed() {
            StudentCourseHistory failed = Fixtures.history(1L, 1L, englishCourse, semester, "failed");

            assertThatCode(() ->
                    validator.validateNotAlreadyPassed(List.of(failed), englishCourse))
                    .doesNotThrowAnyException();
        }

        @Test
        void passes_whenNoHistory() {
            assertThatCode(() ->
                    validator.validateNotAlreadyPassed(List.of(), englishCourse))
                    .doesNotThrowAnyException();
        }

        @Test
        void passes_whenHistoryIsForADifferentCourse() {
            Course other = Fixtures.simpleCourse(999L, "BIO101");
            StudentCourseHistory passed = Fixtures.history(1L, 1L, other, semester, "passed");

            assertThatCode(() ->
                    validator.validateNotAlreadyPassed(List.of(passed), englishCourse))
                    .doesNotThrowAnyException();
        }
    }

    // ---------------------------------------------------------------------
    // validateNotAlreadyEnrolled
    // ---------------------------------------------------------------------

    @Nested
    class AlreadyEnrolled {

        @Test
        void throws_whenStudentHasEnrollmentInSameSection() {
            Enrollment existing = new Enrollment(1L, englishSection);

            assertThatThrownBy(() ->
                    validator.validateNotAlreadyEnrolled(englishSection, List.of(existing)))
                    .isInstanceOf(EnrollmentException.class)
                    .hasFieldOrPropertyWithValue("type", ApiError.ALREADY_ENROLLED);
        }

        @Test
        void passes_whenEnrolledInADifferentSection() {
            CourseSection otherSection = Fixtures.section(9999L, englishCourse, semester,
                    "TUE,THU", "14:00", "15:00");
            Enrollment existing = new Enrollment(1L, otherSection);

            assertThatCode(() ->
                    validator.validateNotAlreadyEnrolled(englishSection, List.of(existing)))
                    .doesNotThrowAnyException();
        }

        @Test
        void passes_whenNoCurrentEnrollments() {
            assertThatCode(() ->
                    validator.validateNotAlreadyEnrolled(englishSection, List.of()))
                    .doesNotThrowAnyException();
        }
    }

    // ---------------------------------------------------------------------
    // validateMaxCourses
    // ---------------------------------------------------------------------

    @Nested
    class MaxCourses {

        @Test
        void throws_whenStudentAlreadyAtLimit() {
            List<Enrollment> five = List.of(
                    dummyEnrollment("A1", "MON", "08:00", "08:45"),
                    dummyEnrollment("A2", "MON", "09:00", "09:45"),
                    dummyEnrollment("A3", "TUE", "08:00", "08:45"),
                    dummyEnrollment("A4", "WED", "13:00", "13:45"),
                    dummyEnrollment("A5", "THU", "14:00", "14:45")
            );

            assertThatThrownBy(() -> validator.validateMaxCourses(five))
                    .isInstanceOf(EnrollmentException.class)
                    .hasFieldOrPropertyWithValue("type", ApiError.MAX_COURSES);
        }

        @Test
        void passes_whenBelowLimit() {
            List<Enrollment> four = List.of(
                    dummyEnrollment("A1", "MON", "08:00", "08:45"),
                    dummyEnrollment("A2", "TUE", "08:00", "08:45"),
                    dummyEnrollment("A3", "WED", "08:00", "08:45"),
                    dummyEnrollment("A4", "THU", "08:00", "08:45")
            );

            assertThatCode(() -> validator.validateMaxCourses(four))
                    .doesNotThrowAnyException();
        }

        @Test
        void passes_whenNoEnrollments() {
            assertThatCode(() -> validator.validateMaxCourses(List.of()))
                    .doesNotThrowAnyException();
        }

        @Test
        void limitConstantIsFive() {
            assertThat(EnrollmentValidator.MAX_COURSES_PER_SEMESTER).isEqualTo(5);
        }
    }

    // ---------------------------------------------------------------------
    // validatePrerequisite
    // ---------------------------------------------------------------------

    @Nested
    class Prerequisite {

        @Test
        void passes_whenCourseHasNoPrerequisite() {
            assertThatCode(() ->
                    validator.validatePrerequisite(List.of(), englishCourse))
                    .doesNotThrowAnyException();
        }

        @Test
        void throws_whenPrerequisiteNotInHistory() {
            Course prereq = Fixtures.simpleCourse(9L, "ENG101");
            Course advanced = Fixtures.course(10L, "ENG201", "English 201", prereq,
                    9, 12, 1, new BigDecimal("3.0"), "core");

            assertThatThrownBy(() ->
                    validator.validatePrerequisite(List.of(), advanced))
                    .isInstanceOf(EnrollmentException.class)
                    .hasFieldOrPropertyWithValue("type", ApiError.PREREQUISITE)
                    .hasMessageContaining("ENG101");
        }

        @Test
        void throws_whenPrerequisiteFailedPreviously() {
            Course prereq = Fixtures.simpleCourse(9L, "ENG101");
            Course advanced = Fixtures.course(10L, "ENG201", "English 201", prereq,
                    9, 12, 1, new BigDecimal("3.0"), "core");
            StudentCourseHistory failed = Fixtures.history(1L, 1L, prereq, semester, "failed");

            assertThatThrownBy(() ->
                    validator.validatePrerequisite(List.of(failed), advanced))
                    .isInstanceOf(EnrollmentException.class)
                    .hasFieldOrPropertyWithValue("type", ApiError.PREREQUISITE);
        }

        @Test
        void passes_whenPrerequisiteIsPassed() {
            Course prereq = Fixtures.simpleCourse(9L, "ENG101");
            Course advanced = Fixtures.course(10L, "ENG201", "English 201", prereq,
                    9, 12, 1, new BigDecimal("3.0"), "core");
            StudentCourseHistory passed = Fixtures.history(1L, 1L, prereq, semester, "passed");

            assertThatCode(() ->
                    validator.validatePrerequisite(List.of(passed), advanced))
                    .doesNotThrowAnyException();
        }
    }

    // ---------------------------------------------------------------------
    // validateTimeConflict
    // ---------------------------------------------------------------------

    @Nested
    class TimeConflict {

        @Test
        void throws_whenTimesOverlapOnSharedDay() {
            CourseSection existing = Fixtures.section(2000L,
                    Fixtures.simpleCourse(99L, "HIS101"), semester,
                    "MON,WED", "09:30", "10:30");

            assertThatThrownBy(() -> validator.validateTimeConflict(
                    englishSection, List.of(new Enrollment(1L, existing))))
                    .isInstanceOf(EnrollmentException.class)
                    .hasFieldOrPropertyWithValue("type", ApiError.CONFLICT)
                    .hasMessageContaining("HIS101");
        }

        @Test
        void passes_whenDaysDoNotOverlap() {
            CourseSection nonOverlap = Fixtures.section(2001L,
                    Fixtures.simpleCourse(99L, "HIS101"), semester,
                    "TUE,THU", "09:00", "10:00");

            assertThatCode(() -> validator.validateTimeConflict(
                    englishSection, List.of(new Enrollment(1L, nonOverlap))))
                    .doesNotThrowAnyException();
        }

        @Test
        void passes_whenSameDayButNoTimeOverlap() {
            CourseSection later = Fixtures.section(2002L,
                    Fixtures.simpleCourse(99L, "HIS101"), semester,
                    "MON,WED,FRI", "10:00", "11:00");

            assertThatCode(() -> validator.validateTimeConflict(
                    englishSection, List.of(new Enrollment(1L, later))))
                    .doesNotThrowAnyException();
        }

        @Test
        void passes_whenNoOtherEnrollments() {
            assertThatCode(() -> validator.validateTimeConflict(englishSection, List.of()))
                    .doesNotThrowAnyException();
        }
    }

    // ---------------------------------------------------------------------
    // validateCanEnroll (orchestrator)
    // ---------------------------------------------------------------------

    @Nested
    class CanEnroll {

        @Test
        void passes_whenAllRulesSatisfied() {
            assertThatCode(() -> validator.validateCanEnroll(
                    gradeTenStudent, englishCourse, englishSection, List.of(), List.of()))
                    .doesNotThrowAnyException();
        }

        @Test
        void short_circuits_onFirstFailingRule() {
            // Grade level is checked first; if it fails we must not reach prerequisite checks.
            Course apCourse = Fixtures.course(20L, "ENG201", "AP English", null,
                    11, 12, 1, new BigDecimal("3.0"), "core");
            CourseSection apSection = Fixtures.section(2100L, apCourse, semester,
                    "MON,WED,FRI", "09:00", "10:00");

            assertThatThrownBy(() -> validator.validateCanEnroll(
                    gradeTenStudent, apCourse, apSection, List.of(), List.of()))
                    .isInstanceOf(EnrollmentException.class)
                    .hasFieldOrPropertyWithValue("type", ApiError.GRADE_LEVEL);
        }
    }

    // ---------------------------------------------------------------------
    // Helpers
    // ---------------------------------------------------------------------

    private Enrollment dummyEnrollment(String code, String day, String start, String end) {
        Course c = Fixtures.simpleCourse((long) (1000 + code.hashCode()), code);
        CourseSection s = Fixtures.section((long) (2000 + code.hashCode()),
                c, semester, day, start, end);
        return new Enrollment(1L, s);
    }
}
