# 🚀 Quick Start Guide

## Using Dev Container (Recommended)

1. **Prerequisites**:
   - VS Code
   - Docker Desktop
   - "Dev Containers" extension for VS Code

2. **Steps**:
   ```bash
   # Clone the repository
   git clone <your-repo-url>
   cd <your-repo>

   # Open in VS Code
   code .

   # Click "Reopen in Container" when prompted
   # OR use Command Palette (Cmd/Ctrl+Shift+P): "Dev Containers: Reopen in Container"
   ```

3. **Verify Setup**:
   ```bash
   # In the VS Code terminal inside the container:
   java -version    # Should show Java 17
   mvn -version     # Should show Maven
   node -v          # Should show Node 20+
   npm -v           # Should show npm
   sqlite3 --version # Should show SQLite
   ```

---

## Manual Setup (Without Dev Container)

### Backend Setup

1. **Create Spring Boot Project**:
   ```bash
   mkdir backend
   cd backend
   ```

2. **Create `pom.xml`**:
   ```xml
   <?xml version="1.0" encoding="UTF-8"?>
   <project xmlns="http://maven.apache.org/POM/4.0.0"
            xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
            xsi:schemaLocation="http://maven.apache.org/POM/4.0.0
            http://maven.apache.org/xsd/maven-4.0.0.xsd">
       <modelVersion>4.0.0</modelVersion>

       <parent>
           <groupId>org.springframework.boot</groupId>
           <artifactId>spring-boot-starter-parent</artifactId>
           <version>3.2.0</version>
       </parent>

       <groupId>com.maplewood</groupId>
       <artifactId>course-planning</artifactId>
       <version>1.0.0</version>

       <properties>
           <java.version>17</java.version>
       </properties>

       <dependencies>
           <dependency>
               <groupId>org.springframework.boot</groupId>
               <artifactId>spring-boot-starter-web</artifactId>
           </dependency>
           <dependency>
               <groupId>org.springframework.boot</groupId>
               <artifactId>spring-boot-starter-data-jpa</artifactId>
           </dependency>
           <dependency>
               <groupId>org.xerial</groupId>
               <artifactId>sqlite-jdbc</artifactId>
               <version>3.44.1.0</version>
           </dependency>
           <dependency>
               <groupId>org.hibernate.orm</groupId>
               <artifactId>hibernate-community-dialects</artifactId>
           </dependency>
       </dependencies>
   </project>
   ```

3. **Run Backend**:
   ```bash
   mvn spring-boot:run
   # Backend will run on http://localhost:8080
   ```

### Frontend Setup

1. **Create React App**:
   ```bash
   npx create-react-app frontend --template typescript
   cd frontend
   ```

2. **Install State Management** (choose one):
   ```bash
   # Redux Toolkit (recommended)
   npm install @reduxjs/toolkit react-redux

   # OR Zustand
   npm install zustand

   # OR Jotai
   npm install jotai
   ```

3. **Install Additional Dependencies**:
   ```bash
   npm install axios
   ```

4. **Run Frontend**:
   ```bash
   npm start
   # Frontend will run on http://localhost:3000
   ```

---

## Database Exploration

```bash
# Open the database
sqlite3 maplewood_school.sqlite

# Useful queries:
sqlite> .tables
sqlite> SELECT * FROM students LIMIT 5;
sqlite> SELECT * FROM courses WHERE grade_level_min <= 10 LIMIT 10;
sqlite> SELECT * FROM student_course_history WHERE student_id = 1;
sqlite> .quit
```

---

## API Testing

Use curl or Postman to test your endpoints:

```bash
# Get all courses
curl http://localhost:8080/api/courses

# Get student profile
curl http://localhost:8080/api/students/1

# Enroll in course
curl -X POST http://localhost:8080/api/enrollments \
  -H "Content-Type: application/json" \
  -d '{"studentId": 1, "courseId": 5}'
```

---

## Project Structure Example

```
.
├── .devcontainer/
│   ├── devcontainer.json
│   └── Dockerfile
├── backend/
│   ├── src/main/java/com/maplewood/
│   │   ├── controller/
│   │   ├── service/
│   │   ├── repository/
│   │   ├── model/
│   │   └── Application.java
│   ├── src/main/resources/
│   │   └── application.properties
│   └── pom.xml
├── frontend/
│   ├── src/
│   │   ├── components/
│   │   ├── store/          # State management
│   │   ├── api/
│   │   ├── types/
│   │   ├── App.tsx
│   │   └── index.tsx
│   ├── package.json
│   └── tsconfig.json
└── maplewood_school.sqlite
```

---

## Common Pitfalls to Avoid

1. **Don't spend too long on setup** - Use dev container or simple setup
2. **Don't over-design** - Start with simple, working features
3. **Don't skip validation** - This is key to the challenge
4. **Don't forget error handling** - Show loading/error states
5. **Don't neglect state management** - This is a main evaluation criterion

---

**Ready? Let's build! 🚀**
