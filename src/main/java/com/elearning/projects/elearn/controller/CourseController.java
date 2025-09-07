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
public class CourseController {

    private final CourseService courseService;

    // Instructor endpoints
    @PostMapping
    @PreAuthorize("hasRole('INSTRUCTOR')")
    public ResponseEntity<CourseResponseDto> createCourse(
            @Valid @RequestBody CreateCourseRequestDto requestDto,
            @AuthenticationPrincipal User currentUser) {
        CourseResponseDto response = courseService.createCourse(requestDto, currentUser.getId());
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    @PutMapping("/{courseId}")
    @PreAuthorize("hasRole('INSTRUCTOR')")
    public ResponseEntity<CourseResponseDto> updateCourse(
            @PathVariable Long courseId,
            @Valid @RequestBody UpdateCourseRequestDto requestDto,
            @AuthenticationPrincipal User currentUser) {
        CourseResponseDto response = courseService.updateCourse(courseId, requestDto, currentUser.getId());
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
@PreAuthorize("hasRole('INSTRUCTOR')")
public ResponseEntity<ApiResponse<String>> deleteCourse(
        @PathVariable Long courseId,
        @AuthenticationPrincipal User currentUser) {
    
    courseService.deleteCourse(courseId, currentUser.getId());
    
    // Build the standard response object directly
    ApiResponse<String> response = new ApiResponse<>("Course deleted successfully");
    
    return ResponseEntity.ok(response);
}

    @GetMapping("/instructor/my-courses")
    @PreAuthorize("hasRole('INSTRUCTOR')")
    public ResponseEntity<List<CourseResponseDto>> getInstructorCourses(
            @AuthenticationPrincipal User currentUser) {
        List<CourseResponseDto> courses = courseService.getInstructorCourses(currentUser.getId());
        return ResponseEntity.ok(courses);
    }

    @GetMapping("/instructor/{courseId}")
    @PreAuthorize("hasRole('INSTRUCTOR')")
    public ResponseEntity<CourseResponseDto> getCourseById(
            @PathVariable Long courseId,
            @AuthenticationPrincipal User currentUser) {
        CourseResponseDto course = courseService.getCourseById(courseId, currentUser.getId());
        return ResponseEntity.ok(course);
    }

    // Student endpoints
    @GetMapping("/available")
    @PreAuthorize("hasRole('STUDENT')")
    public ResponseEntity<List<CourseResponseDto>> getAvailableCourses() {
        List<CourseResponseDto> courses = courseService.getAvailableCourses();
        return ResponseEntity.ok(courses);
    }

    @PostMapping("/{courseId}/enroll")
    @PreAuthorize("hasRole('STUDENT')")
    public ResponseEntity<String> enrollInCourse(
            @PathVariable Long courseId,
            @AuthenticationPrincipal User currentUser) {
        courseService.enrollStudent(courseId, currentUser.getId());
        return ResponseEntity.ok("Successfully enrolled in course");
    }

    @GetMapping("/student/enrolled")
    @PreAuthorize("hasRole('STUDENT')")
    public ResponseEntity<List<CourseResponseDto>> getEnrolledCourses(
            @AuthenticationPrincipal User currentUser) {
        List<CourseResponseDto> courses = courseService.getStudentEnrolledCourses(currentUser.getId());
        return ResponseEntity.ok(courses);
    }
}
