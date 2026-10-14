package com.example.student.config;

import com.example.student.model.Course;
import com.example.student.model.Lesson;
import com.example.student.model.Question;
import com.example.student.model.User;
import com.example.student.repository.CourseRepository;
import com.example.student.repository.LessonRepository;
import com.example.student.repository.QuestionRepository;
import com.example.student.repository.UserRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
public class DataSeeder implements CommandLineRunner {

    private final UserRepository userRepository;
    private final CourseRepository courseRepository;
    private final LessonRepository lessonRepository;
    private final QuestionRepository questionRepository;
    private final PasswordEncoder passwordEncoder;

    public DataSeeder(UserRepository userRepository, CourseRepository courseRepository,
                      LessonRepository lessonRepository, QuestionRepository questionRepository,
                      PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.courseRepository = courseRepository;
        this.lessonRepository = lessonRepository;
        this.questionRepository = questionRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void run(String... args) {
        if (!userRepository.existsByEmail("admin@portal.com")) {
            User admin = new User();
            admin.setName("Admin");
            admin.setEmail("admin@portal.com");
            admin.setPassword(passwordEncoder.encode("admin123"));
            admin.setRole("ADMIN");
            userRepository.save(admin);
        }

        if (courseRepository.count() == 0) {
            addCourse("Java Programming", "Core Java from basics to OOP.", "Programming");
            addCourse("React for Beginners", "Build modern web UIs with React.", "Web Development");
            addCourse("MySQL Essentials", "Learn SQL queries and database design.", "Database");
            addCourse("Spring Boot REST APIs", "Build backend APIs with Spring Boot.", "Web Development");
        }

        if (lessonRepository.count() == 0) {
            for (Course course : courseRepository.findAll()) {
                addLesson(course.getId(), "Introduction");
                addLesson(course.getId(), "Core Concepts");
                addLesson(course.getId(), "Hands-on Practice");
                addLesson(course.getId(), "Mini Project");
            }
        }

        if (questionRepository.count() == 0) {
            for (Course course : courseRepository.findAll()) {
                seedQuestions(course);
            }
        }
    }

    private void seedQuestions(Course course) {
        Long id = course.getId();
        switch (course.getTitle()) {
            case "Java Programming" -> {
                addQuestion(id, "Which keyword is used to inherit a class in Java?",
                        "implements", "extends", "inherits", "super", "B");
                addQuestion(id, "Which of these is NOT a primitive data type in Java?",
                        "int", "boolean", "String", "char", "C");
                addQuestion(id, "What is the default value of an int field in a class?",
                        "0", "null", "1", "undefined", "A");
            }
            case "React for Beginners" -> {
                addQuestion(id, "Which hook is used to manage state in a function component?",
                        "useEffect", "useState", "useRef", "useMemo", "B");
                addQuestion(id, "What does JSX stand for?",
                        "JavaScript XML", "Java Syntax Extension", "JSON XML", "JavaScript Extra", "A");
                addQuestion(id, "Which hook runs code after the component renders?",
                        "useState", "useContext", "useEffect", "useReducer", "C");
            }
            case "MySQL Essentials" -> {
                addQuestion(id, "Which SQL statement is used to fetch data from a table?",
                        "GET", "SELECT", "FETCH", "OPEN", "B");
                addQuestion(id, "Which key uniquely identifies each row in a table?",
                        "Foreign key", "Primary key", "Index key", "Secondary key", "B");
                addQuestion(id, "Which clause filters rows in a SELECT query?",
                        "ORDER BY", "GROUP BY", "WHERE", "LIMIT", "C");
            }
            case "Spring Boot REST APIs" -> {
                addQuestion(id, "Which annotation marks the main class of a Spring Boot app?",
                        "@SpringBootApplication", "@EnableWeb", "@MainApp", "@Application", "A");
                addQuestion(id, "Which annotation creates a REST controller?",
                        "@Service", "@Repository", "@RestController", "@Entity", "C");
                addQuestion(id, "Which annotation maps an HTTP GET request to a method?",
                        "@PostMapping", "@GetMapping", "@PutMapping", "@RequestBody", "B");
            }
            default -> { }
        }
    }

    private void addCourse(String title, String description, String category) {
        Course course = new Course();
        course.setTitle(title);
        course.setDescription(description);
        course.setCategory(category);
        courseRepository.save(course);
    }

    private void addLesson(Long courseId, String title) {
        Lesson lesson = new Lesson();
        lesson.setCourseId(courseId);
        lesson.setTitle(title);
        lessonRepository.save(lesson);
    }

    private void addQuestion(Long courseId, String text, String a, String b, String c, String d,
                             String correct) {
        Question question = new Question();
        question.setCourseId(courseId);
        question.setQuestionText(text);
        question.setOptionA(a);
        question.setOptionB(b);
        question.setOptionC(c);
        question.setOptionD(d);
        question.setCorrectOption(correct);
        questionRepository.save(question);
    }
}