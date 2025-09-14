package com.elearning.projects.elearn.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import com.elearning.projects.elearn.advice.ApiResponse;
import com.elearning.projects.elearn.dto.*;
import com.elearning.projects.elearn.entity.User;
import com.elearning.projects.elearn.service.CourseService;

import java.util.List;

@RestController
@RequestMapping("/courses")
@RequiredArgsConstructor
public class TaskController {

    private final CourseService courseService;

    // Instructor endpoints
    @PostMapping
    @PreAuthorize("hasRole('HR')")
    public ResponseEntity<TaskResponseDto> createCourse(
            @Valid @RequestBody CreateTaskRequestDto requestDto,
            @AuthenticationPrincipal User currentUser) {
        TaskResponseDto response = courseService.createCourse(requestDto, currentUser.getId());
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    @PutMapping("/{courseId}")
    @PreAuthorize("hasRole('HR')")
    public ResponseEntity<TaskResponseDto> updateCourse(
            @PathVariable Long courseId,
            @Valid @RequestBody UpdateCourseRequestDto requestDto,
            @AuthenticationPrincipal User currentUser) {
        TaskResponseDto response = courseService.updateCourse(courseId, requestDto, currentUser.getId());
        return ResponseEntity.ok(response);
    }

    // @DeleteMapping("/{courseId}")
    // @PreAuthorize("hasRole('INSTRUCTOR')")
    // public ResponseEntity<String> deleteCourse(
    //         @PathVariable Long courseId,
    //         @AuthenticationPrincipal User currentUser) {
    //     courseService.deleteCourse(courseId, currentUser.getId());
    //     return ResponseEntity.ok("Course deleted successfully");
    // }

    @DeleteMapping("/{courseId}")
@PreAuthorize("hasRole('HR')")
public ResponseEntity<ApiResponse<String>> deleteCourse(
        @PathVariable Long courseId,
        @AuthenticationPrincipal User currentUser) {
    
    courseService.deleteCourse(courseId, currentUser.getId());
    
    // Build the standard response object directly
    ApiResponse<String> response = new ApiResponse<>("Course deleted successfully");
    
    return ResponseEntity.ok(response);
}

    @GetMapping("/instructor/my-courses")
    @PreAuthorize("hasRole('HR')")
    public ResponseEntity<List<TaskResponseDto>> getInstructorCourses(
            @AuthenticationPrincipal User currentUser) {
        List<TaskResponseDto> courses = courseService.getInstructorCourses(currentUser.getId());
        return ResponseEntity.ok(courses);
    }

    @GetMapping("/instructor/{courseId}")
    @PreAuthorize("hasRole('HR')")
    public ResponseEntity<TaskResponseDto> getCourseById(
            @PathVariable Long courseId,
            @AuthenticationPrincipal User currentUser) {
        TaskResponseDto course = courseService.getCourseById(courseId, currentUser.getId());
        return ResponseEntity.ok(course);
    }

    // Student endpoints
    @GetMapping("/available")
    @PreAuthorize("hasRole('EMPLOYEE')")
    public ResponseEntity<List<TaskResponseDto>> getAvailableCourses() {
        List<TaskResponseDto> courses = courseService.getAvailableCourses();
        return ResponseEntity.ok(courses);
    }

    // @PostMapping("/{courseId}/enroll")
    // @PreAuthorize("hasRole('STUDENT')")
    // public ResponseEntity<String> enrollInCourse(
    //         @PathVariable Long courseId,
    //         @AuthenticationPrincipal User currentUser) {
    //     courseService.enrollStudent(courseId, currentUser.getId());
    //     return ResponseEntity.ok("Successfully enrolled in course");
    // }


@PostMapping("/{courseId}/enroll")
@PreAuthorize("hasRole('EMPLOYEE')")
public ResponseEntity<ApiResponse<String>> enrollInCourse(
        @PathVariable Long courseId,
        @AuthenticationPrincipal User currentUser) {
        
    courseService.enrollStudent(courseId, currentUser.getId());
    
    // Build the standard response object directly
    ApiResponse<String> response = new ApiResponse<>("Successfully enrolled in course");
    
    return ResponseEntity.ok(response);
}

    @GetMapping("/student/enrolled")
    @PreAuthorize("hasRole('EMPLOYEE')")
    public ResponseEntity<List<TaskResponseDto>> getEnrolledCourses(
            @AuthenticationPrincipal User currentUser) {
        List<TaskResponseDto> courses = courseService.getStudentEnrolledCourses(currentUser.getId());
        return ResponseEntity.ok(courses);
    }
}
