package com.maplewood.repository;

import com.maplewood.model.StudentCourseHistory;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface StudentCourseHistoryRepository extends JpaRepository<StudentCourseHistory, Long> {

    List<StudentCourseHistory> findByStudentId(Long studentId);
}
