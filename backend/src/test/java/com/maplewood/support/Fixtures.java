package com.maplewood.support;

import com.maplewood.model.Classroom;
import com.maplewood.model.Course;
import com.maplewood.model.CourseSection;
import com.maplewood.model.Enrollment;
import com.maplewood.model.Semester;
import com.maplewood.model.Student;
import com.maplewood.model.StudentCourseHistory;
import com.maplewood.model.Teacher;
import org.springframework.test.util.ReflectionTestUtils;

import java.math.BigDecimal;

/**
 * Reusable factory methods for building entity instances in tests.
 * Uses ReflectionTestUtils to set private fields since entities intentionally
 * have no public setters (they're populated by JPA in production).
 */
public final class Fixtures {

    private Fixtures() {}

    public static Student student(Long id, String first, String last, Integer gradeLevel) {
        Student s = new Student();
        ReflectionTestUtils.setField(s, "id", id);
        ReflectionTestUtils.setField(s, "firstName", first);
        ReflectionTestUtils.setField(s, "lastName", last);
        ReflectionTestUtils.setField(s, "gradeLevel", gradeLevel);
        return s;
    }

    public static Semester semester(Long id, String name, Integer year, Integer order, boolean active) {
        Semester sem = new Semester();
        ReflectionTestUtils.setField(sem, "id", id);
        ReflectionTestUtils.setField(sem, "name", name);
        ReflectionTestUtils.setField(sem, "year", year);
        ReflectionTestUtils.setField(sem, "orderInYear", order);
        ReflectionTestUtils.setField(sem, "active", active);
        return sem;
    }

    public static Course course(Long id, String code, String name, Course prerequisite,
                                Integer gradeMin, Integer gradeMax, Integer semesterOrder,
                                BigDecimal credits, String courseType) {
        Course c = new Course();
        ReflectionTestUtils.setField(c, "id", id);
        ReflectionTestUtils.setField(c, "code", code);
        ReflectionTestUtils.setField(c, "name", name);
        ReflectionTestUtils.setField(c, "description", "Description for " + code);
        ReflectionTestUtils.setField(c, "credits", credits);
        ReflectionTestUtils.setField(c, "hoursPerWeek", 3);
        ReflectionTestUtils.setField(c, "prerequisite", prerequisite);
        ReflectionTestUtils.setField(c, "courseType", courseType);
        ReflectionTestUtils.setField(c, "gradeLevelMin", gradeMin);
        ReflectionTestUtils.setField(c, "gradeLevelMax", gradeMax);
        ReflectionTestUtils.setField(c, "semesterOrder", semesterOrder);
        return c;
    }

    public static Course simpleCourse(Long id, String code) {
        return course(id, code, code + " Name", null, 9, 12, 1, new BigDecimal("3.0"), "core");
    }

    public static Teacher teacher(Long id, String firstName, String lastName) {
        Teacher t = new Teacher();
        ReflectionTestUtils.setField(t, "id", id);
        ReflectionTestUtils.setField(t, "firstName", firstName);
        ReflectionTestUtils.setField(t, "lastName", lastName);
        return t;
    }

    public static Classroom classroom(Long id, String name) {
        Classroom r = new Classroom();
        ReflectionTestUtils.setField(r, "id", id);
        ReflectionTestUtils.setField(r, "name", name);
        ReflectionTestUtils.setField(r, "floor", 1);
        return r;
    }

    public static CourseSection section(Long id, Course course, Semester semester,
                                        String days, String start, String end) {
        CourseSection sec = new CourseSection();
        ReflectionTestUtils.setField(sec, "id", id);
        ReflectionTestUtils.setField(sec, "course", course);
        ReflectionTestUtils.setField(sec, "semester", semester);
        ReflectionTestUtils.setField(sec, "daysOfWeek", days);
        ReflectionTestUtils.setField(sec, "startTime", start);
        ReflectionTestUtils.setField(sec, "endTime", end);
        ReflectionTestUtils.setField(sec, "capacity", 30);
        return sec;
    }

    public static CourseSection sectionWithStaff(Long id, Course course, Semester semester,
                                                 Teacher teacher, Classroom classroom,
                                                 String days, String start, String end) {
        CourseSection sec = section(id, course, semester, days, start, end);
        ReflectionTestUtils.setField(sec, "teacher", teacher);
        ReflectionTestUtils.setField(sec, "classroom", classroom);
        return sec;
    }

    public static StudentCourseHistory history(Long id, Long studentId, Course course,
                                               Semester semester, String status) {
        StudentCourseHistory h = new StudentCourseHistory();
        ReflectionTestUtils.setField(h, "id", id);
        ReflectionTestUtils.setField(h, "studentId", studentId);
        ReflectionTestUtils.setField(h, "course", course);
        ReflectionTestUtils.setField(h, "semester", semester);
        ReflectionTestUtils.setField(h, "status", status);
        return h;
    }

    public static Enrollment enrollment(Long studentId, CourseSection section) {
        return new Enrollment(studentId, section);
    }
}
