import { describe, it, expect, vi } from 'vitest';
import { renderHook, act } from '@testing-library/react';
import { StudentProvider, useSelectedStudent } from './StudentContext';

describe('StudentContext', () => {
  it('defaults selectedStudentId to null', () => {
    const { result } = renderHook(() => useSelectedStudent(), {
      wrapper: ({ children }) => <StudentProvider>{children}</StudentProvider>,
    });
    expect(result.current.selectedStudentId).toBeNull();
  });

  it('updates selectedStudentId via setter', () => {
    const { result } = renderHook(() => useSelectedStudent(), {
      wrapper: ({ children }) => <StudentProvider>{children}</StudentProvider>,
    });

    act(() => result.current.setSelectedStudentId(42));
    expect(result.current.selectedStudentId).toBe(42);

    act(() => result.current.setSelectedStudentId(null));
    expect(result.current.selectedStudentId).toBeNull();
  });

  it('throws a clear error when used outside a StudentProvider', () => {
    // React logs caught render errors to console.error; silence it for this expected failure.
    const spy = vi.spyOn(console, 'error').mockImplementation(() => {});
    expect(() => renderHook(() => useSelectedStudent())).toThrow(
      /must be used within a StudentProvider/i
    );
    spy.mockRestore();
  });
});
