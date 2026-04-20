package com.maplewood.repository;

import com.maplewood.model.Course;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface CourseRepository extends JpaRepository<Course, Long> {

    @Query("SELECT c FROM Course c ORDER BY c.code")
    List<Course> findAllOrdered();
}
