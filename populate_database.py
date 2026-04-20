#!/usr/bin/env python3
"""
Maplewood High School Database Population Script
Generates realistic sample data for the fullstack coding challenge
"""

import sqlite3
import random
from datetime import datetime, date
import os

# Sample data for realistic generation
FIRST_NAMES = [
    "James", "Mary", "John", "Patricia", "Robert", "Jennifer", "Michael", "Linda",
    "William", "Elizabeth", "David", "Barbara", "Richard", "Susan", "Joseph", "Jessica",
    "Thomas", "Sarah", "Christopher", "Karen", "Charles", "Nancy", "Daniel", "Lisa",
    "Matthew", "Betty", "Anthony", "Helen", "Mark", "Sandra", "Donald", "Donna",
    "Steven", "Carol", "Paul", "Ruth", "Andrew", "Sharon", "Joshua", "Michelle",
    "Kenneth", "Laura", "Kevin", "Sarah", "Brian", "Kimberly", "George", "Deborah",
    "Edward", "Dorothy", "Ronald", "Lisa", "Timothy", "Nancy", "Jason", "Karen",
    "Jeffrey", "Betty", "Ryan", "Helen", "Jacob", "Sandra", "Gary", "Donna",
    "Nicholas", "Carol", "Eric", "Ruth", "Jonathan", "Sharon", "Stephen", "Michelle"
]

LAST_NAMES = [
    "Smith", "Johnson", "Williams", "Brown", "Jones", "Garcia", "Miller", "Davis",
    "Rodriguez", "Martinez", "Hernandez", "Lopez", "Gonzalez", "Wilson", "Anderson", "Thomas",
    "Taylor", "Moore", "Jackson", "Martin", "Lee", "Perez", "Thompson", "White",
    "Harris", "Sanchez", "Clark", "Ramirez", "Lewis", "Robinson", "Walker", "Young",
    "Allen", "King", "Wright", "Scott", "Torres", "Nguyen", "Hill", "Flores",
    "Green", "Adams", "Nelson", "Baker", "Hall", "Rivera", "Campbell", "Mitchell",
    "Carter", "Roberts", "Gomez", "Phillips", "Evans", "Turner", "Diaz", "Parker",
    "Cruz", "Edwards", "Collins", "Reyes", "Stewart", "Morris", "Morales", "Murphy",
    "Cook", "Rogers", "Gutierrez", "Ortiz", "Morgan", "Cooper", "Peterson", "Bailey"
]

def create_database():
    """Create the SQLite database with schema"""
    # Remove existing database if it exists
    if os.path.exists('maplewood_school.sqlite'):
        os.remove('maplewood_school.sqlite')

    # Create new database
    conn = sqlite3.connect('maplewood_school.sqlite')

    # Read and execute schema
    with open('create_database.sql', 'r') as f:
        schema = f.read()

    conn.executescript(schema)
    conn.commit()
    return conn

def populate_room_types(conn):
    """Populate room types"""
    room_types = [
        ('classroom', 'Standard classroom for general subjects'),
        ('science_lab', 'Laboratory for chemistry, biology, and physics'),
        ('art_studio', 'Studio space for art and creative courses'),
        ('gym', 'Gymnasium for physical education'),
        ('computer_lab', 'Computer laboratory with workstations'),
        ('music_room', 'Music room with instruments and acoustics'),
        ('library', 'Library and study space'),
        ('auditorium', 'Large space for presentations and assemblies')
    ]

    conn.executemany(
        "INSERT INTO room_types (name, description) VALUES (?, ?)",
        room_types
    )
    print("✓ Populated room types")

def populate_specializations(conn):
    """Populate teacher specializations"""
    specializations = [
        ('Mathematics', 1, 'Math and related quantitative subjects'),
        ('English', 1, 'English language arts and literature'),
        ('Science', 2, 'Biology, chemistry, physics, earth science'),
        ('Social_Studies', 1, 'History, government, economics, geography'),
        ('Arts', 3, 'Visual arts, drawing, painting, sculpture'),
        ('Music', 6, 'Band, choir, music theory, individual instruments'),
        ('Physical_Education', 4, 'Physical fitness, sports, health education'),
        ('Computer_Science', 5, 'Programming, web development, digital literacy'),
        ('Foreign_Language', 1, 'Spanish, French, German language instruction')
    ]

    conn.executemany(
        "INSERT INTO specializations (name, room_type_id, description) VALUES (?, ?, ?)",
        specializations
    )
    print("✓ Populated specializations")

