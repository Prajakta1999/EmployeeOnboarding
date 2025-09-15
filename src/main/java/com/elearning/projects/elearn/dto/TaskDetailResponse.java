package com.elearning.projects.elearn.dto;

import com.elearning.projects.elearn.entity.TaskType;
import com.elearning.projects.elearn.entity.TaskStatus;
import java.time.LocalDateTime;

public record TaskDetailResponse(
    Long taskId,
    TaskType taskType,
    String taskName,
    String description,
    TaskStatus status,
    boolean mandatory,
    LocalDateTime completedAt,
    String notes
) {}
