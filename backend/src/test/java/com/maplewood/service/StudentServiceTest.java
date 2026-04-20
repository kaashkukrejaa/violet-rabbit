package com.maplewood.service;

import com.maplewood.dto.StudentProfileDto;
import com.maplewood.dto.StudentSummaryDto;
import com.maplewood.exception.NotFoundException;
import com.maplewood.model.Course;
import com.maplewood.model.Semester;
import com.maplewood.model.Student;
import com.maplewood.repository.StudentCourseHistoryRepository;
import com.maplewood.repository.StudentRepository;
import com.maplewood.support.Fixtures;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

/**
 * Unit tests for {@link StudentService} with a focus on GPA calculation,
 * graduation progress, and history sorting.
 */
@ExtendWith(MockitoExtension.class)
class StudentServiceTest {

    @Mock private StudentRepository studentRepository;
    @Mock private StudentCourseHistoryRepository historyRepository;

    @InjectMocks private StudentService service;

    // ---------- listStudents ----------

    @Test
    void listStudents_mapsEntitiesToSummaryDtos() {
        when(studentRepository.findTop50ByOrderByLastNameAscFirstNameAsc()).thenReturn(List.of(
                Fixtures.student(1L, "Emma",  "Wilson", 10),
                Fixtures.student(2L, "James", "Lee",    11)
        ));

        List<StudentSummaryDto> result = service.listStudents();

        assertThat(result).extracting(StudentSummaryDto::id).containsExactly(1L, 2L);
        assertThat(result).extracting(StudentSummaryDto::lastName).containsExactly("Wilson", "Lee");
    }

    // ---------- getProfile: not found ----------

    @Test
    void getProfile_throwsNotFound_whenStudentMissing() {
        when(studentRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.getProfile(99L))
                .isInstanceOf(NotFoundException.class)
                .hasMessageContaining("Student not found: 99");
    }

    // ---------- getProfile: GPA calculation ----------

    @Test
    void getProfile_returnsZeroGpa_whenNoCoursesAttempted() {
        Student student = Fixtures.student(1L, "Emma", "Wilson", 10);
        when(studentRepository.findById(1L)).thenReturn(Optional.of(student));
        when(historyRepository.findByStudentId(1L)).thenReturn(List.of());

        StudentProfileDto profile = service.getProfile(1L);

        assertThat(profile.gpa()).isEqualByComparingTo("0.0");
        assertThat(profile.creditsEarned()).isEqualByComparingTo("0.0");
        assertThat(profile.graduationProgress()).isEqualByComparingTo("0.0");
        assertThat(profile.courseHistory()).isEmpty();
    }

    @Test
    void getProfile_calculatesGpaAsFourPointScale_whenAllPassed() {
        // 3 credits attempted, 3 earned → 3/3 × 4 = 4.00 GPA
        Student student = Fixtures.student(1L, "Emma", "Wilson", 10);
        Semester sem = Fixtures.semester(5L, "Fall", 2023, 1, false);
        Course c = Fixtures.course(10L, "ENG101", "English", null, 9, 12, 1, new BigDecimal("3.0"), "core");

        when(studentRepository.findById(1L)).thenReturn(Optional.of(student));
        when(historyRepository.findByStudentId(1L)).thenReturn(List.of(
                Fixtures.history(1L, 1L, c, sem, "passed")
        ));

        StudentProfileDto profile = service.getProfile(1L);

        assertThat(profile.gpa()).isEqualByComparingTo("4.00");
        assertThat(profile.creditsEarned()).isEqualByComparingTo("3.0");
    }

    @Test
    void getProfile_calculatesPartialGpa_whenSomeCoursesFailed() {
        // 6 credits attempted, 3 passed → 3/6 × 4 = 2.00 GPA
        Student student = Fixtures.student(1L, "Emma", "Wilson", 10);
        Semester sem = Fixtures.semester(5L, "Fall", 2023, 1, false);
        Course passed = Fixtures.course(10L, "ENG101", "English", null, 9, 12, 1, new BigDecimal("3.0"), "core");
        Course failed = Fixtures.course(11L, "MAT101", "Math",    null, 9, 12, 1, new BigDecimal("3.0"), "core");

        when(studentRepository.findById(1L)).thenReturn(Optional.of(student));
        when(historyRepository.findByStudentId(1L)).thenReturn(List.of(
                Fixtures.history(1L, 1L, passed, sem, "passed"),
                Fixtures.history(2L, 1L, failed, sem, "failed")
        ));

        StudentProfileDto profile = service.getProfile(1L);

        assertThat(profile.gpa()).isEqualByComparingTo("2.00");
        assertThat(profile.creditsEarned()).isEqualByComparingTo("3.0");
    }

