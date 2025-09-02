package com.elearning.projects.elearn.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.elearning.projects.elearn.entity.User;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByEmail(String email);
}
