package com.elearning.projects.elearn.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import com.elearning.projects.elearn.dto.*;
import com.elearning.projects.elearn.entity.User;
import com.elearning.projects.elearn.service.ModuleService;
import com.elearning.projects.elearn.advice.ApiResponse;


import java.util.List;

@RestController
@RequestMapping("/modules")
@RequiredArgsConstructor
public class OnboardingModuleController {

    private final ModuleService moduleService;

    // Instructor endpoints

    @PostMapping
    @PreAuthorize("hasRole('HR')")
    public ResponseEntity<OnboardingModuleResponseDto> createModule(
            @Valid @RequestBody CreateModuleRequestDto requestDto,
            @AuthenticationPrincipal User currentUser) {
        OnboardingModuleResponseDto response = moduleService.createModule(requestDto, currentUser.getId());
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    @PutMapping("/{moduleId}")
    @PreAuthorize("hasRole('HR')")
    public ResponseEntity<OnboardingModuleResponseDto> updateModule(
            @PathVariable Long moduleId,
            @Valid @RequestBody UpdateModuleRequestDto requestDto,
            @AuthenticationPrincipal User currentUser) {
        OnboardingModuleResponseDto response = moduleService.updateModule(moduleId, requestDto, currentUser.getId());
        return ResponseEntity.ok(response);
    }

    @PatchMapping("/{moduleId}/publish")
    @PreAuthorize("hasRole('HR')")
    public ResponseEntity<OnboardingModuleResponseDto> publishModule(
            @PathVariable Long moduleId,
            @AuthenticationPrincipal User currentUser) {
        OnboardingModuleResponseDto response = moduleService.publishModule(moduleId, currentUser.getId());
        return ResponseEntity.ok(response);
    }

    @PatchMapping("/{moduleId}/unpublish")
    @PreAuthorize("hasRole('HR')")
    public ResponseEntity<OnboardingModuleResponseDto> unpublishModule(
            @PathVariable Long moduleId,
            @AuthenticationPrincipal User currentUser) {
        OnboardingModuleResponseDto response = moduleService.unpublishModule(moduleId, currentUser.getId());
        return ResponseEntity.ok(response);
    }

    // @DeleteMapping("/{moduleId}")
    // @PreAuthorize("hasRole('INSTRUCTOR')")
    // public ResponseEntity<String> deleteModule(
    //         @PathVariable Long moduleId,
    //         @AuthenticationPrincipal User currentUser) {
    //     moduleService.deleteModule(moduleId, currentUser.getId());
    //     return ResponseEntity.ok("Module deleted successfully");
    // }

    @DeleteMapping("/{moduleId}")
@PreAuthorize("hasRole('HR')")
public ResponseEntity<ApiResponse<String>> deleteModule(
        @PathVariable Long moduleId,
        @AuthenticationPrincipal User currentUser) {
    
    moduleService.deleteModule(moduleId, currentUser.getId());
    
    // Build the standard response object directly, just like you did for the Course controller
    ApiResponse<String> response = new ApiResponse<>("Module deleted successfully");
    
    return ResponseEntity.ok(response);
}


    @GetMapping("/instructor/my-modules")
    @PreAuthorize("hasRole('HR')")
    public ResponseEntity<List<OnboardingModuleResponseDto>> getInstructorModules(
            @RequestParam(required = false) Long courseId,
            @AuthenticationPrincipal User currentUser) {
        List<OnboardingModuleResponseDto> modules = moduleService.getInstructorModules(currentUser.getId(), courseId);
        return ResponseEntity.ok(modules);
    }

    @GetMapping("/instructor/{moduleId}")
    @PreAuthorize("hasRole('HR')")
    public ResponseEntity<OnboardingModuleResponseDto> getInstructorModuleById(
            @PathVariable Long moduleId,
            @AuthenticationPrincipal User currentUser) {
        OnboardingModuleResponseDto module = moduleService.getInstructorModuleById(moduleId, currentUser.getId());
        return ResponseEntity.ok(module);
    }

    // Student endpoints

    @GetMapping("/student/available")
    @PreAuthorize("hasRole('EMPLOYEE')")
    public ResponseEntity<List<OnboardingModuleResponseDto>> getAvailableModules(
            @RequestParam(required = false) String courseName,
            @RequestParam(required = false) String moduleTitle,
            @RequestParam(required = false) String contentType,
            @AuthenticationPrincipal User currentUser) {

        ModuleSearchRequestDto searchDto = new ModuleSearchRequestDto();
        searchDto.setCourseName(courseName);
        searchDto.setModuleTitle(moduleTitle);

        if (contentType != null && !contentType.trim().isEmpty()) {
            try {
                searchDto.setContentType(
                        com.elearning.projects.elearn.entity.enums.ContentType.valueOf(contentType.toUpperCase()));
            } catch (IllegalArgumentException e) {
                // Invalid content type, ignore filter
            }
        }

        List<OnboardingModuleResponseDto> modules = moduleService.getPublishedModulesForStudent(currentUser.getId(), searchDto);
        return ResponseEntity.ok(modules);
    }

    @GetMapping("/student/{moduleId}")
    @PreAuthorize("hasRole('EMPLOYEE')")
    public ResponseEntity<OnboardingModuleResponseDto> getModuleById(
            @PathVariable Long moduleId,
            @AuthenticationPrincipal User currentUser) {
        OnboardingModuleResponseDto module = moduleService.getModuleById(moduleId, currentUser.getId());
        return ResponseEntity.ok(module);
    }
}
