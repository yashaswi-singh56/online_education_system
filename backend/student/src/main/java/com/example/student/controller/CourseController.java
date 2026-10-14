package com.example.student.controller;

import com.example.student.model.Course;
import com.example.student.repository.CourseRepository;
import com.example.student.repository.EnrollmentRepository;
import com.example.student.repository.LessonCompletionRepository;
import com.example.student.repository.LessonRepository;
import com.example.student.repository.QuestionRepository;
import com.example.student.repository.QuizResultRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/courses")
public class CourseController {

    private final CourseRepository courseRepository;
    private final EnrollmentRepository enrollmentRepository;
    private final LessonRepository lessonRepository;
    private final LessonCompletionRepository completionRepository;
    private final QuestionRepository questionRepository;
    private final QuizResultRepository resultRepository;

    public CourseController(CourseRepository courseRepository,
                            EnrollmentRepository enrollmentRepository,
                            LessonRepository lessonRepository,
                            LessonCompletionRepository completionRepository,
                            QuestionRepository questionRepository,
                            QuizResultRepository resultRepository) {
        this.courseRepository = courseRepository;
        this.enrollmentRepository = enrollmentRepository;
        this.lessonRepository = lessonRepository;
        this.completionRepository = completionRepository;
        this.questionRepository = questionRepository;
        this.resultRepository = resultRepository;
    }

    @GetMapping
    public List<Course> getAll(@RequestParam(required = false) String search,
                               @RequestParam(required = false) String category) {
        return courseRepository.findAll().stream()
                .filter(c -> search == null || search.isBlank()
                        || (c.getTitle() != null && c.getTitle().toLowerCase().contains(search.toLowerCase())))
                .filter(c -> category == null || category.isBlank() || category.equals(c.getCategory()))
                .toList();
    }

    @GetMapping("/categories")
    public List<String> categories() {
        return courseRepository.findAll().stream()
                .map(Course::getCategory)
                .filter(c -> c != null && !c.isBlank())
                .distinct()
                .sorted()
                .toList();
    }

    @PostMapping
    public Course add(@RequestBody Course course) {
        course.setId(null);
        return courseRepository.save(course);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Course> update(@PathVariable Long id, @RequestBody Course updated) {
        return courseRepository.findById(id)
                .map(existing -> {
                    existing.setTitle(updated.getTitle());
                    existing.setDescription(updated.getDescription());
                    existing.setCategory(updated.getCategory());
                    return ResponseEntity.ok(courseRepository.save(existing));
                })
                .orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    @Transactional
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        if (!courseRepository.existsById(id)) {
            return ResponseEntity.notFound().build();
        }
        lessonRepository.findByCourseIdOrderByIdAsc(id)
                .forEach(lesson -> completionRepository.deleteByLessonId(lesson.getId()));
        lessonRepository.deleteByCourseId(id);
        questionRepository.deleteByCourseId(id);
        resultRepository.deleteByCourseId(id);
        enrollmentRepository.deleteByCourseId(id);
        courseRepository.deleteById(id);
        return ResponseEntity.noContent().build();
    }
}