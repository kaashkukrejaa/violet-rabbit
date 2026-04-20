package com.maplewood.repository;

import com.maplewood.model.CourseSection;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface CourseSectionRepository extends JpaRepository<CourseSection, Long> {

    List<CourseSection> findBySemesterId(Long semesterId);

    Optional<CourseSection> findByCourseIdAndSemesterId(Long courseId, Long semesterId);
}
