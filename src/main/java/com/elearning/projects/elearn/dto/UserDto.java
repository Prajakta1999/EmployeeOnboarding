package com.elearning.projects.elearn.dto;

import java.util.Set;

import com.elearning.projects.elearn.entity.enums.Role;

import lombok.Data;

@Data
public class UserDto {
    private Long id;
    private String email;
    private String name;
    private String phoneNumber;
    private Set<Role> roles; // Show user roles in response
}
