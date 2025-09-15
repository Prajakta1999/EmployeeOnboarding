package com.elearning.projects.elearn.dto;

import jakarta.validation.constraints.*;
import java.time.LocalDate;

public record AddEmployeeOnboardingRequest(
    @NotNull(message = "User ID is required")
    Long userId,
    
    @NotBlank(message = "Department is required")
    String department,
    
    @NotBlank(message = "Designation is required")
    String designation,
    
    @NotNull(message = "Joining date is required")
    LocalDate joiningDate,
    
    @NotBlank(message = "Employee ID is required")
    String employeeId
) {}
