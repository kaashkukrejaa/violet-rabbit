-- New tables to support the current-semester course planning feature.
-- Uses IF NOT EXISTS so the pre-populated database stays intact.

CREATE TABLE IF NOT EXISTS course_sections (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    course_id INTEGER NOT NULL,
    semester_id INTEGER NOT NULL,
    teacher_id INTEGER,
    classroom_id INTEGER,
    days_of_week VARCHAR(32) NOT NULL,
    start_time VARCHAR(5) NOT NULL,
    end_time VARCHAR(5) NOT NULL,
    capacity INTEGER NOT NULL DEFAULT 30,
    FOREIGN KEY (course_id) REFERENCES courses(id),
    FOREIGN KEY (semester_id) REFERENCES semesters(id),
    FOREIGN KEY (teacher_id) REFERENCES teachers(id),
    FOREIGN KEY (classroom_id) REFERENCES classrooms(id),
    UNIQUE(course_id, semester_id)
);

CREATE INDEX IF NOT EXISTS idx_sections_semester ON course_sections(semester_id);
CREATE INDEX IF NOT EXISTS idx_sections_course ON course_sections(course_id);

CREATE TABLE IF NOT EXISTS enrollments (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    student_id INTEGER NOT NULL,
    section_id INTEGER NOT NULL,
    status VARCHAR(20) NOT NULL DEFAULT 'enrolled',
    enrolled_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (student_id) REFERENCES students(id),
    FOREIGN KEY (section_id) REFERENCES course_sections(id),
    UNIQUE(student_id, section_id)
);

CREATE INDEX IF NOT EXISTS idx_enrollments_student ON enrollments(student_id);
CREATE INDEX IF NOT EXISTS idx_enrollments_section ON enrollments(section_id);