    @Test
    void getProfile_skipsHistoryRecordsWithNullCredits_inGpaMath() {
        Student student = Fixtures.student(1L, "Emma", "Wilson", 10);
        Semester sem = Fixtures.semester(5L, "Fall", 2023, 1, false);
        Course real = Fixtures.course(10L, "ENG101", "English", null,
                9, 12, 1, new BigDecimal("3.0"), "core");
        Course noCredits = Fixtures.course(11L, "MAT101", "Math", null,
                9, 12, 1, null /* null credits */, "core");

        when(studentRepository.findById(1L)).thenReturn(Optional.of(student));
        when(historyRepository.findByStudentId(1L)).thenReturn(List.of(
                Fixtures.history(1L, 1L, real, sem, "passed"),
                Fixtures.history(2L, 1L, noCredits, sem, "passed")
        ));

        StudentProfileDto profile = service.getProfile(1L);

        // Only the record with valid credits contributes to GPA/credit math.
        assertThat(profile.creditsEarned()).isEqualByComparingTo("3.0");
        // Both records still appear in the history list (they are shown to the user).
        assertThat(profile.courseHistory()).hasSize(2);
    }

    // ---------- getProfile: graduation progress ----------

    @Test
    void getProfile_capsGraduationProgressAt100() {
        // Student with 60 credits earned → would be 200%, should cap at 100.0
        Student student = Fixtures.student(1L, "Emma", "Wilson", 12);
        Semester sem = Fixtures.semester(5L, "Fall", 2023, 1, false);
        Course bigCredit = Fixtures.course(10L, "ENG101", "English", null, 9, 12, 1, new BigDecimal("60.0"), "core");

        when(studentRepository.findById(1L)).thenReturn(Optional.of(student));
        when(historyRepository.findByStudentId(1L)).thenReturn(List.of(
                Fixtures.history(1L, 1L, bigCredit, sem, "passed")
        ));

        StudentProfileDto profile = service.getProfile(1L);

        assertThat(profile.graduationProgress()).isEqualByComparingTo("100.0");
    }

    // ---------- getProfile: history sorting ----------

    @Test
    void getProfile_sortsHistoryByYearThenSemesterOrderWithinYear() {
        // Uses distinct year+order combinations so the sort is unambiguous.
        // Sort contract: year ascending first, then orderInYear ascending.
        Student student = Fixtures.student(1L, "Emma", "Wilson", 12);
        Semester y2022o1 = Fixtures.semester(1L, "SemA", 2022, 1, false);
        Semester y2022o2 = Fixtures.semester(2L, "SemB", 2022, 2, false);
        Semester y2023o1 = Fixtures.semester(3L, "SemC", 2023, 1, false);
        Course c = Fixtures.course(10L, "ENG101", "English", null, 9, 12, 1, new BigDecimal("3.0"), "core");

        when(studentRepository.findById(1L)).thenReturn(Optional.of(student));
        // Deliberately out of order in the input.
        when(historyRepository.findByStudentId(1L)).thenReturn(List.of(
                Fixtures.history(10L, 1L, c, y2023o1, "passed"),
                Fixtures.history(11L, 1L, c, y2022o1, "passed"),
                Fixtures.history(12L, 1L, c, y2022o2, "passed")
        ));

        StudentProfileDto profile = service.getProfile(1L);

        assertThat(profile.courseHistory()).extracting(h -> h.semesterYear() + "-" + h.semesterName())
                .containsExactly("2022-SemA", "2022-SemB", "2023-SemC");
    }

    // ---------- getProfile: population ----------

    @Test
    void getProfile_populatesAllTopLevelFieldsFromStudent() {
        Student student = Fixtures.student(7L, "Emma", "Wilson", 10);
        // expectedGraduationYear is optional and not set via Fixtures; verify null passes through
        when(studentRepository.findById(7L)).thenReturn(Optional.of(student));
        when(historyRepository.findByStudentId(7L)).thenReturn(List.of());

        StudentProfileDto profile = service.getProfile(7L);

        assertThat(profile.id()).isEqualTo(7L);
        assertThat(profile.firstName()).isEqualTo("Emma");
        assertThat(profile.lastName()).isEqualTo("Wilson");
        assertThat(profile.gradeLevel()).isEqualTo(10);
        assertThat(profile.creditsRequired()).isEqualByComparingTo("30");
    }
}
