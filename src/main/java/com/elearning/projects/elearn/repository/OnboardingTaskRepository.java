package com.elearning.projects.elearn.repository;

import com.elearning.projects.elearn.entity.Employee;
import com.elearning.projects.elearn.entity.OnboardingTask;
import com.elearning.projects.elearn.entity.TaskStatus;
import com.elearning.projects.elearn.entity.TaskType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface OnboardingTaskRepository extends JpaRepository<OnboardingTask, Long> {
    
    List<OnboardingTask> findByEmployee(Employee employee);
    
    List<OnboardingTask> findByEmployeeId(Long employeeId);
    
    List<OnboardingTask> findByEmployeeAndStatus(Employee employee, TaskStatus status);
    
    Optional<OnboardingTask> findByEmployeeAndTaskType(Employee employee, TaskType taskType);
    
    List<OnboardingTask> findByStatus(TaskStatus status);
    
    @Query("SELECT COUNT(t) FROM OnboardingTask t WHERE t.employee.id = :employeeId AND t.status = :status")
    int countByEmployeeIdAndStatus(@Param("employeeId") Long employeeId, @Param("status") TaskStatus status);
    
    @Query("SELECT COUNT(t) FROM OnboardingTask t WHERE t.employee.id = :employeeId")
    int countByEmployeeId(@Param("employeeId") Long employeeId);
    
    boolean existsByEmployeeAndTaskType(Employee employee, TaskType taskType);
}
