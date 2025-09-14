package com.elearning.projects.elearn.dto;

import lombok.Data;
import com.elearning.projects.elearn.entity.enums.ContentType;
import java.time.LocalDateTime;

@Data
public class OnboardingModuleResponseDto {
    private Long id;
    private String title;
    private String description;
    private ContentType contentType;
    private String contentUrl;
    private Boolean isPublished;
    private String courseName;
    private Long courseId;
    private String instructorName;
    private Boolean isCompleted; // For student view
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
