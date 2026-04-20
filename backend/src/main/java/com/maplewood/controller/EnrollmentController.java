package com.maplewood.controller;

import com.maplewood.dto.EnrollmentRequest;
import com.maplewood.dto.ScheduleDto;
import com.maplewood.service.EnrollmentService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/enrollments")
public class EnrollmentController {

    private final EnrollmentService enrollmentService;

    public EnrollmentController(EnrollmentService enrollmentService) {
        this.enrollmentService = enrollmentService;
    }

    /**
     * Enrolls a student in a course for the current semester.
     * Returns 201 Created with updated schedule on success.
     */
    @PostMapping
    public ResponseEntity<ScheduleDto> enroll(@Valid @RequestBody EnrollmentRequest req) {
        ScheduleDto schedule = enrollmentService.enroll(req.studentId(), req.courseId());
        return ResponseEntity.status(201).body(schedule);
    }

    /**
     * Drops a student from a course in the current semester.
     * Returns updated schedule without the dropped course.
     */
    @DeleteMapping
    public ScheduleDto drop(@Valid @RequestBody EnrollmentRequest req) {
        return enrollmentService.drop(req.studentId(), req.courseId());
    }
}
