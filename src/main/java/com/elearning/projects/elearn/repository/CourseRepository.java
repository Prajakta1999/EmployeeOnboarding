package com.elearning.projects.elearn.repository;

import com.elearning.projects.elearn.entity.OnboardingTask;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CourseRepository extends JpaRepository<OnboardingTask, Long> {

    /**
     * Finds all courses created by a specific instructor.
     *
     * @param instructorId the ID of the instructor (User)
     * @return a list of courses taught by the instructor
     */
    List<OnboardingTask> findByHr_Id(Long hrId);

    @Query("SELECT DISTINCT c FROM OnboardingTask  c JOIN c.modules m WHERE m.isPublished = true")
	List<OnboardingTask> findCoursesWithPublishedModules();
}
