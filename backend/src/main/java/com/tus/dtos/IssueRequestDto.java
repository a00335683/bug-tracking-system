package com.tus.dtos;

public class IssueRequestDto {

    private Long projectId;
    private Long reporterId;
    private String title;
    private String description;
    private String priority;

    public IssueRequestDto() {}

    public Long getProjectId() {
        return projectId;
    }

    public Long getReporterId() {
        return reporterId;
    }

    public String getTitle() {
        return title;
    }

    public String getDescription() {
        return description;
    }

    public String getPriority() {
        return priority;
    }
}