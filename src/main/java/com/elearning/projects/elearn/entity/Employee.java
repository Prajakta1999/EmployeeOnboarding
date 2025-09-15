package com.elearning.projects.elearn.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Getter
@Setter
@Table(name = "employees")
public class Employee {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @OneToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;
    
    @Column(nullable = false, unique = true)
    @NotBlank
    private String employeeId;
    
    @Column(nullable = false)
    @NotBlank
    private String department;
    
    @Column(nullable = false)
    @NotBlank
    private String designation;
    
    @Column(nullable = false)
    @NotNull
    private LocalDate joiningDate;
    
    @Enumerated(EnumType.STRING)
    private OnboardingStatus onboardingStatus = OnboardingStatus.PENDING;
    
    @CreationTimestamp
    private LocalDateTime createdAt;
    
    @UpdateTimestamp
    private LocalDateTime updatedAt;
    
    // Default constructor
    public Employee() {}
    
    // Constructor
    public Employee(User user, String employeeId, String department, String designation, LocalDate joiningDate) {
        this.user = user;
        this.employeeId = employeeId;
        this.department = department;
        this.designation = designation;
        this.joiningDate = joiningDate;
    }
}
