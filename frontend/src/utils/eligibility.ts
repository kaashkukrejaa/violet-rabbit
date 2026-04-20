import type {
  CourseOffering,
  Schedule,
  Section,
  StudentProfile,
} from '../types';

/**
 * Client-side eligibility evaluation for real-time UI feedback.
 * 
 * Mirrors backend validation rules to provide instant feedback without server round-trips.
 * The backend remains the authoritative source of truth for actual enrollment.
 * 
 * Checks performed:
 * - Already enrolled in the course
 * - Already passed the course
 * - Grade level restrictions
 * - Missing prerequisites
 * - Maximum course limit (5 per semester)
 * - Time conflicts with existing schedule
 */
export type EnrollmentStatus =
  | { kind: 'eligible' }
  | { kind: 'enrolled'; reason: string }
  | { kind: 'already_passed'; reason: string }
  | { kind: 'grade_level'; reason: string }
  | { kind: 'prerequisite'; reason: string }
  | { kind: 'conflict'; reason: string }
  | { kind: 'max_courses'; reason: string };

export function computeEnrollmentStatus(
  offering: CourseOffering,
  profile: StudentProfile | null,
  schedule: Schedule | null
): EnrollmentStatus {
  if (!profile) return { kind: 'eligible' };
  const { course, section } = offering;

  if (schedule?.items.some((i) => i.course.id === course.id)) {
    return { kind: 'enrolled', reason: 'Already in schedule' };
  }

  const passed = profile.courseHistory.filter((h) => h.status === 'passed');
  const passedIds = new Set(passed.map((h) => h.courseId));
  const passedCodes = new Set(passed.map((h) => h.courseCode));

  if (passedIds.has(course.id)) {
    return { kind: 'already_passed', reason: 'Already passed' };
  }

  if (
    course.gradeLevelMin !== null &&
    course.gradeLevelMax !== null &&
    (profile.gradeLevel < course.gradeLevelMin ||
      profile.gradeLevel > course.gradeLevelMax)
  ) {
    const range =
      course.gradeLevelMin === course.gradeLevelMax
        ? `grade ${course.gradeLevelMin}`
        : `grades ${course.gradeLevelMin}–${course.gradeLevelMax}`;
    return {
      kind: 'grade_level',
      reason: `Restricted to ${range}`,
    };
  }

  if (course.prerequisite && !passedCodes.has(course.prerequisite.code)) {
    return {
      kind: 'prerequisite',
      reason: `Requires ${course.prerequisite.code}`,
    };
  }

  if (schedule && schedule.courseCount >= schedule.maxCourses) {
    return {
      kind: 'max_courses',
      reason: `Max ${schedule.maxCourses} courses reached`,
    };
  }

  if (schedule) {
    const conflict = schedule.items.find((i) =>
      sectionsOverlap(i.section, section)
    );
    if (conflict) {
      return {
        kind: 'conflict',
        reason: `Conflicts with ${conflict.course.code}`,
      };
    }
  }

  return { kind: 'eligible' };
}

/**
 * Checks if two course sections have overlapping schedules.
 * Sections overlap if they share at least one day AND their time ranges intersect.
 * 
 * Time overlap: [A_start, A_end) overlaps [B_start, B_end) if A_start < B_end AND B_start < A_end
 */
function sectionsOverlap(a: Section, b: Section): boolean {
  const shareDay = a.daysOfWeek.some((d) => b.daysOfWeek.includes(d));
  if (!shareDay) return false;
  return a.startTime < b.endTime && b.startTime < a.endTime;
}
