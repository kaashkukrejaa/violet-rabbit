import { describe, it, expect, vi, beforeEach } from 'vitest';
import { screen, waitFor } from '@testing-library/react';
import App from './App';
import { renderWithProviders } from './test/utils';
import { makeOffering, makeProfile, makeSchedule, makeStudentSummary } from './test/fixtures';

vi.mock('./api/client', () => ({
  studentsApi: {
    list: vi.fn(),
    getProfile: vi.fn(),
    getSchedule: vi.fn(),
  },
  coursesApi: {
    listOfferings: vi.fn(),
  },
  enrollmentsApi: { enroll: vi.fn(), drop: vi.fn() },
  toApiError: () => ({ type: 'unknown', message: 'err' }),
}));

import { coursesApi, studentsApi } from './api/client';

describe('App (smoke)', () => {
  beforeEach(() => vi.clearAllMocks());

  it('renders the shell (header, dashboard, schedule, course browser)', async () => {
    (studentsApi.list as ReturnType<typeof vi.fn>).mockResolvedValue([
      makeStudentSummary({ id: 1, firstName: 'Emma', lastName: 'Wilson' }),
    ]);
    (studentsApi.getProfile as ReturnType<typeof vi.fn>).mockResolvedValue(makeProfile());
    (studentsApi.getSchedule as ReturnType<typeof vi.fn>).mockResolvedValue(makeSchedule());
    (coursesApi.listOfferings as ReturnType<typeof vi.fn>).mockResolvedValue([
      makeOffering({ id: 1, code: 'ENG101', name: 'English 101' }),
    ]);

    renderWithProviders(<App />);

    expect(screen.getByText('Maplewood High')).toBeInTheDocument();
    expect(screen.getByText('Course Catalog')).toBeInTheDocument();
    expect(screen.getByText('Current Schedule')).toBeInTheDocument();

    await waitFor(() => expect(screen.getByText('Emma Wilson')).toBeInTheDocument());
    await waitFor(() => expect(screen.getByText('ENG101')).toBeInTheDocument());
  });
});
