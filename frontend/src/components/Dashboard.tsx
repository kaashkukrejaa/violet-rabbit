import { useStudentProfile, useStudentSchedule } from '../hooks/useStudents';
import { useSelectedStudent } from '../contexts/StudentContext';

/**
 * Student dashboard showing academic profile, GPA, credits, graduation progress,
 * and complete course history sorted chronologically.
 */
export default function Dashboard() {
  const { selectedStudentId } = useSelectedStudent();
  const { data: profile, isLoading: loadingProfile } = useStudentProfile(selectedStudentId);
  const { data: schedule } = useStudentSchedule(selectedStudentId);

  if (loadingProfile && !profile) {
    return <section className="card"><p className="muted">Loading profile…</p></section>;
  }
  if (!profile) {
    return <section className="card"><p className="muted">Select a student to begin.</p></section>;
  }

  const progress = Math.min(profile.graduationProgress, 100);
  const semesterLabel = schedule
    ? `${schedule.semesterName} ${schedule.semesterYear}`
    : '';

  return (
    <section className="card dashboard">
      <header className="card-header">
        <div>
          <h2>{profile.firstName} {profile.lastName}</h2>
          <p className="muted">
            Grade {profile.gradeLevel} · Expected graduation {profile.expectedGraduationYear ?? '—'}
            {semesterLabel ? ` · ${semesterLabel}` : ''}
          </p>
        </div>
      </header>

      <div className="metric-grid">
        <Metric label="GPA" value={profile.gpa.toFixed(2)} suffix="/ 4.00" />
        <Metric
          label="Credits Earned"
          value={profile.creditsEarned.toFixed(1)}
          suffix={`/ ${profile.creditsRequired.toFixed(0)}`}
        />
        <Metric
          label="Graduation Progress"
          value={`${progress.toFixed(1)}%`}
        />
      </div>

      <div className="progress-bar">
        <div className="progress-bar__fill" style={{ width: `${progress}%` }} />
      </div>

      <details className="history">
        <summary>Academic history ({profile.courseHistory.length} records)</summary>
        <table className="history-table">
          <thead>
            <tr>
              <th>Code</th>
              <th>Course</th>
              <th>Credits</th>
              <th>Semester</th>
              <th>Status</th>
            </tr>
          </thead>
          <tbody>
            {profile.courseHistory.map((h) => (
              <tr key={h.id}>
                <td><code>{h.courseCode}</code></td>
                <td>{h.courseName}</td>
                <td>{h.credits.toFixed(1)}</td>
                <td>{h.semesterName} {h.semesterYear}</td>
                <td>
                  <span className={`status-pill status-${h.status}`}>{h.status}</span>
                </td>
              </tr>
            ))}
            {profile.courseHistory.length === 0 && (
              <tr><td colSpan={5} className="muted">No prior coursework.</td></tr>
            )}
          </tbody>
        </table>
      </details>
    </section>
  );
}

function Metric({ label, value, suffix }: { label: string; value: string; suffix?: string }) {
  return (
    <div className="metric">
      <div className="metric-label">{label}</div>
      <div className="metric-value">
        {value}
        {suffix && <span className="metric-suffix"> {suffix}</span>}
      </div>
    </div>
  );
}
