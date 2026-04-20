package com.maplewood.config;

import com.maplewood.model.Classroom;
import com.maplewood.model.Course;
import com.maplewood.model.CourseSection;
import com.maplewood.model.Semester;
import com.maplewood.model.Teacher;
import com.maplewood.repository.CourseRepository;
import com.maplewood.repository.CourseSectionRepository;
import com.maplewood.repository.SemesterRepository;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;


/**
 * Seeds course sections for the active semester on application startup.
 * Creates one section per course with deterministic time slot assignments.
 * Runs idempotently - checks for existing data before seeding.
 */
@Component
public class DataSeeder implements ApplicationRunner {

    private static final Logger log = LoggerFactory.getLogger(DataSeeder.class);

    private static final String[][] TIME_SLOTS = {
            {"MON,WED,FRI", "08:00", "09:00"},
            {"MON,WED,FRI", "09:00", "10:00"},
            {"MON,WED,FRI", "10:00", "11:00"},
            {"MON,WED,FRI", "11:00", "12:00"},
            {"TUE,THU",     "08:00", "09:30"},
            {"TUE,THU",     "09:30", "11:00"},
            {"MON,WED,FRI", "13:00", "14:00"},
            {"MON,WED,FRI", "14:00", "15:00"},
            {"TUE,THU",     "13:00", "14:30"},
            {"TUE,THU",     "14:30", "16:00"},
    };

    private final SemesterRepository semesterRepository;
    private final CourseRepository courseRepository;
    private final CourseSectionRepository sectionRepository;

    @PersistenceContext
    private EntityManager em;

    public DataSeeder(SemesterRepository semesterRepository,
                      CourseRepository courseRepository,
                      CourseSectionRepository sectionRepository) {
        this.semesterRepository = semesterRepository;
        this.courseRepository = courseRepository;
        this.sectionRepository = sectionRepository;
    }

    @Override
    @Transactional
    public void run(ApplicationArguments args) {
        Optional<Semester> activeOpt = semesterRepository.findFirstByActiveTrue();
        if (activeOpt.isEmpty()) {
            log.warn("No active semester found; skipping section seeding.");
            return;
        }
        Semester active = activeOpt.get();

        List<Teacher> teachers = em.createQuery("SELECT t FROM Teacher t", Teacher.class).getResultList();
        List<Classroom> classrooms = em.createQuery("SELECT r FROM Classroom r", Classroom.class).getResultList();
        if (teachers.isEmpty() || classrooms.isEmpty()) {
            log.warn("Teachers or classrooms missing; skipping section seeding.");
            return;
        }

        List<Course> courses = courseRepository.findAll().stream()
                .filter(course -> course.getSemesterOrder() != null
                        && course.getSemesterOrder().equals(active.getOrderInYear()))
                .toList();

        int created = 0;
        for (Course course : courses) {
            if (sectionRepository.findByCourseIdAndSemesterId(course.getId(), active.getId()).isPresent()) {
                continue;
            }
            String[] slot = TIME_SLOTS[(int) (course.getId() % TIME_SLOTS.length)];
            Teacher teacher = teachers.get((int) (course.getId() % teachers.size()));
            Classroom classroom = classrooms.get((int) (course.getId() % classrooms.size()));

            CourseSection section = new CourseSection();
            setField(section, "course", course);
            setField(section, "semester", active);
            setField(section, "teacher", teacher);
            setField(section, "classroom", classroom);
            setField(section, "daysOfWeek", slot[0]);
            setField(section, "startTime", slot[1]);
            setField(section, "endTime", slot[2]);
            setField(section, "capacity", 30);
            sectionRepository.save(section);
            created++;
        }

        if (created > 0) {
            log.info("Seeded {} course sections for {}", created, active.getName() + " " + active.getYear());
        } else {
            log.info("Course sections already seeded for {}", active.getName() + " " + active.getYear());
        }
    }

    private static void setField(Object target, String fieldName, Object value) {
        try {
            var field = target.getClass().getDeclaredField(fieldName);
            field.setAccessible(true);
            field.set(target, value);
        } catch (ReflectiveOperationException e) {
            throw new IllegalStateException("Could not set " + fieldName, e);
        }
    }
}
