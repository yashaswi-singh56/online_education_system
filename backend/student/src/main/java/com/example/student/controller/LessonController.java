package com.example.student.controller;

import com.example.student.model.Lesson;
import com.example.student.model.LessonCompletion;
import com.example.student.model.User;
import com.example.student.repository.CourseRepository;
import com.example.student.repository.EnrollmentRepository;
import com.example.student.repository.LessonCompletionRepository;
import com.example.student.repository.LessonRepository;
import com.example.student.repository.UserRepository;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api")
public class LessonController {

    public record LessonView(Long id, String title, boolean completed) {}

    private final LessonRepository lessonRepository;
    private final LessonCompletionRepository completionRepository;
    private final EnrollmentRepository enrollmentRepository;
    private final UserRepository userRepository;
    private final CourseRepository courseRepository;

    public LessonController(LessonRepository lessonRepository,
                            LessonCompletionRepository completionRepository,
                            EnrollmentRepository enrollmentRepository,
                            UserRepository userRepository,
                            CourseRepository courseRepository) {
        this.lessonRepository = lessonRepository;
        this.completionRepository = completionRepository;
        this.enrollmentRepository = enrollmentRepository;
        this.userRepository = userRepository;
        this.courseRepository = courseRepository;
    }

    @GetMapping("/courses/{courseId}/lessons")
    public List<LessonView> getLessons(@PathVariable Long courseId, Authentication authentication) {
        User user = userRepository.findByEmail(authentication.getName()).orElseThrow();
        Set<Long> doneIds = completionRepository.findByUserId(user.getId()).stream()
                .map(LessonCompletion::getLessonId)
                .collect(Collectors.toSet());

        return lessonRepository.findByCourseIdOrderByIdAsc(courseId).stream()
                .map(lesson -> new LessonView(lesson.getId(), lesson.getTitle(), doneIds.contains(lesson.getId())))
                .toList();
    }

    @PostMapping("/courses/{courseId}/lessons")
    public ResponseEntity<Lesson> addLesson(@PathVariable Long courseId, @RequestBody Lesson lesson) {
        if (!courseRepository.existsById(courseId)) {
            return ResponseEntity.notFound().build();
        }
        lesson.setId(null);
        lesson.setCourseId(courseId);
        return ResponseEntity.ok(lessonRepository.save(lesson));
    }

    @DeleteMapping("/lessons/{lessonId}")
    @Transactional
    public ResponseEntity<Void> deleteLesson(@PathVariable Long lessonId) {
        if (!lessonRepository.existsById(lessonId)) {
            return ResponseEntity.notFound().build();
        }
        completionRepository.deleteByLessonId(lessonId);
        lessonRepository.deleteById(lessonId);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/lessons/{lessonId}/toggle")
    @Transactional
    public ResponseEntity<?> toggle(@PathVariable Long lessonId, Authentication authentication) {
        User user = userRepository.findByEmail(authentication.getName()).orElseThrow();
        Lesson lesson = lessonRepository.findById(lessonId).orElse(null);
        if (lesson == null) {
            return ResponseEntity.notFound().build();
        }
        if (!enrollmentRepository.existsByUserIdAndCourseId(user.getId(), lesson.getCourseId())) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Enroll in this course first");
        }

        if (completionRepository.existsByUserIdAndLessonId(user.getId(), lessonId)) {
            completionRepository.deleteByUserIdAndLessonId(user.getId(), lessonId);
            return ResponseEntity.ok(false);
        }
        LessonCompletion completion = new LessonCompletion();
        completion.setUserId(user.getId());
        completion.setLessonId(lessonId);
        completionRepository.save(completion);
        return ResponseEntity.ok(true);
    }
}