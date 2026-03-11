package com.tus.dtos;

import java.time.LocalDateTime;

public class IssueResponseDto {

    private Long id;
    private String title;
    private String description;
    private String status;
    private String priority;
    private LocalDateTime createdAt;
    private Long projectId;
    private Long reportedById;
    private Long assignedToId;
    private String resolutionNote;
    private String assignedToUsername;

    public IssueResponseDto(Long id,
                            String title,
                            String description,
                            String status,
                            String priority,
                            LocalDateTime createdAt,
                            Long projectId,
                            Long reportedById,
                            Long assignedToId,
                            String resolutionNote,
                            String assignedToUsername) {
        this.id = id;
        this.title = title;
        this.description = description;
        this.status = status;
        this.priority = priority;
        this.createdAt = createdAt;
        this.projectId = projectId;
        this.reportedById = reportedById;
        this.assignedToId = assignedToId;
        this.resolutionNote = resolutionNote;
        this.assignedToUsername = assignedToUsername;
    }

    public Long getId() { return id; }
    public String getTitle() { return title; }
    public String getDescription() { return description; }
    public String getStatus() { return status; }
    public String getPriority() { return priority; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public Long getProjectId() { return projectId; }
    public Long getReportedById() { return reportedById; }
    public Long getAssignedToId() { return assignedToId; }
    public String getResolutionNote() {
        return resolutionNote;
    }

    public void setResolutionNote(String resolutionNote) {
        this.resolutionNote = resolutionNote;
    }
    public String getAssignedToUsername() {
        return assignedToUsername;
    }

    public void setAssignedToUsername(String assignedToUsername) {
        this.assignedToUsername = assignedToUsername;
    }
}