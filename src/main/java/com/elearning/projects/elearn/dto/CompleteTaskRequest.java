package com.elearning.projects.elearn.dto;

import com.elearning.projects.elearn.entity.TaskType;
import jakarta.validation.constraints.*;

public record CompleteTaskRequest(
    @NotNull(message = "Task type is required")
    TaskType taskType,
    
    String notes
) {}
