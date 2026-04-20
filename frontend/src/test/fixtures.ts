import type { CourseOffering, Schedule, StudentProfile, StudentSummary } from '../types';

/** Reusable factory helpers for realistic test data. */

export function makeStudentSummary(overrides: Partial<StudentSummary> = {}): StudentSummary {
  return {
    id: 1,
    firstName: 'Emma',
    lastName: 'Wilson',
    gradeLevel: 10,
    ...overrides,
  };
}

export function makeProfile(overrides: Partial<StudentProfile> = {}): StudentProfile {
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

export function makeOffering(
  courseOverrides: Partial<CourseOffering['course']> = {},
  sectionOverrides: Partial<CourseOffering['section']> = {}
): CourseOffering {
  return {
    course: {
      id: 1,
      code: 'ENG101',
      name: 'English 101',
      description: 'Intro to literature.',
      credits: 3,
      hoursPerWeek: 3,
      courseType: 'core',
      gradeLevelMin: 9,
      gradeLevelMax: 12,
      prerequisite: null,
      ...courseOverrides,
    },
    section: {
      id: 100,
      daysOfWeek: ['MON', 'WED', 'FRI'],
      startTime: '09:00',
      endTime: '10:00',
      teacherName: 'Ms. Park',
      classroomName: 'Room 12',
      ...sectionOverrides,
    },
  };
}

export function makeSchedule(overrides: Partial<Schedule> = {}): Schedule {
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
