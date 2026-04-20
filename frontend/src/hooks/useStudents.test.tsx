import { describe, it, expect, vi, beforeEach } from 'vitest';
import { renderHook, waitFor } from '@testing-library/react';
import { QueryClientProvider } from '@tanstack/react-query';
import { useStudents, useStudentProfile, useStudentSchedule } from './useStudents';
import { makeQueryClient } from '../test/utils';
import { makeProfile, makeSchedule, makeStudentSummary } from '../test/fixtures';

vi.mock('../api/client', () => ({
  studentsApi: {
    list: vi.fn(),
    getProfile: vi.fn(),
    getSchedule: vi.fn(),
  },
}));

import { studentsApi } from '../api/client';

function wrap() {
  const client = makeQueryClient();
  const wrapper = ({ children }: { children: React.ReactNode }) => (
    <QueryClientProvider client={client}>{children}</QueryClientProvider>
  );
  return { wrapper };
}

describe('useStudents', () => {
  beforeEach(() => vi.clearAllMocks());

  it('loads the student list', async () => {
    const students = [makeStudentSummary(), makeStudentSummary({ id: 2, firstName: 'James' })];
    (studentsApi.list as ReturnType<typeof vi.fn>).mockResolvedValue(students);

    const { wrapper } = wrap();
    const { result } = renderHook(() => useStudents(), { wrapper });

    await waitFor(() => expect(result.current.isSuccess).toBe(true));
    expect(result.current.data).toEqual(students);
  });
});

describe('useStudentProfile', () => {
  beforeEach(() => vi.clearAllMocks());

  it('does NOT fetch when studentId is null', () => {
    const { wrapper } = wrap();
    renderHook(() => useStudentProfile(null), { wrapper });
    expect(studentsApi.getProfile).not.toHaveBeenCalled();
  });

  it('fetches profile when a studentId is provided', async () => {
    const profile = makeProfile({ id: 7 });
    (studentsApi.getProfile as ReturnType<typeof vi.fn>).mockResolvedValue(profile);

    const { wrapper } = wrap();
    const { result } = renderHook(() => useStudentProfile(7), { wrapper });

    await waitFor(() => expect(result.current.isSuccess).toBe(true));
    expect(studentsApi.getProfile).toHaveBeenCalledWith(7);
    expect(result.current.data).toEqual(profile);
  });
});

describe('useStudentSchedule', () => {
  beforeEach(() => vi.clearAllMocks());

  it('does NOT fetch when studentId is null', () => {
    const { wrapper } = wrap();
    renderHook(() => useStudentSchedule(null), { wrapper });
    expect(studentsApi.getSchedule).not.toHaveBeenCalled();
  });

  it('fetches schedule when a studentId is provided', async () => {
    const schedule = makeSchedule({ courseCount: 2 });
    (studentsApi.getSchedule as ReturnType<typeof vi.fn>).mockResolvedValue(schedule);

    const { wrapper } = wrap();
    const { result } = renderHook(() => useStudentSchedule(3), { wrapper });

    await waitFor(() => expect(result.current.isSuccess).toBe(true));
    expect(studentsApi.getSchedule).toHaveBeenCalledWith(3);
    expect(result.current.data).toEqual(schedule);
  });
});
