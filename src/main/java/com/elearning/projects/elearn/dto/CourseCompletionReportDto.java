package com.elearning.projects.elearn.dto;

import lombok.Data;
import java.util.List;

@Data
public class CourseCompletionReportDto {
    private Long courseId;
    private String courseName;
    private List<CompletionReportDto> moduleReports;
    private Double overallCompletionPercentage;
    private Long totalEnrolledStudents;
}
