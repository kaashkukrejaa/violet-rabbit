# Approach

Two goals from the brief: **extend the schema** for a master schedule, and **let students enrol** with full validation. Everything below is in service of those.

## Stack

- **Backend** — Spring Boot 3.2 / Java 17 / SQLite / JPA / Lombok / JUnit 5 + Mockito
- **Frontend** — React 18 + TypeScript / Vite / React Query (server state) + Context (UI state) / Axios / Vitest + RTL
- Monorepo: `backend/`, `frontend/`, `maplewood_school.sqlite`, `HOW_TO_RUN.md`

## Backend

- **Layering.** Controllers are thin (routing only, return DTOs). `EnrollmentService` handles orchestration + transactions. **Business rules are extracted into `EnrollmentValidator`** — a stateless `@Component` with no repository deps, so the service fetches `history` and `currentEnrollments` once and hands them in. Validator tests are pure JUnit, no Mockito.
- **Rich domain model.** Time-conflict arithmetic lives on `CourseSection.overlapsWith(...)` — tell-don't-ask rather than leaking the logic into the service.
- **The seven rules** (all enforced server-side, error codes in parens): `no_offering`, `grade_level`, `prerequisite`, `already_enrolled`, `already_passed`, `conflict`, `max_courses`.
- **Typed errors.** A single `GlobalExceptionHandler` returns `{ type, message }` for every failure — business rules, validation, 404s — so the UI branches on `type` in one place.
- **Java `DataSeeder` (not `data.sql`).** `course_sections` needs idempotent seeding; SQLite's SQL dialect made that painful in plain SQL, so an `ApplicationRunner` does it on startup and safely no-ops on restart.
- **Lombok** on entities for `@Getter` + `@NoArgsConstructor`. Behaviour methods stay handwritten.

## Frontend

- **React Query for server state, Context for UI state.** No Redux. The app has three small contexts (`Student`, `Filters`, `Toast`), each <30 lines. Every async fetch is "just a query", which is exactly what React Query is for.
- **Client-side validation mirrors the server** (`utils/eligibility.ts`) purely for UX — the button disables immediately with a reason instead of waiting for a round-trip. Server remains the source of truth.
- **Unified error shape.** `toApiError(err)` turns any Axios failure into the same `{ type, message }` the server already returns, so every mutation hook shows toasts via one code path.

## Database

Additive schema only — two new tables via `schema.sql` with `IF NOT EXISTS`:

- `course_sections` — one row per offering (course × semester × time slot × teacher × classroom). `UNIQUE(course_id, semester_id)`.
- `enrollments` — active enrolments in the current semester. `UNIQUE(student_id, section_id)` — the DB-level guard against double-click duplicates.

## Trade-offs

- **No auth** — the student picker stands in for "who am I". Real auth would just populate `selectedStudentId` from a JWT.
- **No pagination.** `/api/students` caps at 50 via `findTop50ByOrderByLastNameAscFirstNameAsc()` and `/api/courses` returns the full catalogue. Fine for this dataset; at real scale I'd switch to Spring Data's `Pageable` + `Page<T>` and a cursor or offset-based API.
- **No application-level logs** — Spring's startup logs and the global exception handler are enough at this scale; I'd add structured `INFO` on enrol/drop + `ERROR` fallback before shipping to prod.
- **No extra concurrency control** — the `UNIQUE(student_id, section_id)` constraint catches double-click duplicates; single SQLite writer serialises the rest. At production scale I'd add `@Version` on `Student` or `SELECT … FOR UPDATE` to close the theoretical count-based race (`max_courses`, time conflict) across concurrent tabs.
- **No caching** — small dataset, React Query handles client-side; not worth the invalidation complexity.

## Running it

See [`HOW_TO_RUN.md`](./HOW_TO_RUN.md).

```bash
cd backend && mvn -s settings.xml spring-boot:run     # :8080
cd frontend && npm install && npm run dev             # :3000

cd backend && mvn -s settings.xml test                # 103 tests
cd frontend && npm test                               # 74 tests
```
