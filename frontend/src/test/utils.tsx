import type { ReactNode } from 'react';
import { render, type RenderOptions } from '@testing-library/react';
import { QueryClient, QueryClientProvider } from '@tanstack/react-query';
import { StudentProvider } from '../contexts/StudentContext';
import { ToastProvider } from '../contexts/ToastContext';
import { FiltersProvider } from '../contexts/FiltersContext';

/**
 * Creates a fresh QueryClient with retries disabled — retries slow tests
 * and mask errors. Each test should call this to avoid shared cache state.
 */
export function makeQueryClient() {
  return new QueryClient({
    defaultOptions: {
      queries: { retry: false, gcTime: 0 },
      mutations: { retry: false },
    },
  });
}

interface AllProvidersProps {
  children: ReactNode;
  queryClient?: QueryClient;
}

/**
 * Wraps children with every provider the app relies on.
 * Useful for component tests that sit deep in the tree.
 */
export function AllProviders({ children, queryClient }: AllProvidersProps) {
  const client = queryClient ?? makeQueryClient();
  return (
    <QueryClientProvider client={client}>
      <StudentProvider>
        <FiltersProvider>
          <ToastProvider>{children}</ToastProvider>
        </FiltersProvider>
      </StudentProvider>
    </QueryClientProvider>
  );
}

/**
 * Custom render that wraps with AllProviders by default.
 * Pass { wrapper } to override for narrower tests.
 */
export function renderWithProviders(
  ui: ReactNode,
  options?: RenderOptions & { queryClient?: QueryClient }
) {
  const { queryClient, ...rest } = options ?? {};
  return render(<AllProviders queryClient={queryClient}>{ui}</AllProviders>, rest);
}
