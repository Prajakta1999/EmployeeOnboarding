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
@Table(name = "documents")
public class Document {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne
    @JoinColumn(name = "employee_id", nullable = false)
    private Employee employee;
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private DocumentType documentType;
    
    @Column(nullable = false)
    @NotBlank
    private String documentUrl;
    
    @Enumerated(EnumType.STRING)
    private DocumentStatus status = DocumentStatus.PENDING_REVIEW;
    
    private String reviewComments;
    
    @ManyToOne
    @JoinColumn(name = "reviewed_by")
    private User reviewedBy;
    
    private LocalDateTime reviewedAt;
    
    @CreationTimestamp
    private LocalDateTime createdAt;
    
    @UpdateTimestamp
    private LocalDateTime updatedAt;
    
    // Default constructor
    public Document() {}
    
    // Constructor
    public Document(Employee employee, DocumentType documentType, String documentUrl) {
        this.employee = employee;
        this.documentType = documentType;
        this.documentUrl = documentUrl;
    }
}
