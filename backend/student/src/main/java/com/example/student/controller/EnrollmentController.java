package com.example.student.controller;

import com.example.student.model.Course;
import com.example.student.model.Enrollment;
import com.example.student.model.User;
import com.example.student.repository.CourseRepository;
import com.example.student.repository.EnrollmentRepository;
import com.example.student.repository.UserRepository;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/enrollments")
public class EnrollmentController {

    private final EnrollmentRepository enrollmentRepository;
    private final CourseRepository courseRepository;
    private final UserRepository userRepository;

    public EnrollmentController(EnrollmentRepository enrollmentRepository,
                                CourseRepository courseRepository,
                                UserRepository userRepository) {
        this.enrollmentRepository = enrollmentRepository;
        this.courseRepository = courseRepository;
        this.userRepository = userRepository;
    }

    @PostMapping("/{courseId}")
    public ResponseEntity<?> enroll(@PathVariable Long courseId, Authentication authentication) {
        User user = userRepository.findByEmail(authentication.getName()).orElseThrow();

        if (!courseRepository.existsById(courseId)) {
            return ResponseEntity.notFound().build();
        }
        if (enrollmentRepository.existsByUserIdAndCourseId(user.getId(), courseId)) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body("Already enrolled");
        }

        Enrollment enrollment = new Enrollment();
        enrollment.setUserId(user.getId());
        enrollment.setCourseId(courseId);
        return ResponseEntity.ok(enrollmentRepository.save(enrollment));
    }

    @GetMapping("/my")
    public List<Course> myCourses(Authentication authentication) {
        User user = userRepository.findByEmail(authentication.getName()).orElseThrow();
        List<Long> courseIds = enrollmentRepository.findByUserId(user.getId())
                .stream()
                .map(Enrollment::getCourseId)
                .toList();
        return courseRepository.findAllById(courseIds);
    }

    @DeleteMapping("/{courseId}")
    @Transactional
    public ResponseEntity<Void> unenroll(@PathVariable Long courseId, Authentication authentication) {
        User user = userRepository.findByEmail(authentication.getName()).orElseThrow();
        enrollmentRepository.deleteByUserIdAndCourseId(user.getId(), courseId);
        return ResponseEntity.noContent().build();
    }
}
