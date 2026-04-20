# Problem Statement: Maplewood High School Course Planning System

## 🎓 The Challenge

Maplewood High School needs a course planning system that allows students to browse available courses, build their semester schedule, and track their progress toward graduation. Currently, students struggle with manual scheduling that often results in prerequisite violations, time conflicts, and credit tracking errors.

Your task is to build a full-stack web application that solves these problems.

## 📋 The Problem Domain

### Students Need To:
1. **Browse and discover courses** - View available courses with relevant information like credits, prerequisites, and grade level requirements
2. **Plan their semester** - Select courses for the current semester while avoiding scheduling conflicts
3. **Track graduation progress** - Monitor their GPA, earned credits, and remaining requirements to graduate

### The System Must Prevent:
- Enrolling in courses without completing prerequisites
- Scheduling courses that conflict in time
- Overloading (more than 5 courses per semester)
- Enrolling in courses outside the student's grade level range

### Business Rules:
- Students must pass prerequisite courses before enrolling in dependent courses
- Students can enroll in a maximum of 5 courses per semester
- Students need 30 total credits to graduate
- Course schedules cannot have time slot conflicts
- Course enrollment is grade-level appropriate

## 🎯 What You Need to Deliver

### A Backend Application That:
- Provides access to course catalog information
- Manages student academic profiles and history
- Handles course enrollment with proper validation
- Tracks current semester schedules
- Calculates academic metrics (GPA, credits earned)

### A Frontend Application That:
- Displays courses in a browsable, filterable interface
- Shows student academic information and progress
- Provides an interactive schedule builder
- Validates enrollment decisions in real-time
- Communicates errors and system state clearly to users

### State Management:
The frontend must implement proper state management using a centralized solution of your choice (Redux Toolkit, Zustand, Jotai, Context+useReducer, or similar). Your implementation should demonstrate:
- Clean separation of concerns
- Proper handling of asynchronous operations
- Loading and error states
- Predictable state updates

## 🗄️ Starting Point

You are provided with a pre-populated SQLite database (`maplewood_school.sqlite`) containing:
- **400 students** with their academic history from previous semesters
- **57 courses** across various subjects with prerequisite relationships
- **~6,700 historical enrollment records**
- **Semester definitions**

See `DATABASE.md` for the complete schema.

### What You Need to Add:
The existing database contains historical data, but you'll need to create additional structures to support:
- Course sections with specific time slots for the current semester
- Current semester enrollment tracking
- Any other data structures your solution requires

## 📊 Example Scenarios to Support

### Scenario 1: Valid Enrollment
- Student "Emma Wilson" (Grade 10, student_id: 1) wants to enroll in "AP English Literature"
- System checks: Has she passed "English II"? (Yes, Grade A)
- System checks: Does it conflict with her current schedule? (No)
- System checks: Is she at/below 5 courses? (Currently has 3)
- Result: ✅ Enrollment succeeds

### Scenario 2: Prerequisite Violation
- Student "James Lee" (Grade 9, student_id: 2) attempts "AP Calculus BC"
- System checks: Has he passed "AP Calculus AB"? (No record)
- Result: ❌ Enrollment blocked - missing prerequisite

### Scenario 3: Time Conflict
- Student attempts to add "Physics I" (Mon/Wed/Fri 9:00-10:00)
- Current schedule includes "Chemistry I" (Mon/Wed/Fri 9:00-10:00)
- Result: ❌ Enrollment blocked - schedule conflict

### Scenario 4: Course Limit
- Student already enrolled in 5 courses attempts to add a 6th
- Result: ❌ Enrollment blocked - maximum courses exceeded

## 🎯 Success Criteria

Your solution should:
1. **Enforce all business rules** reliably
2. **Provide clear feedback** when operations fail
3. **Handle edge cases** gracefully
4. **Maintain data integrity** across operations
5. **Offer good user experience** with responsive UI and clear state
6. **Demonstrate quality state management** with clean architecture

## 💡 Important Notes

- Focus on **working functionality** over complex features
- **Validation is critical** - this is a core requirement
- **State management quality** is a key evaluation area
- **Error handling** matters - show meaningful feedback to users
- **Code clarity** is valued - make it readable and maintainable

## 📚 Additional Resources

- **README.md** - Complete technical requirements and tech stack
- **DATABASE.md** - Database schema and sample queries
- **QUICKSTART.md** - Setup and development environment guide
- **starter-templates/** - Optional boilerplate code

---

**Questions?** Email: <DL-eBay-Data-Productization@ebay.com>
