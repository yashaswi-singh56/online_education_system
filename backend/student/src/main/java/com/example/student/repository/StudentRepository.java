package com.example.student.repository;
import java.util.List;
import com.example.student.model.Student;
import org.springframework.data.jpa.repository.JpaRepository;

public interface StudentRepository extends JpaRepository<Student, Long> {
        List<Student> findByNameContainingIgnoreCase(String name);
}