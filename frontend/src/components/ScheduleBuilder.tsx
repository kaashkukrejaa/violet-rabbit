import { useStudentSchedule } from '../hooks/useStudents';
import { useDropCourse } from '../hooks/useEnrollment';
import { useSelectedStudent } from '../contexts/StudentContext';
import { formatDays, formatTimeRange } from '../utils/format';

/**
 * Schedule builder showing student's current semester enrollments.
 * Displays course count, credit total, and allows dropping courses.
 * Courses are shown sorted by start time (chronological order).
 */
export default function ScheduleBuilder() {
  const { selectedStudentId } = useSelectedStudent();
  const { data: schedule, isLoading } = useStudentSchedule(selectedStudentId);
  const dropMutation = useDropCourse();

  if (!schedule) {
    return (
      <section className="card">
        <h2>Current Schedule</h2>
        <p className="muted">{isLoading ? 'Loading schedule…' : 'No schedule available.'}</p>
      </section>
    );
  }

  const remainingSlots = schedule.maxCourses - schedule.courseCount;

  const handleDrop = (courseId: number) => {
    if (selectedStudentId !== null) {
      dropMutation.mutate({ studentId: selectedStudentId, courseId });
    }
  };

  return (
    <section className="card">
      <header className="card-header">
        <div>
          <h2>Current Schedule</h2>
          <p className="muted">
            {schedule.semesterName} {schedule.semesterYear} ·{' '}
            {schedule.courseCount}/{schedule.maxCourses} courses ·{' '}
            {schedule.totalCredits.toFixed(1)} credits
          </p>
        </div>
        <span className={`badge ${remainingSlots === 0 ? 'badge-warning' : 'badge-ok'}`}>
          {remainingSlots === 0 ? 'Full' : `${remainingSlots} slot${remainingSlots === 1 ? '' : 's'} left`}
        </span>
      </header>

      {schedule.items.length === 0 ? (
        <p className="muted">No courses enrolled. Pick some from the catalog.</p>
      ) : (
        <ul className="schedule-list">
          {schedule.items.map(({ course, section }) => (
            <li key={section.id} className="schedule-row">
              <div>
                <div className="schedule-title">
                  <code>{course.code}</code>
                  <span>{course.name}</span>
                </div>
                <div className="muted small">
                  {formatDays(section.daysOfWeek)} · {formatTimeRange(section.startTime, section.endTime)}
                  {section.teacherName && <> · {section.teacherName}</>}
                  {section.classroomName && <> · {section.classroomName}</>}
                </div>
              </div>
              <button
                type="button"
                className="btn btn-secondary"
                disabled={dropMutation.isPending || selectedStudentId === null}
                onClick={() => handleDrop(course.id)}
              >
                Drop
              </button>
            </li>
          ))}
        </ul>
      )}
    </section>
  );
}
