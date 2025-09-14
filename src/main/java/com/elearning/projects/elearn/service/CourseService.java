package com.elearning.projects.elearn.service;

import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.elearning.projects.elearn.dto.*;
import com.elearning.projects.elearn.entity.*;
import com.elearning.projects.elearn.entity.enums.Role;
import com.elearning.projects.elearn.exception.ResourceNotFoundException;
import com.elearning.projects.elearn.exception.UnAuthorisedException;
import com.elearning.projects.elearn.repository.*;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class CourseService {

    private final CourseRepository courseRepository;
    private final UserRepository userRepository;
    private final EnrollmentRepository enrollmentRepository;
    private final ModuleRepository moduleRepository;
    private final ModelMapper modelMapper;
    private final EnrollmentService enrollmentService;

    public TaskResponseDto createCourse(CreateTaskRequestDto requestDto, Long instructorId) {
        User instructor = userRepository.findById(instructorId)
                .orElseThrow(() -> new ResourceNotFoundException("Instructor not found"));

        if (!instructor.getRoles().contains(Role.HR)) {
            throw new UnAuthorisedException("Only instructors can create courses");
        }

        OnboardingTask course = modelMapper.map(requestDto, OnboardingTask.class);
        course.setHr(instructor);
        course = courseRepository.save(course);

        return mapToCourseResponseDto(course);
    }

    public TaskResponseDto updateCourse(Long courseId, UpdateCourseRequestDto requestDto, Long instructorId) {
        OnboardingTask course = courseRepository.findById(courseId)
                .orElseThrow(() -> new ResourceNotFoundException("Course not found"));

        if (!course.getHr().getId().equals(instructorId)) {
            throw new UnAuthorisedException("You can only update your own courses");
        }

        if (requestDto.getName() != null && !requestDto.getName().trim().isEmpty()) {
            course.setName(requestDto.getName().trim());
        }
        if (requestDto.getDescription() != null) {
            course.setDescription(requestDto.getDescription().trim());
        }

        course = courseRepository.save(course);
        return mapToCourseResponseDto(course);
    }

    public void deleteCourse(Long courseId, Long instructorId) {
        OnboardingTask course = courseRepository.findById(courseId)
                .orElseThrow(() -> new ResourceNotFoundException("Course not found"));

        if (!course.getHr().getId().equals(instructorId)) {
            throw new UnAuthorisedException("You can only delete your own courses");
        }

        // Check if there are any enrollments
        long enrollmentCount = enrollmentRepository.countByCourseId(courseId);
        if (enrollmentCount > 0) {
            throw new RuntimeException("Cannot delete course with enrolled students");
        }

        courseRepository.delete(course);
    }

    public List<TaskResponseDto> getInstructorCourses(Long instructorId) {
        User instructor = userRepository.findById(instructorId)
                .orElseThrow(() -> new ResourceNotFoundException("Instructor not found"));

        if (!instructor.getRoles().contains(Role.HR)) {
            throw new UnAuthorisedException("Only instructors can view instructor courses");
        }

        List<OnboardingTask> courses = courseRepository.findByHr_Id(instructorId);
        return courses.stream()
                .map(this::mapToCourseResponseDto)
                .collect(Collectors.toList());
    }

    public TaskResponseDto getCourseById(Long courseId, Long instructorId) {
        OnboardingTask course = courseRepository.findById(courseId)
                .orElseThrow(() -> new ResourceNotFoundException("Course not found"));

        if (!course.getHr().getId().equals(instructorId)) {
            throw new UnAuthorisedException("You can only view your own courses");
        }

        return mapToCourseResponseDto(course);
    }

    // public List<CourseResponseDto> getAvailableCourses() {
    // List<Course> courses = courseRepository.findCoursesWithPublishedModules();
    // return courses.stream()
    // .map(this::mapToCourseResponseDto)
    // .collect(Collectors.toList());
    // }

    public List<TaskResponseDto> getAvailableCourses() {
        return enrollmentService.getAvailableCoursesForStudent(null); // Or create separate method
    }

    // public void enrollStudent(Long courseId, Long studentId) {
    // User student = userRepository.findById(studentId)
    // .orElseThrow(() -> new ResourceNotFoundException("Student not found"));

    // Course course = courseRepository.findById(courseId)
    // .orElseThrow(() -> new ResourceNotFoundException("Course not found"));

    // if (!student.getRoles().contains(Role.STUDENT)) {
    // throw new UnAuthorisedException("Only students can enroll in courses");
    // }

    // // Check if course has published modules
    // long publishedModulesCount =
    // moduleRepository.countByCourseIdAndIsPublishedTrue(courseId);
    // if (publishedModulesCount == 0) {
    // throw new RuntimeException("Cannot enroll in course without published
    // modules");
    // }

    // if (enrollmentRepository.findByStudentIdAndCourseId(studentId,
    // courseId).isPresent()) {
    // throw new RuntimeException("Student is already enrolled in this course");
    // }

    // Enrollment enrollment = new Enrollment();
    // enrollment.setStudent(student);
    // enrollment.setCourse(course);
    // enrollmentRepository.save(enrollment);
    // }

    public void enrollStudent(Long courseId, Long studentId) {
        enrollmentService.enrollStudent(courseId, studentId);
    }

    // public List<CourseResponseDto> getStudentEnrolledCourses(Long studentId) {
    // User student = userRepository.findById(studentId)
    // .orElseThrow(() -> new ResourceNotFoundException("Student not found"));

    // if (!student.getRoles().contains(Role.STUDENT)) {
    // throw new UnAuthorisedException("Only students can view enrolled courses");
    // }

    // List<Enrollment> enrollments =
    // enrollmentRepository.findByStudentId(studentId);
    // return enrollments.stream()
    // .map(enrollment -> mapToCourseResponseDto(enrollment.getCourse()))
    // .collect(Collectors.toList());
    // }

    public List<TaskResponseDto> getStudentEnrolledCourses(Long studentId) {
        return enrollmentService.getStudentEnrolledCourses(studentId);
    }

    private TaskResponseDto mapToCourseResponseDto(OnboardingTask course) {
        TaskResponseDto dto = modelMapper.map(course, TaskResponseDto.class);
        dto.setInstructorName(course.getHr().getName());
        dto.setInstructorId(course.getHr().getId());

        // Count modules
        int totalModules = moduleRepository.countByCourseId(course.getId());
        int publishedModules = moduleRepository.countByCourseIdAndIsPublishedTrue(course.getId());

        dto.setTotalModules(totalModules);
        dto.setPublishedModules(publishedModules);

        return dto;
    }
}
