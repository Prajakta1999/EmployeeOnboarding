// 5. ErrorResponse.java
package com.elearning.projects.elearn.dto;

import java.time.LocalDateTime;

public record ErrorResponse(
    String error,
    String message,
    int statusCode,
    LocalDateTime timestamp,
    String path
) {}
