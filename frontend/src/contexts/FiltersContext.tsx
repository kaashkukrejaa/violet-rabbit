import { createContext, useContext, useState, ReactNode } from 'react';

export type CourseTypeFilter = 'all' | 'core' | 'elective';

interface FiltersContextType {
  search: string;
  setSearch: (search: string) => void;
  typeFilter: CourseTypeFilter;
  setTypeFilter: (filter: CourseTypeFilter) => void;
  onlyEligible: boolean;
  setOnlyEligible: (only: boolean) => void;
}

const FiltersContext = createContext<FiltersContextType | undefined>(undefined);

export function FiltersProvider({ children }: { children: ReactNode }) {
  const [search, setSearch] = useState('');
  const [typeFilter, setTypeFilter] = useState<CourseTypeFilter>('all');
  const [onlyEligible, setOnlyEligible] = useState(false);

  return (
    <FiltersContext.Provider
      value={{ search, setSearch, typeFilter, setTypeFilter, onlyEligible, setOnlyEligible }}
    >
      {children}
    </FiltersContext.Provider>
  );
}

export function useFilters() {
  const context = useContext(FiltersContext);
  if (context === undefined) {
    throw new Error('useFilters must be used within a FiltersProvider');
  }
  return context;
}
