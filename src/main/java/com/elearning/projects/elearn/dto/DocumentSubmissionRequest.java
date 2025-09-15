package com.elearning.projects.elearn.dto;

import jakarta.validation.constraints.*;

public record DocumentSubmissionRequest(
    @NotBlank(message = "ID proof URL is required")
    String idProofUrl,
    
    String panAadharUrl,
    
    String bankDetailsUrl,
    
    @NotBlank(message = "Offer letter URL is required")
    String offerLetterUrl
) {}
