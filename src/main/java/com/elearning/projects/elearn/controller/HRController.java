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
@RequestMapping("/hr")
@RequiredArgsConstructor
@PreAuthorize("hasRole('HR')")
public class HRController {
    
    private final EmployeeService employeeService;
    private final DocumentService documentService;

    // Employee Management Endpoints
    @GetMapping("/employees/available")
    public ResponseEntity<List<AvailableEmployeeResponse>> getAvailableEmployees() {
        List<AvailableEmployeeResponse> employees = employeeService.getAvailableEmployeesForOnboarding();
        return ResponseEntity.ok(employees);
    }

    @PostMapping("/onboarding")
    public ResponseEntity<SuccessResponse> addEmployeeToOnboarding(@Valid @RequestBody AddEmployeeOnboardingRequest request) {
        SuccessResponse response = employeeService.addEmployeeToOnboarding(request);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/onboarding")
    public ResponseEntity<List<EmployeeOnboardingSummaryResponse>> getAllOnboardingEmployees() {
        List<EmployeeOnboardingSummaryResponse> employees = employeeService.getAllOnboardingEmployees();
        return ResponseEntity.ok(employees);
    }

    @GetMapping("/onboarding/{employeeId}")
    public ResponseEntity<EmployeeOnboardingDetailResponse> getEmployeeOnboardingDetails(@PathVariable Long employeeId) {
        EmployeeOnboardingDetailResponse details = employeeService.getEmployeeOnboardingDetails(employeeId);
        return ResponseEntity.ok(details);
    }

    // Filtering Endpoints
    @PostMapping("/onboarding/filter")
    public ResponseEntity<List<EmployeeOnboardingSummaryResponse>> getFilteredOnboardingEmployees(
            @RequestBody OnboardingFilterRequest filter) {
        List<EmployeeOnboardingSummaryResponse> employees = employeeService.getFilteredOnboardingEmployees(filter);
        return ResponseEntity.ok(employees);
    }

    @GetMapping("/onboarding/filter")
    public ResponseEntity<List<EmployeeOnboardingSummaryResponse>> getFilteredOnboardingEmployeesQuery(
            @RequestParam(required = false) String department,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String joiningDateFrom,
            @RequestParam(required = false) String joiningDateTo) {
        
        OnboardingFilterRequest filter = new OnboardingFilterRequest(
                department,
                status != null ? com.elearning.projects.elearn.entity.OnboardingStatus.valueOf(status) : null,
                joiningDateFrom != null ? java.time.LocalDate.parse(joiningDateFrom) : null,
                joiningDateTo != null ? java.time.LocalDate.parse(joiningDateTo) : null
        );
        
        List<EmployeeOnboardingSummaryResponse> employees = employeeService.getFilteredOnboardingEmployees(filter);
        return ResponseEntity.ok(employees);
    }

    // Document Review Endpoints
    @GetMapping("/documents/{employeeId}")
    public ResponseEntity<List<DocumentDetailResponse>> getEmployeeDocumentsForReview(@PathVariable Long employeeId) {
        List<DocumentDetailResponse> documents = documentService.getDocumentsForReview(employeeId);
        return ResponseEntity.ok(documents);
    }

    @PutMapping("/documents/{documentId}/approve")
    public ResponseEntity<SuccessResponse> approveDocument(
            @PathVariable Long documentId,
            @RequestBody(required = false) DocumentReviewRequest request) {
        
        DocumentReviewRequest approveRequest = new DocumentReviewRequest(
                com.elearning.projects.elearn.entity.DocumentStatus.APPROVED,
                request != null ? request.comments() : "Approved"
        );
        
        SuccessResponse response = documentService.reviewDocument(documentId, approveRequest, getCurrentUserId());
        return ResponseEntity.ok(response);
    }

    @PutMapping("/documents/{documentId}/reject")
    public ResponseEntity<SuccessResponse> rejectDocument(
            @PathVariable Long documentId,
            @Valid @RequestBody DocumentReviewRequest request) {
        
        DocumentReviewRequest rejectRequest = new DocumentReviewRequest(
                com.elearning.projects.elearn.entity.DocumentStatus.REJECTED,
                request.comments()
        );
        
        SuccessResponse response = documentService.reviewDocument(documentId, rejectRequest, getCurrentUserId());
        return ResponseEntity.ok(response);
    }

    @PutMapping("/documents/{documentId}/review")
    public ResponseEntity<SuccessResponse> reviewDocument(
            @PathVariable Long documentId,
            @Valid @RequestBody DocumentReviewRequest request) {
        
        SuccessResponse response = documentService.reviewDocument(documentId, request, getCurrentUserId());
        return ResponseEntity.ok(response);
    }

    // Completion Endpoint
    @PutMapping("/onboarding/{employeeId}/complete")
    public ResponseEntity<SuccessResponse> completeOnboarding(@PathVariable Long employeeId) {
        SuccessResponse response = employeeService.completeEmployeeOnboarding(employeeId);
        return ResponseEntity.ok(response);
    }

    // Dashboard and Reports
    @GetMapping("/dashboard")
    public ResponseEntity<HRDashboardResponse> getHRDashboard() {
        HRDashboardResponse dashboard = employeeService.getHRDashboard();
        return ResponseEntity.ok(dashboard);
    }

    @GetMapping("/reports/completion")
    public ResponseEntity<HRDashboardResponse> getCompletionReport() {
        HRDashboardResponse report = employeeService.getHRDashboard();
        return ResponseEntity.ok(report);
    }

    // Utility Endpoints
    @GetMapping("/departments")
    public ResponseEntity<List<String>> getAllDepartments() {
        List<String> departments = employeeService.getAllDepartments();
        return ResponseEntity.ok(departments);
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
