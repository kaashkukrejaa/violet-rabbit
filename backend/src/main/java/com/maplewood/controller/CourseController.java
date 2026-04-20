package com.maplewood.controller;

import com.maplewood.dto.CourseDto;
import com.maplewood.dto.CourseOfferingDto;
import com.maplewood.service.CourseService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/courses")
public class CourseController {

    private final CourseService courseService;

    public CourseController(CourseService courseService) {
        this.courseService = courseService;
    }

    /**
     * Lists courses with optional filtering for browsing the course catalog.
     * Query params: ?grade=10&semesterOrder=1
     */
    @GetMapping
    public List<CourseDto> list(
            @RequestParam(required = false) Integer grade,
            @RequestParam(required = false) Integer semesterOrder) {
        return courseService.listCourses(grade, semesterOrder);
    }

    /**
     * Returns all course offerings (course + section + time) for the active semester.
     * This is what students can actually enroll in right now.
     */
    @GetMapping("/offerings")
    public List<CourseOfferingDto> offerings() {
        return courseService.listActiveOfferings();
    }
}
