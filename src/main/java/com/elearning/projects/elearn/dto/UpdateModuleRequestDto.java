package com.elearning.projects.elearn.dto;

import lombok.Data;
import com.elearning.projects.elearn.entity.enums.ContentType;

@Data
public class UpdateModuleRequestDto {
    private String title;
    private String description;
    private String contentUrl;
    private ContentType contentType;
}
