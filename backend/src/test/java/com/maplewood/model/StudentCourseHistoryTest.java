package com.maplewood.model;

import com.maplewood.support.Fixtures;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Unit tests for {@link StudentCourseHistory#isPassed()}.
 */
class StudentCourseHistoryTest {

    @Test
    void isPassed_trueForExactStatus() {
        StudentCourseHistory h = Fixtures.history(1L, 1L, null, null, "passed");
        assertThat(h.isPassed()).isTrue();
    }

    @Test
    void isPassed_caseInsensitive() {
        assertThat(Fixtures.history(1L, 1L, null, null, "PASSED").isPassed()).isTrue();
        assertThat(Fixtures.history(1L, 1L, null, null, "Passed").isPassed()).isTrue();
    }

    @Test
    void isPassed_falseForOtherStatuses() {
        assertThat(Fixtures.history(1L, 1L, null, null, "failed").isPassed()).isFalse();
        assertThat(Fixtures.history(1L, 1L, null, null, "in_progress").isPassed()).isFalse();
        assertThat(Fixtures.history(1L, 1L, null, null, "dropped").isPassed()).isFalse();
    }
}
