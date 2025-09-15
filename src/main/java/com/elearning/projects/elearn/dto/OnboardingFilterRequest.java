package com.elearning.projects.elearn.dto;

import com.elearning.projects.elearn.entity.OnboardingStatus;
import java.time.LocalDate;

public record OnboardingFilterRequest(
    String department,
    OnboardingStatus status,
    LocalDate joiningDateFrom,
    LocalDate joiningDateTo
) {}
