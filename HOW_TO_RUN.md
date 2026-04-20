# Running the Application (no Dev Container)

This project is a monorepo with a Spring Boot backend and a React + TypeScript frontend (React Query + Context for state, Axios for HTTP), backed by the pre-populated `maplewood_school.sqlite`.

## Prerequisites

- Java 17 (`java -version`)
- Maven 3.8+ (`mvn -version`)
- Node 20+ (`node -v`)
- SQLite 3 CLI — *optional*, only needed if you want to inspect the DB manually; the app uses a bundled JDBC driver.

## 1. Start the backend

```bash
cd backend
mvn -s settings.xml spring-boot:run
```

Notes:
- `settings.xml` is a project-local Maven config that bypasses any corporate mirror you may have in `~/.m2/settings.xml`. If you don't need it, you can omit `-s settings.xml`.
- On first boot a Java seeder creates one `course_sections` row per course for the active semester (Fall 2024). New tables (`course_sections`, `enrollments`) are created automatically by `schema.sql` on startup.
- Backend listens on `http://localhost:8080`.

Quick smoke test:
```bash
curl http://localhost:8080/api/courses/offerings | head
curl http://localhost:8080/api/students/1
curl http://localhost:8080/api/students/1/schedule
```

## 2. Start the frontend

```bash
cd frontend
npm install        # first time only
npm run dev
```

- Dev server runs on `http://localhost:3000` and proxies `/api/*` to `http://localhost:8080`.
- Production build: `npm run build`.

## 3. Run the tests

Backend (JUnit 5 + Mockito + AssertJ, 103 tests):
```bash
cd backend
mvn -s settings.xml test
```

Frontend (Vitest + React Testing Library, 74 tests):
```bash
cd frontend
npm test             # single run
npm run test:watch   # watch mode
npm run test:ui      # Vitest UI
```

## API reference

| Method | Path                              | Description                                                           |
| ------ | --------------------------------- | --------------------------------------------------------------------- |
| GET    | `/api/courses`                    | List courses (optional filters: `grade`, `semesterOrder`)             |
| GET    | `/api/courses/offerings`          | Course offerings for the active semester (course + section details)   |
| GET    | `/api/students`                   | First 50 students (demo picker)                                       |
| GET    | `/api/students/{id}`              | Profile: GPA, credits earned, graduation progress, course history     |
| GET    | `/api/students/{id}/schedule`     | Current-semester schedule                                             |
| POST   | `/api/enrollments`                | `{studentId, courseId}` — enroll, with full validation (201 on success) |
| DELETE | `/api/enrollments`                | `{studentId, courseId}` — drop (returns updated schedule)             |

All enrollment failures return `{ "type": <error-code>, "message": <human-readable> }` with a 4xx status.
Error codes: `prerequisite`, `conflict`, `max_courses`, `grade_level`, `already_enrolled`, `already_passed`, `no_offering`, `not_found`, `bad_request`.

## Business rules enforced by the backend

- Must pass the prerequisite course before enrolling.
- Max 5 courses per semester.
- No time-slot conflicts between sections on shared days.
- Student grade level must fall within the course's `grade_level_min..max` range.
- Cannot re-enroll in a course already passed.
- Cannot enroll twice in the same course within the active semester.
- The course must be offered (have a section) in the active semester.

The frontend mirrors these rules in `src/utils/eligibility.ts` for instant visual feedback, but the server remains the source of truth.
