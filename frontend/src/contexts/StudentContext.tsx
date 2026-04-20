import { createContext, useContext, useState, ReactNode } from 'react';

interface StudentContextType {
  selectedStudentId: number | null;
  setSelectedStudentId: (id: number | null) => void;
}

const StudentContext = createContext<StudentContextType | undefined>(undefined);

export function StudentProvider({ children }: { children: ReactNode }) {
  const [selectedStudentId, setSelectedStudentId] = useState<number | null>(null);

  return (
    <StudentContext.Provider value={{ selectedStudentId, setSelectedStudentId }}>
      {children}
    </StudentContext.Provider>
  );
}

export function useSelectedStudent() {
  const context = useContext(StudentContext);
  if (context === undefined) {
    throw new Error('useSelectedStudent must be used within a StudentProvider');
  }
  return context;
}
