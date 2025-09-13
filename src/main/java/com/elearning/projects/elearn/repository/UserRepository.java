package com.elearning.projects.elearn.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.elearning.projects.elearn.entity.User;
import com.elearning.projects.elearn.entity.enums.Role;

import java.util.List;
import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByEmail(String email);
    
    @Query(value = """
            SELECT u.* 
            FROM app_user u
            JOIN user_roles r ON u.id = r.user_id
            WHERE r.roles = 'STUDENT'
            """, nativeQuery = true)    List<User> findByRole(@Param("role") Role role);
    
  //  List<User> findByRolesContaining(Role role);



}
