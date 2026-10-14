package com.example.student.repository;

import com.example.student.model.LessonCompletion;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface LessonCompletionRepository extends JpaRepository<LessonCompletion, Long> {
    List<LessonCompletion> findByUserId(Long userId);
    boolean existsByUserIdAndLessonId(Long userId, Long lessonId);
    void deleteByUserIdAndLessonId(Long userId, Long lessonId);
    void deleteByUserId(Long userId);
    void deleteByLessonId(Long lessonId);
}