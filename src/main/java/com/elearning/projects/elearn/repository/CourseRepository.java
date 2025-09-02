package com.elearning.projects.elearn.repository;

import com.elearning.projects.elearn.entity.Course;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CourseRepository extends JpaRepository<Course, Long> {

    /**
     * Finds all courses created by a specific instructor.
     *
     * @param instructorId the ID of the instructor (User)
     * @return a list of courses taught by the instructor
     */
    List<Course> findByInstructorId(Long instructorId);

    @Query("SELECT DISTINCT c FROM Course c JOIN c.modules m WHERE m.isPublished = true")
    List<Course> findCoursesWithPublishedModules();
}
