package com.elearning.projects.elearn.dto;

import lombok.Data;

@Data
public class CompletionReportDto {
    private Long moduleId;
    private String moduleTitle;
    private Long totalStudents;
    private Long completedStudents;
    private Double completionPercentage;
}
