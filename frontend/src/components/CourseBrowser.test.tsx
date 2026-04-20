import { describe, it, expect, vi, beforeEach } from 'vitest';
import { screen, waitFor } from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import CourseBrowser from './CourseBrowser';
import { renderWithProviders } from '../test/utils';
import { makeOffering, makeProfile, makeSchedule } from '../test/fixtures';

vi.mock('../hooks/useEnrollment', () => ({
  useEnrollInCourse: () => ({ mutate: vi.fn(), isPending: false }),
  useDropCourse: () => ({ mutate: vi.fn(), isPending: false }),
}));

vi.mock('../contexts/StudentContext', async (importOriginal) => {
  const actual = await importOriginal<typeof import('../contexts/StudentContext')>();
  return { ...actual, useSelectedStudent: () => ({ selectedStudentId: 1, setSelectedStudentId: vi.fn() }) };
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

import { coursesApi, studentsApi } from '../api/client';

function setupApi(offerings = [
  makeOffering({ id: 1, code: 'ENG101', name: 'English 101', courseType: 'core' }),
  makeOffering({ id: 2, code: 'ART301', name: 'Painting', courseType: 'elective' }, { id: 200 }),
  makeOffering({ id: 3, code: 'MAT201', name: 'Algebra II', courseType: 'core',
    prerequisite: { code: 'MAT101', name: 'Algebra I' } }, { id: 300 }),
]) {
  (coursesApi.listOfferings as ReturnType<typeof vi.fn>).mockResolvedValue(offerings);
  (studentsApi.getProfile as ReturnType<typeof vi.fn>).mockResolvedValue(makeProfile());
  (studentsApi.getSchedule as ReturnType<typeof vi.fn>).mockResolvedValue(makeSchedule());
}

describe('CourseBrowser', () => {
  beforeEach(() => vi.clearAllMocks());

  it('renders all offerings by default with a visible count', async () => {
    setupApi();
    renderWithProviders(<CourseBrowser />);

    await waitFor(() => expect(screen.getByText('ENG101')).toBeInTheDocument());
    expect(screen.getByText('ART301')).toBeInTheDocument();
    expect(screen.getByText('MAT201')).toBeInTheDocument();
    expect(screen.getByText(/3 of 3 courses shown/)).toBeInTheDocument();
  });

  it('filters by search query (matches course code or name, case-insensitive)', async () => {
    setupApi();
    renderWithProviders(<CourseBrowser />);
    const user = userEvent.setup();

    await waitFor(() => screen.getByText('ENG101'));

    const searchBox = screen.getByPlaceholderText(/Search by code or name/i);
    await user.type(searchBox, 'algebra');

    expect(screen.getByText('MAT201')).toBeInTheDocument();
    expect(screen.queryByText('ENG101')).toBeNull();
    expect(screen.queryByText('ART301')).toBeNull();
  });

  it('filters by course type via the dropdown', async () => {
    setupApi();
    renderWithProviders(<CourseBrowser />);
    const user = userEvent.setup();

    await waitFor(() => screen.getByText('ENG101'));

    await user.selectOptions(screen.getByRole('combobox'), 'elective');

    expect(screen.getByText('ART301')).toBeInTheDocument();
    expect(screen.queryByText('ENG101')).toBeNull();
    expect(screen.queryByText('MAT201')).toBeNull();
  });

  it('hides ineligible courses when "Eligible only" is toggled on', async () => {
    // MAT201 has a prerequisite (MAT101) that the student has not passed → ineligible
    setupApi();
    renderWithProviders(<CourseBrowser />);
    const user = userEvent.setup();

    await waitFor(() => screen.getByText('MAT201'));

    await user.click(screen.getByLabelText(/Eligible only/i));

    expect(screen.queryByText('MAT201')).toBeNull();
    expect(screen.getByText('ENG101')).toBeInTheDocument();
    expect(screen.getByText('ART301')).toBeInTheDocument();
  });

  it('shows an empty-state message when no course matches the current filters', async () => {
    setupApi();
    renderWithProviders(<CourseBrowser />);
    const user = userEvent.setup();

    await waitFor(() => screen.getByText('ENG101'));
    await user.type(screen.getByPlaceholderText(/Search by code/i), 'does-not-exist');

    expect(screen.getByText(/No courses match your filters/i)).toBeInTheDocument();
  });
});
