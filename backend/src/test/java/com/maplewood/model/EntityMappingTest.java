package com.maplewood.model;

import com.maplewood.support.Fixtures;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Smoke tests for the pure-shell entity classes (Classroom, Teacher, Specialization,
 * Course, Student). These entities hold only JPA-mapped state with Lombok getters;
 * there is no behaviour to unit-test, so we instead verify:
 *   1. the classes can be instantiated by the no-arg JPA constructor,
 *   2. their declared fields round-trip via the generated getters,
 *   3. they carry {@link Entity} + {@link Table} metadata (the contract that lets
 *      JPA find and manage them at runtime).
 */
class EntityMappingTest {

    @Test
    void classroom_roundTripsBasicFields_andIsJpaEntity() {
        Classroom c = Fixtures.classroom(1L, "Room 12");

        assertThat(c.getId()).isEqualTo(1L);
        assertThat(c.getName()).isEqualTo("Room 12");
        assertThat(c.getFloor()).isEqualTo(1);
        assertIsJpaEntity(Classroom.class, "classrooms");
    }

    @Test
    void teacher_roundTripsBasicFields_andIsJpaEntity() {
        Teacher t = Fixtures.teacher(1L, "Grace", "Park");

        assertThat(t.getId()).isEqualTo(1L);
        assertThat(t.getFirstName()).isEqualTo("Grace");
        assertThat(t.getLastName()).isEqualTo("Park");
        assertIsJpaEntity(Teacher.class, "teachers");
    }

    @Test
    void specialization_isJpaEntity() {
        assertIsJpaEntity(Specialization.class, "specializations");
    }

    @Test
    void course_roundTripsBasicFields_andIsJpaEntity() {
        Course prereq = Fixtures.simpleCourse(9L, "PRE101");
        Course c = Fixtures.course(1L, "ENG101", "English 101", prereq,
                9, 12, 1, new BigDecimal("3.0"), "core");

        assertThat(c.getId()).isEqualTo(1L);
        assertThat(c.getCode()).isEqualTo("ENG101");
        assertThat(c.getName()).isEqualTo("English 101");
        assertThat(c.getCredits()).isEqualByComparingTo("3.0");
        assertThat(c.getHoursPerWeek()).isEqualTo(3);
        assertThat(c.getPrerequisite()).isSameAs(prereq);
        assertThat(c.getCourseType()).isEqualTo("core");
        assertThat(c.getGradeLevelMin()).isEqualTo(9);
        assertThat(c.getGradeLevelMax()).isEqualTo(12);
        assertThat(c.getSemesterOrder()).isEqualTo(1);
        assertIsJpaEntity(Course.class, "courses");
    }

    @Test
    void student_roundTripsBasicFields_andIsJpaEntity() {
        com.maplewood.model.Student s = Fixtures.student(1L, "Emma", "Wilson", 10);

        assertThat(s.getId()).isEqualTo(1L);
        assertThat(s.getFirstName()).isEqualTo("Emma");
        assertThat(s.getLastName()).isEqualTo("Wilson");
        assertThat(s.getGradeLevel()).isEqualTo(10);
        assertIsJpaEntity(com.maplewood.model.Student.class, "students");
    }

    // -- helpers -----------------------------------------------------------

    private static void assertIsJpaEntity(Class<?> type, String expectedTable) {
        assertThat(type.getAnnotation(Entity.class))
                .as("%s should be annotated with @Entity", type.getSimpleName())
                .isNotNull();
        Table table = type.getAnnotation(Table.class);
        assertThat(table)
                .as("%s should be annotated with @Table", type.getSimpleName())
                .isNotNull();
        assertThat(table.name()).isEqualTo(expectedTable);
    }
}
