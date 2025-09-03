package com.elearning.projects.elearn.service;

import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.elearning.projects.elearn.dto.*;
import com.elearning.projects.elearn.entity.*;
import com.elearning.projects.elearn.entity.Module;
import com.elearning.projects.elearn.entity.enums.Role;
import com.elearning.projects.elearn.exception.ResourceNotFoundException;
import com.elearning.projects.elearn.exception.UnAuthorisedException;
import com.elearning.projects.elearn.repository.*;

import jakarta.annotation.PostConstruct;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class ModuleService {

    private final ModuleRepository moduleRepository;
    private final CourseRepository courseRepository;
    private final UserRepository userRepository;
    private final ModuleProgressRepository moduleProgressRepository;
    private final EnrollmentRepository enrollmentRepository;
    private final ModelMapper modelMapper;

    public ModuleResponseDto createModule(CreateModuleRequestDto requestDto, Long instructorId) {
        User instructor = userRepository.findById(instructorId)
                .orElseThrow(() -> new ResourceNotFoundException("Instructor not found"));

        if (!instructor.getRoles().contains(Role.INSTRUCTOR)) {
            throw new UnAuthorisedException("Only instructors can create modules");
        }

        Course course = courseRepository.findById(requestDto.getCourseId())
                .orElseThrow(() -> new ResourceNotFoundException("Course not found"));

        if (!course.getInstructor().getId().equals(instructorId)) {
            throw new UnAuthorisedException("You can only create modules for your own courses");
        }

        modelMapper.typeMap(CreateModuleRequestDto.class, Module.class)
                .addMappings(mapper -> mapper.skip(Module::setId));

        Module module = modelMapper.map(requestDto, Module.class);

        module.setCourse(course);
        module.setIsPublished(false); // Default unpublished
        module = moduleRepository.save(module);

        return mapToModuleResponseDto(module, null);
    }

    // public ModuleResponseDto createModule(CreateModuleRequestDto requestDto, Long
    // instructorId) {
    // User instructor = userRepository.findById(instructorId)
    // .orElseThrow(() -> new ResourceNotFoundException("Instructor not found"));

    // if (!instructor.getRoles().contains(Role.INSTRUCTOR)) {
    // throw new UnAuthorisedException("Only instructors can create modules");
    // }

    // Course course = courseRepository.findById(requestDto.getCourseId())
    // .orElseThrow(() -> new ResourceNotFoundException("Course not found"));

    // if (!course.getInstructor().getId().equals(instructorId)) {
    // throw new UnAuthorisedException("You can only create modules for your own
    // courses");
    // }

    // // --- REVISED LOGIC ---
    // // 1. Create a new, empty Module instance.
    // Module module = new Module();
    // // 2. Use ModelMapper to copy properties from the DTO to the new instance.
    // // This avoids the problematic implicit mapping of 'courseId' to 'course.id'.
    // modelMapper.map(requestDto, module);

    // // 3. Manually set the managed Course entity.
    // module.setCourse(course);
    // module.setIsPublished(false); // Default unpublished

    // Module savedModule = moduleRepository.save(module);

    // return mapToModuleResponseDto(savedModule, null);
    // }

    public ModuleResponseDto updateModule(Long moduleId, UpdateModuleRequestDto requestDto, Long instructorId) {
        Module module = moduleRepository.findById(moduleId)
                .orElseThrow(() -> new ResourceNotFoundException("Module not found"));

        if (!module.getCourse().getInstructor().getId().equals(instructorId)) {
            throw new UnAuthorisedException("You can only update your own modules");
        }

        if (requestDto.getTitle() != null && !requestDto.getTitle().trim().isEmpty()) {
            module.setTitle(requestDto.getTitle().trim());
        }
        if (requestDto.getDescription() != null && !requestDto.getDescription().trim().isEmpty()) {
            module.setDescription(requestDto.getDescription().trim());
        }
        if (requestDto.getContentUrl() != null && !requestDto.getContentUrl().trim().isEmpty()) {
            module.setContentUrl(requestDto.getContentUrl().trim());
        }
        if (requestDto.getContentType() != null) {
            module.setContentType(requestDto.getContentType());
        }

        module = moduleRepository.save(module);
        return mapToModuleResponseDto(module, null);
    }

    public ModuleResponseDto publishModule(Long moduleId, Long instructorId) {
        return toggleModulePublishStatus(moduleId, instructorId, true);
    }

    public ModuleResponseDto unpublishModule(Long moduleId, Long instructorId) {
        return toggleModulePublishStatus(moduleId, instructorId, false);
    }

    private ModuleResponseDto toggleModulePublishStatus(Long moduleId, Long instructorId, boolean isPublished) {
        Module module = moduleRepository.findById(moduleId)
                .orElseThrow(() -> new ResourceNotFoundException("Module not found"));

        if (!module.getCourse().getInstructor().getId().equals(instructorId)) {
            throw new UnAuthorisedException("You can only manage your own modules");
        }

        module.setIsPublished(isPublished);
        module = moduleRepository.save(module);

        return mapToModuleResponseDto(module, null);
    }

    public void deleteModule(Long moduleId, Long instructorId) {
        Module module = moduleRepository.findById(moduleId)
                .orElseThrow(() -> new ResourceNotFoundException("Module not found"));

        if (!module.getCourse().getInstructor().getId().equals(instructorId)) {
            throw new UnAuthorisedException("You can only delete your own modules");
        }

        // Check if any students have progress on this module
        long progressCount = moduleProgressRepository.countByModuleId(moduleId);
        if (progressCount > 0) {
            throw new RuntimeException("Cannot delete module with student progress");
        }

        moduleRepository.delete(module);
    }

    public List<ModuleResponseDto> getInstructorModules(Long instructorId, Long courseId) {
        User instructor = userRepository.findById(instructorId)
                .orElseThrow(() -> new ResourceNotFoundException("Instructor not found"));

        if (!instructor.getRoles().contains(Role.INSTRUCTOR)) {
            throw new UnAuthorisedException("Only instructors can view instructor modules");
        }

        List<Module> modules;
        if (courseId != null) {
            // Get modules for specific course
            Course course = courseRepository.findById(courseId)
                    .orElseThrow(() -> new ResourceNotFoundException("Course not found"));

            if (!course.getInstructor().getId().equals(instructorId)) {
                throw new UnAuthorisedException("You can only view modules from your own courses");
            }

            modules = moduleRepository.findByCourseId(courseId);
        } else {
            // Get all modules for instructor
            modules = moduleRepository.findByCourseInstructorId(instructorId);
        }

        return modules.stream()
                .map(module -> mapToModuleResponseDto(module, null))
                .collect(Collectors.toList());
    }

    public List<ModuleResponseDto> getPublishedModulesForStudent(Long studentId, ModuleSearchRequestDto searchDto) {
        User student = userRepository.findById(studentId)
                .orElseThrow(() -> new ResourceNotFoundException("Student not found"));

        if (!student.getRoles().contains(Role.STUDENT)) {
            throw new UnAuthorisedException("Only students can view published modules");
        }

        // Get enrolled course IDs
        List<Long> enrolledCourseIds = enrollmentRepository.findByStudentId(studentId)
                .stream()
                .map(enrollment -> enrollment.getCourse().getId())
                .collect(Collectors.toList());

        if (enrolledCourseIds.isEmpty()) {
            return List.of(); // No enrolled courses
        }

        List<Module> modules = moduleRepository.findPublishedModulesWithFilters(
                enrolledCourseIds,
                searchDto.getCourseName(),
                searchDto.getModuleTitle(),
                searchDto.getContentType());

        return modules.stream()
                .map(module -> {
                    // Check if student completed this module
                    Optional<ModuleProgress> progress = moduleProgressRepository
                            .findByStudentIdAndModuleId(studentId, module.getId());
                    boolean isCompleted = progress.map(ModuleProgress::getIsCompleted).orElse(false);

                    return mapToModuleResponseDto(module, isCompleted);
                })
                .collect(Collectors.toList());
    }

    public ModuleResponseDto getModuleById(Long moduleId, Long studentId) {
        Module module = moduleRepository.findById(moduleId)
                .orElseThrow(() -> new ResourceNotFoundException("Module not found"));

        User student = userRepository.findById(studentId)
                .orElseThrow(() -> new ResourceNotFoundException("Student not found"));

        if (!student.getRoles().contains(Role.STUDENT)) {
            throw new UnAuthorisedException("Only students can view module details");
        }

        // Check if student is enrolled in the course
        if (!enrollmentRepository.findByStudentIdAndCourseId(studentId, module.getCourse().getId()).isPresent()) {
            throw new UnAuthorisedException("You are not enrolled in this course");
        }

        if (!module.getIsPublished()) {
            throw new UnAuthorisedException("Module is not published");
        }

        // Check completion status
        Optional<ModuleProgress> progress = moduleProgressRepository
                .findByStudentIdAndModuleId(studentId, moduleId);
        boolean isCompleted = progress.map(ModuleProgress::getIsCompleted).orElse(false);

        return mapToModuleResponseDto(module, isCompleted);
    }

    public ModuleResponseDto getInstructorModuleById(Long moduleId, Long instructorId) {
        Module module = moduleRepository.findById(moduleId)
                .orElseThrow(() -> new ResourceNotFoundException("Module not found"));

        if (!module.getCourse().getInstructor().getId().equals(instructorId)) {
            throw new UnAuthorisedException("You can only view your own modules");
        }

        return mapToModuleResponseDto(module, null);
    }

    private ModuleResponseDto mapToModuleResponseDto(Module module, Boolean isCompleted) {
        ModuleResponseDto dto = modelMapper.map(module, ModuleResponseDto.class);
        dto.setCourseName(module.getCourse().getName());
        dto.setCourseId(module.getCourse().getId());
        dto.setInstructorName(module.getCourse().getInstructor().getName());

        if (isCompleted != null) {
            dto.setIsCompleted(isCompleted);
        }

        return dto;
    }
}
