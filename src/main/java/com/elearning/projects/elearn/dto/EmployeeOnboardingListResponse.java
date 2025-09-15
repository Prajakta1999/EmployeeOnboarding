// 2. EmployeeOnboardingListResponse.java
package com.elearning.projects.elearn.dto;

import java.util.List;

public record EmployeeOnboardingListResponse(
    List<EmployeeOnboardingSummaryResponse> employees,
    int totalCount,
    int currentPage,
    int totalPages
) {}
