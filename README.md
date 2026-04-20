# 🏫 Maplewood High School - Course Planning Challenge

Build a full-stack course planning application for students to browse courses, plan their semester schedule, and track graduation progress.

**Tech Stack**: Spring Boot + React + TypeScript + State Management Library

---

## 🎯 Requirements

### **Backend API (Spring Boot)**

**Required Endpoints:**
- List courses (with filters for grade/semester)
- Student profile with academic history, GPA, credits
- Enroll in a course (validate prerequisites, conflicts, max 5 courses)
- Current semester schedule

**Business Rules:**
- Prerequisites must be passed before enrollment
- Max 5 courses per semester
- 30 credits to graduate
- No time slot conflicts

### **Frontend (React + TypeScript)**

**Required Features:**
- **Course Browser** - List/filter courses with details (credits, prerequisites)
- **Schedule Builder** - Add/remove courses with real-time validation (prerequisites, conflicts, limits)
- **Student Dashboard** - GPA, credits, graduation progress

**State Management:**
Implement centralized state using Redux Toolkit, Zustand, Jotai, Context+useReducer, or your preferred solution. Handle loading states, errors, and optimistic updates.

---

## 🗄️ Database

Pre-populated SQLite database (`maplewood_school.sqlite`) with 400 students, 57 courses, and ~6,700 historical records.

**Key Tables:** `students`, `courses`, `student_course_history`, `semesters` (see [DATABASE.md](./DATABASE.md))

**Your Task:** Create additional tables for course sections, time slots, and current semester enrollments.

---

## 🚀 Setup

**Dev Container (Recommended):** Open in VS Code with Dev Containers extension - everything pre-configured.

**Manual Setup:** Java 17, Maven 3.8+, Node.js 20+, SQLite 3

## 📁 Project Structure

Create your project with the following structure:

```
fullstack-challenge/
├── backend/
│   ├── src/
│   │   ├── main/
│   │   │   ├── java/
│   │   │   │   └── com/maplewood/
│   │   │   │       ├── controller/
│   │   │   │       ├── service/
│   │   │   │       ├── repository/
│   │   │   │       ├── model/
│   │   │   │       └── dto/
│   │   │   └── resources/
│   │   │       └── application.properties
│   │   └── test/
│   └── pom.xml
│
├── frontend/
│   ├── src/
│   │   ├── components/
│   │   ├── store/          # State management
│   │   ├── api/            # API client
│   │   ├── types/
│   │   └── App.tsx
│   ├── package.json
│   └── tsconfig.json
│
└── maplewood_school.sqlite
```

---

**Questions?** Email: <DL-eBay-Data-Productization@ebay.com>
