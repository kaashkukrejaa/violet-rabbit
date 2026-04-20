import { describe, it, expect, vi } from 'vitest';
import { render, screen } from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import { QueryClient, QueryClientProvider } from '@tanstack/react-query';
import CourseCard from './CourseCard';
import { StudentProvider } from '../contexts/StudentContext';
import { ToastProvider } from '../contexts/ToastContext';
import type { CourseOffering } from '../types';

// Mock the enrollment hook so we don't make real network calls.
const mutateMock = vi.fn();
vi.mock('../hooks/useEnrollment', () => ({
  useEnrollInCourse: () => ({ mutate: mutateMock, isPending: false }),
  useDropCourse: () => ({ mutate: vi.fn(), isPending: false }),
}));

// Fix the selected student so the Enroll button can trigger the mutation.
vi.mock('../contexts/StudentContext', async (importOriginal) => {
  const actual = await importOriginal<typeof import('../contexts/StudentContext')>();
  return {
    ...actual,
    useSelectedStudent: () => ({ selectedStudentId: 42, setSelectedStudentId: vi.fn() }),
  };
});

const sampleOffering: CourseOffering = {
  course: {
    id: 7,
    code: 'MAT201',
    name: 'Algebra II',
    description: 'Advanced algebra topics.',
    credits: 3,
    hoursPerWeek: 3,
    courseType: 'core',
    gradeLevelMin: 10,
    gradeLevelMax: 12,
    prerequisite: { code: 'MAT101', name: 'Algebra I' },
  },
  section: {
    id: 700,
    daysOfWeek: ['MON', 'WED', 'FRI'],
    startTime: '09:00',
    endTime: '10:00',
    teacherName: 'Ms. Park',
    classroomName: 'Room 12',
  },
};

function renderWithProviders(ui: React.ReactNode) {
  const client = new QueryClient({ defaultOptions: { queries: { retry: false } } });
  return render(
    <QueryClientProvider client={client}>
      <StudentProvider>
        <ToastProvider>{ui}</ToastProvider>
      </StudentProvider>
    </QueryClientProvider>
  );
}

describe('CourseCard', () => {
  it('renders course details: code, name, credits, prerequisite', () => {
    renderWithProviders(
      <CourseCard offering={sampleOffering} status={{ kind: 'eligible' }} />
    );

    expect(screen.getByText('MAT201')).toBeInTheDocument();
    expect(screen.getByText('Algebra II')).toBeInTheDocument();
    expect(screen.getByText('3.0')).toBeInTheDocument();
    expect(screen.getByText('MAT101')).toBeInTheDocument();
  });

  it('shows an Enroll button when eligible and triggers the mutation on click', async () => {
    mutateMock.mockClear();
    const user = userEvent.setup();

    renderWithProviders(
      <CourseCard offering={sampleOffering} status={{ kind: 'eligible' }} />
    );

    const button = screen.getByRole('button', { name: /enroll/i });
    await user.click(button);

    expect(mutateMock).toHaveBeenCalledWith({ studentId: 42, courseId: 7 });
  });

  it('shows the ineligibility reason (no Enroll button) when not eligible', () => {
    renderWithProviders(
      <CourseCard
        offering={sampleOffering}
        status={{ kind: 'prerequisite', reason: 'Requires MAT101' }}
      />
    );

    expect(screen.getByText('Requires MAT101')).toBeInTheDocument();
    expect(screen.queryByRole('button', { name: /enroll/i })).toBeNull();
  });
});
