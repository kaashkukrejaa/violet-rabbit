package com.maplewood.service;

import com.maplewood.dto.ApiError;
import com.maplewood.dto.CourseOfferingDto;
import com.maplewood.dto.ScheduleDto;
import com.maplewood.exception.EnrollmentException;
import com.maplewood.exception.NotFoundException;
import com.maplewood.model.Course;
import com.maplewood.model.CourseSection;
import com.maplewood.model.Enrollment;
import com.maplewood.model.Semester;
import com.maplewood.model.Student;
import com.maplewood.model.StudentCourseHistory;
import com.maplewood.repository.CourseRepository;
import com.maplewood.repository.CourseSectionRepository;
import com.maplewood.repository.EnrollmentRepository;
import com.maplewood.repository.SemesterRepository;
import com.maplewood.repository.StudentCourseHistoryRepository;
import com.maplewood.repository.StudentRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Comparator;
import java.util.List;

/**
 * Orchestrates enrolment use-cases: fetching schedules, enrolling, and dropping.
 * <p>
 * Business-rule enforcement is delegated to {@link EnrollmentValidator}, which keeps
 * this class focused on data access, transactions, and response shaping.
 */
@Service
public class EnrollmentService {

    public static final int MAX_COURSES_PER_SEMESTER = EnrollmentValidator.MAX_COURSES_PER_SEMESTER;

    private final StudentRepository studentRepository;
    private final CourseRepository courseRepository;
    private final CourseSectionRepository sectionRepository;
    private final EnrollmentRepository enrollmentRepository;
    private final SemesterRepository semesterRepository;
    private final StudentCourseHistoryRepository historyRepository;
    private final EnrollmentValidator validator;

    public EnrollmentService(StudentRepository studentRepository,
                             CourseRepository courseRepository,
                             CourseSectionRepository sectionRepository,
                             EnrollmentRepository enrollmentRepository,
                             SemesterRepository semesterRepository,
                             StudentCourseHistoryRepository historyRepository,
                             EnrollmentValidator validator) {
        this.studentRepository = studentRepository;
        this.courseRepository = courseRepository;
        this.sectionRepository = sectionRepository;
        this.enrollmentRepository = enrollmentRepository;
        this.semesterRepository = semesterRepository;
        this.historyRepository = historyRepository;
        this.validator = validator;
    }

    /**
     * Retrieves a student's current semester schedule with all enrolled courses.
     * Courses are sorted by start time, then alphabetically by code for consistent ordering.
     */
    @Transactional(readOnly = true)
    public ScheduleDto getSchedule(Long studentId) {
        Semester semester = getActiveSemesterOrThrow();
        List<Enrollment> enrollments = enrollmentRepository.findActiveEnrollmentsByStudent(studentId);

        List<CourseOfferingDto> items = enrollments.stream()
                .sorted(Comparator
                        .comparing((Enrollment enrollment) -> enrollment.getSection().getStartTime())
                        .thenComparing(enrollment -> enrollment.getSection().getCourse().getCode()))
                .map(enrollment -> CourseOfferingDto.from(enrollment.getSection()))
                .toList();

        BigDecimal totalCredits = enrollments.stream()
                .map(enrollment -> enrollment.getSection().getCourse().getCredits())
                .reduce(BigDecimal.ZERO, BigDecimal::add)
                .setScale(1, RoundingMode.HALF_UP);

        return new ScheduleDto(
                semester.getName(),
                semester.getYear(),
                enrollments.size(),
                MAX_COURSES_PER_SEMESTER,
                totalCredits,
                items
        );
    }

    /**
     * Enrols a student in a course after running every business rule.
     *
     * @return updated schedule including the new enrolment
     * @throws EnrollmentException if any rule in {@link EnrollmentValidator} fails
     * @throws NotFoundException   if the student, course, or active semester is missing,
     *                             or the course is not offered this semester
     */
    @Transactional
    public ScheduleDto enroll(Long studentId, Long courseId) {
        Student student = getStudentOrThrow(studentId);
        Course course = courseRepository.findById(courseId)
                .orElseThrow(() -> new NotFoundException("Course not found: " + courseId));
        Semester semester = getActiveSemesterOrThrow();

        CourseSection section = sectionRepository
                .findByCourseIdAndSemesterId(courseId, semester.getId())
                .orElseThrow(() -> new EnrollmentException(ApiError.NO_OFFERING,
                        course.getCode() + " is not offered in " + semester.getDisplayName()));

        List<StudentCourseHistory> history = historyRepository.findByStudentId(studentId);
        List<Enrollment> currentEnrollments = enrollmentRepository.findActiveEnrollmentsByStudent(studentId);

        validator.validateCanEnroll(student, course, section, history, currentEnrollments);

        enrollmentRepository.save(new Enrollment(studentId, section));
        return getSchedule(studentId);
    }

    /**
     * Drops a student from a course in the current semester.
     *
     * @return updated schedule without the dropped course
     * @throws NotFoundException if course is not offered this semester or student is not enrolled
     */
    @Transactional
    public ScheduleDto drop(Long studentId, Long courseId) {
        Semester semester = getActiveSemesterOrThrow();
        CourseSection section = sectionRepository
                .findByCourseIdAndSemesterId(courseId, semester.getId())
                .orElseThrow(() -> new NotFoundException("Course is not offered in " + semester.getDisplayName()));

        Enrollment enrollment = enrollmentRepository
                .findByStudentIdAndSectionId(studentId, section.getId())
                .orElseThrow(() -> new NotFoundException("Student is not enrolled in this course"));
        enrollmentRepository.delete(enrollment);
        return getSchedule(studentId);
    }

    private Student getStudentOrThrow(Long studentId) {
        return studentRepository.findById(studentId)
                .orElseThrow(() -> new NotFoundException("Student not found: " + studentId));
    }

    private Semester getActiveSemesterOrThrow() {
        return semesterRepository.findFirstByActiveTrue()
                .orElseThrow(() -> new NotFoundException("No active semester configured"));
    }
}
