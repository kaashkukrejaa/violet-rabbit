import { useEffect } from 'react';
import { useToast } from '../contexts/ToastContext';

/**
 * Toast notification for enrollment success/error feedback.
 * Auto-dismisses after 4.5 seconds or can be manually closed.
 */
export default function Toast() {
  const { message, type, hideToast } = useToast();

  useEffect(() => {
    if (!message) return;
    const t = window.setTimeout(() => hideToast(), 4500);
    return () => window.clearTimeout(t);
  }, [message, hideToast]);

  if (!message || !type) return null;

  return (
    <div className={`toast toast-${type}`} role="status">
      {message}
      <button
        type="button"
        className="toast-close"
        aria-label="Dismiss"
        onClick={hideToast}
      >
        ×
      </button>
    </div>
  );
}
