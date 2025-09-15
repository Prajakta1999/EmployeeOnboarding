package com.elearning.projects.elearn.controller;

import com.elearning.projects.elearn.service.EmployeeService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/common")
@RequiredArgsConstructor
@PreAuthorize("hasRole('HR') or hasRole('EMPLOYEE')")
public class CommonController {
    
    private final EmployeeService employeeService;

    @GetMapping("/departments")
    public ResponseEntity<List<String>> getAllDepartments() {
        List<String> departments = employeeService.getAllDepartments();
        return ResponseEntity.ok(departments);
    }

    @GetMapping("/health")
    public ResponseEntity<String> healthCheck() {
        return ResponseEntity.ok("Employee Onboarding System is running");
    }
}
