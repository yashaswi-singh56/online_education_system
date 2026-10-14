package com.example.student.service;

import com.example.student.model.Student;
import com.example.student.repository.StudentRepository;
import org.springframework.stereotype.Service;
import java.util.Optional;
import java.util.List;

@Service
public class StudentService {

    private final StudentRepository studentRepository;

    public StudentService(StudentRepository studentRepository) {
        this.studentRepository = studentRepository;
    }

    public List<Student> getAllStudents() {
        return studentRepository.findAll();
    }
        public Student saveStudent(Student student) {
        return studentRepository.save(student);
    }
        public Optional<Student> getStudentById(Long id) {
        return studentRepository.findById(id);
    }
        public Optional<Student> updateStudent(Long id, Student updated) {
        return studentRepository.findById(id).map(existing -> {
            existing.setName(updated.getName());
            existing.setEmail(updated.getEmail());
            existing.setCourse(updated.getCourse());
            return studentRepository.save(existing);
        });
    }

    public boolean deleteStudent(Long id) {
        if (!studentRepository.existsById(id)) {
            return false;
        }
        studentRepository.deleteById(id);
        return true;
    }

    public List<Student> searchStudents(String name) {
        return studentRepository.findByNameContainingIgnoreCase(name);
    }
}