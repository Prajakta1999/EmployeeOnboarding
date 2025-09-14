package com.elearning.projects.elearn.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Getter
@Setter
@Table(name = "module_progress", 
       uniqueConstraints = @UniqueConstraint(columnNames = {"employee_id", "module_id"}))
public class ModuleProgress {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "employee_id", nullable = false)
    private User employee;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "module_id", nullable = false)
    private Module module;
    
    @Column(nullable = false)
    private Boolean isCompleted = false;
    
    private LocalDateTime completedAt;
    
    @CreationTimestamp
    private LocalDateTime createdAt;
}
