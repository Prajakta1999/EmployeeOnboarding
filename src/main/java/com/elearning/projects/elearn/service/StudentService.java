package com.elearning.projects.elearn.service;

import java.util.List;

import org.springframework.stereotype.Service;

import com.elearning.projects.elearn.entity.User;
import com.elearning.projects.elearn.repository.UserRepository;

@Service
public class StudentService {

    private final UserRepository userRepository;

    public StudentService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public List<User> getAllStudents() {
        // Either option works, pick one:
        return userRepository.findByRole(com.elearning.projects.elearn.entity.enums.Role.STUDENT);
        // return userRepository.findByRolesContaining(com.elearning.projects.elearn.entity.enums.Role.STUDENT);
    }
}
