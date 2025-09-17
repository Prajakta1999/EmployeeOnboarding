package com.elearning.projects.elearn.service;

import com.elearning.projects.elearn.dto.*;
import com.elearning.projects.elearn.entity.*;
import com.elearning.projects.elearn.exception.OperationFailedException;
import com.elearning.projects.elearn.exception.ResourceNotFoundException;
import com.elearning.projects.elearn.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class DocumentServiceImpl implements DocumentService {
    
    private final DocumentRepository documentRepository;
    private final EmployeeRepository employeeRepository;
    private final UserRepository userRepository;
    // INJECT THE TASK REPOSITORY TO UPDATE THE TASK STATUS
    private final OnboardingTaskRepository taskRepository;

    @Override
    public SuccessResponse submitDocuments(Long userId, DocumentSubmissionRequest request) {
        Employee employee = employeeRepository.findByUserId(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Employee not found for user ID: " + userId));
        
        // Submit ID Proof
        submitOrUpdateDocument(employee, DocumentType.ID_PROOF, request.idProofUrl());
        
        // Submit PAN/Aadhar if provided
        if (request.panAadharUrl() != null && !request.panAadharUrl().trim().isEmpty()) {
            submitOrUpdateDocument(employee, DocumentType.PAN_AADHAR, request.panAadharUrl());
        }
        
        // Submit Bank Details if provided
        if (request.bankDetailsUrl() != null && !request.bankDetailsUrl().trim().isEmpty()) {
            submitOrUpdateDocument(employee, DocumentType.BANK_DETAILS, request.bankDetailsUrl());
        }
        
        // Submit Offer Letter
        submitOrUpdateDocument(employee, DocumentType.OFFER_LETTER, request.offerLetterUrl());
        
        return new SuccessResponse("Documents submitted successfully", LocalDateTime.now());
    }

    @Override
    public SuccessResponse updateDocument(Long userId, Long documentId, String documentUrl) {
        Employee employee = employeeRepository.findByUserId(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Employee not found for user ID: " + userId));
        
        Document document = documentRepository.findById(documentId)
                .orElseThrow(() -> new ResourceNotFoundException("Document not found with ID: " + documentId));
        
        // Verify document belongs to the employee
        if (!document.getEmployee().getId().equals(employee.getId())) {
            throw new OperationFailedException("Document does not belong to this employee");
        }
        
        // Only allow updates for rejected or pending documents
        if (document.getStatus() == DocumentStatus.APPROVED) {
            throw new OperationFailedException("Cannot update approved document");
        }
        
        document.setDocumentUrl(documentUrl);
        document.setStatus(DocumentStatus.PENDING_REVIEW);
        document.setReviewComments(null);
        document.setReviewedBy(null);
        document.setReviewedAt(null);
        
        documentRepository.save(document);
        
        return new SuccessResponse("Document updated successfully", LocalDateTime.now());
    }

    @Override
    public List<DocumentDetailResponse> getEmployeeDocuments(Long userId) {
        Employee employee = employeeRepository.findByUserId(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Employee not found for user ID: " + userId));
        
        return documentRepository.findByEmployee(employee).stream()
                .map(this::mapToDocumentResponse)
                .collect(Collectors.toList());
    }

    @Override
    public List<DocumentDetailResponse> getDocumentsForReview(Long employeeId) {
        Employee employee = employeeRepository.findById(employeeId)
                .orElseThrow(() -> new ResourceNotFoundException("Employee not found with ID: " + employeeId));
        
        return documentRepository.findByEmployee(employee).stream()
                .map(this::mapToDocumentResponse)
                .collect(Collectors.toList());
    }

    @Override
    public SuccessResponse reviewDocument(Long documentId, DocumentReviewRequest request, Long reviewerId) {
        Document document = documentRepository.findById(documentId)
                .orElseThrow(() -> new ResourceNotFoundException("Document not found with ID: " + documentId));
        
        User reviewer = userRepository.findById(reviewerId)
                .orElseThrow(() -> new ResourceNotFoundException("Reviewer not found with ID: " + reviewerId));
        
        document.setStatus(request.status());
        document.setReviewComments(request.comments());
        document.setReviewedBy(reviewer);
        document.setReviewedAt(LocalDateTime.now());
        
        documentRepository.save(document);
        
        // --- START: NEW LOGIC TO AUTO-COMPLETE THE DOCUMENT SUBMISSION TASK ---
        
        // 1. Only check for task completion if the document was approved
        if (request.status() == DocumentStatus.APPROVED) {
            Employee employee = document.getEmployee();
            List<Document> allEmployeeDocs = documentRepository.findByEmployee(employee);
            
            // 2. Check if all MANDATORY documents are now approved
            boolean allMandatoryDocsApproved = allEmployeeDocs.stream()
                    .filter(doc -> doc.getDocumentType().isMandatory())
                    .allMatch(doc -> doc.getStatus() == DocumentStatus.APPROVED);
            
            if (allMandatoryDocsApproved) {
                // 3. Find the associated "DOCUMENT_SUBMISSION" task
                Optional<OnboardingTask> taskOptional = taskRepository.findByEmployeeAndTaskType(employee, TaskType.DOCUMENT_SUBMISSION);
                
                if (taskOptional.isPresent()) {
                    OnboardingTask docTask = taskOptional.get();
                    // 4. If the task is pending, mark it as completed
                    if (docTask.getStatus() == TaskStatus.PENDING) {
                        docTask.setStatus(TaskStatus.COMPLETED);
                        docTask.setCompletedAt(LocalDateTime.now());
                        docTask.setNotes("Automatically completed after all mandatory documents were approved by HR.");
                        taskRepository.save(docTask);
                    }
                }
            }
        }
        // --- END: NEW LOGIC ---

        String action = request.status() == DocumentStatus.APPROVED ? "approved" : "rejected";
        return new SuccessResponse("Document " + action + " successfully", LocalDateTime.now());
    }

    // Helper methods
    private void submitOrUpdateDocument(Employee employee, DocumentType documentType, String documentUrl) {
        Document existingDoc = documentRepository.findByEmployeeAndDocumentType(employee, documentType)
                .orElse(null);
        
        if (existingDoc != null) {
            // Update existing document
            existingDoc.setDocumentUrl(documentUrl);
            existingDoc.setStatus(DocumentStatus.PENDING_REVIEW);
            existingDoc.setReviewComments(null);
            existingDoc.setReviewedBy(null);
            existingDoc.setReviewedAt(null);
            documentRepository.save(existingDoc);
        } else {
            // Create new document
            Document newDoc = new Document(employee, documentType, documentUrl);
            documentRepository.save(newDoc);
        }
    }

    private DocumentDetailResponse mapToDocumentResponse(Document document) {
        return new DocumentDetailResponse(
                document.getId(),
                document.getDocumentType(),
                document.getDocumentType().getDisplayName(),
                document.getDocumentUrl(),
                document.getStatus(),
                document.getDocumentType().isMandatory(),
                document.getReviewComments(),
                document.getReviewedBy() != null ? document.getReviewedBy().getName() : null,
                document.getReviewedAt(),
                document.getCreatedAt()
        );
    }
}