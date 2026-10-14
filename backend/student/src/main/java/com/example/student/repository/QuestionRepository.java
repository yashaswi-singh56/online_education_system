package com.example.student.repository;

import com.example.student.model.Question;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface QuestionRepository extends JpaRepository<Question, Long> {
    List<Question> findByCourseIdOrderByIdAsc(Long courseId);
    long countByCourseId(Long courseId);
    void deleteByCourseId(Long courseId);
}