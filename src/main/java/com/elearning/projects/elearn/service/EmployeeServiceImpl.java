package com.elearning.projects.elearn.service;

import com.elearning.projects.elearn.dto.*;
import com.elearning.projects.elearn.entity.*;
import com.elearning.projects.elearn.entity.enums.Role;
import com.elearning.projects.elearn.exception.OperationFailedException;
import com.elearning.projects.elearn.exception.ResourceNotFoundException;
import com.elearning.projects.elearn.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class EmployeeServiceImpl implements EmployeeService {
    
    private final EmployeeRepository employeeRepository;
    private final UserRepository userRepository;
    private final OnboardingTaskRepository taskRepository;
    private final DocumentRepository documentRepository;

    @Override
    public SuccessResponse addEmployeeToOnboarding(AddEmployeeOnboardingRequest request) {
        // Validate user exists and has EMPLOYEE role
        User user = userRepository.findById(request.userId())
                .orElseThrow(() -> new ResourceNotFoundException("User not found with ID: " + request.userId()));
        
        if (!user.getRoles().contains(Role.EMPLOYEE)) {
            throw new OperationFailedException("User must have EMPLOYEE role for onboarding");
        }
        
        // Check if employee already exists
        if (employeeRepository.findByUserId(request.userId()).isPresent()) {
            throw new OperationFailedException("Employee already exists in onboarding system");
        }
        
        if (employeeRepository.existsByEmployeeId(request.employeeId())) {
            throw new OperationFailedException("Employee ID already exists: " + request.employeeId());
        }
        
        // Create employee
        Employee employee = new Employee(user, request.employeeId(), request.department(), 
                                       request.designation(), request.joiningDate());
        employee.setOnboardingStatus(OnboardingStatus.IN_PROGRESS);
        employee = employeeRepository.save(employee);
        
        // Create default onboarding tasks
        createDefaultOnboardingTasks(employee);
        
        return new SuccessResponse("Employee added to onboarding successfully", LocalDateTime.now());
    }

    @Override
    public List<EmployeeOnboardingSummaryResponse> getAllOnboardingEmployees() {
        return employeeRepository.findAll().stream()
                .map(this::mapToSummaryResponse)
                .collect(Collectors.toList());
    }

    @Override
    public List<EmployeeOnboardingSummaryResponse> getFilteredOnboardingEmployees(OnboardingFilterRequest filter) {
        List<Employee> employees = employeeRepository.findAll();
        
        return employees.stream()
                .filter(emp -> filter.department() == null || emp.getDepartment().equals(filter.department()))
                .filter(emp -> filter.status() == null || emp.getOnboardingStatus().equals(filter.status()))
                .filter(emp -> filter.joiningDateFrom() == null || !emp.getJoiningDate().isBefore(filter.joiningDateFrom()))
                .filter(emp -> filter.joiningDateTo() == null || !emp.getJoiningDate().isAfter(filter.joiningDateTo()))
                .map(this::mapToSummaryResponse)
                .collect(Collectors.toList());
    }

    @Override
    public EmployeeOnboardingDetailResponse getEmployeeOnboardingDetails(Long employeeId) {
        Employee employee = employeeRepository.findById(employeeId)
                .orElseThrow(() -> new ResourceNotFoundException("Employee not found with ID: " + employeeId));
        
        List<TaskDetailResponse> tasks = taskRepository.findByEmployee(employee).stream()
                .map(this::mapToTaskResponse)
                .collect(Collectors.toList());
        
        List<DocumentDetailResponse> documents = documentRepository.findByEmployee(employee).stream()
                .map(this::mapToDocumentResponse)
                .collect(Collectors.toList());
        
        int completionPercentage = calculateCompletionPercentage(employee);
        
        return new EmployeeOnboardingDetailResponse(
                employee.getId(),
                employee.getUser().getName(),
                employee.getUser().getEmail(),
                employee.getUser().getPhoneNumber(),
                employee.getDepartment(),
                employee.getDesignation(),
                employee.getEmployeeId(),
                employee.getOnboardingStatus(),
                employee.getJoiningDate(),
                tasks,
                documents,
                completionPercentage,
                employee.getUpdatedAt()
        );
    }

    @Override
    public SuccessResponse completeEmployeeOnboarding(Long employeeId) {
        Employee employee = employeeRepository.findById(employeeId)
                .orElseThrow(() -> new ResourceNotFoundException("Employee not found with ID: " + employeeId));
        
        // Check if all mandatory tasks are completed
        long completedTasks = taskRepository.countByEmployeeIdAndStatus(employeeId, TaskStatus.COMPLETED);
        long totalTasks = taskRepository.countByEmployeeId(employeeId);
        
        if (completedTasks < totalTasks) {
            throw new OperationFailedException("Cannot complete onboarding. Pending tasks exist.");
        }
        
        // Check if all mandatory documents are approved
        List<DocumentType> mandatoryTypes = Arrays.stream(DocumentType.values())
                .filter(DocumentType::isMandatory)
                .collect(Collectors.toList());
        
        int approvedMandatoryDocs = documentRepository.countApprovedMandatoryDocuments(employeeId, mandatoryTypes);
        
        if (approvedMandatoryDocs < mandatoryTypes.size()) {
            throw new OperationFailedException("Cannot complete onboarding. Pending document approvals exist.");
        }
        
        employee.setOnboardingStatus(OnboardingStatus.COMPLETED);
        employeeRepository.save(employee);
        
        return new SuccessResponse("Employee onboarding completed successfully", LocalDateTime.now());
    }

    @Override
    public HRDashboardResponse getHRDashboard() {
        int totalOnboarded = employeeRepository.countByOnboardingStatus(OnboardingStatus.COMPLETED);
        int pendingTasks = (int) taskRepository.findByStatus(TaskStatus.PENDING).stream()
                .map(task -> task.getEmployee().getId())
                .distinct()
                .count();
        int pendingDocuments = (int) documentRepository.findByStatus(DocumentStatus.PENDING_REVIEW).stream()
                .map(doc -> doc.getEmployee().getId())
                .distinct()
                .count();
        int inProgress = employeeRepository.countByOnboardingStatus(OnboardingStatus.IN_PROGRESS);
        
        List<EmployeeOnboardingSummaryResponse> recentOnboardings = employeeRepository
                .findByOnboardingStatus(OnboardingStatus.IN_PROGRESS).stream()
                .limit(5)
                .map(this::mapToSummaryResponse)
                .collect(Collectors.toList());
        
        return new HRDashboardResponse(totalOnboarded, pendingTasks, pendingDocuments, inProgress, recentOnboardings);
    }

    @Override
    public EmployeeDashboardResponse getEmployeeDashboard(Long userId) {
        Employee employee = employeeRepository.findByUserId(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Employee not found for user ID: " + userId));
        
        List<OnboardingTask> allTasks = taskRepository.findByEmployee(employee);
        List<TaskDetailResponse> pendingTasks = allTasks.stream()
                .filter(task -> task.getStatus() == TaskStatus.PENDING)
                .map(this::mapToTaskResponse)
                .collect(Collectors.toList());
        
        List<TaskDetailResponse> completedTasks = allTasks.stream()
                .filter(task -> task.getStatus() == TaskStatus.COMPLETED)
                .map(this::mapToTaskResponse)
                .collect(Collectors.toList());
        
        List<DocumentDetailResponse> documents = documentRepository.findByEmployee(employee).stream()
                .map(this::mapToDocumentResponse)
                .collect(Collectors.toList());
        
        int completionPercentage = calculateCompletionPercentage(employee);
        String nextAction = getNextAction(employee);
        
        return new EmployeeDashboardResponse(
                employee.getUser().getName(),
                employee.getUser().getEmail(),
                employee.getUser().getPhoneNumber(),
                employee.getDepartment(),
                employee.getDesignation(),
                employee.getEmployeeId(),
                employee.getOnboardingStatus(),
                employee.getJoiningDate(),
                pendingTasks,
                completedTasks,
                documents,
                completionPercentage,
                nextAction
        );
    }

    @Override
    public List<TaskDetailResponse> getEmployeeTasks(Long userId) {
        Employee employee = employeeRepository.findByUserId(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Employee not found for user ID: " + userId));
        
        return taskRepository.findByEmployee(employee).stream()
                .map(this::mapToTaskResponse)
                .collect(Collectors.toList());
    }

    @Override
    public SuccessResponse completeTask(Long userId, CompleteTaskRequest request) {
        Employee employee = employeeRepository.findByUserId(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Employee not found for user ID: " + userId));
        
        OnboardingTask task = taskRepository.findByEmployeeAndTaskType(employee, request.taskType())
                .orElseThrow(() -> new ResourceNotFoundException("Task not found: " + request.taskType()));
        
        if (task.getStatus() == TaskStatus.COMPLETED) {
            throw new OperationFailedException("Task already completed");
        }
        
        task.setStatus(TaskStatus.COMPLETED);
        task.setCompletedAt(LocalDateTime.now());
        task.setNotes(request.notes());
        taskRepository.save(task);
        
        return new SuccessResponse("Task completed successfully", LocalDateTime.now());
    }

    @Override
    public List<AvailableEmployeeResponse> getAvailableEmployeesForOnboarding() {
        return userRepository.findAll().stream()
                .filter(user -> user.getRoles().contains(Role.EMPLOYEE))
                .filter(user -> employeeRepository.findByUserId(user.getId()).isEmpty())
                .map(user -> new AvailableEmployeeResponse(
                        user.getId(),
                        user.getName(),
                        user.getEmail(),
                        user.getPhoneNumber()
                ))
                .collect(Collectors.toList());
    }

    @Override
    public List<String> getAllDepartments() {
        return employeeRepository.findAllDepartments();
    }

    // Helper methods
    private void createDefaultOnboardingTasks(Employee employee) {
        Arrays.stream(TaskType.values()).forEach(taskType -> {
            OnboardingTask task = new OnboardingTask(employee, taskType, taskType.getDescription());
            taskRepository.save(task);
        });
    }

    private EmployeeOnboardingSummaryResponse mapToSummaryResponse(Employee employee) {
        int completedTasks = taskRepository.countByEmployeeIdAndStatus(employee.getId(), TaskStatus.COMPLETED);
        int totalTasks = taskRepository.countByEmployeeId(employee.getId());
        int approvedDocs = documentRepository.countByEmployeeIdAndStatus(employee.getId(), DocumentStatus.APPROVED);
        int totalDocs = (int) documentRepository.findByEmployee(employee).size();
        int completionPercentage = calculateCompletionPercentage(employee);
        
        return new EmployeeOnboardingSummaryResponse(
                employee.getId(),
                employee.getUser().getName(),
                employee.getUser().getEmail(),
                employee.getUser().getPhoneNumber(),
                employee.getDepartment(),
                employee.getDesignation(),
                employee.getEmployeeId(),
                employee.getOnboardingStatus(),
                employee.getJoiningDate(),
                completionPercentage,
                completedTasks,
                totalTasks,
                approvedDocs,
                totalDocs
        );
    }

    private TaskDetailResponse mapToTaskResponse(OnboardingTask task) {
        return new TaskDetailResponse(
                task.getId(),
                task.getTaskType(),
                task.getTaskType().name(),
                task.getTaskDescription(),
                task.getStatus(),
                true, // All tasks are mandatory
                task.getCompletedAt(),
                task.getNotes()
        );
    }

    private DocumentDetailResponse mapToDocumentResponse(Document document) {
        return new DocumentDetailResponse(
                document.getId(),
                document.getDocumentType(),
                document.getDocumentType().getDisplayName(),
                document.getDocumentUrl(),
                document.getStatus(),
                document.getDocumentType().isMandatory(),
                document.getReviewComments(),
                document.getReviewedBy() != null ? document.getReviewedBy().getName() : null,
                document.getReviewedAt(),
                document.getCreatedAt()
        );
    }

    // private int calculateCompletionPercentage(Employee employee) {
    //     int completedTasks = taskRepository.countByEmployeeIdAndStatus(employee.getId(), TaskStatus.COMPLETED);
    //     int totalTasks = taskRepository.countByEmployeeId(employee.getId());
    //     int approvedDocs = documentRepository.countByEmployeeIdAndStatus(employee.getId(), DocumentStatus.APPROVED);
        
    //     List<DocumentType> mandatoryTypes = Arrays.stream(DocumentType.values())
    //             .filter(DocumentType::isMandatory)
    //             .collect(Collectors.toList());
        
    //     int totalItems = totalTasks + mandatoryTypes.size();
    //     int completedItems = completedTasks + approvedDocs;
        
    //     return totalItems > 0 ? (completedItems * 100) / totalItems : 0;
    // }

    private int calculateCompletionPercentage(Employee employee) {
    int completedTasks = taskRepository.countByEmployeeIdAndStatus(employee.getId(), TaskStatus.COMPLETED);
    int totalTasks = taskRepository.countByEmployeeId(employee.getId());

    // --- FIX STARTS HERE ---
    
    // 1. Get all mandatory document types
    List<DocumentType> mandatoryTypes = Arrays.stream(DocumentType.values())
            .filter(DocumentType::isMandatory)
            .collect(Collectors.toList());

    // 2. Count how many of those mandatory documents are approved
    int approvedMandatoryDocs = documentRepository.countApprovedMandatoryDocuments(employee.getId(), mandatoryTypes);
    
    // --- FIX ENDS HERE ---

    // 3. The total number of items is the sum of all tasks and all mandatory documents
    int totalItems = totalTasks + mandatoryTypes.size();

    // 4. The number of completed items is the sum of completed tasks and approved mandatory documents
    int completedItems = completedTasks + approvedMandatoryDocs;
    
    return totalItems > 0 ? (completedItems * 100) / totalItems : 0;
}

    private String getNextAction(Employee employee) {
        long pendingTasks = taskRepository.countByEmployeeIdAndStatus(employee.getId(), TaskStatus.PENDING);
        long rejectedDocs = documentRepository.countByEmployeeIdAndStatus(employee.getId(), DocumentStatus.REJECTED);
        
        if (pendingTasks > 0) {
            return "Complete pending onboarding tasks";
        } else if (rejectedDocs > 0) {
            return "Update rejected documents";
        } else {
            return "Wait for document approval";
        }
    }
}
