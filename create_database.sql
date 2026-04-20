-- Maplewood High School Database Schema
-- SQLite Database for Fullstack Coding Challenge

PRAGMA foreign_keys = ON;

-- Table 1: Room Types
CREATE TABLE room_types (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    name VARCHAR(50) NOT NULL UNIQUE,
    description TEXT
);

-- Table 2: Specializations
CREATE TABLE specializations (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    name VARCHAR(50) NOT NULL UNIQUE,
    room_type_id INTEGER,
    description TEXT,
    FOREIGN KEY (room_type_id) REFERENCES room_types(id)
);

-- Table 3: Teachers
CREATE TABLE teachers (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    first_name VARCHAR(50) NOT NULL,
    last_name VARCHAR(50) NOT NULL,
    specialization_id INTEGER NOT NULL,
    email VARCHAR(100) UNIQUE,
    max_daily_hours INTEGER DEFAULT 4 CHECK (max_daily_hours <= 4),
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (specialization_id) REFERENCES specializations(id)
);

-- Table 4: Classrooms
CREATE TABLE classrooms (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    name VARCHAR(20) NOT NULL UNIQUE,
    room_type_id INTEGER NOT NULL,
    capacity INTEGER DEFAULT 10 CHECK (capacity <= 10),
    equipment TEXT,
    floor INTEGER,
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (room_type_id) REFERENCES room_types(id)
);

-- Table 5: Semesters
CREATE TABLE semesters (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    name VARCHAR(20) NOT NULL,
    year INTEGER NOT NULL,
    -- Semester order within academic year: 1=Fall, 2=Spring
    order_in_year INTEGER NOT NULL CHECK (order_in_year IN (1, 2)),
    start_date DATE,
    end_date DATE,
    is_active BOOLEAN DEFAULT FALSE,
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    UNIQUE(name, year),
    -- Ensure logical semester ordering: Fall=1, Spring=2
    CHECK (
        (name = 'Fall' AND order_in_year = 1) OR
        (name = 'Spring' AND order_in_year = 2)
    )
);

-- Table 6: Courses
CREATE TABLE courses (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    code VARCHAR(10) NOT NULL UNIQUE,
    name VARCHAR(100) NOT NULL,
    description TEXT,
    credits DECIMAL(3,1) NOT NULL CHECK (credits > 0),
    hours_per_week INTEGER NOT NULL CHECK (hours_per_week BETWEEN 2 AND 6),
    specialization_id INTEGER NOT NULL,
    prerequisite_id INTEGER,
    course_type VARCHAR(20) NOT NULL CHECK (course_type IN ('core', 'elective')),
    grade_level_min INTEGER CHECK (grade_level_min BETWEEN 9 AND 12),
    grade_level_max INTEGER CHECK (grade_level_max BETWEEN 9 AND 12),
    -- Semester order: 1=Fall courses, 2=Spring courses
    -- This ensures logical prerequisite progression across semesters
    semester_order INTEGER NOT NULL CHECK (semester_order IN (1, 2)),
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (specialization_id) REFERENCES specializations(id),
    FOREIGN KEY (prerequisite_id) REFERENCES courses(id),
    CHECK (grade_level_max >= grade_level_min)
);

-- Table 7: Students
CREATE TABLE students (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    first_name VARCHAR(50) NOT NULL,
    last_name VARCHAR(50) NOT NULL,
    email VARCHAR(100) UNIQUE,
    grade_level INTEGER NOT NULL CHECK (grade_level BETWEEN 9 AND 12),
    enrollment_year INTEGER NOT NULL,
    expected_graduation_year INTEGER,
    status VARCHAR(20) DEFAULT 'active' CHECK (status IN ('active', 'inactive', 'graduated')),
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP
);

-- Table 8: Student Course History
-- Tracks completed courses for prerequisite validation and GPA calculation
CREATE TABLE student_course_history (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    student_id INTEGER NOT NULL,
    course_id INTEGER NOT NULL,
    semester_id INTEGER NOT NULL,
    status VARCHAR(20) NOT NULL CHECK (status IN ('passed', 'failed')),
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (student_id) REFERENCES students(id),
    FOREIGN KEY (course_id) REFERENCES courses(id),
    FOREIGN KEY (semester_id) REFERENCES semesters(id),
    UNIQUE(student_id, course_id, semester_id)
);

