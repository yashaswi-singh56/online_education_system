package com.example.student.service;

import com.example.student.model.Lesson;
import com.example.student.model.LessonCompletion;
import com.example.student.model.QuizResult;
import com.example.student.repository.LessonCompletionRepository;
import com.example.student.repository.LessonRepository;
import com.example.student.repository.QuestionRepository;
import com.example.student.repository.QuizResultRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class CourseStatusService {

    public static final int PASS_PERCENT = 60;

    public record CourseStatus(int lessonsTotal, int lessonsDone, int questionCount,
                               int bestPercent, boolean quizPassed, boolean certificateReady) {}

    private final LessonRepository lessonRepository;
    private final LessonCompletionRepository completionRepository;
    private final QuestionRepository questionRepository;
    private final QuizResultRepository resultRepository;

    public CourseStatusService(LessonRepository lessonRepository,
                               LessonCompletionRepository completionRepository,
                               QuestionRepository questionRepository,
                               QuizResultRepository resultRepository) {
        this.lessonRepository = lessonRepository;
        this.completionRepository = completionRepository;
        this.questionRepository = questionRepository;
        this.resultRepository = resultRepository;
    }

    public CourseStatus getStatus(Long userId, Long courseId) {
        List<Lesson> lessons = lessonRepository.findByCourseIdOrderByIdAsc(courseId);
        Set<Long> doneIds = completionRepository.findByUserId(userId).stream()
                .map(LessonCompletion::getLessonId)
                .collect(Collectors.toSet());
        int lessonsDone = (int) lessons.stream()
                .filter(lesson -> doneIds.contains(lesson.getId()))
                .count();

        int questionCount = (int) questionRepository.countByCourseId(courseId);
        int bestPercent = resultRepository.findByUserIdAndCourseId(userId, courseId).stream()
                .mapToInt(QuizResult::getPercent)
                .max()
                .orElse(0);

        boolean quizPassed = questionCount > 0 && bestPercent >= PASS_PERCENT;
        boolean allLessonsDone = !lessons.isEmpty() && lessonsDone == lessons.size();

        return new CourseStatus(lessons.size(), lessonsDone, questionCount,
                bestPercent, quizPassed, allLessonsDone && quizPassed);
    }
}