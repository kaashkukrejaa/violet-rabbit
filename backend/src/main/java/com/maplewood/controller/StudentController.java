package com.maplewood.controller;

import com.maplewood.dto.ScheduleDto;
import com.maplewood.dto.StudentProfileDto;
import com.maplewood.dto.StudentSummaryDto;
import com.maplewood.service.EnrollmentService;
import com.maplewood.service.StudentService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/students")
public class StudentController {

    private final StudentService studentService;
    private final EnrollmentService enrollmentService;

    public StudentController(StudentService studentService, EnrollmentService enrollmentService) {
        this.studentService = studentService;
        this.enrollmentService = enrollmentService;
    }

    /**
     * Returns first 50 students for demo picker dropdown.
     */
    @GetMapping
    public List<StudentSummaryDto> list() {
        return studentService.listStudents();
    }

    /**
     * Returns complete student profile including GPA, credits earned, 
     * graduation progress, and full course history.
     */
    @GetMapping("/{id}")
    public StudentProfileDto get(@PathVariable Long id) {
        return studentService.getProfile(id);
    }

    /**
     * Returns student's current semester schedule with enrolled courses.
     */
    @GetMapping("/{id}/schedule")
    public ScheduleDto schedule(@PathVariable Long id) {
        return enrollmentService.getSchedule(id);
    }
}
