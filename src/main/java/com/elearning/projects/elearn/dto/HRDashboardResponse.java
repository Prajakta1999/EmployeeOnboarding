// 1. HRDashboardResponse.java
package com.elearning.projects.elearn.dto;

import java.util.List;

public record HRDashboardResponse(
    int totalEmployeesOnboarded,
    int employeesWithPendingTasks,
    int employeesWithPendingDocuments,
    int employeesInProgress,
    List<EmployeeOnboardingSummaryResponse> recentOnboardings
) {}
