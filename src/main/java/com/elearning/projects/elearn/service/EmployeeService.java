package com.elearning.projects.elearn.service;

import com.elearning.projects.elearn.dto.*;
import com.elearning.projects.elearn.entity.OnboardingStatus;
import java.util.List;

public interface EmployeeService {
    
    // HR operations
    SuccessResponse addEmployeeToOnboarding(AddEmployeeOnboardingRequest request);
    List<EmployeeOnboardingSummaryResponse> getAllOnboardingEmployees();
    List<EmployeeOnboardingSummaryResponse> getFilteredOnboardingEmployees(OnboardingFilterRequest filter);
    EmployeeOnboardingDetailResponse getEmployeeOnboardingDetails(Long employeeId);
    SuccessResponse completeEmployeeOnboarding(Long employeeId);
    HRDashboardResponse getHRDashboard();
    
    // Employee operations  
    EmployeeDashboardResponse getEmployeeDashboard(Long userId);
    List<TaskDetailResponse> getEmployeeTasks(Long userId);
    SuccessResponse completeTask(Long userId, CompleteTaskRequest request);
    
    // Common operations
    List<AvailableEmployeeResponse> getAvailableEmployeesForOnboarding();
    List<String> getAllDepartments();
}
