// 4. SuccessResponse.java
package com.elearning.projects.elearn.dto;

import java.time.LocalDateTime;

public record SuccessResponse(
    String message,
    LocalDateTime timestamp
) {}
