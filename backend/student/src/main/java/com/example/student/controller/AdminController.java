package com.example.student.controller;

import com.example.student.model.User;
import com.example.student.repository.EnrollmentRepository;
import com.example.student.repository.LessonCompletionRepository;
import com.example.student.repository.QuizResultRepository;
import com.example.student.repository.UserRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin")
public class AdminController {

    private final UserRepository userRepository;
    private final EnrollmentRepository enrollmentRepository;
    private final LessonCompletionRepository completionRepository;
    private final QuizResultRepository resultRepository;

    public AdminController(UserRepository userRepository,
                           EnrollmentRepository enrollmentRepository,
                           LessonCompletionRepository completionRepository,
                           QuizResultRepository resultRepository) {
        this.userRepository = userRepository;
        this.enrollmentRepository = enrollmentRepository;
        this.completionRepository = completionRepository;
        this.resultRepository = resultRepository;
    }

    @GetMapping("/users")
    public List<User> allUsers() {
        return userRepository.findAll();
    }

    @DeleteMapping("/users/{id}")
    @Transactional
    public ResponseEntity<?> deleteUser(@PathVariable Long id) {
        return userRepository.findById(id)
                .map(user -> {
                    if ("ADMIN".equals(user.getRole())) {
                        return ResponseEntity.badRequest().body("Admin cannot be deleted");
                    }
                    resultRepository.deleteByUserId(id);
                    completionRepository.deleteByUserId(id);
                    enrollmentRepository.deleteByUserId(id);
                    userRepository.deleteById(id);
                    return ResponseEntity.noContent().build();
                })
                .orElse(ResponseEntity.notFound().build());
    }
}