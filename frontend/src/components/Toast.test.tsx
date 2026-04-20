import { describe, it, expect, vi, afterEach, beforeEach } from 'vitest';
import { render, screen, act } from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import Toast from './Toast';
import { ToastProvider, useToast } from '../contexts/ToastContext';

/**
 * Small controller component so tests can drive toast state without
 * relying on the external showToast caller (mutation hooks, etc).
 */
function Controller() {
  const { showToast } = useToast();
  return (
    <>
      <button onClick={() => showToast('It worked', 'success')}>show-success</button>
      <button onClick={() => showToast('It broke', 'error')}>show-error</button>
      <Toast />
    </>
  );
}

describe('Toast component', () => {
  beforeEach(() => vi.useFakeTimers());
  afterEach(() => {
    vi.clearAllTimers();
    vi.useRealTimers();
  });

  it('renders nothing by default (no active message)', () => {
    render(<ToastProvider><Toast /></ToastProvider>);
    expect(screen.queryByRole('status')).toBeNull();
  });

  it('renders the message and type-specific class when shown', () => {
    const user = userEvent.setup({ advanceTimers: vi.advanceTimersByTime });

    render(<ToastProvider><Controller /></ToastProvider>);

    return user.click(screen.getByText('show-success')).then(() => {
      const toast = screen.getByRole('status');
      expect(toast).toHaveTextContent('It worked');
      expect(toast.className).toContain('toast-success');
    });
  });

  it('auto-dismisses after 4.5 seconds', async () => {
    const user = userEvent.setup({ advanceTimers: vi.advanceTimersByTime });

    render(<ToastProvider><Controller /></ToastProvider>);
    await user.click(screen.getByText('show-error'));
    expect(screen.getByRole('status')).toBeInTheDocument();

    act(() => {
      vi.advanceTimersByTime(4500);
    });

    expect(screen.queryByRole('status')).toBeNull();
  });

  it('closes immediately when the dismiss button is clicked', async () => {
    const user = userEvent.setup({ advanceTimers: vi.advanceTimersByTime });

    render(<ToastProvider><Controller /></ToastProvider>);
    await user.click(screen.getByText('show-success'));
    expect(screen.getByRole('status')).toBeInTheDocument();

    await user.click(screen.getByLabelText('Dismiss'));
    expect(screen.queryByRole('status')).toBeNull();
  });
});
