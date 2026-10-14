package com.example.student.repository;

import com.example.student.model.Lesson;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface LessonRepository extends JpaRepository<Lesson, Long> {
    List<Lesson> findByCourseIdOrderByIdAsc(Long courseId);
    void deleteByCourseId(Long courseId);
}