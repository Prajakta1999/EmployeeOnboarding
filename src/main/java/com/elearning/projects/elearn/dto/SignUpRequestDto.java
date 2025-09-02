package com.elearning.projects.elearn.dto;

import lombok.Data;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Email;
import com.elearning.projects.elearn.entity.enums.Role;

@Data
public class SignUpRequestDto {
    
    @NotBlank(message = "Name is required")
    private String name;
    
    @Email(message = "Please provide a valid email")
    @NotBlank(message = "Email is required")
    private String email;
    
    @NotBlank(message = "Phone number is required")
    private String phoneNumber; // Add this field as per requirements
    
    @NotBlank(message = "Password is required")
    private String password;
    
    @NotNull(message = "Role is required")
    private Role role; // Add role selection - INSTRUCTOR or STUDENT
}