def populate_classrooms(conn):
    """Populate 60 classrooms"""
    classrooms = []

    # Standard classrooms (30)
    for i in range(1, 31):
        classrooms.append((f"Room-{100 + i}", 1, 10, "Whiteboard, projector", random.randint(1, 3)))

    # Science labs (10)
    lab_equipment = ["Lab benches, fume hood, microscopes", "Chemistry equipment, periodic table",
                     "Physics equipment, measuring tools", "Biology specimens, dissection tools"]
    for i in range(1, 11):
        classrooms.append((f"Lab-{i}", 2, 10, random.choice(lab_equipment), random.randint(1, 2)))

    # Art studios (6)
    for i in range(1, 7):
        classrooms.append((f"Art-{i}", 3, 10, "Easels, art supplies, pottery wheel", random.randint(1, 2)))

    # Gyms (3)
    for i in range(1, 4):
        classrooms.append((f"Gym-{i}", 4, 10, "Basketball court, volleyball net, exercise equipment", 1))

    # Computer labs (6)
    for i in range(1, 7):
        classrooms.append((f"CompLab-{i}", 5, 10, "30 computers, smartboard, 3D printer", random.randint(2, 3)))

    # Music rooms (5)
    music_equipment = ["Piano, music stands", "Band instruments", "Choir risers", "Recording equipment"]
    for i in range(1, 6):
        classrooms.append((f"Music-{i}", 6, 10, random.choice(music_equipment), random.randint(1, 2)))

    conn.executemany(
        "INSERT INTO classrooms (name, room_type_id, capacity, equipment, floor) VALUES (?, ?, ?, ?, ?)",
        classrooms
    )
    print("✓ Populated 60 classrooms")

def populate_teachers(conn):
    """Populate 50 teachers with realistic distribution"""
    teachers = []

    # Distribution by specialization (approximate realistic ratios)
    specialization_counts = {
        1: 8,   # Mathematics
        2: 8,   # English
        3: 10,  # Science
        4: 6,   # Social Studies
        5: 4,   # Arts
        6: 4,   # Music
        7: 4,   # Physical Education
        8: 3,   # Computer Science
        9: 3    # Foreign Language
    }

    used_names = set()

    for spec_id, count in specialization_counts.items():
        for _ in range(count):
            # Generate unique name
            while True:
                first = random.choice(FIRST_NAMES)
                last = random.choice(LAST_NAMES)
                full_name = f"{first} {last}"
                if full_name not in used_names:
                    used_names.add(full_name)
                    break

            email = f"{first.lower()}.{last.lower()}@maplewood.edu"
            teachers.append((first, last, spec_id, email, 4))

    conn.executemany(
        "INSERT INTO teachers (first_name, last_name, specialization_id, email, max_daily_hours) VALUES (?, ?, ?, ?, ?)",
        teachers
    )
    print("✓ Populated 50 teachers")

def populate_semesters(conn):
    """Populate historical and current semesters with proper ordering"""
    semesters = [
        # Historical semesters (6 semesters for academic history)
        ('Fall', 2021, 1, '2021-08-20', '2021-12-15', False),    # 6 semesters ago
        ('Spring', 2021, 2, '2022-01-15', '2022-05-15', False),  # 5 semesters ago
        ('Fall', 2022, 1, '2022-08-20', '2022-12-15', False),    # 4 semesters ago
        ('Spring', 2022, 2, '2023-01-15', '2023-05-15', False),  # 3 semesters ago
        ('Fall', 2023, 1, '2023-08-20', '2023-12-15', False),    # 2 semesters ago
        ('Spring', 2023, 2, '2024-01-15', '2024-05-15', False),  # 1 semester ago
        # Current and future semesters
        ('Fall', 2024, 1, '2024-08-20', '2024-12-15', True),     # Current semester (7th for 12th graders)
        ('Spring', 2024, 2, '2025-01-15', '2025-05-15', False),  # Next semester
        ('Fall', 2025, 1, '2025-08-20', '2025-12-15', False)     # Future semester
    ]

    conn.executemany(
        "INSERT INTO semesters (name, year, order_in_year, start_date, end_date, is_active) VALUES (?, ?, ?, ?, ?, ?)",
        semesters
    )
    print("✓ Populated 9 semesters (6 historical + current + 2 future) with ordering (Fall=1, Spring=2)")

