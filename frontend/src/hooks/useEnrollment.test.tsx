import { describe, it, expect, vi, beforeEach } from 'vitest';
import { renderHook, waitFor, act } from '@testing-library/react';
import { QueryClientProvider } from '@tanstack/react-query';
import { AxiosError, AxiosHeaders } from 'axios';
import { useEnrollInCourse, useDropCourse } from './useEnrollment';
import { ToastProvider, useToast } from '../contexts/ToastContext';
import { makeQueryClient } from '../test/utils';
import { makeOffering, makeSchedule } from '../test/fixtures';

vi.mock('../api/client', async () => {
  // Keep the real toApiError; only stub enrollmentsApi network calls.
  const actual = await vi.importActual<typeof import('../api/client')>('../api/client');
  return {
    ...actual,
    enrollmentsApi: {
      enroll: vi.fn(),
      drop: vi.fn(),
    },
  };
});

import { enrollmentsApi } from '../api/client';

/**
 * Renders the mutation hook alongside useToast so tests can assert toast state
 * without any rendered UI.
 */
function renderWithToast<T>(cb: () => T) {
  const client = makeQueryClient();
  const wrapper = ({ children }: { children: React.ReactNode }) => (
    <QueryClientProvider client={client}>
      <ToastProvider>{children}</ToastProvider>
    </QueryClientProvider>
  );
  return renderHook(() => ({ mutation: cb(), toast: useToast() }), { wrapper });
}

describe('useEnrollInCourse', () => {
  beforeEach(() => vi.clearAllMocks());

  it('shows a success toast with the enrolled course code', async () => {
    const offering = makeOffering({ id: 10, code: 'MAT101' });
    const newSchedule = makeSchedule({ courseCount: 1, items: [offering] });
    (enrollmentsApi.enroll as ReturnType<typeof vi.fn>).mockResolvedValue(newSchedule);

    const { result } = renderWithToast(() => useEnrollInCourse());

    act(() => result.current.mutation.mutate({ studentId: 1, courseId: 10 }));

    await waitFor(() => expect(result.current.toast.message).toBe('Enrolled in MAT101'));
    expect(result.current.toast.type).toBe('success');
  });

  it('shows an error toast with the backend-provided message on failure', async () => {
    const headers = new AxiosHeaders();
    const err = new AxiosError('422', 'ERR', { headers } as never, null, {
      data: { type: 'prerequisite', message: 'Must pass ENG101' },
      status: 422,
      statusText: 'Unprocessable',
      headers,
      config: { headers } as never,
    });
    (enrollmentsApi.enroll as ReturnType<typeof vi.fn>).mockRejectedValue(err);

    const { result } = renderWithToast(() => useEnrollInCourse());
    act(() => result.current.mutation.mutate({ studentId: 1, courseId: 10 }));

    await waitFor(() => expect(result.current.toast.message).toBe('Must pass ENG101'));
    expect(result.current.toast.type).toBe('error');
  });
});

describe('useDropCourse', () => {
  beforeEach(() => vi.clearAllMocks());

  it('shows a success toast after dropping', async () => {
    (enrollmentsApi.drop as ReturnType<typeof vi.fn>).mockResolvedValue(makeSchedule());

    const { result } = renderWithToast(() => useDropCourse());
    act(() => result.current.mutation.mutate({ studentId: 1, courseId: 10 }));

    await waitFor(() => expect(result.current.toast.message).toBe('Course dropped successfully'));
    expect(result.current.toast.type).toBe('success');
  });

  it('shows an error toast when drop fails', async () => {
    (enrollmentsApi.drop as ReturnType<typeof vi.fn>).mockRejectedValue(new Error('server down'));

    const { result } = renderWithToast(() => useDropCourse());
    act(() => result.current.mutation.mutate({ studentId: 1, courseId: 10 }));

    await waitFor(() => expect(result.current.toast.message).toBe('server down'));
    expect(result.current.toast.type).toBe('error');
  });
});
