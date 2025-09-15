package com.elearning.projects.elearn.repository;

import com.elearning.projects.elearn.entity.Employee;
import com.elearning.projects.elearn.entity.OnboardingStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface EmployeeRepository extends JpaRepository<Employee, Long> {
    
    Optional<Employee> findByUserId(Long userId);
    
    Optional<Employee> findByEmployeeId(String employeeId);
    
    List<Employee> findByOnboardingStatus(OnboardingStatus status);
    
    List<Employee> findByDepartment(String department);
    
    List<Employee> findByDepartmentAndOnboardingStatus(String department, OnboardingStatus status);
    
    @Query("SELECT e FROM Employee e WHERE e.joiningDate BETWEEN :startDate AND :endDate")
    List<Employee> findByJoiningDateBetween(@Param("startDate") LocalDate startDate, @Param("endDate") LocalDate endDate);
    
    @Query("SELECT COUNT(e) FROM Employee e WHERE e.onboardingStatus = :status")
    int countByOnboardingStatus(@Param("status") OnboardingStatus status);
    
    @Query("SELECT DISTINCT e.department FROM Employee e")
    List<String> findAllDepartments();
    
    boolean existsByEmployeeId(String employeeId);
}
