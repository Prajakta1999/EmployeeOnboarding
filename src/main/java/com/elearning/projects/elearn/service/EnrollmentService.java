package com.elearning.projects.elearn.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.elearning.projects.elearn.dto.*;
import com.elearning.projects.elearn.entity.*;
import com.elearning.projects.elearn.entity.Module;
import com.elearning.projects.elearn.entity.enums.Role;
import com.elearning.projects.elearn.exception.ResourceNotFoundException;
import com.elearning.projects.elearn.exception.UnAuthorisedException;
import com.elearning.projects.elearn.repository.*;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class EnrollmentService {

    private final EnrollmentRepository enrollmentRepository;
    private final CourseRepository courseRepository;
    private final UserRepository userRepository;
    private final ModuleRepository moduleRepository;
    private final ModuleProgressRepository moduleProgressRepository;

    @Transactional
    public void enrollStudent(Long courseId, Long studentId) {
        User student = userRepository.findById(studentId)
                .orElseThrow(() -> new ResourceNotFoundException("Student not found"));

        if (!student.getRoles().contains(Role.EMPLOYEE)) {
            throw new UnAuthorisedException("Only students can enroll in courses");
        }

        OnboardingTask course = courseRepository.findById(courseId)
                .orElseThrow(() -> new ResourceNotFoundException("Course not found"));

        // Check if course has published modules
        long publishedModulesCount = moduleRepository.countByCourseIdAndIsPublishedTrue(courseId);
        if (publishedModulesCount == 0) {
            throw new RuntimeException("Cannot enroll in course without published modules");
        }

        // Check if already enrolled
        if (enrollmentRepository.findByEmployee_IdAndCourse_Id(studentId, courseId).isPresent()) {
            throw new RuntimeException("Student is already enrolled in this course");
        }

        Enrollment enrollment = new Enrollment();
        enrollment.setEmployee(student);
        enrollment.setCourse(course);
        enrollmentRepository.save(enrollment);
    }

    @Transactional
    public void unenrollStudent(Long courseId, Long studentId) {
        User student = userRepository.findById(studentId)
                .orElseThrow(() -> new ResourceNotFoundException("Student not found"));

        if (!student.getRoles().contains(Role.EMPLOYEE)) {
            throw new UnAuthorisedException("Only students can unenroll from courses");
        }

        Enrollment enrollment = enrollmentRepository.findByEmployee_IdAndCourse_Id(studentId, courseId)
                .orElseThrow(() -> new ResourceNotFoundException("Enrollment not found"));

        // Check if student has progress in any modules
        List<Module> courseModules = moduleRepository.findByCourseId(courseId);
        List<Long> moduleIds = courseModules.stream().map(Module::getId).collect(Collectors.toList());

        long progressCount = moduleProgressRepository.countByStudentIdAndModuleIdInAndIsCompletedTrue(studentId,
                moduleIds);

        if (progressCount > 0) {
            throw new RuntimeException("Cannot unenroll from course with completed modules. Contact administrator.");
        }

        // Delete all progress records for this course
        List<ModuleProgress> allProgress = moduleProgressRepository.findByStudentIdAndModuleIds(studentId, moduleIds);
        if (!allProgress.isEmpty()) {
            moduleProgressRepository.deleteAll(allProgress);
        }

        enrollmentRepository.delete(enrollment);
    }

    @Transactional(readOnly = true)
    public List<TaskResponseDto> getStudentEnrolledCourses(Long studentId) {
        User student = userRepository.findById(studentId)
                .orElseThrow(() -> new ResourceNotFoundException("Student not found"));

        if (!student.getRoles().contains(Role.EMPLOYEE)) {
            throw new UnAuthorisedException("Only students can view enrolled courses");
        }

        List<Enrollment> enrollments = enrollmentRepository.findByEmployee_Id(studentId);

        return enrollments.stream()
                .map(enrollment -> {
                    OnboardingTask course = enrollment.getCourse();
                    TaskResponseDto dto = new TaskResponseDto();
                    dto.setId(course.getId());
                    dto.setName(course.getName());
                    dto.setDescription(course.getDescription());
                    dto.setInstructorName(course.getHr().getName());
                    dto.setInstructorId(course.getHr().getId());
                    dto.setCreatedAt(course.getCreatedAt());
                    dto.setUpdatedAt(course.getUpdatedAt());

                    // Count modules
                    int totalModules = moduleRepository.countByCourseId(course.getId());
                    int publishedModules = moduleRepository.countByCourseIdAndIsPublishedTrue(course.getId());

                    dto.setTotalModules(totalModules);
                    dto.setPublishedModules(publishedModules);

                    return dto;
                })
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<AssignedEmployeeDto> getCourseEnrolledStudents(Long courseId, Long instructorId) {
        OnboardingTask course = courseRepository.findById(courseId)
                .orElseThrow(() -> new ResourceNotFoundException("Course not found"));

        if (!course.getHr().getId().equals(instructorId)) {
            throw new UnAuthorisedException("You can only view enrollments for your own courses");
        }

        List<Enrollment> enrollments = enrollmentRepository.findByCourseId(courseId);

        return enrollments.stream()
                .map(enrollment -> {
                    User student = enrollment.getEmployee();

                    // Calculate progress
                    List<Module> publishedModules = moduleRepository.findByCourseIdAndIsPublishedTrue(courseId);
                    List<Long> moduleIds = publishedModules.stream().map(Module::getId).collect(Collectors.toList());

                    long completedModules = moduleProgressRepository
                            .countByStudentIdAndModuleIdInAndIsCompletedTrue(student.getId(), moduleIds);

                    AssignedEmployeeDto dto = new AssignedEmployeeDto();
                    dto.setStudentId(student.getId());
                    dto.setStudentName(student.getName());
                    dto.setStudentEmail(student.getEmail());
                    dto.setEnrolledAt(enrollment.getEnrolledAt());
                    dto.setTotalModules((long) publishedModules.size());
                    dto.setCompletedModules(completedModules);
                    dto.setProgressPercentage(
                            publishedModules.size() > 0 ? (completedModules * 100.0) / publishedModules.size() : 0.0);

                    return dto;
                })
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public boolean isStudentEnrolled(Long courseId, Long studentId) {
        return enrollmentRepository.findByEmployee_IdAndCourse_Id(studentId, courseId).isPresent();
    }

    @Transactional(readOnly = true)
    public List<TaskResponseDto> getAvailableCoursesForStudent(Long studentId) {
        User student = userRepository.findById(studentId)
                .orElseThrow(() -> new ResourceNotFoundException("Student not found"));

        if (!student.getRoles().contains(Role.EMPLOYEE)) {
            throw new UnAuthorisedException("Only students can view available courses");
        }

        // Get all enrolled course IDs
        List<Long> enrolledCourseIds = enrollmentRepository.findByEmployee_Id(studentId)
                .stream()
                .map(enrollment -> enrollment.getCourse().getId())
                .collect(Collectors.toList());

        // Get all courses with published modules
        List<OnboardingTask> allAvailableCourses = courseRepository.findCoursesWithPublishedModules();

        // Filter out already enrolled courses
        return allAvailableCourses.stream()
                .filter(course -> !enrolledCourseIds.contains(course.getId()))
                .map(course -> {
                    TaskResponseDto dto = new TaskResponseDto();
                    dto.setId(course.getId());
                    dto.setName(course.getName());
                    dto.setDescription(course.getDescription());
                    dto.setInstructorName(course.getHr().getName());
                    dto.setInstructorId(course.getHr().getId());
                    dto.setCreatedAt(course.getCreatedAt());
                    dto.setUpdatedAt(course.getUpdatedAt());

                    int totalModules = moduleRepository.countByCourseId(course.getId());
                    int publishedModules = moduleRepository.countByCourseIdAndIsPublishedTrue(course.getId());

                    dto.setTotalModules(totalModules);
                    dto.setPublishedModules(publishedModules);

                    return dto;
                })
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public EnrollmentStatsDto getEnrollmentStats(Long instructorId) {
        User instructor = userRepository.findById(instructorId)
                .orElseThrow(() -> new ResourceNotFoundException("Instructor not found"));

        if (!instructor.getRoles().contains(Role.HR)) {
            throw new UnAuthorisedException("Only instructors can view enrollment statistics");
        }

        List<OnboardingTask> instructorCourses = courseRepository.findByHr_Id(instructorId);

        long totalCourses = instructorCourses.size();
        long totalEnrollments = instructorCourses.stream()
                .mapToLong(course -> enrollmentRepository.countByCourseId(course.getId()))
                .sum();

        EnrollmentStatsDto stats = new EnrollmentStatsDto();
        stats.setTotalCourses(totalCourses);
        stats.setTotalEnrollments(totalEnrollments);
        stats.setAverageEnrollmentsPerCourse(totalCourses > 0 ? totalEnrollments / (double) totalCourses : 0.0);

        return stats;
    }
}
