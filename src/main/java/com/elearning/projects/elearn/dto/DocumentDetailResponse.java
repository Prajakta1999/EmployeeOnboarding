package com.elearning.projects.elearn.dto;

import com.elearning.projects.elearn.entity.DocumentType;
import com.elearning.projects.elearn.entity.DocumentStatus;
import java.time.LocalDateTime;

public record DocumentDetailResponse(
    Long documentId,
    DocumentType documentType,
    String documentName,
    String documentUrl,
    DocumentStatus status,
    boolean mandatory,
    String reviewComments,
    String reviewedByName,
    LocalDateTime reviewedAt,
    LocalDateTime submittedAt
) {}
