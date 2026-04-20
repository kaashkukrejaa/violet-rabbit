package com.maplewood.model;

import com.maplewood.support.Fixtures;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Unit tests for {@link Enrollment}.
 * Validates the custom constructor wires fields correctly and applies the default status.
 */
class EnrollmentTest {

    @Test
    void constructor_setsDefaultStatusToEnrolled() {
        CourseSection section = Fixtures.section(10L,
                Fixtures.simpleCourse(1L, "ENG101"),
                Fixtures.semester(1L, "Fall", 2024, 1, true),
                "MON", "09:00", "10:00");

        Enrollment enrollment = new Enrollment(42L, section);

        assertThat(enrollment.getStudentId()).isEqualTo(42L);
        assertThat(enrollment.getSection()).isSameAs(section);
        assertThat(enrollment.getStatus()).isEqualTo("enrolled");
    }

    @Test
    void noArgsConstructor_availableForJpa() {
        // Verifies Lombok's @NoArgsConstructor is generated (JPA requires it).
        Enrollment e = new Enrollment();
        assertThat(e.getStatus()).isEqualTo("enrolled");
    }
}
