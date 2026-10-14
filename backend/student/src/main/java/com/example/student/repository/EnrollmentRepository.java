package com.example.student.repository;

import com.example.student.model.Enrollment;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface EnrollmentRepository extends JpaRepository<Enrollment, Long> {
    List<Enrollment> findByUserId(Long userId);
    boolean existsByUserIdAndCourseId(Long userId, Long courseId);
    void deleteByUserId(Long userId);
    void deleteByCourseId(Long courseId);
    void deleteByUserIdAndCourseId(Long userId, Long courseId);
}