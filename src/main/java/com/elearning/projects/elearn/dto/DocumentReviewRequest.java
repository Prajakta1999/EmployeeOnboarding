package com.elearning.projects.elearn.dto;

import com.elearning.projects.elearn.entity.DocumentStatus;
import jakarta.validation.constraints.*;

public record DocumentReviewRequest(
    @NotNull(message = "Document status is required")
    DocumentStatus status,
    
    String comments
) {}
