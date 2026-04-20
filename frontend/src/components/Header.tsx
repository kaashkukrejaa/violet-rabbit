import { useEffect } from 'react';
import { useStudents } from '../hooks/useStudents';
import { useSelectedStudent } from '../contexts/StudentContext';

/**
 * Application header with student picker dropdown.
 * Selecting a student triggers loading of their profile and schedule.
 */
export default function Header() {
  const { data: students, isLoading } = useStudents();
  const { selectedStudentId, setSelectedStudentId } = useSelectedStudent();

  // Auto-select first student when list loads
  useEffect(() => {
    if (students && students.length > 0 && selectedStudentId === null) {
      setSelectedStudentId(students[0].id);
    }
  }, [students, selectedStudentId, setSelectedStudentId]);

  return (
    <header className="app-header">
      <div className="brand">
        <span className="brand-mark">M</span>
        <div>
          <h1>Maplewood High</h1>
          <p>Course Planning Portal</p>
        </div>
      </div>

      <label className="student-picker">
        <span>Viewing as</span>
        <select
          value={selectedStudentId ?? ''}
          onChange={(e) => setSelectedStudentId(Number(e.target.value))}
          disabled={isLoading || !students || students.length === 0}
        >
          {students?.map((s) => (
            <option key={s.id} value={s.id}>
              {s.lastName}, {s.firstName} — Grade {s.gradeLevel}
            </option>
          ))}
        </select>
      </label>
    </header>
  );
}
