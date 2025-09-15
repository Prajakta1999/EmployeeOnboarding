// 3. EmployeeOnboardingDetailResponse.java
package com.elearning.projects.elearn.dto;

import com.elearning.projects.elearn.entity.OnboardingStatus;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

public record EmployeeOnboardingDetailResponse(
    Long employeeId,
    String employeeName,
    String email,
    String phoneNumber,
    String department,
    String designation,
    String employeeIdNumber,
    OnboardingStatus status,
    LocalDate joiningDate,
    List<TaskDetailResponse> tasks,
    List<DocumentDetailResponse> documents,
    int completionPercentage,
    LocalDateTime lastUpdated
) {}
