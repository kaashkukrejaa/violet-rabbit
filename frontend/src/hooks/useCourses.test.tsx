import { describe, it, expect, vi, beforeEach } from 'vitest';
import { renderHook, waitFor } from '@testing-library/react';
import { QueryClientProvider } from '@tanstack/react-query';
import { useCourseOfferings } from './useCourses';
import { makeQueryClient } from '../test/utils';
import { makeOffering } from '../test/fixtures';

vi.mock('../api/client', () => ({
  coursesApi: {
    listOfferings: vi.fn(),
  },
}));

import { coursesApi } from '../api/client';

describe('useCourseOfferings', () => {
  beforeEach(() => {
    vi.clearAllMocks();
  });

  it('fetches course offerings via the API client', async () => {
    const offerings = [makeOffering({ code: 'MAT101' })];
    (coursesApi.listOfferings as ReturnType<typeof vi.fn>).mockResolvedValue(offerings);

    const client = makeQueryClient();
    const { result } = renderHook(() => useCourseOfferings(), {
      wrapper: ({ children }) => (
        <QueryClientProvider client={client}>{children}</QueryClientProvider>
      ),
    });

    await waitFor(() => expect(result.current.isSuccess).toBe(true));
    expect(result.current.data).toEqual(offerings);
    expect(coursesApi.listOfferings).toHaveBeenCalledOnce();
  });

  it('exposes loading state before data arrives', () => {
    (coursesApi.listOfferings as ReturnType<typeof vi.fn>).mockReturnValue(new Promise(() => {}));

    const client = makeQueryClient();
    const { result } = renderHook(() => useCourseOfferings(), {
      wrapper: ({ children }) => (
        <QueryClientProvider client={client}>{children}</QueryClientProvider>
      ),
    });

    expect(result.current.isLoading).toBe(true);
    expect(result.current.data).toBeUndefined();
  });
});
