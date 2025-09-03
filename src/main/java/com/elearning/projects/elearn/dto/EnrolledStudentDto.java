package com.elearning.projects.elearn.dto;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class EnrolledStudentDto {
    private Long studentId;
    private String studentName;
    private String studentEmail;
    private LocalDateTime enrolledAt;
    private Long totalModules;
    private Long completedModules;
    private Double progressPercentage;
}