def populate_courses(conn):
    """Populate courses with realistic prerequisite chains"""
    courses = []

    # Core Courses (20 total)

    # English (8 courses) - Alternating Fall/Spring progression
    english_courses = [
        ('ENG101', 'English I: Foundations', 'Basic writing and literature', 1.0, 5, 2, None, 'core', 9, 9, 1),  # Fall
        ('ENG102', 'English I: Composition', 'Advanced writing and grammar', 1.0, 5, 2, None, 'core', 9, 9, 2),  # Spring
        ('ENG201', 'English II: Literature', 'World literature and analysis', 1.0, 5, 2, None, 'core', 10, 10, 1),   # Fall
        ('ENG202', 'English II: Rhetoric', 'Advanced composition and speech', 1.0, 5, 2, None, 'core', 10, 10, 2),   # Spring
        ('ENG301', 'English III: American Literature', 'American literary traditions', 1.0, 5, 2, None, 'core', 11, 11, 1),  # Fall
        ('ENG302', 'English III: Research Writing', 'Research methods and academic writing', 1.0, 5, 2, None, 'core', 11, 11, 2),  # Spring
        ('ENG401', 'English IV: British Literature', 'British literary canon', 1.0, 5, 2, None, 'core', 12, 12, 1),  # Fall
        ('ENG402', 'English IV: Creative Writing', 'Poetry, fiction, and drama writing', 1.0, 5, 2, None, 'core', 12, 12, 2)  # Spring
    ]

    # Mathematics (5 courses) - Strategic Fall/Spring distribution
    math_courses = [
        ('MAT101', 'Algebra I', 'Basic algebraic concepts and equations', 1.0, 6, 1, None, 'core', 9, 10, 1),  # Fall
        ('MAT102', 'Geometry', 'Geometric principles and proofs', 1.0, 6, 1, None, 'core', 9, 11, 2),         # Spring
        ('MAT201', 'Algebra II', 'Advanced algebraic concepts', 1.0, 6, 1, None, 'core', 10, 12, 1),           # Fall (can take after MAT101)
        ('MAT202', 'Pre-Calculus', 'Functions and trigonometry', 1.0, 6, 1, None, 'core', 11, 12, 2),         # Spring
        ('MAT301', 'Calculus', 'Differential and integral calculus', 1.0, 6, 1, None, 'core', 12, 12, 1)      # Fall
    ]

    # Science (4 courses) - Progressive Fall/Spring sequence
    science_courses = [
        ('SCI101', 'Biology I', 'Introduction to life sciences', 1.0, 6, 3, None, 'core', 9, 10, 1),           # Fall
        ('SCI102', 'Earth Science', 'Geology, weather, and astronomy', 1.0, 6, 3, None, 'core', 9, 10, 2),     # Spring (independent)
        ('SCI201', 'Chemistry I', 'Basic chemical principles', 1.0, 6, 3, None, 'core', 10, 12, 1),            # Fall (needs Biology)
        ('SCI301', 'Physics I', 'Mechanics and thermodynamics', 1.0, 6, 3, None, 'core', 11, 12, 2)           # Spring (needs Algebra II)
    ]

    # Social Studies (3 courses) - Independent progression
    social_courses = [
        ('SOC101', 'World History', 'Ancient civilizations to modern era', 1.0, 4, 4, None, 'core', 9, 10, 1),    # Fall
        ('SOC201', 'Government', 'American government and civics', 1.0, 4, 4, None, 'core', 11, 12, 2),           # Spring
        ('SOC301', 'Economics', 'Micro and macroeconomic principles', 1.0, 4, 4, None, 'core', 12, 12, 1)         # Fall
    ]

    # Combine core courses
    all_core = english_courses + math_courses + science_courses + social_courses
    courses.extend(all_core)

    # Elective Courses (40 total)

    # Arts electives - Progressive Fall/Spring
    art_electives = [
        ('ART101', 'Art I: Drawing', 'Basic drawing techniques', 0.5, 4, 5, None, 'elective', 9, 12, 1),           # Fall
        ('ART201', 'Art II: Painting', 'Watercolor and acrylic techniques', 0.5, 4, 5, None, 'elective', 9, 12, 2),  # Spring
        ('ART301', 'Art III: Sculpture', 'Three-dimensional art creation', 0.5, 4, 5, None, 'elective', 10, 12, 1),  # Fall
        ('ART401', 'Advanced Art Portfolio', 'Portfolio development for college', 0.5, 4, 5, None, 'elective', 11, 12, 2), # Spring
        ('PHOT101', 'Photography I', 'Digital photography basics', 0.5, 3, 5, None, 'elective', 9, 12, 1),         # Fall
        ('PHOT201', 'Photography II', 'Advanced techniques and editing', 0.5, 3, 5, None, 'elective', 10, 12, 2)    # Spring
    ]

    # Music electives - Theory alternates with Performance
    music_electives = [
        ('MUS101', 'Music Theory I', 'Basic music theory and notation', 0.5, 3, 6, None, 'elective', 9, 12, 1),        # Fall
        ('MUS201', 'Music Theory II', 'Advanced harmony and composition', 0.5, 3, 6, None, 'elective', 10, 12, 2),       # Spring
        ('BAND101', 'Concert Band', 'Instrumental ensemble performance', 0.5, 4, 6, None, 'elective', 9, 12, 2),       # Spring
        ('BAND201', 'Jazz Band', 'Jazz ensemble and improvisation', 0.5, 4, 6, None, 'elective', 10, 12, 1),            # Fall
        ('CHOIR101', 'Concert Choir', 'Vocal ensemble performance', 0.5, 4, 6, None, 'elective', 9, 12, 1),            # Fall
        ('CHOIR201', 'Chamber Choir', 'Advanced vocal techniques', 0.5, 4, 6, None, 'elective', 10, 12, 2)              # Spring
    ]

    # PE electives - Progressive and seasonal distribution
    pe_electives = [
        ('PE101', 'Physical Education I', 'Basic fitness and sports', 0.5, 4, 7, None, 'elective', 9, 12, 1),          # Fall
        ('PE201', 'Physical Education II', 'Advanced fitness training', 0.5, 4, 7, None, 'elective', 10, 12, 2),         # Spring
        ('HLTH101', 'Health Education', 'Nutrition and wellness', 0.5, 2, 7, None, 'elective', 9, 12, 2),              # Spring
        ('SPORT101', 'Team Sports', 'Basketball, volleyball, soccer', 0.5, 4, 7, None, 'elective', 9, 12, 1)           # Fall
    ]

    # Computer Science electives - Progressive skill building
    cs_electives = [
        ('CS101', 'Intro to Programming', 'Basic programming concepts', 0.5, 4, 8, None, 'elective', 9, 12, 1),        # Fall
        ('CS201', 'Web Development', 'HTML, CSS, JavaScript basics', 0.5, 4, 8, None, 'elective', 10, 12, 2),            # Spring
        ('CS301', 'Advanced Programming', 'Data structures and algorithms', 0.5, 4, 8, None, 'elective', 11, 12, 1),     # Fall
        ('CS401', 'Computer Science Projects', 'Capstone programming projects', 0.5, 4, 8, None, 'elective', 12, 12, 2)  # Spring
    ]

    # Foreign Language electives - Progressive sequences
    lang_electives = [
        ('SPAN101', 'Spanish I', 'Basic Spanish language', 0.5, 4, 9, None, 'elective', 9, 12, 1),                     # Fall
        ('SPAN201', 'Spanish II', 'Intermediate Spanish', 0.5, 4, 9, None, 'elective', 10, 12, 2),                       # Spring
        ('SPAN301', 'Spanish III', 'Advanced Spanish', 0.5, 4, 9, None, 'elective', 11, 12, 1),                         # Fall
        ('FREN101', 'French I', 'Basic French language', 0.5, 4, 9, None, 'elective', 9, 12, 1),                      # Fall (foundation)
        ('FREN201', 'French II', 'Intermediate French', 0.5, 4, 9, None, 'elective', 10, 12, 2),                        # Spring
        ('FREN301', 'French III', 'Advanced French', 0.5, 4, 9, None, 'elective', 11, 12, 1),                          # Fall
        ('GERM101', 'German I', 'Basic German language', 0.5, 4, 9, None, 'elective', 9, 12, 1),                       # Fall (foundation)
        ('GERM201', 'German II', 'Intermediate German', 0.5, 4, 9, None, 'elective', 10, 12, 2),                        # Spring
        ('GERM301', 'German III', 'Advanced German', 0.5, 4, 9, None, 'elective', 11, 12, 1)                          # Fall
    ]

    # Additional electives to reach 40 - Balanced distribution
    other_electives = [
        ('DRAMA101', 'Drama I', 'Basic acting and theater', 0.5, 3, 5, None, 'elective', 9, 12, 1),                      # Fall
        ('DRAMA201', 'Drama II', 'Advanced acting techniques', 0.5, 3, 5, None, 'elective', 10, 12, 2),                   # Spring
        ('DEBATE101', 'Speech and Debate', 'Public speaking skills', 0.5, 3, 2, None, 'elective', 9, 12, 2),             # Spring
        ('JOURN101', 'Journalism', 'School newspaper and media', 0.5, 3, 2, None, 'elective', 10, 12, 1),               # Fall
        ('PSYCH101', 'Psychology', 'Introduction to psychology', 0.5, 3, 4, None, 'elective', 11, 12, 1),               # Fall
        ('STATS101', 'Statistics', 'Data analysis and probability', 0.5, 3, 1, None, 'elective', 11, 12, 2),              # Spring
        ('ENVIRON101', 'Environmental Science', 'Ecology and conservation', 0.5, 4, 3, None, 'elective', 10, 12, 2),      # Spring
        ('ASTRO101', 'Astronomy', 'Stars, planets, and space', 0.5, 3, 3, None, 'elective', 11, 12, 1)                    # Fall
    ]

    # Combine all electives
    all_electives = art_electives + music_electives + pe_electives + cs_electives + lang_electives + other_electives
    courses.extend(all_electives)

    # Insert courses
    conn.executemany(
        """INSERT INTO courses
           (code, name, description, credits, hours_per_week, specialization_id, prerequisite_id,
            course_type, grade_level_min, grade_level_max, semester_order)
           VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)""",
        courses
    )
    print(f"✓ Populated {len(courses)} courses (20 core + {len(all_electives)} electives) with semester ordering")

    # Now add prerequisites using course codes
    prerequisite_updates = [
        ('ENG102', 'ENG101'),
        ('ENG201', 'ENG102'),
        ('ENG202', 'ENG201'),
        ('ENG301', 'ENG202'),
        ('ENG302', 'ENG301'),
        ('ENG401', 'ENG302'),
        ('ENG402', 'ENG401'),
        ('MAT102', 'MAT101'),
        ('MAT201', 'MAT101'),
        ('MAT202', 'MAT201'),
        ('MAT301', 'MAT202'),
        ('SCI201', 'SCI101'),
        ('SCI301', 'MAT201'),
        ('ART201', 'ART101'),
        ('ART301', 'ART201'),
        ('ART401', 'ART301'),
        ('MUS201', 'MUS101'),
        ('BAND201', 'BAND101'),
        ('CHOIR201', 'CHOIR101'),
        ('PE201', 'PE101'),
        ('CS201', 'CS101'),
        ('CS301', 'CS101'),
        ('CS401', 'CS301'),
        ('SPAN201', 'SPAN101'),
        ('SPAN301', 'SPAN201'),
        ('FREN201', 'FREN101'),
        ('FREN301', 'FREN201'),
        ('GERM201', 'GERM101'),
        ('GERM301', 'GERM201'),
        ('DRAMA201', 'DRAMA101'),
        ('STATS101', 'MAT201'),
        ('ENVIRON101', 'SCI101'),
        ('ASTRO101', 'SCI102'),
        ('PHOT201', 'PHOT101')
    ]

    for course_code, prereq_code in prerequisite_updates:
        conn.execute("""
            UPDATE courses
            SET prerequisite_id = (SELECT id FROM courses WHERE code = ?)
            WHERE code = ?
        """, (prereq_code, course_code))

    print(f"✓ Added {len(prerequisite_updates)} prerequisite relationships")

