import React from 'react';
import ReactDOM from 'react-dom/client';
import { QueryClient, QueryClientProvider } from '@tanstack/react-query';
import App from './App';
import { StudentProvider } from './contexts/StudentContext';
import { ToastProvider } from './contexts/ToastContext';
import { FiltersProvider } from './contexts/FiltersContext';
import './styles.css';

const queryClient = new QueryClient({
  defaultOptions: {
    queries: {
      retry: 1,
      refetchOnWindowFocus: false,
    },
  },
});

ReactDOM.createRoot(document.getElementById('root')!).render(
  <React.StrictMode>
    <QueryClientProvider client={queryClient}>
      <StudentProvider>
        <ToastProvider>
          <FiltersProvider>
            <App />
          </FiltersProvider>
        </ToastProvider>
      </StudentProvider>
    </QueryClientProvider>
  </React.StrictMode>
);
