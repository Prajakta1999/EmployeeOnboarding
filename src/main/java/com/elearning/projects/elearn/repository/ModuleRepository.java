package com.elearning.projects.elearn.repository;

import com.elearning.projects.elearn.entity.Module;
import com.elearning.projects.elearn.entity.enums.ContentType;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ModuleRepository extends JpaRepository<Module, Long> {

    /**
     * Finds all modules associated with a specific course.
     *
     * @param courseId the ID of the course
     * @return a list of modules for the given course
     */
    List<Module> findByCourseId(Long courseId);

    List<Module> findByCourseInstructorId(Long instructorId);

    List<Module> findByCourseIdAndIsPublishedTrue(Long courseId);

    int countByCourseId(Long courseId);

    int countByCourseIdAndIsPublishedTrue(Long courseId);

    @Query("SELECT m FROM Module m WHERE m.isPublished = true AND m.course.id IN :courseIds " +
            "AND (:courseName IS NULL OR LOWER(m.course.name) LIKE LOWER(CONCAT('%', :courseName, '%'))) " +
            "AND (:moduleTitle IS NULL OR LOWER(m.title) LIKE LOWER(CONCAT('%', :moduleTitle, '%'))) " +
            "AND (:contentType IS NULL OR m.contentType = :contentType) " +
            "ORDER BY m.createdAt DESC")
    List<Module> findPublishedModulesWithFilters(@Param("courseIds") List<Long> courseIds,
            @Param("courseName") String courseName,
            @Param("moduleTitle") String moduleTitle,
            @Param("contentType") ContentType contentType);
}
