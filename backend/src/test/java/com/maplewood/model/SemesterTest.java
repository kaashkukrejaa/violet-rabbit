package com.maplewood.model;

import com.maplewood.support.Fixtures;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Unit tests for {@link Semester#getDisplayName()}.
 */
class SemesterTest {

    @Test
    void getDisplayName_concatenatesNameAndYear() {
        Semester fall = Fixtures.semester(1L, "Fall", 2024, 1, true);
        assertThat(fall.getDisplayName()).isEqualTo("Fall 2024");
    }

    @Test
    void getDisplayName_worksForSpringSemester() {
        Semester spring = Fixtures.semester(2L, "Spring", 2025, 2, false);
        assertThat(spring.getDisplayName()).isEqualTo("Spring 2025");
    }

    @Test
    void basicAccessors() {
        Semester s = Fixtures.semester(3L, "Summer", 2024, 3, false);
        assertThat(s.getId()).isEqualTo(3L);
        assertThat(s.getName()).isEqualTo("Summer");
        assertThat(s.getYear()).isEqualTo(2024);
        assertThat(s.getOrderInYear()).isEqualTo(3);
        assertThat(s.getActive()).isFalse();
    }
}
