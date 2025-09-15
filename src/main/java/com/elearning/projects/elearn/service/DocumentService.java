package com.elearning.projects.elearn.service;

import com.elearning.projects.elearn.dto.*;
import java.util.List;

public interface DocumentService {
    
    // Employee operations
    SuccessResponse submitDocuments(Long userId, DocumentSubmissionRequest request);
    SuccessResponse updateDocument(Long userId, Long documentId, String documentUrl);
    List<DocumentDetailResponse> getEmployeeDocuments(Long userId);
    
    // HR operations
    List<DocumentDetailResponse> getDocumentsForReview(Long employeeId);
    SuccessResponse reviewDocument(Long documentId, DocumentReviewRequest request, Long reviewerId);
}
