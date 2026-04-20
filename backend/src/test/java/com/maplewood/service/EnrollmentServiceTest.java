package com.maplewood.service;

import com.maplewood.dto.ApiError;
import com.maplewood.dto.ScheduleDto;
import com.maplewood.exception.EnrollmentException;
import com.maplewood.exception.NotFoundException;
import com.maplewood.model.Course;
import com.maplewood.model.CourseSection;
import com.maplewood.model.Enrollment;
import com.maplewood.model.Semester;
import com.maplewood.model.Student;
import com.maplewood.repository.CourseRepository;
import com.maplewood.repository.CourseSectionRepository;
import com.maplewood.repository.EnrollmentRepository;
import com.maplewood.repository.SemesterRepository;
import com.maplewood.repository.StudentCourseHistoryRepository;
import com.maplewood.repository.StudentRepository;
import com.maplewood.support.Fixtures;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Unit tests for {@link EnrollmentService}.
 * <p>
 * Focus: orchestration, data access, transactions, and error-mapping.
 * Rule-by-rule coverage lives in {@link EnrollmentValidatorTest}; this class
 * keeps a single rule-violation test (grade level) as a smoke test that the
 * service actually delegates to the validator.
 */
@ExtendWith(MockitoExtension.class)
class EnrollmentServiceTest {

    @Mock private StudentRepository studentRepository;
    @Mock private CourseRepository courseRepository;
    @Mock private CourseSectionRepository sectionRepository;
    @Mock private EnrollmentRepository enrollmentRepository;
    @Mock private SemesterRepository semesterRepository;
    @Mock private StudentCourseHistoryRepository historyRepository;
    @Spy  private EnrollmentValidator validator = new EnrollmentValidator();

    @InjectMocks private EnrollmentService service;

    private Student student;
    private Course course;
    private Semester semester;
    private CourseSection section;

    @BeforeEach
    void setUp() {
        student = Fixtures.student(1L, "Test", "Student", 10);
        semester = Fixtures.semester(100L, "Fall", 2024, 1, true);
        course = Fixtures.course(10L, "ENG101", "English 101", null,
                9, 12, 1, new BigDecimal("3.0"), "core");
        section = Fixtures.section(1000L, course, semester,
                "MON,WED,FRI", "09:00", "10:00");
    }

    // ---------------------------------------------------------------------
    // getSchedule
    // ---------------------------------------------------------------------

    @Test
    void getSchedule_returnsEmptyForStudentWithNoEnrollments() {
        when(semesterRepository.findFirstByActiveTrue()).thenReturn(Optional.of(semester));
        when(enrollmentRepository.findActiveEnrollmentsByStudent(1L)).thenReturn(List.of());

        ScheduleDto result = service.getSchedule(1L);

        assertThat(result.semesterName()).isEqualTo("Fall");
        assertThat(result.semesterYear()).isEqualTo(2024);
        assertThat(result.courseCount()).isZero();
        assertThat(result.maxCourses()).isEqualTo(EnrollmentService.MAX_COURSES_PER_SEMESTER);
        assertThat(result.totalCredits()).isEqualByComparingTo("0.0");
        assertThat(result.items()).isEmpty();
    }

    @Test
    void getSchedule_sumsCreditsAndSortsByStartTime() {
        Course bio = Fixtures.course(11L, "BIO101", "Biology", null,
                9, 12, 1, new BigDecimal("3.0"), "core");
        CourseSection laterSection = Fixtures.section(1001L, bio, semester,
                "TUE,THU", "14:00", "15:00");
        Enrollment later = new Enrollment(1L, laterSection);
        Enrollment earlier = new Enrollment(1L, section); // 09:00

        when(semesterRepository.findFirstByActiveTrue()).thenReturn(Optional.of(semester));
        when(enrollmentRepository.findActiveEnrollmentsByStudent(1L)).thenReturn(List.of(later, earlier));

        ScheduleDto result = service.getSchedule(1L);

        assertThat(result.courseCount()).isEqualTo(2);
        assertThat(result.items()).hasSize(2);
        assertThat(result.items().get(0).course().code()).isEqualTo("ENG101");
        assertThat(result.totalCredits()).isEqualByComparingTo("6.0");
    }

