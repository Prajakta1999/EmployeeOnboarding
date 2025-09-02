package com.elearning.projects.elearn.dto;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class CourseResponseDto {
    private Long id;
    private String name;
    private String description;
    private String instructorName;
    private Long instructorId;
    private Integer totalModules;
    private Integer publishedModules;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
