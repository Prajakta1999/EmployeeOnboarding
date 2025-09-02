package com.elearning.projects.elearn.dto;

import lombok.Data;
import jakarta.validation.constraints.NotBlank;

@Data
public class CreateCourseRequestDto {
    @NotBlank(message = "Course name is required")
    private String name;
    
    private String description;
}
