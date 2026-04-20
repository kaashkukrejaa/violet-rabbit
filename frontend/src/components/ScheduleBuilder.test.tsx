import { describe, it, expect, vi, beforeEach } from 'vitest';
import { screen, waitFor } from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import ScheduleBuilder from './ScheduleBuilder';
import { renderWithProviders } from '../test/utils';
import { makeOffering, makeSchedule } from '../test/fixtures';

const dropMock = vi.fn();

vi.mock('../hooks/useEnrollment', () => ({
  useEnrollInCourse: () => ({ mutate: vi.fn(), isPending: false }),
  useDropCourse: () => ({ mutate: dropMock, isPending: false }),
}));

vi.mock('../contexts/StudentContext', async (importOriginal) => {
  const actual = await importOriginal<typeof import('../contexts/StudentContext')>();
  return { ...actual, useSelectedStudent: () => ({ selectedStudentId: 42, setSelectedStudentId: vi.fn() }) };
});

vi.mock('../api/client', () => ({
  studentsApi: {
    list: vi.fn(),
    getProfile: vi.fn(),
    getSchedule: vi.fn(),
  },
  coursesApi: { listOfferings: vi.fn() },
  enrollmentsApi: { enroll: vi.fn(), drop: vi.fn() },
  toApiError: () => ({ type: 'unknown', message: 'err' }),
}));

import { studentsApi } from '../api/client';

describe('ScheduleBuilder', () => {
  beforeEach(() => {
    dropMock.mockClear();
    vi.clearAllMocks();
  });

  it('shows a loading placeholder while the schedule is fetching', () => {
    (studentsApi.getSchedule as ReturnType<typeof vi.fn>).mockReturnValue(new Promise(() => {}));
    renderWithProviders(<ScheduleBuilder />);
    expect(screen.getByText(/Loading schedule/i)).toBeInTheDocument();
  });

  it('shows empty state when enrolled in zero courses', async () => {
    (studentsApi.getSchedule as ReturnType<typeof vi.fn>).mockResolvedValue(
      makeSchedule({ courseCount: 0, items: [] })
    );
    renderWithProviders(<ScheduleBuilder />);
    await waitFor(() => expect(screen.getByText(/No courses enrolled/i)).toBeInTheDocument());
    expect(screen.getByText(/5 slots left/i)).toBeInTheDocument();
  });

  it('shows a "Full" badge when at max capacity', async () => {
    (studentsApi.getSchedule as ReturnType<typeof vi.fn>).mockResolvedValue(
      makeSchedule({
        courseCount: 5,
        maxCourses: 5,
        items: [
          makeOffering({ id: 1, code: 'A' }),
          makeOffering({ id: 2, code: 'B' }, { id: 2 }),
          makeOffering({ id: 3, code: 'C' }, { id: 3 }),
          makeOffering({ id: 4, code: 'D' }, { id: 4 }),
          makeOffering({ id: 5, code: 'E' }, { id: 5 }),
        ],
      })
    );
    renderWithProviders(<ScheduleBuilder />);
    await waitFor(() => expect(screen.getByText('Full')).toBeInTheDocument());
  });

  it('lists each enrolled course with a Drop button', async () => {
    (studentsApi.getSchedule as ReturnType<typeof vi.fn>).mockResolvedValue(
      makeSchedule({
        courseCount: 2,
        totalCredits: 6,
        items: [
          makeOffering({ id: 10, code: 'ENG101', name: 'English' }, { id: 101 }),
          makeOffering({ id: 20, code: 'MAT101', name: 'Math' }, { id: 202, startTime: '11:00', endTime: '12:00' }),
        ],
      })
    );
    renderWithProviders(<ScheduleBuilder />);

    await waitFor(() => expect(screen.getByText('ENG101')).toBeInTheDocument());
    expect(screen.getByText('MAT101')).toBeInTheDocument();
    expect(screen.getAllByRole('button', { name: /drop/i })).toHaveLength(2);
    expect(screen.getByText(/2\/5 courses/)).toBeInTheDocument();
    expect(screen.getByText(/6\.0 credits/)).toBeInTheDocument();
  });

  it('calls the drop mutation with the correct args when Drop is clicked', async () => {
    (studentsApi.getSchedule as ReturnType<typeof vi.fn>).mockResolvedValue(
      makeSchedule({
        courseCount: 1,
        items: [makeOffering({ id: 10, code: 'ENG101' })],
      })
    );
    renderWithProviders(<ScheduleBuilder />);
    const user = userEvent.setup();

    const button = await waitFor(() => screen.getByRole('button', { name: /drop/i }));
    await user.click(button);

    expect(dropMock).toHaveBeenCalledWith({ studentId: 42, courseId: 10 });
  });
});
