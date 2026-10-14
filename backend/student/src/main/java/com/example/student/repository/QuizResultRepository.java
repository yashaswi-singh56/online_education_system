package com.example.student.repository;

import com.example.student.model.QuizResult;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface QuizResultRepository extends JpaRepository<QuizResult, Long> {
    List<QuizResult> findByUserIdAndCourseId(Long userId, Long courseId);
    void deleteByUserId(Long userId);
    void deleteByCourseId(Long courseId);
}