package com.elearning.projects.elearn.dto;

import lombok.Data;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import com.elearning.projects.elearn.entity.enums.ContentType;

@Data
public class CreateModuleRequestDto {
    
    @NotNull(message = "Course ID is required")
    private Long courseId;
    
    @NotBlank(message = "Module title is required")
    private String title;
    
    @NotBlank(message = "Module description is required")
    private String description;
    
    @NotNull(message = "Content type is required")
    private ContentType contentType;
    
    @NotBlank(message = "Content URL is required")
    private String contentUrl;
}
