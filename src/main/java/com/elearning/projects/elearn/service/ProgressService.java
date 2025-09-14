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

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ProgressService {

    private final ModuleProgressRepository moduleProgressRepository;
    private final ModuleRepository moduleRepository;
    private final EnrollmentRepository enrollmentRepository;
    private final UserRepository userRepository;
    private final CourseRepository courseRepository;

    @Transactional
    public void markModuleCompleted(Long moduleId, Long studentId) {
        User student = userRepository.findById(studentId)
                .orElseThrow(() -> new ResourceNotFoundException("Student not found"));

        if (!student.getRoles().contains(Role.EMPLOYEE)) {
            throw new UnAuthorisedException("Only students can mark modules as completed");
        }

        Module module = moduleRepository.findById(moduleId)
                .orElseThrow(() -> new ResourceNotFoundException("Module not found"));

        // Check if student is enrolled in course
        if (!enrollmentRepository.findByEmployee_IdAndCourse_Id(studentId, module.getCourse().getId()).isPresent()) {
            throw new UnAuthorisedException("You are not enrolled in this course");
        }

        // Check if module is published
        if (!module.getIsPublished()) {
            throw new UnAuthorisedException("Cannot complete unpublished module");
        }

        Optional<ModuleProgress> existingProgress = moduleProgressRepository
                .findByStudentIdAndModuleId(studentId, moduleId);

        ModuleProgress progress;
        if (existingProgress.isPresent()) {
            progress = existingProgress.get();
            if (progress.getIsCompleted()) {
                throw new RuntimeException("Module already completed");
            }
        } else {
            progress = new ModuleProgress();
            progress.setEmployee(student);
            progress.setModule(module);
        }

        progress.setIsCompleted(true);
        progress.setCompletedAt(LocalDateTime.now());
        moduleProgressRepository.save(progress);
    }

    @Transactional
    public void unmarkModuleCompleted(Long moduleId, Long studentId) {
        User student = userRepository.findById(studentId)
                .orElseThrow(() -> new ResourceNotFoundException("Student not found"));

        if (!student.getRoles().contains(Role.EMPLOYEE)) {
            throw new UnAuthorisedException("Only students can unmark module completion");
        }

        ModuleProgress progress = moduleProgressRepository.findByStudentIdAndModuleId(studentId, moduleId)
                .orElseThrow(() -> new ResourceNotFoundException("Progress record not found"));

        progress.setIsCompleted(false);
        progress.setCompletedAt(null);
        moduleProgressRepository.save(progress);
    }

    @Transactional(readOnly = true)
    public List<ProgressResponseDto> getStudentProgress(Long studentId) {
        User student = userRepository.findById(studentId)
                .orElseThrow(() -> new ResourceNotFoundException("Student not found"));

        if (!student.getRoles().contains(Role.EMPLOYEE)) {
            throw new UnAuthorisedException("Only students can view progress");
        }

        List<Enrollment> enrollments = enrollmentRepository.findByEmployee_Id(studentId);

        return enrollments.stream()
                .map(enrollment -> {
                    OnboardingTask course = enrollment.getCourse();
                    List<Module> publishedModules = moduleRepository
                            .findByCourseIdAndIsPublishedTrue(course.getId());

                    long totalModules = publishedModules.size();
                    List<Long> moduleIds = publishedModules.stream()
                            .map(Module::getId)
                            .collect(Collectors.toList());

                    long completedModules = moduleProgressRepository
                            .countByStudentIdAndModuleIdInAndIsCompletedTrue(studentId, moduleIds);

                    ProgressResponseDto dto = new ProgressResponseDto();
                    dto.setCourseId(course.getId());
                    dto.setCourseName(course.getName());
                    dto.setInstructorName(course.getHr().getName());
                    dto.setTotalModulesAvailable((long) totalModules);
                    dto.setTotalModulesCompleted(completedModules);
                    dto.setCompletionPercentage(
                            totalModules > 0 ? (completedModules * 100.0) / totalModules : 0.0
                    );

                    return dto;
                })
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<CourseCompletionReportDto> getInstructorCompletionReport(Long instructorId) {
        User instructor = userRepository.findById(instructorId)
                .orElseThrow(() -> new ResourceNotFoundException("Instructor not found"));

        if (!instructor.getRoles().contains(Role.HR)) {
            throw new UnAuthorisedException("Only instructors can view completion reports");
        }

        List<OnboardingTask> courses = courseRepository.findByHr_Id(instructorId);

        return courses.stream()
                .map(course -> {
                    List<Module> publishedModules = moduleRepository
                            .findByCourseIdAndIsPublishedTrue(course.getId());
                    
                    List<Long> enrolledStudentIds = enrollmentRepository.findByCourseId(course.getId())
                            .stream()
                            .map(enrollment -> enrollment.getEmployee().getId())
                            .collect(Collectors.toList());

                    List<CompletionReportDto> moduleReports = publishedModules.stream()
                            .map(module -> {
                                long totalStudents = enrolledStudentIds.size();
                                long completedStudents = moduleProgressRepository
                                        .countByModuleIdAndStudentIdInAndIsCompletedTrue(
                                                module.getId(), enrolledStudentIds);

                                CompletionReportDto reportDto = new CompletionReportDto();
                                reportDto.setModuleId(module.getId());
                                reportDto.setModuleTitle(module.getTitle());
                                reportDto.setTotalStudents(totalStudents);
                                reportDto.setCompletedStudents(completedStudents);
                                reportDto.setCompletionPercentage(
                                        totalStudents > 0 ? (completedStudents * 100.0) / totalStudents : 0.0
                                );

                                return reportDto;
                            })
                            .collect(Collectors.toList());

                    double overallCompletion = moduleReports.stream()
                            .mapToDouble(CompletionReportDto::getCompletionPercentage)
                            .average()
                            .orElse(0.0);

                    CourseCompletionReportDto courseReport = new CourseCompletionReportDto();
                    courseReport.setCourseId(course.getId());
                    courseReport.setCourseName(course.getName());
                    courseReport.setModuleReports(moduleReports);
                    courseReport.setOverallCompletionPercentage(overallCompletion);
                    courseReport.setTotalEnrolledStudents((long) enrolledStudentIds.size());

                    return courseReport;
                })
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public CourseCompletionReportDto getCourseCompletionReport(Long courseId, Long instructorId) {
        OnboardingTask course = courseRepository.findById(courseId)
                .orElseThrow(() -> new ResourceNotFoundException("Course not found"));

        if (!course.getHr().getId().equals(instructorId)) {
            throw new UnAuthorisedException("You can only view reports for your own courses");
        }

        return getInstructorCompletionReport(instructorId).stream()
                .filter(report -> report.getCourseId().equals(courseId))
                .findFirst()
                .orElseThrow(() -> new ResourceNotFoundException("Course report not found"));
    }
}