def populate_students(conn):
    """Populate 400 students (100 per grade level)"""
    students = []
    used_names = set()

    for grade in [9, 10, 11, 12]:
        for i in range(100):
            # Generate unique name
            while True:
                first = random.choice(FIRST_NAMES)
                last = random.choice(LAST_NAMES)
                full_name = f"{first} {last} {grade}"
                if full_name not in used_names:
                    used_names.add(full_name)
                    break

            email = f"{first.lower()}.{last.lower()}{grade}@student.maplewood.edu"
            enrollment_year = 2024 - (grade - 9)
            expected_graduation = enrollment_year + 4

            students.append((first, last, email, grade, enrollment_year, expected_graduation, 'active'))

    conn.executemany(
        """INSERT INTO students
           (first_name, last_name, email, grade_level, enrollment_year, expected_graduation_year, status)
           VALUES (?, ?, ?, ?, ?, ?, ?)""",
        students
    )
    print("✓ Populated 400 students (100 per grade)")

def populate_student_course_history(conn):
    """Generate realistic academic history for students based on their grade level"""
    print("Generating student course history...")

    # Get all students, courses, and semesters
    students = conn.execute("SELECT id, grade_level, enrollment_year FROM students").fetchall()
    courses = conn.execute("""
        SELECT id, code, course_type, grade_level_min, grade_level_max,
               semester_order, prerequisite_id, credits
        FROM courses ORDER BY grade_level_min, semester_order
    """).fetchall()
    semesters = conn.execute("""
        SELECT id, name, year, order_in_year
        FROM semesters
        WHERE year < 2024
        ORDER BY year, order_in_year
    """).fetchall()

    history_records = []

    for student_id, current_grade, enrollment_year in students:
        # Calculate how many semesters this student has been enrolled
        completed_semesters = (current_grade - 9) * 2  # 2 semesters per grade
        if current_grade > 9:  # Add one more for Spring of previous grade
            completed_semesters += 1

        # Start from their enrollment year
        student_semester_start = len(semesters) - completed_semesters
        if student_semester_start < 0:
            student_semester_start = 0

        student_completed_courses = set()

        # Process each semester for this student
        for sem_index in range(student_semester_start, len(semesters)):
            semester_id, sem_name, sem_year, sem_order = semesters[sem_index]

            # Determine what grade level the student was in during this semester
            semesters_since_enrollment = sem_index - student_semester_start
            student_grade_during_semester = 9 + (semesters_since_enrollment // 2)

            # Get eligible courses for this grade level and semester
            eligible_courses = [
                course for course in courses
                if (course[3] <= student_grade_during_semester <= course[4] and  # grade range
                    course[5] == sem_order)  # semester order matches
            ]

            # Filter courses based on prerequisites
            available_courses = []
            for course in eligible_courses:
                course_id, code, course_type, _, _, _, prereq_id, credits = course

                # Skip if already taken
                if course_id in student_completed_courses:
                    continue

                # Check prerequisite
                if prereq_id is None or prereq_id in student_completed_courses:
                    available_courses.append(course)

            # Select courses for this semester
            # Core courses: try to take if available
            # Electives: random selection
            semester_courses = []

            # Prioritize core courses
            core_courses = [c for c in available_courses if c[2] == 'core']
            elective_courses = [c for c in available_courses if c[2] == 'elective']

            # Add core courses (up to 3 per semester)
            semester_courses.extend(core_courses[:3])

            # Add electives to fill up to 5 courses total
            remaining_slots = 5 - len(semester_courses)
            if remaining_slots > 0 and elective_courses:
                selected_electives = random.sample(
                    elective_courses,
                    min(remaining_slots, len(elective_courses))
                )
                semester_courses.extend(selected_electives)

            # Add to history with realistic pass/fail rates
            for course in semester_courses:
                course_id = course[0]

                # 85% pass rate, 15% fail rate
                rand = random.random()
                if rand < 0.85:
                    status = 'passed'
                    student_completed_courses.add(course_id)
                else:
                    status = 'failed'

                history_records.append((student_id, course_id, semester_id, status))

    # Insert all history records
    conn.executemany(
        """INSERT INTO student_course_history
           (student_id, course_id, semester_id, status)
           VALUES (?, ?, ?, ?)""",
        history_records
    )

    print(f"✓ Generated {len(history_records)} student course history records")

def main():
    """Main function to create and populate the database"""
    print("🏫 Creating Maplewood High School Database...")
    print("=" * 50)

    # Create database and schema
    conn = create_database()
    print("✓ Created database schema")

    # Populate tables in order (respecting foreign key constraints)
    populate_room_types(conn)
    populate_specializations(conn)
    populate_teachers(conn)
    populate_classrooms(conn)
    populate_semesters(conn)
    populate_courses(conn)
    populate_students(conn)
    populate_student_course_history(conn)

    # Commit and close
    conn.commit()
    conn.close()

    print("=" * 50)
    print("✅ Database created successfully: maplewood_school.sqlite")
    print("\n📊 Database Summary:")
    print("   • 8 room types")
    print("   • 9 specializations")
    print("   • 50 teachers")
    print("   • 60 classrooms")
    print("   • 9 semesters (6 historical + current + 2 future) with ordering (Fall=1, Spring=2)")
    print("   • 57 courses (20 core + 37 electives) with semester ordering")
    print("   • 400 students (100 per grade)")
    print("   • Realistic student course history with pass/fail records for prerequisite validation")
    print("   • 34 prerequisite relationships enforced by triggers")
    print("\n🚀 Ready for the scheduling challenge!")

if __name__ == "__main__":
    main()