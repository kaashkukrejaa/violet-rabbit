package com.maplewood.repository;

import com.maplewood.model.Enrollment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface EnrollmentRepository extends JpaRepository<Enrollment, Long> {

    @Query("""
        SELECT e FROM Enrollment e
        JOIN FETCH e.section s
        JOIN FETCH s.course
        JOIN FETCH s.semester sem
        LEFT JOIN FETCH s.teacher
        LEFT JOIN FETCH s.classroom
        WHERE e.studentId = :studentId AND sem.active = true
        """)
    List<Enrollment> findActiveEnrollmentsByStudent(Long studentId);

    Optional<Enrollment> findByStudentIdAndSectionId(Long studentId, Long sectionId);
}
