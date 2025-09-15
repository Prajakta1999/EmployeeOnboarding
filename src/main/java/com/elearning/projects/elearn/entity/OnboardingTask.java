package com.elearning.projects.elearn.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

@Entity
@Getter
@Setter
@Table(name = "onboarding_tasks")
public class OnboardingTask {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne
    @JoinColumn(name = "employee_id", nullable = false)
    private Employee employee;
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TaskType taskType;
    
    @Column(nullable = false)
    @NotBlank
    private String taskDescription;
    
    @Enumerated(EnumType.STRING)
    private TaskStatus status = TaskStatus.PENDING;
    
    private LocalDateTime completedAt;
    
    private String notes;
    
    @CreationTimestamp
    private LocalDateTime createdAt;
    
    @UpdateTimestamp
    private LocalDateTime updatedAt;
    
    // Default constructor
    public OnboardingTask() {}
    
    // Constructor
    public OnboardingTask(Employee employee, TaskType taskType, String taskDescription) {
        this.employee = employee;
        this.taskType = taskType;
        this.taskDescription = taskDescription;
    }
}
