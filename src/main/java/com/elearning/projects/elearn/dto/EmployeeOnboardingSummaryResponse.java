package com.elearning.projects.elearn.dto;

import com.elearning.projects.elearn.entity.OnboardingStatus;
import java.time.LocalDate;

public record EmployeeOnboardingSummaryResponse(
    Long employeeId,
    String employeeName,
    String email,
    String phoneNumber,  // Updated field name
    String department,
    String designation,
    String employeeIdNumber,
    OnboardingStatus status,
    LocalDate joiningDate,
    int completionPercentage,
    int completedTasks,
    int totalTasks,
    int approvedDocuments,
    int totalDocuments
) {}