    @Test
    void getSchedule_throwsWhenNoActiveSemester() {
        when(semesterRepository.findFirstByActiveTrue()).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.getSchedule(1L))
                .isInstanceOf(NotFoundException.class)
                .hasMessageContaining("No active semester");
    }

    // ---------------------------------------------------------------------
    // enroll — happy path & lookup failures
    // ---------------------------------------------------------------------

    @Test
    void enroll_savesEnrollmentAndReturnsUpdatedSchedule() {
        stubBasicEnrollDeps();
        when(historyRepository.findByStudentId(1L)).thenReturn(List.of());
        when(enrollmentRepository.findActiveEnrollmentsByStudent(1L)).thenReturn(List.of());

        service.enroll(1L, 10L);

        verify(enrollmentRepository).save(any(Enrollment.class));
    }

    @Test
    void enroll_throwsNotFoundWhenStudentMissing() {
        when(studentRepository.findById(1L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.enroll(1L, 10L))
                .isInstanceOf(NotFoundException.class)
                .hasMessageContaining("Student not found");

        verify(enrollmentRepository, never()).save(any());
    }

    @Test
    void enroll_throwsNotFoundWhenCourseMissing() {
        when(studentRepository.findById(1L)).thenReturn(Optional.of(student));
        when(courseRepository.findById(10L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.enroll(1L, 10L))
                .isInstanceOf(NotFoundException.class)
                .hasMessageContaining("Course not found");
    }

    @Test
    void enroll_throwsWhenCourseNotOfferedThisSemester() {
        when(studentRepository.findById(1L)).thenReturn(Optional.of(student));
        when(courseRepository.findById(10L)).thenReturn(Optional.of(course));
        when(semesterRepository.findFirstByActiveTrue()).thenReturn(Optional.of(semester));
        when(sectionRepository.findByCourseIdAndSemesterId(10L, 100L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.enroll(1L, 10L))
                .isInstanceOf(EnrollmentException.class)
                .hasFieldOrPropertyWithValue("type", ApiError.NO_OFFERING);

        verify(enrollmentRepository, never()).save(any());
    }

    @Test
    void enroll_propagatesValidatorFailure() {
        // Sanity check that the service actually delegates to the validator.
        // Exhaustive rule coverage lives in EnrollmentValidatorTest.
        Course apCourse = Fixtures.course(10L, "ENG201", "AP English", null,
                11, 12, 1, new BigDecimal("3.0"), "core");
        section = Fixtures.section(1000L, apCourse, semester,
                "MON,WED", "09:00", "10:00");

        stubBasicEnrollDeps(apCourse);
        when(historyRepository.findByStudentId(1L)).thenReturn(List.of());
        when(enrollmentRepository.findActiveEnrollmentsByStudent(1L)).thenReturn(List.of());

        assertThatThrownBy(() -> service.enroll(1L, 10L))
                .isInstanceOf(EnrollmentException.class)
                .hasFieldOrPropertyWithValue("type", ApiError.GRADE_LEVEL);

        verify(enrollmentRepository, never()).save(any());
    }

    // ---------------------------------------------------------------------
    // drop
    // ---------------------------------------------------------------------

    @Test
    void drop_deletesEnrollmentAndReturnsUpdatedSchedule() {
        Enrollment existing = new Enrollment(1L, section);
        when(semesterRepository.findFirstByActiveTrue()).thenReturn(Optional.of(semester));
        when(sectionRepository.findByCourseIdAndSemesterId(10L, 100L)).thenReturn(Optional.of(section));
        when(enrollmentRepository.findByStudentIdAndSectionId(1L, 1000L)).thenReturn(Optional.of(existing));
        when(enrollmentRepository.findActiveEnrollmentsByStudent(1L)).thenReturn(List.of());

        service.drop(1L, 10L);

        verify(enrollmentRepository).delete(existing);
    }

    @Test
    void drop_throwsWhenStudentNotEnrolled() {
        when(semesterRepository.findFirstByActiveTrue()).thenReturn(Optional.of(semester));
        when(sectionRepository.findByCourseIdAndSemesterId(10L, 100L)).thenReturn(Optional.of(section));
        when(enrollmentRepository.findByStudentIdAndSectionId(1L, 1000L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.drop(1L, 10L))
                .isInstanceOf(NotFoundException.class)
                .hasMessageContaining("not enrolled");

        verify(enrollmentRepository, never()).delete(any());
    }

    @Test
    void drop_throwsWhenCourseNotOfferedThisSemester() {
        when(semesterRepository.findFirstByActiveTrue()).thenReturn(Optional.of(semester));
        when(sectionRepository.findByCourseIdAndSemesterId(10L, 100L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.drop(1L, 10L))
                .isInstanceOf(NotFoundException.class)
                .hasMessageContaining("not offered");

        verify(enrollmentRepository, never()).delete(any());
    }

    // ---------------------------------------------------------------------
    // Helpers
    // ---------------------------------------------------------------------

    private void stubBasicEnrollDeps() {
        stubBasicEnrollDeps(course);
    }

    private void stubBasicEnrollDeps(Course theCourse) {
        when(studentRepository.findById(1L)).thenReturn(Optional.of(student));
        when(courseRepository.findById(10L)).thenReturn(Optional.of(theCourse));
        when(semesterRepository.findFirstByActiveTrue()).thenReturn(Optional.of(semester));
        when(sectionRepository.findByCourseIdAndSemesterId(10L, 100L)).thenReturn(Optional.of(section));
    }
}
