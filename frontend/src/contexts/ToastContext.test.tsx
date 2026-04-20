import { describe, it, expect, vi } from 'vitest';
import { renderHook, act } from '@testing-library/react';
import { ToastProvider, useToast } from './ToastContext';

const wrapper = ({ children }: { children: React.ReactNode }) => (
  <ToastProvider>{children}</ToastProvider>
);

describe('ToastContext', () => {
  it('starts with no active toast', () => {
    const { result } = renderHook(() => useToast(), { wrapper });
    expect(result.current.message).toBeNull();
    expect(result.current.type).toBeNull();
  });

  it('showToast sets message and type', () => {
    const { result } = renderHook(() => useToast(), { wrapper });

    act(() => result.current.showToast('Enrolled!', 'success'));

    expect(result.current.message).toBe('Enrolled!');
    expect(result.current.type).toBe('success');
  });

  it('hideToast clears message and type', () => {
    const { result } = renderHook(() => useToast(), { wrapper });

    act(() => result.current.showToast('Failed', 'error'));
    act(() => result.current.hideToast());

    expect(result.current.message).toBeNull();
    expect(result.current.type).toBeNull();
  });

  it('throws a clear error when used outside a ToastProvider', () => {
    const spy = vi.spyOn(console, 'error').mockImplementation(() => {});
    expect(() => renderHook(() => useToast())).toThrow(/must be used within a ToastProvider/i);
    spy.mockRestore();
  });
});
