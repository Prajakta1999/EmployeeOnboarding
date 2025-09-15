package com.elearning.projects.elearn.entity;

public enum TaskType {
    POLICY_ACKNOWLEDGMENT("Complete company policy acknowledgment"),
    ORIENTATION_SESSION("Attend orientation session"),
    DOCUMENT_SUBMISSION("Submit required documents");
    
    private final String description;
    
    TaskType(String description) {
        this.description = description;
    }
    
    public String getDescription() {
        return description;
    }
}
