package com.maplewood.dto;

public record ApiError(String type, String message) {

    public static final String PREREQUISITE = "prerequisite";
    public static final String CONFLICT = "conflict";
    public static final String MAX_COURSES = "max_courses";
    public static final String GRADE_LEVEL = "grade_level";
    public static final String ALREADY_ENROLLED = "already_enrolled";
    public static final String ALREADY_PASSED = "already_passed";
    public static final String NOT_FOUND = "not_found";
    public static final String NO_OFFERING = "no_offering";
    public static final String BAD_REQUEST = "bad_request";
}
