package com.example.student.controller;

import com.example.student.model.Question;
import com.example.student.model.QuizResult;
import com.example.student.model.User;
import com.example.student.repository.CourseRepository;
import com.example.student.repository.EnrollmentRepository;
import com.example.student.repository.QuestionRepository;
import com.example.student.repository.QuizResultRepository;
import com.example.student.repository.UserRepository;
import com.example.student.service.CourseStatusService;
import com.example.student.service.CourseStatusService.CourseStatus;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@RestController
public class QuizController {

    public record QuestionView(Long id, String questionText,
                               String optionA, String optionB, String optionC, String optionD) {}

    public record ReviewItem(Long questionId, String questionText,
                             String yourAnswer, String correctAnswer, boolean correct) {}

    public record SubmitResponse(int score, int total, int percent,
                                 boolean passed, List<ReviewItem> review) {}

    private final UserRepository userRepository;
    private final CourseRepository courseRepository;
    private final EnrollmentRepository enrollmentRepository;
    private final QuestionRepository questionRepository;
    private final QuizResultRepository resultRepository;
    private final CourseStatusService statusService;

    public QuizController(UserRepository userRepository,
                          CourseRepository courseRepository,
                          EnrollmentRepository enrollmentRepository,
                          QuestionRepository questionRepository,
                          QuizResultRepository resultRepository,
                          CourseStatusService statusService) {
        this.userRepository = userRepository;
        this.courseRepository = courseRepository;
        this.enrollmentRepository = enrollmentRepository;
        this.questionRepository = questionRepository;
        this.resultRepository = resultRepository;
        this.statusService = statusService;
    }

    // ---------- Student ----------

    @GetMapping("/api/quiz/{courseId}")
    public ResponseEntity<?> getQuiz(@PathVariable Long courseId, Authentication authentication) {
        User user = userRepository.findByEmail(authentication.getName()).orElseThrow();
        if (!enrollmentRepository.existsByUserIdAndCourseId(user.getId(), courseId)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Enroll in this course first");
        }
        List<QuestionView> questions = questionRepository.findByCourseIdOrderByIdAsc(courseId).stream()
                .map(q -> new QuestionView(q.getId(), q.getQuestionText(),
                        q.getOptionA(), q.getOptionB(), q.getOptionC(), q.getOptionD()))
                .toList();
        return ResponseEntity.ok(questions);
    }

    @PostMapping("/api/quiz/{courseId}/submit")
    public ResponseEntity<?> submit(@PathVariable Long courseId,
                                    @RequestBody Map<Long, String> answers,
                                    Authentication authentication) {
        User user = userRepository.findByEmail(authentication.getName()).orElseThrow();
        if (!enrollmentRepository.existsByUserIdAndCourseId(user.getId(), courseId)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Enroll in this course first");
        }

        List<Question> questions = questionRepository.findByCourseIdOrderByIdAsc(courseId);
        if (questions.isEmpty()) {
            return ResponseEntity.badRequest().body("This course has no quiz yet");
        }

        int score = 0;
        List<ReviewItem> review = new ArrayList<>();
        for (Question question : questions) {
            String yourAnswer = answers.get(question.getId());
            boolean correct = question.getCorrectOption().equals(yourAnswer);
            if (correct) {
                score++;
            }
            review.add(new ReviewItem(question.getId(), question.getQuestionText(),
                    yourAnswer, question.getCorrectOption(), correct));
        }

        int total = questions.size();
        int percent = score * 100 / total;
        boolean passed = percent >= CourseStatusService.PASS_PERCENT;

        QuizResult result = new QuizResult();
        result.setUserId(user.getId());
        result.setCourseId(courseId);
        result.setScore(score);
        result.setTotal(total);
        result.setPercent(percent);
        result.setPassed(passed);
        resultRepository.save(result);

        return ResponseEntity.ok(new SubmitResponse(score, total, percent, passed, review));
    }

    @GetMapping("/api/quiz/{courseId}/status")
    public CourseStatus status(@PathVariable Long courseId, Authentication authentication) {
        User user = userRepository.findByEmail(authentication.getName()).orElseThrow();
        return statusService.getStatus(user.getId(), courseId);
    }

    // ---------- Admin ----------

    @GetMapping("/api/admin/courses/{courseId}/questions")
    public List<Question> adminQuestions(@PathVariable Long courseId) {
        return questionRepository.findByCourseIdOrderByIdAsc(courseId);
    }

    @PostMapping("/api/admin/courses/{courseId}/questions")
    public ResponseEntity<?> addQuestion(@PathVariable Long courseId, @RequestBody Question question) {
        if (!courseRepository.existsById(courseId)) {
            return ResponseEntity.notFound().build();
        }
        if (isBlank(question.getQuestionText()) || isBlank(question.getOptionA())
                || isBlank(question.getOptionB()) || isBlank(question.getOptionC())
                || isBlank(question.getOptionD()) || isBlank(question.getCorrectOption())) {
            return ResponseEntity.badRequest().body("All fields are required");
        }
        String correct = question.getCorrectOption().trim().toUpperCase();
        if (!List.of("A", "B", "C", "D").contains(correct)) {
            return ResponseEntity.badRequest().body("Correct option must be A, B, C or D");
        }
        question.setId(null);
        question.setCourseId(courseId);
        question.setCorrectOption(correct);
        return ResponseEntity.ok(questionRepository.save(question));
    }

    @DeleteMapping("/api/admin/questions/{id}")
    @Transactional
    public ResponseEntity<Void> deleteQuestion(@PathVariable Long id) {
        if (!questionRepository.existsById(id)) {
            return ResponseEntity.notFound().build();
        }
        questionRepository.deleteById(id);
        return ResponseEntity.noContent().build();
    }

    private boolean isBlank(String value) {
        return value == null || value.isBlank();
    }
}