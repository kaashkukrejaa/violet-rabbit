import { describe, it, expect, vi, beforeEach } from 'vitest';
import { screen, waitFor } from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import Header from './Header';
import { renderWithProviders } from '../test/utils';
import { makeStudentSummary } from '../test/fixtures';

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

import { studentsApi } from '../api/client';

describe('Header', () => {
  beforeEach(() => vi.clearAllMocks());

  it('shows app branding and a student picker dropdown', async () => {
    (studentsApi.list as ReturnType<typeof vi.fn>).mockResolvedValue([
      makeStudentSummary({ id: 1, firstName: 'Emma', lastName: 'Wilson' }),
      makeStudentSummary({ id: 2, firstName: 'James', lastName: 'Lee' }),
    ]);

    renderWithProviders(<Header />);

    expect(screen.getByText('Maplewood High')).toBeInTheDocument();
    expect(screen.getByText('Course Planning Portal')).toBeInTheDocument();

    await waitFor(() => expect(screen.getByRole('combobox')).not.toBeDisabled());
    expect(screen.getByText(/Wilson, Emma/)).toBeInTheDocument();
    expect(screen.getByText(/Lee, James/)).toBeInTheDocument();
  });

  it('auto-selects the first student once the list loads', async () => {
    (studentsApi.list as ReturnType<typeof vi.fn>).mockResolvedValue([
      makeStudentSummary({ id: 10, firstName: 'Alice' }),
      makeStudentSummary({ id: 20, firstName: 'Bob' }),
    ]);

    renderWithProviders(<Header />);

    await waitFor(() => {
      const select = screen.getByRole('combobox') as HTMLSelectElement;
      expect(select.value).toBe('10');
    });
  });

  it('allows selecting a different student', async () => {
    (studentsApi.list as ReturnType<typeof vi.fn>).mockResolvedValue([
      makeStudentSummary({ id: 10, firstName: 'Alice' }),
      makeStudentSummary({ id: 20, firstName: 'Bob', lastName: 'Brown' }),
    ]);

    renderWithProviders(<Header />);
    const user = userEvent.setup();

    const select = await waitFor(() => {
      const el = screen.getByRole('combobox') as HTMLSelectElement;
      if (el.value !== '10') throw new Error('not ready');
      return el;
    });

    await user.selectOptions(select, '20');
    expect(select.value).toBe('20');
  });

  it('disables the picker while the student list is loading', () => {
    (studentsApi.list as ReturnType<typeof vi.fn>).mockReturnValue(new Promise(() => {}));
    renderWithProviders(<Header />);
    expect(screen.getByRole('combobox')).toBeDisabled();
  });
});
