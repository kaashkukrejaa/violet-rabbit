import { useEnrollInCourse } from '../hooks/useEnrollment';
import { useSelectedStudent } from '../contexts/StudentContext';
import type { CourseOffering } from '../types';
import { formatDays, formatTimeRange } from '../utils/format';
import type { EnrollmentStatus } from '../utils/eligibility';

interface Props {
  offering: CourseOffering;
  status: EnrollmentStatus;
}

/**
 * Individual course card showing course details and enrollment status.
 * 
 * Visual states:
 * - eligible: Green border, enroll button enabled
 * - enrolled: Blue border, shows "Enrolled" badge
 * - ineligible: Gray border with reason (prerequisite, conflict, etc.)
 * 
 * Displays: credits, hours/week, grade levels, prerequisites, schedule, teacher, classroom
 */
export default function CourseCard({ offering, status }: Props) {
  const { course, section } = offering;
  const { selectedStudentId } = useSelectedStudent();
  const enrollMutation = useEnrollInCourse();

  const onEnroll = () => {
    if (selectedStudentId === null) return;
    enrollMutation.mutate({
      studentId: selectedStudentId,
      courseId: course.id,
    });
  };

  const disabled =
    enrollMutation.isPending || selectedStudentId === null || (status.kind !== 'eligible');

  return (
    <article className={`course-card course-card--${status.kind}`}>
      <header>
        <div>
          <code className="course-code">{course.code}</code>
          <h3>{course.name}</h3>
        </div>
        <span className={`badge badge-${course.courseType}`}>{course.courseType}</span>
      </header>

      {course.description && <p className="course-desc">{course.description}</p>}

      <dl className="course-meta">
        <div>
          <dt>Credits</dt>
          <dd>{course.credits.toFixed(1)}</dd>
        </div>
        <div>
          <dt>Hours/week</dt>
          <dd>{course.hoursPerWeek}</dd>
        </div>
        <div>
          <dt>Grade</dt>
          <dd>
            {course.gradeLevelMin === course.gradeLevelMax
              ? course.gradeLevelMin
              : `${course.gradeLevelMin}–${course.gradeLevelMax}`}
          </dd>
        </div>
        <div>
          <dt>Schedule</dt>
          <dd>
            {formatDays(section.daysOfWeek)} · {formatTimeRange(section.startTime, section.endTime)}
          </dd>
        </div>
        {course.prerequisite && (
          <div className="col-span-2">
            <dt>Prerequisite</dt>
            <dd><code>{course.prerequisite.code}</code> {course.prerequisite.name}</dd>
          </div>
        )}
      </dl>

      <footer>
        {status.kind === 'eligible' && (
          <button className="btn btn-primary" onClick={onEnroll} disabled={disabled}>
            Enroll
          </button>
        )}
        {status.kind !== 'eligible' && (
          <span className={`status-note status-${status.kind}`}>{status.reason}</span>
        )}
      </footer>
    </article>
  );
}
