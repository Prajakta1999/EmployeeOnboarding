package com.elearning.projects.elearn.dto;

import lombok.Data;

@Data
public class EnrollmentStatsDto {
    private Long totalCourses;
    private Long totalEnrollments;
    private Double averageEnrollmentsPerCourse;
}
