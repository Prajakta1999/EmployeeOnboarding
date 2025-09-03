package com.elearning.projects.elearn.dto;

import lombok.Data;

@Data
public class ProgressResponseDto {
    private Long totalModulesCompleted;
    private Long totalModulesAvailable;
    private Double completionPercentage;
    private Long courseId;
    private String courseName;
    private String instructorName;
}
