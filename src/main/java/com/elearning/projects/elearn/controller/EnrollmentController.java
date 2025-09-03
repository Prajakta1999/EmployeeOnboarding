package com.elearning.projects.elearn.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import com.elearning.projects.elearn.dto.*;
import com.elearning.projects.elearn.entity.User;
import com.elearning.projects.elearn.service.EnrollmentService;

import java.util.List;

@RestController
@RequestMapping("/enrollments")
@RequiredArgsConstructor
public class EnrollmentController {

    private final EnrollmentService enrollmentService;

    // Student endpoints

    @PostMapping("/courses/{courseId}")
    @PreAuthorize("hasRole('STUDENT')")
    public ResponseEntity<String> enrollInCourse(
            @PathVariable Long courseId,
            @AuthenticationPrincipal User currentUser) {
        enrollmentService.enrollStudent(courseId, currentUser.getId());
        return ResponseEntity.ok("Successfully enrolled in course");
    }

    @DeleteMapping("/courses/{courseId}")
    @PreAuthorize("hasRole('STUDENT')")
    public ResponseEntity<String> unenrollFromCourse(
            @PathVariable Long courseId,
            @AuthenticationPrincipal User currentUser) {
        enrollmentService.unenrollStudent(courseId, currentUser.getId());
        return ResponseEntity.ok("Successfully unenrolled from course");
    }

    @GetMapping("/student/my-courses")
    @PreAuthorize("hasRole('STUDENT')")
    public ResponseEntity<List<CourseResponseDto>> getMyEnrolledCourses(
            @AuthenticationPrincipal User currentUser) {
        List<CourseResponseDto> courses = enrollmentService.getStudentEnrolledCourses(currentUser.getId());
        return ResponseEntity.ok(courses);
    }

    @GetMapping("/student/available-courses")
    @PreAuthorize("hasRole('STUDENT')")
    public ResponseEntity<List<CourseResponseDto>> getAvailableCourses(
            @AuthenticationPrincipal User currentUser) {
        List<CourseResponseDto> courses = enrollmentService.getAvailableCoursesForStudent(currentUser.getId());
        return ResponseEntity.ok(courses);
    }

    @GetMapping("/courses/{courseId}/is-enrolled")
    @PreAuthorize("hasRole('STUDENT')")
    public ResponseEntity<Boolean> checkEnrollmentStatus(
            @PathVariable Long courseId,
            @AuthenticationPrincipal User currentUser) {
        boolean isEnrolled = enrollmentService.isStudentEnrolled(courseId, currentUser.getId());
        return ResponseEntity.ok(isEnrolled);
    }

    // Instructor endpoints

    @GetMapping("/instructor/courses/{courseId}/students")
    @PreAuthorize("hasRole('INSTRUCTOR')")
    public ResponseEntity<List<EnrolledStudentDto>> getCourseEnrolledStudents(
            @PathVariable Long courseId,
            @AuthenticationPrincipal User currentUser) {
        List<EnrolledStudentDto> students = enrollmentService.getCourseEnrolledStudents(courseId, currentUser.getId());
        return ResponseEntity.ok(students);
    }

    @GetMapping("/instructor/statistics")
    @PreAuthorize("hasRole('INSTRUCTOR')")
    public ResponseEntity<EnrollmentStatsDto> getEnrollmentStatistics(
            @AuthenticationPrincipal User currentUser) {
        EnrollmentStatsDto stats = enrollmentService.getEnrollmentStats(currentUser.getId());
        return ResponseEntity.ok(stats);
    }
}
