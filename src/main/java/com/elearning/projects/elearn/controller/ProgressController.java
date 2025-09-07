package com.elearning.projects.elearn.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import com.elearning.projects.elearn.advice.ApiResponse;
import com.elearning.projects.elearn.dto.*;
import com.elearning.projects.elearn.entity.User;
import com.elearning.projects.elearn.service.ProgressService;

import java.util.List;

@RestController
@RequestMapping("/progress")
@RequiredArgsConstructor
public class ProgressController {

    private final ProgressService progressService;

    // Student endpoints

    // @PostMapping("/modules/{moduleId}/complete")
    // @PreAuthorize("hasRole('STUDENT')")
    // public ResponseEntity<String> markModuleCompleted(
    //         @PathVariable Long moduleId,
    //         @AuthenticationPrincipal User currentUser) {
    //     progressService.markModuleCompleted(moduleId, currentUser.getId());
    //     return ResponseEntity.ok("Module marked as completed successfully");
    // }

    // @PostMapping("/modules/{moduleId}/uncomplete")
    // @PreAuthorize("hasRole('STUDENT')")
    // public ResponseEntity<String> unmarkModuleCompleted(
    //         @PathVariable Long moduleId,
    //         @AuthenticationPrincipal User currentUser) {
    //     progressService.unmarkModuleCompleted(moduleId, currentUser.getId());
    //     return ResponseEntity.ok("Module completion unmarked successfully");
    // }


@PostMapping("/modules/{moduleId}/complete")
@PreAuthorize("hasRole('STUDENT')")
public ResponseEntity<ApiResponse<String>> markModuleCompleted(
        @PathVariable Long moduleId,
        @AuthenticationPrincipal User currentUser) {
        
    progressService.markModuleCompleted(moduleId, currentUser.getId());
    
    ApiResponse<String> response = new ApiResponse<>("Module marked as completed successfully");
    return ResponseEntity.ok(response);
}

@PostMapping("/modules/{moduleId}/uncomplete")
@PreAuthorize("hasRole('STUDENT')")
public ResponseEntity<ApiResponse<String>> unmarkModuleCompleted(
        @PathVariable Long moduleId,
        @AuthenticationPrincipal User currentUser) {
        
    progressService.unmarkModuleCompleted(moduleId, currentUser.getId());
    
    ApiResponse<String> response = new ApiResponse<>("Module completion unmarked successfully");
    return ResponseEntity.ok(response);
}

    @GetMapping("/student/my-progress")
    @PreAuthorize("hasRole('STUDENT')")
    public ResponseEntity<List<ProgressResponseDto>> getMyProgress(
            @AuthenticationPrincipal User currentUser) {
        List<ProgressResponseDto> progress = progressService.getStudentProgress(currentUser.getId());
        return ResponseEntity.ok(progress);
    }

    // Instructor endpoints

    @GetMapping("/instructor/completion-reports")
    @PreAuthorize("hasRole('INSTRUCTOR')")
    public ResponseEntity<List<CourseCompletionReportDto>> getCompletionReports(
            @AuthenticationPrincipal User currentUser) {
        List<CourseCompletionReportDto> reports = progressService.getInstructorCompletionReport(currentUser.getId());
        return ResponseEntity.ok(reports);
    }

    @GetMapping("/instructor/courses/{courseId}/completion-report")
    @PreAuthorize("hasRole('INSTRUCTOR')")
    public ResponseEntity<CourseCompletionReportDto> getCourseCompletionReport(
            @PathVariable Long courseId,
            @AuthenticationPrincipal User currentUser) {
        CourseCompletionReportDto report = progressService.getCourseCompletionReport(courseId, currentUser.getId());
        return ResponseEntity.ok(report);
    }
}
