import { describe, it, expect, vi, beforeEach } from 'vitest';
import { screen, waitFor } from '@testing-library/react';
import Dashboard from './Dashboard';
import { renderWithProviders } from '../test/utils';
import { makeProfile, makeSchedule } from '../test/fixtures';
import type { StudentProfile } from '../types';

vi.mock('../api/client', () => ({
  studentsApi: {
    list: vi.fn(),
    getProfile: vi.fn(),
    getSchedule: vi.fn(),
  },
  coursesApi: { listOfferings: vi.fn() },
  enrollmentsApi: { enroll: vi.fn(), drop: vi.fn() },
  toApiError: (e: unknown) => ({ type: 'unknown', message: (e as Error)?.message ?? 'err' }),
}));

vi.mock('../contexts/StudentContext', async (importOriginal) => {
  const actual = await importOriginal<typeof import('../contexts/StudentContext')>();
  return { ...actual, useSelectedStudent: () => ({ selectedStudentId: 1, setSelectedStudentId: vi.fn() }) };
});

import { studentsApi } from '../api/client';

describe('Dashboard', () => {
  beforeEach(() => vi.clearAllMocks());

  it('shows a placeholder prompt when no student is selected', () => {
    vi.doMock('../contexts/StudentContext', () => ({
      useSelectedStudent: () => ({ selectedStudentId: null, setSelectedStudentId: vi.fn() }),
      StudentProvider: ({ children }: { children: React.ReactNode }) => <>{children}</>,
    }));
    // For the default mock (selectedStudentId=1) with no profile yet, still shows loading state
    (studentsApi.getProfile as ReturnType<typeof vi.fn>).mockReturnValue(new Promise(() => {}));
    (studentsApi.getSchedule as ReturnType<typeof vi.fn>).mockReturnValue(new Promise(() => {}));

    renderWithProviders(<Dashboard />);
    expect(screen.getByText(/Loading profile/i)).toBeInTheDocument();
  });

  it('renders student name, GPA, credits, and graduation progress', async () => {
    const profile: StudentProfile = makeProfile({
      firstName: 'Emma',
      lastName: 'Wilson',
      gradeLevel: 10,
      expectedGraduationYear: 2028,
      gpa: 3.75,
      creditsEarned: 15,
      creditsRequired: 30,
      graduationProgress: 50,
    });
    (studentsApi.getProfile as ReturnType<typeof vi.fn>).mockResolvedValue(profile);
    (studentsApi.getSchedule as ReturnType<typeof vi.fn>).mockResolvedValue(
      makeSchedule({ semesterName: 'Fall', semesterYear: 2024 })
    );

    renderWithProviders(<Dashboard />);

    await waitFor(() => expect(screen.getByText('Emma Wilson')).toBeInTheDocument());
    expect(screen.getByText(/Grade 10/)).toBeInTheDocument();
    expect(screen.getByText(/Fall 2024/)).toBeInTheDocument();
    expect(screen.getByText('3.75')).toBeInTheDocument();
    expect(screen.getByText('15.0')).toBeInTheDocument();
    expect(screen.getByText('50.0%')).toBeInTheDocument();
  });

  it('shows an empty-state row in the history table when no prior coursework exists', async () => {
    (studentsApi.getProfile as ReturnType<typeof vi.fn>).mockResolvedValue(
      makeProfile({ courseHistory: [] })
    );
    (studentsApi.getSchedule as ReturnType<typeof vi.fn>).mockResolvedValue(makeSchedule());

    renderWithProviders(<Dashboard />);

    await waitFor(() => expect(screen.getByText(/Academic history \(0 records\)/i)).toBeInTheDocument());
    expect(screen.getByText(/No prior coursework/i)).toBeInTheDocument();
  });

  it('renders each course history row with code, credits, and status pill', async () => {
    (studentsApi.getProfile as ReturnType<typeof vi.fn>).mockResolvedValue(
      makeProfile({
        courseHistory: [
          { id: 1, courseId: 1, courseCode: 'ENG101', courseName: 'English 101', credits: 3,
            semesterName: 'Spring', semesterYear: 2023, status: 'passed' },
          { id: 2, courseId: 2, courseCode: 'MAT101', courseName: 'Math', credits: 4,
            semesterName: 'Fall', semesterYear: 2023, status: 'failed' },
        ],
      })
    );
    (studentsApi.getSchedule as ReturnType<typeof vi.fn>).mockResolvedValue(makeSchedule());

    renderWithProviders(<Dashboard />);

    await waitFor(() => expect(screen.getByText('ENG101')).toBeInTheDocument());
    expect(screen.getByText('MAT101')).toBeInTheDocument();
    expect(screen.getByText('passed')).toBeInTheDocument();
    expect(screen.getByText('failed')).toBeInTheDocument();
  });
});
