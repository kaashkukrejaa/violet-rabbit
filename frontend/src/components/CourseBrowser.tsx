import { useMemo } from 'react';
import { useCourseOfferings } from '../hooks/useCourses';
import { useStudentProfile, useStudentSchedule } from '../hooks/useStudents';
import { useSelectedStudent } from '../contexts/StudentContext';
import { useFilters, type CourseTypeFilter } from '../contexts/FiltersContext';
import CourseCard from './CourseCard';
import { computeEnrollmentStatus } from '../utils/eligibility';
import type { EnrollmentStatus } from '../utils/eligibility';

/**
 * Course catalog browser with search, filtering, and real-time eligibility status.
 * 
 * Features:
 * - Search by course code or name
 * - Filter by type (core/elective)
 * - Toggle eligible-only view
 * - Shows ineligible courses with reasons (prerequisite, grade level, conflicts, etc.)
 * - Sorts eligible courses first, then alphabetically
 */
export default function CourseBrowser() {
  const { selectedStudentId } = useSelectedStudent();
  const { data: offerings = [], isLoading, error } = useCourseOfferings();
  const { data: profile } = useStudentProfile(selectedStudentId);
  const { data: schedule } = useStudentSchedule(selectedStudentId);
  const { search, setSearch, typeFilter, setTypeFilter, onlyEligible, setOnlyEligible } = useFilters();

  const annotated = useMemo(() => {
    return offerings
      .map((o) => ({
        offering: o,
        status: computeEnrollmentStatus(o, profile ?? null, schedule ?? null),
      }))
      .filter(({ offering, status }) => applyFilters(offering.course, status, search, typeFilter, onlyEligible))
      .sort((a, b) => {
        const aOk = a.status.kind === 'eligible' ? 0 : 1;
        const bOk = b.status.kind === 'eligible' ? 0 : 1;
        if (aOk !== bOk) return aOk - bOk;
        return a.offering.course.code.localeCompare(b.offering.course.code);
      });
  }, [offerings, profile, schedule, search, typeFilter, onlyEligible]);

  return (
    <section className="card">
      <header className="card-header">
        <div>
          <h2>Course Catalog</h2>
          <p className="muted">
            {isLoading
              ? 'Loading…'
              : `${annotated.length} of ${offerings.length} courses shown`}
          </p>
        </div>
      </header>

      <div className="filters">
        <input
          type="search"
          placeholder="Search by code or name…"
          value={search}
          onChange={(e) => setSearch(e.target.value)}
        />
        <select
          value={typeFilter}
          onChange={(e) => setTypeFilter(e.target.value as CourseTypeFilter)}
        >
          <option value="all">All types</option>
          <option value="core">Core</option>
          <option value="elective">Elective</option>
        </select>
        <label className="checkbox">
          <input
            type="checkbox"
            checked={onlyEligible}
            onChange={(e) => setOnlyEligible(e.target.checked)}
          />
          Eligible only
        </label>
      </div>

      {error && <div className="alert alert-error">{error.message}</div>}

      <div className="course-grid">
        {annotated.map(({ offering, status }) => (
          <CourseCard
            key={offering.section.id}
            offering={offering}
            status={status}
          />
        ))}
        {!isLoading && annotated.length === 0 && (
          <p className="muted">No courses match your filters.</p>
        )}
      </div>
    </section>
  );
}

function applyFilters(
  course: { code: string; name: string; courseType: string },
  status: EnrollmentStatus,
  search: string,
  typeFilter: CourseTypeFilter,
  onlyEligible: boolean
): boolean {
  if (typeFilter !== 'all' && course.courseType !== typeFilter) return false;
  if (onlyEligible && status.kind !== 'eligible' && status.kind !== 'enrolled') return false;
  if (search.trim()) {
    const q = search.trim().toLowerCase();
    if (!course.code.toLowerCase().includes(q) && !course.name.toLowerCase().includes(q)) {
      return false;
    }
  }
  return true;
}
