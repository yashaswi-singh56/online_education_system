package com.example.student.controller;

import com.example.student.model.Enrollment;
import com.example.student.model.Lesson;
import com.example.student.model.LessonCompletion;
import com.example.student.model.User;
import com.example.student.repository.CourseRepository;
import com.example.student.repository.EnrollmentRepository;
import com.example.student.repository.LessonCompletionRepository;
import com.example.student.repository.LessonRepository;
import com.example.student.repository.UserRepository;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/dashboard")
public class DashboardController {

    public record CourseProgress(Long courseId, String title, String category,
                                 int totalLessons, int completedLessons, int percent) {}

    public record DashboardView(int enrolledCount, int completedCount,
                                int averageProgress, List<CourseProgress> courses) {}

    private final UserRepository userRepository;
    private final EnrollmentRepository enrollmentRepository;
    private final CourseRepository courseRepository;
    private final LessonRepository lessonRepository;
    private final LessonCompletionRepository completionRepository;

    public DashboardController(UserRepository userRepository,
                               EnrollmentRepository enrollmentRepository,
                               CourseRepository courseRepository,
                               LessonRepository lessonRepository,
                               LessonCompletionRepository completionRepository) {
        this.userRepository = userRepository;
        this.enrollmentRepository = enrollmentRepository;
        this.courseRepository = courseRepository;
        this.lessonRepository = lessonRepository;
        this.completionRepository = completionRepository;
    }

    @GetMapping
    public DashboardView dashboard(Authentication authentication) {
        User user = userRepository.findByEmail(authentication.getName()).orElseThrow();

        Set<Long> doneIds = completionRepository.findByUserId(user.getId()).stream()
                .map(LessonCompletion::getLessonId)
                .collect(Collectors.toSet());

        List<Long> courseIds = enrollmentRepository.findByUserId(user.getId()).stream()
                .map(Enrollment::getCourseId)
                .toList();

        List<CourseProgress> progressList = courseRepository.findAllById(courseIds).stream()
                .map(course -> {
                    List<Lesson> lessons = lessonRepository.findByCourseIdOrderByIdAsc(course.getId());
                    int total = lessons.size();
                    int completed = (int) lessons.stream()
                            .filter(lesson -> doneIds.contains(lesson.getId()))
                            .count();
                    int percent = total == 0 ? 0 : completed * 100 / total;
                    return new CourseProgress(course.getId(), course.getTitle(), course.getCategory(),
                            total, completed, percent);
                })
                .toList();

        int completedCount = (int) progressList.stream()
                .filter(c -> c.totalLessons() > 0 && c.percent() == 100)
                .count();
        int average = progressList.isEmpty() ? 0
                : (int) progressList.stream().mapToInt(CourseProgress::percent).average().orElse(0);

        return new DashboardView(progressList.size(), completedCount, average, progressList);
    }
}