-- Create indexes for performance
CREATE INDEX idx_teachers_specialization ON teachers(specialization_id);
CREATE INDEX idx_courses_specialization ON courses(specialization_id);
CREATE INDEX idx_courses_prerequisite ON courses(prerequisite_id);
CREATE INDEX idx_courses_semester_order ON courses(semester_order);
CREATE INDEX idx_semesters_order_year ON semesters(order_in_year, year);
CREATE INDEX idx_students_grade_level ON students(grade_level);
CREATE INDEX idx_student_course_history_student ON student_course_history(student_id);
CREATE INDEX idx_student_course_history_course ON student_course_history(course_id);
CREATE INDEX idx_student_course_history_semester ON student_course_history(semester_id);

-- Triggers for enforcing business rules

-- Trigger to enforce prerequisite semester ordering constraint
-- Ensures logical academic progression: courses can only depend on prerequisites
-- from the same semester or earlier semesters within the same grade level,
-- or any semester from earlier grade levels
CREATE TRIGGER enforce_prerequisite_semester_order
    BEFORE INSERT ON courses
    FOR EACH ROW
    WHEN NEW.prerequisite_id IS NOT NULL
BEGIN
    SELECT CASE
        WHEN (
            -- Same grade level: prerequisite must be same or earlier semester
            SELECT c1.grade_level_min FROM courses c1 WHERE c1.id = NEW.prerequisite_id
        ) = NEW.grade_level_min AND (
            SELECT c1.semester_order FROM courses c1 WHERE c1.id = NEW.prerequisite_id
        ) > NEW.semester_order
        THEN RAISE(ABORT, 'Course cannot be scheduled before its prerequisite within the same grade level.')
    END;
END;

-- Trigger for UPDATE operations
CREATE TRIGGER enforce_prerequisite_semester_order_update
    BEFORE UPDATE ON courses
    FOR EACH ROW
    WHEN NEW.prerequisite_id IS NOT NULL
BEGIN
    SELECT CASE
        WHEN (
            -- Same grade level: prerequisite must be same or earlier semester
            SELECT c1.grade_level_min FROM courses c1 WHERE c1.id = NEW.prerequisite_id
        ) = NEW.grade_level_min AND (
            SELECT c1.semester_order FROM courses c1 WHERE c1.id = NEW.prerequisite_id
        ) > NEW.semester_order
        THEN RAISE(ABORT, 'Course cannot be scheduled before its prerequisite within the same grade level.')
    END;
END;

-- Trigger to enforce prerequisite completion before course enrollment
-- Ensures students have passed prerequisite courses before enrolling in dependent courses
CREATE TRIGGER enforce_prerequisite_completion
    BEFORE INSERT ON student_course_history
    FOR EACH ROW
BEGIN
    -- Check if the course has a prerequisite
    SELECT CASE
        WHEN (
            SELECT c.prerequisite_id FROM courses c WHERE c.id = NEW.course_id
        ) IS NOT NULL AND NOT EXISTS (
            -- Check if student has passed the prerequisite course
            SELECT 1 FROM student_course_history sch
            WHERE sch.student_id = NEW.student_id
            AND sch.course_id = (SELECT c.prerequisite_id FROM courses c WHERE c.id = NEW.course_id)
            AND sch.status = 'passed'
        )
        THEN RAISE(ABORT, 'Student must pass prerequisite course before enrolling in this course.')
    END;
END;

-- Trigger to prevent duplicate course enrollment
-- Ensures students cannot retake courses they have already passed
CREATE TRIGGER prevent_duplicate_passed_course
    BEFORE INSERT ON student_course_history
    FOR EACH ROW
    WHEN NEW.status = 'passed'
BEGIN
    SELECT CASE
        WHEN EXISTS (
            SELECT 1 FROM student_course_history sch
            WHERE sch.student_id = NEW.student_id
            AND sch.course_id = NEW.course_id
            AND sch.status = 'passed'
        )
        THEN RAISE(ABORT, 'Student has already passed this course.')
    END;
END;