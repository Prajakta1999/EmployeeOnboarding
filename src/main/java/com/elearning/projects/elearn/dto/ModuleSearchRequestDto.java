package com.elearning.projects.elearn.dto;

import lombok.Data;
import com.elearning.projects.elearn.entity.enums.ContentType;

@Data
public class ModuleSearchRequestDto {
    private String courseName;
    private String moduleTitle;
    private ContentType contentType;
}
