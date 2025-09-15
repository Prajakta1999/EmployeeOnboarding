package com.elearning.projects.elearn.dto;

import com.elearning.projects.elearn.entity.OnboardingStatus;
import java.time.LocalDate;
import java.util.List;

public record EmployeeDashboardResponse(
    String employeeName,
    String email,
    String phoneNumber,  // Updated field name
    String department,
    String designation,
    String employeeIdNumber,
    OnboardingStatus status,
    LocalDate joiningDate,
    List<TaskDetailResponse> pendingTasks,
    List<TaskDetailResponse> completedTasks,
    List<DocumentDetailResponse> documents,
    int completionPercentage,
    String nextAction
) {}
