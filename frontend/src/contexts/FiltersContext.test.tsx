import { describe, it, expect, vi } from 'vitest';
import { renderHook, act } from '@testing-library/react';
import { FiltersProvider, useFilters } from './FiltersContext';

const wrapper = ({ children }: { children: React.ReactNode }) => (
  <FiltersProvider>{children}</FiltersProvider>
);

describe('FiltersContext', () => {
  it('provides sensible defaults (empty search, "all" type, onlyEligible off)', () => {
    const { result } = renderHook(() => useFilters(), { wrapper });
    expect(result.current.search).toBe('');
    expect(result.current.typeFilter).toBe('all');
    expect(result.current.onlyEligible).toBe(false);
  });

  it('updates each filter independently', () => {
    const { result } = renderHook(() => useFilters(), { wrapper });

    act(() => result.current.setSearch('ENG'));
    act(() => result.current.setTypeFilter('elective'));
    act(() => result.current.setOnlyEligible(true));

    expect(result.current.search).toBe('ENG');
    expect(result.current.typeFilter).toBe('elective');
    expect(result.current.onlyEligible).toBe(true);
  });

  it('throws a clear error when used outside a FiltersProvider', () => {
    const spy = vi.spyOn(console, 'error').mockImplementation(() => {});
    expect(() => renderHook(() => useFilters())).toThrow(/must be used within a FiltersProvider/i);
    spy.mockRestore();
  });
});
