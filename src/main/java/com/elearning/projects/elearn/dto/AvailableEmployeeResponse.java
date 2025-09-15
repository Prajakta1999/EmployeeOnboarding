package com.elearning.projects.elearn.dto;

public record AvailableEmployeeResponse(
    Long userId,
    String name,
    String email,
    String phoneNumber  // Updated field name
) {}
