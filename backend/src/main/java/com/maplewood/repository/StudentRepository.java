package com.maplewood.repository;

import com.maplewood.model.Student;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface StudentRepository extends JpaRepository<Student, Long> {

    List<Student> findTop50ByOrderByLastNameAscFirstNameAsc();
}
