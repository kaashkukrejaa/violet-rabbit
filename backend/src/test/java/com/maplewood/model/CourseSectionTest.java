package com.maplewood.model;

import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Unit tests for the pure domain logic in {@link CourseSection}.
 * No Spring context, no database — tests hit {@code overlapsWith} and
 * {@code getDaysList} in isolation.
 */
class CourseSectionTest {

    private static CourseSection section(String daysOfWeek, String startTime, String endTime) {
        CourseSection s = new CourseSection();
        ReflectionTestUtils.setField(s, "daysOfWeek", daysOfWeek);
        ReflectionTestUtils.setField(s, "startTime", startTime);
        ReflectionTestUtils.setField(s, "endTime", endTime);
        return s;
    }

    // ----- getDaysList -----

    @Test
    void getDaysList_returnsEmptyForNull() {
        CourseSection s = new CourseSection();
        assertThat(s.getDaysList()).isEmpty();
    }

    @Test
    void getDaysList_returnsEmptyForBlank() {
        CourseSection s = section("   ", "09:00", "10:00");
        assertThat(s.getDaysList()).isEmpty();
    }

    @Test
    void getDaysList_splitsAndTrims() {
        CourseSection s = section("MON, WED , FRI", "09:00", "10:00");
        assertThat(s.getDaysList()).containsExactly("MON", "WED", "FRI");
    }

    @Test
    void getDaysList_filtersOutEmptyFragments() {
        CourseSection s = section("MON,,WED", "09:00", "10:00");
        assertThat(s.getDaysList()).containsExactly("MON", "WED");
    }

    // ----- overlapsWith: days -----

    @Test
    void overlapsWith_falseWhenNoSharedDays() {
        CourseSection a = section("MON,WED,FRI", "09:00", "10:00");
        CourseSection b = section("TUE,THU", "09:00", "10:00");
        assertThat(a.overlapsWith(b)).isFalse();
    }

    @Test
    void overlapsWith_trueWhenExactlyOneSharedDay() {
        CourseSection a = section("MON,WED,FRI", "09:00", "10:00");
        CourseSection b = section("THU,FRI", "09:00", "10:00");
        assertThat(a.overlapsWith(b)).isTrue();
    }

    // ----- overlapsWith: times on shared day -----

    @Test
    void overlapsWith_trueWhenTimeRangesFullyOverlap() {
        CourseSection a = section("MON", "09:00", "11:00");
        CourseSection b = section("MON", "09:30", "10:30");
        assertThat(a.overlapsWith(b)).isTrue();
    }

    @Test
    void overlapsWith_trueWhenTimeRangesPartiallyOverlap() {
        CourseSection a = section("MON", "09:00", "10:00");
        CourseSection b = section("MON", "09:30", "10:30");
        assertThat(a.overlapsWith(b)).isTrue();
    }

    @Test
    void overlapsWith_falseWhenRangesAreAdjacentNotOverlapping() {
        // a ends exactly when b starts — no overlap per [start, end) convention
        CourseSection a = section("MON", "09:00", "10:00");
        CourseSection b = section("MON", "10:00", "11:00");
        assertThat(a.overlapsWith(b)).isFalse();
    }

    @Test
    void overlapsWith_falseWhenRangesAreDisjoint() {
        CourseSection a = section("MON", "09:00", "10:00");
        CourseSection b = section("MON", "11:00", "12:00");
        assertThat(a.overlapsWith(b)).isFalse();
    }

    @Test
    void overlapsWith_isSymmetric() {
        CourseSection a = section("MON,WED", "09:00", "10:30");
        CourseSection b = section("WED,FRI", "10:00", "11:00");
        assertThat(a.overlapsWith(b)).isEqualTo(b.overlapsWith(a));
    }
}
