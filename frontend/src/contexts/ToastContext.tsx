import { createContext, useContext, useState, ReactNode, useCallback } from 'react';

interface ToastContextType {
  message: string | null;
  type: 'success' | 'error' | null;
  showToast: (message: string, type: 'success' | 'error') => void;
  hideToast: () => void;
}

const ToastContext = createContext<ToastContextType | undefined>(undefined);

export function ToastProvider({ children }: { children: ReactNode }) {
  const [message, setMessage] = useState<string | null>(null);
  const [type, setType] = useState<'success' | 'error' | null>(null);

  const showToast = useCallback((msg: string, toastType: 'success' | 'error') => {
    setMessage(msg);
    setType(toastType);
  }, []);

  const hideToast = useCallback(() => {
    setMessage(null);
    setType(null);
  }, []);

  return (
    <ToastContext.Provider value={{ message, type, showToast, hideToast }}>
      {children}
    </ToastContext.Provider>
  );
}

export function useToast() {
  const context = useContext(ToastContext);
  if (context === undefined) {
    throw new Error('useToast must be used within a ToastProvider');
  }
  return context;
}
