package com.elearning.projects.elearn.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.elearning.projects.elearn.entity.User;
import com.elearning.projects.elearn.entity.enums.Role;
import com.elearning.projects.elearn.repository.UserRepository;
import com.elearning.projects.elearn.service.StudentService;
@RestController
@RequestMapping("/students")

public class StudentController {

    private final UserRepository userRepository;

    public StudentController(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @GetMapping
    @PreAuthorize("hasRole('HR')")

    public ResponseEntity<List<User>> getAllStudents() {
        return ResponseEntity.ok(userRepository.findByRole(Role.EMPLOYEE));
    }
}
