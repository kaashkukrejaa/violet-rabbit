package com.maplewood.service;

import com.maplewood.dto.CourseDto;
import com.maplewood.dto.CourseOfferingDto;
import com.maplewood.model.Course;
import com.maplewood.model.CourseSection;
import com.maplewood.model.Semester;
import com.maplewood.repository.CourseRepository;
import com.maplewood.repository.CourseSectionRepository;
import com.maplewood.repository.SemesterRepository;
import com.maplewood.support.Fixtures;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

/**
 * Unit tests for {@link CourseService}.
 * Exercises filtering logic, active-offering discovery, and the section lookup map.
 */
@ExtendWith(MockitoExtension.class)
class CourseServiceTest {

    @Mock private CourseRepository courseRepository;
    @Mock private CourseSectionRepository sectionRepository;
    @Mock private SemesterRepository semesterRepository;

    @InjectMocks private CourseService service;

    private Semester activeSemester;

    @BeforeEach
    void setUp() {
        activeSemester = Fixtures.semester(100L, "Fall", 2024, 1, true);
    }

    // ---------- listCourses ----------

    @Test
    void listCourses_returnsAll_whenNoFiltersProvided() {
        Course eng = Fixtures.course(1L, "ENG101", "English", null, 9, 12, 1, new BigDecimal("3.0"), "core");
        Course mat = Fixtures.course(2L, "MAT101", "Math",    null, 9, 12, 2, new BigDecimal("3.0"), "core");
        when(courseRepository.findAllOrdered()).thenReturn(List.of(eng, mat));

        List<CourseDto> result = service.listCourses(null, null);

        assertThat(result).extracting(CourseDto::code).containsExactly("ENG101", "MAT101");
    }

    @Test
    void listCourses_filtersByGradeLevel_inclusiveOnBothSides() {
        Course freshman  = Fixtures.course(1L, "ENG101", "English", null, 9, 9,  1, new BigDecimal("3.0"), "core");
        Course allGrades = Fixtures.course(2L, "MAT101", "Math",    null, 9, 12, 1, new BigDecimal("3.0"), "core");
        Course senior    = Fixtures.course(3L, "AP101",  "AP Bio",  null, 11, 12, 1, new BigDecimal("3.0"), "core");
        when(courseRepository.findAllOrdered()).thenReturn(List.of(freshman, allGrades, senior));

        List<CourseDto> result = service.listCourses(10, null);

        assertThat(result).extracting(CourseDto::code).containsExactly("MAT101");
    }

    @Test
    void listCourses_filtersBySemesterOrder() {
        Course fall   = Fixtures.course(1L, "ENG101", "English", null, 9, 12, 1, new BigDecimal("3.0"), "core");
        Course spring = Fixtures.course(2L, "MAT101", "Math",    null, 9, 12, 2, new BigDecimal("3.0"), "core");
        when(courseRepository.findAllOrdered()).thenReturn(List.of(fall, spring));

        List<CourseDto> result = service.listCourses(null, 2);

        assertThat(result).extracting(CourseDto::code).containsExactly("MAT101");
    }

    @Test
    void listCourses_combinesFilters_andReturnsIntersection() {
        Course fallFreshman = Fixtures.course(1L, "ENG101", "English", null, 9, 9,  1, new BigDecimal("3.0"), "core");
        Course fallAll      = Fixtures.course(2L, "MAT101", "Math",    null, 9, 12, 1, new BigDecimal("3.0"), "core");
        Course springAll    = Fixtures.course(3L, "HIS101", "History", null, 9, 12, 2, new BigDecimal("3.0"), "core");
        when(courseRepository.findAllOrdered()).thenReturn(List.of(fallFreshman, fallAll, springAll));

        List<CourseDto> result = service.listCourses(10, 1);

        assertThat(result).extracting(CourseDto::code).containsExactly("MAT101");
    }

    // ---------- listActiveOfferings ----------

    @Test
    void listActiveOfferings_returnsEmpty_whenNoActiveSemester() {
        when(semesterRepository.findFirstByActiveTrue()).thenReturn(Optional.empty());

        List<CourseOfferingDto> result = service.listActiveOfferings();

        assertThat(result).isEmpty();
    }

    @Test
    void listActiveOfferings_sortsByCourseCode_ascending() {
        Course bio = Fixtures.simpleCourse(2L, "BIO101");
        Course art = Fixtures.simpleCourse(3L, "ART101");
        Course eng = Fixtures.simpleCourse(1L, "ENG101");
        CourseSection s1 = Fixtures.section(10L, bio, activeSemester, "MON", "09:00", "10:00");
        CourseSection s2 = Fixtures.section(11L, art, activeSemester, "TUE", "10:00", "11:00");
        CourseSection s3 = Fixtures.section(12L, eng, activeSemester, "WED", "11:00", "12:00");

        when(semesterRepository.findFirstByActiveTrue()).thenReturn(Optional.of(activeSemester));
        when(sectionRepository.findBySemesterId(100L)).thenReturn(List.of(s1, s2, s3));

        List<CourseOfferingDto> result = service.listActiveOfferings();

        assertThat(result).extracting(o -> o.course().code()).containsExactly("ART101", "BIO101", "ENG101");
    }

    // ---------- activeSectionsByCourseId ----------

    @Test
    void activeSectionsByCourseId_returnsEmptyMap_whenNoActiveSemester() {
        when(semesterRepository.findFirstByActiveTrue()).thenReturn(Optional.empty());

        Map<Long, CourseSection> result = service.activeSectionsByCourseId();

        assertThat(result).isEmpty();
    }

    @Test
    void activeSectionsByCourseId_keyedByCourseId() {
        Course eng = Fixtures.simpleCourse(1L, "ENG101");
        Course mat = Fixtures.simpleCourse(2L, "MAT101");
        CourseSection sEng = Fixtures.section(10L, eng, activeSemester, "MON", "09:00", "10:00");
        CourseSection sMat = Fixtures.section(11L, mat, activeSemester, "TUE", "10:00", "11:00");

        when(semesterRepository.findFirstByActiveTrue()).thenReturn(Optional.of(activeSemester));
        when(sectionRepository.findBySemesterId(100L)).thenReturn(List.of(sEng, sMat));

        Map<Long, CourseSection> result = service.activeSectionsByCourseId();

        assertThat(result).containsOnlyKeys(1L, 2L);
        assertThat(result.get(1L).getId()).isEqualTo(10L);
        assertThat(result.get(2L).getId()).isEqualTo(11L);
    }
}
