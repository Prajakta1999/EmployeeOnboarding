package com.elearning.projects.elearn.repository;

import com.elearning.projects.elearn.entity.Enrollment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface EnrollmentRepository extends JpaRepository<Enrollment, Long> {

    /**
     * Finds a specific enrollment record for a given student and course.
     * Useful for checking if a student is already enrolled.
     *
     * @param studentId the ID of the student (User)
     * @param courseId  the ID of the course
     * @return an Optional containing the enrollment if it exists
     */
    Optional<Enrollment> findByEmployee_IdAndCourse_Id(Long employeeId, Long courseId);

    /**
     * Finds all courses a specific student is enrolled in.
     *
     * @param studentId the ID of the student (User)
     * @return a list of all their enrollments
     */
    List<Enrollment> findByEmployee_Id(Long employeeId);

    List<Enrollment> findByCourseId(Long courseId);

    long countByCourseId(Long courseId);
}
