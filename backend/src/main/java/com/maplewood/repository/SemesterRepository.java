package com.maplewood.repository;

import com.maplewood.model.Semester;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface SemesterRepository extends JpaRepository<Semester, Long> {

    Optional<Semester> findFirstByActiveTrue();
}
