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

import java.util.List;

@RestController
@RequestMapping("/modules")
@RequiredArgsConstructor
public class ModuleController {

    private final ModuleService moduleService;

    // Instructor endpoints

    @PostMapping
    @PreAuthorize("hasRole('INSTRUCTOR')")
    public ResponseEntity<ModuleResponseDto> createModule(
            @Valid @RequestBody CreateModuleRequestDto requestDto,
            @AuthenticationPrincipal User currentUser) {
        ModuleResponseDto response = moduleService.createModule(requestDto, currentUser.getId());
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    @PutMapping("/{moduleId}")
    @PreAuthorize("hasRole('INSTRUCTOR')")
    public ResponseEntity<ModuleResponseDto> updateModule(
            @PathVariable Long moduleId,
            @Valid @RequestBody UpdateModuleRequestDto requestDto,
            @AuthenticationPrincipal User currentUser) {
        ModuleResponseDto response = moduleService.updateModule(moduleId, requestDto, currentUser.getId());
        return ResponseEntity.ok(response);
    }

    @PatchMapping("/{moduleId}/publish")
    @PreAuthorize("hasRole('INSTRUCTOR')")
    public ResponseEntity<ModuleResponseDto> publishModule(
            @PathVariable Long moduleId,
            @AuthenticationPrincipal User currentUser) {
        ModuleResponseDto response = moduleService.publishModule(moduleId, currentUser.getId());
        return ResponseEntity.ok(response);
    }

    @PatchMapping("/{moduleId}/unpublish")
    @PreAuthorize("hasRole('INSTRUCTOR')")
    public ResponseEntity<ModuleResponseDto> unpublishModule(
            @PathVariable Long moduleId,
            @AuthenticationPrincipal User currentUser) {
        ModuleResponseDto response = moduleService.unpublishModule(moduleId, currentUser.getId());
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{moduleId}")
    @PreAuthorize("hasRole('INSTRUCTOR')")
    public ResponseEntity<String> deleteModule(
            @PathVariable Long moduleId,
            @AuthenticationPrincipal User currentUser) {
        moduleService.deleteModule(moduleId, currentUser.getId());
        return ResponseEntity.ok("Module deleted successfully");
    }

    @GetMapping("/instructor/my-modules")
    @PreAuthorize("hasRole('INSTRUCTOR')")
    public ResponseEntity<List<ModuleResponseDto>> getInstructorModules(
            @RequestParam(required = false) Long courseId,
            @AuthenticationPrincipal User currentUser) {
        List<ModuleResponseDto> modules = moduleService.getInstructorModules(currentUser.getId(), courseId);
        return ResponseEntity.ok(modules);
    }

    @GetMapping("/instructor/{moduleId}")
    @PreAuthorize("hasRole('INSTRUCTOR')")
    public ResponseEntity<ModuleResponseDto> getInstructorModuleById(
            @PathVariable Long moduleId,
            @AuthenticationPrincipal User currentUser) {
        ModuleResponseDto module = moduleService.getInstructorModuleById(moduleId, currentUser.getId());
        return ResponseEntity.ok(module);
    }

    // Student endpoints

    @GetMapping("/student/available")
    @PreAuthorize("hasRole('STUDENT')")
    public ResponseEntity<List<ModuleResponseDto>> getAvailableModules(
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

        List<ModuleResponseDto> modules = moduleService.getPublishedModulesForStudent(currentUser.getId(), searchDto);
        return ResponseEntity.ok(modules);
    }

    @GetMapping("/student/{moduleId}")
    @PreAuthorize("hasRole('STUDENT')")
    public ResponseEntity<ModuleResponseDto> getModuleById(
            @PathVariable Long moduleId,
            @AuthenticationPrincipal User currentUser) {
        ModuleResponseDto module = moduleService.getModuleById(moduleId, currentUser.getId());
        return ResponseEntity.ok(module);
    }
}
