package com.elearning.projects.elearn.repository;

import com.elearning.projects.elearn.entity.ModuleProgress;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ModuleProgressRepository extends JpaRepository<ModuleProgress, Long> {

    /**
     * Finds the progress for a specific student on a specific module.
     *
     * @param studentId the ID of the student (User)
     * @param moduleId  the ID of the module
     * @return an Optional containing the module progress if it exists
     */
    Optional<ModuleProgress> findByStudentIdAndModuleId(Long studentId, Long moduleId);

    /**
     * Finds all module progress records for a student in a specific course.
     * This allows you to calculate the overall course completion percentage.
     *
     * @param studentId the ID of the student (User)
     * @param courseId  the ID of the course
     * @return a list of module progress records for that course
     */
    List<ModuleProgress> findByStudentIdAndModuleCourseId(Long studentId, Long courseId);

    long countByModuleId(Long moduleId);
}