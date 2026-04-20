import { describe, it, expect } from 'vitest';
import { computeEnrollmentStatus } from './eligibility';
import type { CourseOffering, Schedule, StudentProfile } from '../types';

// Small factory helpers keep individual tests short and focused.
function makeOffering(overrides: Partial<CourseOffering['course']> = {}, sectionOverrides: Partial<CourseOffering['section']> = {}): CourseOffering {
  return {
    course: {
      id: 1,
      code: 'ENG101',
      name: 'English 101',
      description: null,
      credits: 3,
      hoursPerWeek: 3,
      courseType: 'core',
      gradeLevelMin: 9,
      gradeLevelMax: 12,
      prerequisite: null,
      ...overrides,
    },
    section: {
      id: 100,
      daysOfWeek: ['MON', 'WED', 'FRI'],
      startTime: '09:00',
      endTime: '10:00',
      teacherName: null,
      classroomName: null,
      ...sectionOverrides,
    },
  };
}

function makeProfile(overrides: Partial<StudentProfile> = {}): StudentProfile {
  return {
    id: 1,
    firstName: 'Emma',
    lastName: 'Wilson',
    gradeLevel: 10,
    expectedGraduationYear: 2028,
    gpa: 3.5,
    creditsEarned: 12,
    creditsRequired: 30,
    graduationProgress: 40,
    courseHistory: [],
    ...overrides,
  };
}

function makeSchedule(overrides: Partial<Schedule> = {}): Schedule {
  return {
    semesterName: 'Fall',
    semesterYear: 2024,
    courseCount: 0,
    maxCourses: 5,
    totalCredits: 0,
    items: [],
    ...overrides,
  };
}

describe('computeEnrollmentStatus', () => {
  it('returns eligible when profile is null (no student selected)', () => {
    const status = computeEnrollmentStatus(makeOffering(), null, null);
    expect(status.kind).toBe('eligible');
  });

  it('returns eligible for a basic offering with no blockers', () => {
    const status = computeEnrollmentStatus(makeOffering(), makeProfile(), makeSchedule());
    expect(status.kind).toBe('eligible');
  });

  it('flags "enrolled" when the course is already in the schedule', () => {
    const offering = makeOffering();
    const schedule = makeSchedule({ items: [offering], courseCount: 1 });
    const status = computeEnrollmentStatus(offering, makeProfile(), schedule);
    expect(status.kind).toBe('enrolled');
  });

  it('flags "already_passed" when student has passed the course before', () => {
    const offering = makeOffering({ id: 7, code: 'MAT101' });
    const profile = makeProfile({
      courseHistory: [
        { id: 1, courseId: 7, courseCode: 'MAT101', courseName: 'Math', credits: 3,
          semesterName: 'Spring', semesterYear: 2023, status: 'passed' },
      ],
    });
    const status = computeEnrollmentStatus(offering, profile, makeSchedule());
    expect(status.kind).toBe('already_passed');
  });

  it('flags "grade_level" when student is below the minimum grade', () => {
    const offering = makeOffering({ gradeLevelMin: 11, gradeLevelMax: 12 });
    const profile = makeProfile({ gradeLevel: 10 });
    const status = computeEnrollmentStatus(offering, profile, makeSchedule());
    expect(status.kind).toBe('grade_level');
    if (status.kind === 'grade_level') {
      expect(status.reason).toContain('11');
    }
  });

  it('flags "grade_level" when student is above the maximum grade', () => {
    const offering = makeOffering({ gradeLevelMin: 9, gradeLevelMax: 10 });
    const profile = makeProfile({ gradeLevel: 12 });
    const status = computeEnrollmentStatus(offering, profile, makeSchedule());
    expect(status.kind).toBe('grade_level');
  });

  it('flags "prerequisite" when required course has not been passed', () => {
    const offering = makeOffering({
      code: 'ENG102',
      prerequisite: { code: 'ENG101', name: 'English 101' },
    });
    const profile = makeProfile(); // no history
    const status = computeEnrollmentStatus(offering, profile, makeSchedule());
    expect(status.kind).toBe('prerequisite');
    if (status.kind === 'prerequisite') {
      expect(status.reason).toContain('ENG101');
    }
  });

  it('allows enrollment when prerequisite has been passed', () => {
    const offering = makeOffering({
      code: 'ENG102',
      prerequisite: { code: 'ENG101', name: 'English 101' },
    });
    const profile = makeProfile({
      courseHistory: [
        { id: 1, courseId: 99, courseCode: 'ENG101', courseName: 'English 101', credits: 3,
          semesterName: 'Fall', semesterYear: 2023, status: 'passed' },
      ],
    });
    const status = computeEnrollmentStatus(offering, profile, makeSchedule());
    expect(status.kind).toBe('eligible');
  });

  it('does NOT treat a failed prerequisite attempt as passed', () => {
    const offering = makeOffering({
      code: 'ENG102',
      prerequisite: { code: 'ENG101', name: 'English 101' },
    });
    const profile = makeProfile({
      courseHistory: [
        { id: 1, courseId: 99, courseCode: 'ENG101', courseName: 'English 101', credits: 3,
          semesterName: 'Fall', semesterYear: 2023, status: 'failed' },
      ],
    });
    const status = computeEnrollmentStatus(offering, profile, makeSchedule());
    expect(status.kind).toBe('prerequisite');
  });

  it('flags "max_courses" when schedule is full', () => {
    const schedule = makeSchedule({ courseCount: 5, maxCourses: 5 });
    const status = computeEnrollmentStatus(makeOffering(), makeProfile(), schedule);
    expect(status.kind).toBe('max_courses');
  });

  it('flags "conflict" when times overlap with an existing enrollment on shared days', () => {
    const existing = makeOffering(
      { id: 50, code: 'HIS101' },
      { id: 500, daysOfWeek: ['MON', 'WED', 'FRI'], startTime: '09:00', endTime: '10:00' }
    );
    const newOffering = makeOffering(
      { id: 51, code: 'BIO101' },
      { id: 501, daysOfWeek: ['MON', 'WED'], startTime: '09:30', endTime: '10:30' }
    );
    const schedule = makeSchedule({ items: [existing], courseCount: 1 });
    const status = computeEnrollmentStatus(newOffering, makeProfile(), schedule);
    expect(status.kind).toBe('conflict');
    if (status.kind === 'conflict') {
      expect(status.reason).toContain('HIS101');
    }
  });

  it('does NOT flag conflict when times overlap on completely different days', () => {
    const existing = makeOffering(
      { id: 50 },
      { daysOfWeek: ['MON', 'WED', 'FRI'], startTime: '09:00', endTime: '10:00' }
    );
    const newOffering = makeOffering(
      { id: 51 },
      { daysOfWeek: ['TUE', 'THU'], startTime: '09:00', endTime: '10:00' }
    );
    const schedule = makeSchedule({ items: [existing], courseCount: 1 });
    const status = computeEnrollmentStatus(newOffering, makeProfile(), schedule);
    expect(status.kind).toBe('eligible');
  });

  it('does NOT flag conflict when times are adjacent but not overlapping', () => {
    const existing = makeOffering(
      { id: 50 },
      { daysOfWeek: ['MON'], startTime: '09:00', endTime: '10:00' }
    );
    const newOffering = makeOffering(
      { id: 51 },
      { daysOfWeek: ['MON'], startTime: '10:00', endTime: '11:00' }
    );
    const schedule = makeSchedule({ items: [existing], courseCount: 1 });
    const status = computeEnrollmentStatus(newOffering, makeProfile(), schedule);
    expect(status.kind).toBe('eligible');
  });
});
