export interface CoursePrerequisite {
  code: string;
  name: string;
}

export interface Course {
  id: number;
  code: string;
  name: string;
  description: string | null;
  credits: number;
  hoursPerWeek: number;
  courseType: 'core' | 'elective';
  gradeLevelMin: number | null;
  gradeLevelMax: number | null;
  prerequisite: CoursePrerequisite | null;
}

export interface Section {
  id: number;
  daysOfWeek: string[];
  startTime: string;
  endTime: string;
  teacherName: string | null;
  classroomName: string | null;
}

export interface CourseOffering {
  course: Course;
  section: Section;
}

export interface StudentSummary {
  id: number;
  firstName: string;
  lastName: string;
  gradeLevel: number;
}

export interface CourseHistoryEntry {
  id: number;
  courseId: number;
  courseCode: string;
  courseName: string;
  credits: number;
  semesterName: string;
  semesterYear: number;
  status: 'passed' | 'failed';
}

export interface StudentProfile extends StudentSummary {
  expectedGraduationYear: number | null;
  gpa: number;
  creditsEarned: number;
  creditsRequired: number;
  graduationProgress: number;
  courseHistory: CourseHistoryEntry[];
}

export interface Schedule {
  semesterName: string;
  semesterYear: number;
  courseCount: number;
  maxCourses: number;
  totalCredits: number;
  items: CourseOffering[];
}

export type ApiErrorType =
  | 'prerequisite'
  | 'conflict'
  | 'max_courses'
  | 'grade_level'
  | 'already_enrolled'
  | 'already_passed'
  | 'not_found'
  | 'no_offering'
  | 'unknown';

export interface ApiError {
  type: ApiErrorType;
  message: string;
}
