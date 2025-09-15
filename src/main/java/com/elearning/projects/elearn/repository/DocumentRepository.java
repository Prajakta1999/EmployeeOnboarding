package com.elearning.projects.elearn.repository;

import com.elearning.projects.elearn.entity.Document;
import com.elearning.projects.elearn.entity.DocumentStatus;
import com.elearning.projects.elearn.entity.DocumentType;
import com.elearning.projects.elearn.entity.Employee;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface DocumentRepository extends JpaRepository<Document, Long> {
    
    List<Document> findByEmployee(Employee employee);
    
    List<Document> findByEmployeeId(Long employeeId);
    
    Optional<Document> findByEmployeeAndDocumentType(Employee employee, DocumentType documentType);
    
    List<Document> findByStatus(DocumentStatus status);
    
    List<Document> findByEmployeeIdAndStatus(Long employeeId, DocumentStatus status);
    
    @Query("SELECT COUNT(d) FROM Document d WHERE d.employee.id = :employeeId AND d.status = :status")
    int countByEmployeeIdAndStatus(@Param("employeeId") Long employeeId, @Param("status") DocumentStatus status);
    
    @Query("SELECT COUNT(d) FROM Document d WHERE d.employee.id = :employeeId AND d.documentType IN :mandatoryTypes AND d.status = 'APPROVED'")
    int countApprovedMandatoryDocuments(@Param("employeeId") Long employeeId, @Param("mandatoryTypes") List<DocumentType> mandatoryTypes);
    
    boolean existsByEmployeeAndDocumentType(Employee employee, DocumentType documentType);
}
