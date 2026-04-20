package com.maplewood.service;

import com.maplewood.dto.CourseDto;
import com.maplewood.dto.CourseOfferingDto;
import com.maplewood.model.CourseSection;
import com.maplewood.model.Semester;
import com.maplewood.repository.CourseRepository;
import com.maplewood.repository.CourseSectionRepository;
import com.maplewood.repository.SemesterRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;


@Service
@Transactional(readOnly = true)
public class CourseService {

    private final CourseRepository courseRepository;
    private final CourseSectionRepository sectionRepository;
    private final SemesterRepository semesterRepository;

    public CourseService(CourseRepository courseRepository,
                         CourseSectionRepository sectionRepository,
                         SemesterRepository semesterRepository) {
        this.courseRepository = courseRepository;
        this.sectionRepository = sectionRepository;
        this.semesterRepository = semesterRepository;
    }

    /**
     * Lists all courses with optional filtering by grade level and semester.
     * Used for course catalog browsing.
     * 
     * @param gradeLevel Filter courses appropriate for this grade (e.g., 10, 11, 12)
     * @param semesterOrder Filter by semester (1=Fall, 2=Spring)
     */
    public List<CourseDto> listCourses(Integer gradeLevel, Integer semesterOrder) {
        return courseRepository.findAllOrdered().stream()
                .filter(course -> gradeLevel == null
                        || (course.getGradeLevelMin() != null && course.getGradeLevelMax() != null
                            && course.getGradeLevelMin() <= gradeLevel && course.getGradeLevelMax() >= gradeLevel))
                .filter(course -> semesterOrder == null || course.getSemesterOrder().equals(semesterOrder))
                .map(CourseDto::from)
                .toList();
    }

    /**
     * Returns all course offerings (course + section info) for the active semester.
     * This is what students can actually enroll in right now.
     * Returns empty list if no active semester is configured.
     */
    public List<CourseOfferingDto> listActiveOfferings() {
        Optional<Semester> active = semesterRepository.findFirstByActiveTrue();
        if (active.isEmpty()) return List.of();
        return sectionRepository.findBySemesterId(active.get().getId()).stream()
                .sorted((sectionA, sectionB) -> sectionA.getCourse().getCode().compareTo(sectionB.getCourse().getCode()))
                .map(CourseOfferingDto::from)
                .toList();
    }

    /**
     * Internal helper: Maps course IDs to their active sections for quick lookup.
     * Used by EnrollmentService for finding sections during enrollment.
     */
    public Map<Long, CourseSection> activeSectionsByCourseId() {
        Optional<Semester> active = semesterRepository.findFirstByActiveTrue();
        if (active.isEmpty()) return Map.of();
        return sectionRepository.findBySemesterId(active.get().getId()).stream()
                .collect(Collectors.toMap(section -> section.getCourse().getId(), Function.identity()));
    }
}
