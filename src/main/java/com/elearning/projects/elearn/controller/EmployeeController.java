package com.elearning.projects.elearn.controller;

import com.elearning.projects.elearn.dto.*;
import com.elearning.projects.elearn.service.DocumentService;
import com.elearning.projects.elearn.service.EmployeeService;
import com.elearning.projects.elearn.entity.User;
import com.elearning.projects.elearn.exception.UnAuthorisedException;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/employee")
@RequiredArgsConstructor
@PreAuthorize("hasRole('EMPLOYEE')")
public class EmployeeController {
    
    private final EmployeeService employeeService;
    private final DocumentService documentService;

    // Dashboard
    @GetMapping("/dashboard")
    public ResponseEntity<EmployeeDashboardResponse> getEmployeeDashboard() {
        Long userId = getCurrentUserId();
        EmployeeDashboardResponse dashboard = employeeService.getEmployeeDashboard(userId);
        return ResponseEntity.ok(dashboard);
    }

    // Task Management
    @GetMapping("/onboarding/tasks")
    public ResponseEntity<List<TaskDetailResponse>> getOnboardingTasks() {
        Long userId = getCurrentUserId();
        List<TaskDetailResponse> tasks = employeeService.getEmployeeTasks(userId);
        return ResponseEntity.ok(tasks);
    }

    @GetMapping("/onboarding/progress")
    public ResponseEntity<EmployeeDashboardResponse> getOnboardingProgress() {
        Long userId = getCurrentUserId();
        EmployeeDashboardResponse progress = employeeService.getEmployeeDashboard(userId);
        return ResponseEntity.ok(progress);
    }

    @PutMapping("/onboarding/tasks/{taskId}/complete")
    public ResponseEntity<SuccessResponse> completeTask(
            @PathVariable Long taskId,
            @Valid @RequestBody CompleteTaskRequest request) {
        Long userId = getCurrentUserId();
        SuccessResponse response = employeeService.completeTask(userId, request);
        return ResponseEntity.ok(response);
    }

    // Task-Specific Endpoints
    @PutMapping("/tasks/policy-acknowledgment")
    public ResponseEntity<SuccessResponse> completePolicyAcknowledgment() {
        Long userId = getCurrentUserId();
        CompleteTaskRequest request = new CompleteTaskRequest(
                com.elearning.projects.elearn.entity.TaskType.POLICY_ACKNOWLEDGMENT,
                "Company policy acknowledged"
        );
        SuccessResponse response = employeeService.completeTask(userId, request);
        return ResponseEntity.ok(response);
    }

    @PutMapping("/tasks/orientation-confirmation")
    public ResponseEntity<SuccessResponse> confirmOrientationAttendance() {
        Long userId = getCurrentUserId();
        CompleteTaskRequest request = new CompleteTaskRequest(
                com.elearning.projects.elearn.entity.TaskType.ORIENTATION_SESSION,
                "Orientation session attended"
        );
        SuccessResponse response = employeeService.completeTask(userId, request);
        return ResponseEntity.ok(response);
    }

    @PutMapping("/tasks/documents-submission")
    public ResponseEntity<SuccessResponse> completeDocumentSubmission() {
        Long userId = getCurrentUserId();
        CompleteTaskRequest request = new CompleteTaskRequest(
                com.elearning.projects.elearn.entity.TaskType.DOCUMENT_SUBMISSION,
                "Documents submitted"
        );
        SuccessResponse response = employeeService.completeTask(userId, request);
        return ResponseEntity.ok(response);
    }

    // Document Management
    @GetMapping("/documents")
    public ResponseEntity<List<DocumentDetailResponse>> getMyDocuments() {
        Long userId = getCurrentUserId();
        List<DocumentDetailResponse> documents = documentService.getEmployeeDocuments(userId);
        return ResponseEntity.ok(documents);
    }

    @PutMapping("/documents")
    public ResponseEntity<SuccessResponse> submitDocuments(@Valid @RequestBody DocumentSubmissionRequest request) {
        Long userId = getCurrentUserId();
        SuccessResponse response = documentService.submitDocuments(userId, request);
        return ResponseEntity.ok(response);
    }

    @PutMapping("/documents/{documentId}")
    public ResponseEntity<SuccessResponse> updateDocument(
            @PathVariable Long documentId,
            @RequestBody String documentUrl) {
        Long userId = getCurrentUserId();
        SuccessResponse response = documentService.updateDocument(userId, documentId, documentUrl);
        return ResponseEntity.ok(response);
    }

    // Helper method to get current user ID from JWT
    private Long getCurrentUserId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        
        if (authentication != null && authentication.getPrincipal() instanceof User) {
            User user = (User) authentication.getPrincipal();
            return user.getId();
        }
        
        throw new UnAuthorisedException("No authenticated user found");
    }
}
